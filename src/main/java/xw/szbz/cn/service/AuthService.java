package xw.szbz.cn.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xw.szbz.cn.entity.PasswordResetRateLimit;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.repository.PasswordResetRateLimitRepository;
import xw.szbz.cn.repository.WebUserRepository;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.FieldEncryptionUtil;
import xw.szbz.cn.util.PasswordHashUtil;

/**
 * 认证服务
 * 提供注册、登录、Token刷新、登出等功能
 */
@Service
public class AuthService {
    
    @Autowired
    private WebUserRepository webUserRepository;
    
    @Autowired
    private EnhancedJwtUtil enhancedJwtUtil;
    
    @Autowired
    private FieldEncryptionUtil fieldEncryptionUtil;
    
    @Autowired
    private DataMaskingService maskingService;

    @Autowired
    private PasswordHashUtil passwordHashUtil;

    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration;

    @Value("${password.fixed.salt:szbz-fixed-salt-2024-secure-password}")
    private String fixedSalt;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetRateLimitRepository resetRateLimitRepository;

    @Value("${app.base-url:https://lifeai.wang}")
    private String appBaseUrl;

    @Value("${password.reset.secret}")
    private String resetSecret;

    @Value("${rate.limit.password-reset.daily:3}")
    private int passwordResetDailyLimit;

    // Token黑名单（用于登出），生产环境应使用Redis
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    /**
     * 用户注册
     * 前端传入的密码已经是 SHA256(原密码 + 固定盐)
     */
    @Transactional
    public void register(RegisterRequest request, String ipAddress) {
        // 1. 验证输入
        validateRegisterRequest(request);

        // 2. 加密邮箱
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(request.getEmail());

        // 3. 检查用户名和邮箱是否已存在
        if (webUserRepository.existsByUsername(request.getUsername())) {
            throw new ServiceException("用户名已存在");
        }

        if (webUserRepository.existsByEmail(encryptedEmail)) {
            throw new ServiceException("邮箱已被注册");
        }

        // 4. 创建用户
        // 注意：前端传入的密码已经是 SHA256(原密码 + 固定盐)，直接保存到数据库
        WebUser user = new WebUser();
        user.setUsername(request.getUsername());
        user.setEmail(encryptedEmail);
        user.setPasswordHash(request.getPassword()); // 直接保存前端传入的哈希密码
        user.setEmailVerified(false);
        user.setActive(true);
        user.setCreateTime(System.currentTimeMillis());
        user.setLastLoginIp(ipAddress);
        user.setBizId(UUID.randomUUID().toString()); // 生成业务ID
        user.setFreeCount(5); // 设置免费体验次数为5次

        // 5. 生成邮箱验证令牌（24小时有效）
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpiry(System.currentTimeMillis() + 24 * 60 * 60 * 1000L); // 24小时

        // 6. 保存用户
        webUserRepository.save(user);

        // TODO: 发送邮箱验证邮件
    }
    
    /**
     * 用户登录
     * 前端传入的密码是 SHA256(原密码 + 固定盐 + 随机盐)
     * 注意：随机盐验证已在 WebAuthController 中完成
     */
    @Transactional
    public AuthResponse login(WebLoginRequest request, String ipAddress, String userAgent) {
        // 1. 验证输入
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ServiceException("邮箱不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ServiceException("密码不能为空");
        }
        if (request.getRandomSalt() == null || request.getRandomSalt().isEmpty()) {
            throw new ServiceException("随机盐不能为空");
        }

        // 2. 加密邮箱查询用户
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(request.getEmail());
        WebUser user = webUserRepository.findByEmail(encryptedEmail);
        if (user == null) {
            throw new ServiceException("邮箱或密码错误");
        }

        // 3. 验证密码
        // 数据库中存储的是：SHA256(原密码 + 固定盐)
        // 前端传入的是：SHA256(原密码 + 固定盐 + 随机盐)
        // 需要计算：SHA256(数据库密码 + 随机盐) 并与前端传入的密码比较
        String expectedPassword = passwordHashUtil.sha256Hash(user.getPasswordHash() + request.getRandomSalt());
        if (!expectedPassword.equals(request.getPassword())) {
            throw new ServiceException("邮箱或密码错误");
        }

        // 4. 检查账户状态
        if (!user.getActive()) {
            throw new ServiceException("账户已被禁用");
        }

        logger.info("登录成功，用户ID: {}, bizId: {}", user.getId(), user.getBizId());

        // 5. 使用 bizId 作为业务标识
        String bizId = user.getBizId();

        // 6. 生成会话ID和设备ID
        String sessionId = enhancedJwtUtil.generateSessionId();
        String deviceId = request.getDeviceId() != null ?
            request.getDeviceId() :
            enhancedJwtUtil.generateDeviceId(userAgent, ipAddress);

        // 7. 生成Token（使用 bizId 代替 encryptedUserId）
        String accessToken = enhancedJwtUtil.generateAccessToken(
            bizId,  // 使用 bizId
            user.getUsername(),
            user.getEmailVerified(),
            sessionId,
            deviceId,
            ipAddress
        );

        logger.info("登录成功，生成AccessToken");

        String refreshToken = enhancedJwtUtil.generateRefreshToken(
            bizId,  // 使用 bizId
            sessionId,
            deviceId
        );

        logger.info("登录成功，生成RefreshToken");

        // 8. 更新最后登录信息
        user.setLastLoginTime(System.currentTimeMillis());
        user.setLastLoginIp(ipAddress);
        webUserRepository.save(user);

        // 9. 脱敏邮箱
        String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
        String maskedEmail = maskingService.maskEmail(plainEmail);

        // 10. 返回响应（使用 bizId）
        return new AuthResponse(
            accessToken,
            refreshToken,
            System.currentTimeMillis() + accessTokenExpiration,
            System.currentTimeMillis() + refreshTokenExpiration,
            bizId,  // 返回 bizId
            user.getUsername(),
            maskedEmail
        );
    }
    
    /**
     * 刷新Token
     */
    public AuthResponse refreshToken(String refreshToken, String deviceId, String ipAddress) {
        // 1. 验证Refresh Token
        if (!enhancedJwtUtil.validateToken(refreshToken)) {
            throw new ServiceException("Invalid refresh token");
        }

        if (!enhancedJwtUtil.isRefreshToken(refreshToken)) {
            throw new ServiceException("Token is not a refresh token");
        }

        // 2. 检查是否在黑名单
        String jti = enhancedJwtUtil.getJtiFromToken(refreshToken);
        if (isTokenBlacklisted(jti)) {
            throw new ServiceException("Token has been revoked");
        }

        // 3. 验证设备ID
        if (!enhancedJwtUtil.validateDeviceId(refreshToken, deviceId)) {
            throw new ServiceException("Device ID mismatch");
        }

        // 4. 获取用户信息（通过 bizId）
        String bizId = enhancedJwtUtil.getEncryptedUserIdFromToken(refreshToken);
        WebUser user = webUserRepository.findByBizId(bizId);
        if (user == null) {
            throw new ServiceException("User not found");
        }

        if (!user.getActive()) {
            throw new ServiceException("Account is disabled");
        }

        // 5. 生成新的Token（使用相同的会话ID）
        String sessionId = enhancedJwtUtil.getSessionIdFromToken(refreshToken);

        String newAccessToken = enhancedJwtUtil.generateAccessToken(
            bizId,  // 使用 bizId
            user.getUsername(),
            user.getEmailVerified(),
            sessionId,
            deviceId,
            ipAddress
        );

        String newRefreshToken = enhancedJwtUtil.generateRefreshToken(
            bizId,  // 使用 bizId
            sessionId,
            deviceId
        );

        // 6. 将旧的Refresh Token加入黑名单
        addTokenToBlacklist(jti, refreshTokenExpiration);

        // 7. 脱敏邮箱
        String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
        String maskedEmail = maskingService.maskEmail(plainEmail);

        // 8. 返回响应
        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            System.currentTimeMillis() + accessTokenExpiration,
            System.currentTimeMillis() + refreshTokenExpiration,
            bizId,  // 返回 bizId
            user.getUsername(),
            maskedEmail
        );
    }
    
    /**
     * 登出
     */
    public void logout(String accessToken, String refreshToken) {
        // 将Access Token和Refresh Token加入黑名单
        if (accessToken != null && enhancedJwtUtil.validateToken(accessToken)) {
            String accessJti = enhancedJwtUtil.getJtiFromToken(accessToken);
            addTokenToBlacklist(accessJti, accessTokenExpiration);
        }
        
        if (refreshToken != null && enhancedJwtUtil.validateToken(refreshToken)) {
            String refreshJti = enhancedJwtUtil.getJtiFromToken(refreshToken);
            addTokenToBlacklist(refreshJti, refreshTokenExpiration);
        }
    }
    
    /**
     * 根据加密用户ID获取用户（现在 encryptedUserId 实际是 bizId）
     */
    public WebUser getUserByEncryptedId(String encryptedUserId) {
        // 直接通过 bizId 查询，无需解密
        WebUser user = webUserRepository.findByBizId(encryptedUserId);
        if (user == null) {
            throw new ServiceException("User not found");
        }
        return user;
    }

    /**
     * 根据用户名获取用户
     */
    public WebUser getUserByUsername(String username) {
        return webUserRepository.findByUsername(username);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(WebUser user) {
        webUserRepository.save(user);
    }

    // ========== 私有方法 ==========
    
    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ServiceException("用户名不能为空");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ServiceException("邮箱不能为空");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ServiceException("密码长度至少8位");
        }
        
        // 简单的邮箱格式验证
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ServiceException("邮箱格式不正确");
        }
        
        // 用户名格式验证
        if (!request.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new ServiceException("用户名只能包含字母、数字和下划线，长度3-20位");
        }
    }
    
    private void addTokenToBlacklist(String jti, Long expiration) {
        long expiryTime = System.currentTimeMillis() + expiration;
        tokenBlacklist.put(jti, expiryTime);
        
        // 清理过期的黑名单条目
        tokenBlacklist.entrySet().removeIf(entry -> 
            entry.getValue() < System.currentTimeMillis()
        );
    }
    
    private boolean isTokenBlacklisted(String jti) {
        Long expiryTime = tokenBlacklist.get(jti);
        if (expiryTime == null) {
            return false;
        }

        // 检查是否过期
        if (expiryTime < System.currentTimeMillis()) {
            tokenBlacklist.remove(jti);
            return false;
        }

        return true;
    }

    /**
     * 获取Token黑名单所有条目（用于缓存管理）
     */
    public Map<String, Object> getTokenBlacklistEntries() {
        Map<String, Object> entries = new java.util.HashMap<>();
        long currentTime = System.currentTimeMillis();

        tokenBlacklist.forEach((jti, expiryTime) -> {
            Map<String, Object> tokenData = new java.util.HashMap<>();
            tokenData.put("expiryTime", expiryTime);
            tokenData.put("remainingTime", Math.max(0, expiryTime - currentTime) + "ms");
            tokenData.put("isExpired", expiryTime < currentTime);
            entries.put(jti, tokenData);
        });

        return entries;
    }

    // ========== 密码重置 ==========

    /**
     * 接口A：请求密码重置，发送重置邮件
     * token格式：<uuid>.<hmac-sha256(uuid, secret)>，5分钟有效
     */
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ServiceException("邮箱不能为空");
        }

        String encryptedEmail = fieldEncryptionUtil.encryptEmail(email.trim());
        WebUser user = webUserRepository.findByEmail(encryptedEmail);
        if (user == null) {
            // 安全起见，不透露用户是否存在，静默返回
            logger.info("密码重置请求：邮箱不存在，静默处理, email: {}", email);
            return;
        }

        // 检查每日限流
        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        java.util.Optional<PasswordResetRateLimit> rateLimitOpt =
            resetRateLimitRepository.findByEmailAndDateKey(encryptedEmail, today);

        PasswordResetRateLimit rateLimit;
        if (rateLimitOpt.isPresent()) {
            rateLimit = rateLimitOpt.get();
            if (rateLimit.getRemainingCount() <= 0) {
                throw new ServiceException("今日重置密码次数已用完，请明天再试");
            }
        } else {
            // 当天首次请求，新建记录
            rateLimit = new PasswordResetRateLimit();
            rateLimit.setEmail(encryptedEmail);
            rateLimit.setRemainingCount(passwordResetDailyLimit);
            rateLimit.setDateKey(today);
        }

        // 生成随机 UUID 作为 token 主体
        String tokenId = UUID.randomUUID().toString().replace("-", "");
        // HMAC 签名防伪造
        String hmac = hmacSha256(tokenId, resetSecret);
        String signedToken = tokenId + "." + hmac;

        // 5分钟有效期
        long expiry = System.currentTimeMillis() + 5 * 60 * 1000L;
        user.setPasswordResetToken(tokenId);
        user.setPasswordResetExpiry(expiry);
        webUserRepository.save(user);

        // 构建重置链接
        String resetLink = appBaseUrl + "/auth/reset-password?token=" + signedToken;

        // 使用 Gmail 发送重置邮件（网站对外邮件）
        String subject = "Password Reset / 密码重置 - Life Strategy AI";
        String htmlContent = buildResetEmailHtml(user.getUsername(), resetLink);
        emailService.sendHtmlEmailViaGmail(email.trim(), subject, htmlContent);

        // 邮件发送成功后扣减次数并保存
        rateLimit.setRemainingCount(rateLimit.getRemainingCount() - 1);
        resetRateLimitRepository.save(rateLimit);

        logger.info("密码重置邮件已发送, email: {}, expiry: {}, 今日剩余次数: {}", email, expiry, rateLimit.getRemainingCount());
    }

    /**
     * 接口B：验证重置 token 有效性
     * @return 解析出的邮箱（脱敏），供前端展示
     */
    public String verifyResetToken(String signedToken) {
        String[] parts = parseSignedToken(signedToken);
        String tokenId = parts[0];
        String hmac = parts[1];

        // 验证 HMAC 签名
        String expectedHmac = hmacSha256(tokenId, resetSecret);
        if (!expectedHmac.equals(hmac)) {
            throw new ServiceException("重置链接无效或已被篡改");
        }

        // 查询数据库
        WebUser user = webUserRepository.findByPasswordResetToken(tokenId);
        if (user == null) {
            throw new ServiceException("重置链接无效或已使用");
        }

        // 验证有效期（5分钟）
        if (System.currentTimeMillis() > user.getPasswordResetExpiry()) {
            throw new ServiceException("重置链接已过期，请重新申请");
        }

        // 返回脱敏邮箱
        String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
        return maskingService.maskEmail(plainEmail);
    }

    /**
     * 接口C：重置密码
     * newPassword 已经是前端加密后的 SHA256(原密码 + 固定盐)，与注册规则一致
     */
    @Transactional
    public void resetPassword(String signedToken, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ServiceException("密码长度至少8位");
        }

        String[] parts = parseSignedToken(signedToken);
        String tokenId = parts[0];
        String hmac = parts[1];

        // 验证 HMAC 签名
        String expectedHmac = hmacSha256(tokenId, resetSecret);
        if (!expectedHmac.equals(hmac)) {
            throw new ServiceException("重置链接无效或已被篡改");
        }

        // 查询数据库
        WebUser user = webUserRepository.findByPasswordResetToken(tokenId);
        if (user == null) {
            throw new ServiceException("重置链接无效或已使用");
        }

        // 验证有效期
        if (System.currentTimeMillis() > user.getPasswordResetExpiry()) {
            throw new ServiceException("重置链接已过期，请重新申请");
        }

        // 更新密码，清除 token（防止重复使用）
        user.setPasswordHash(newPassword);
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        webUserRepository.save(user);

        logger.info("密码重置成功, userId: {}", user.getId());
    }

    // ========== 私有工具方法 ==========

    private String[] parseSignedToken(String signedToken) {
        if (signedToken == null || !signedToken.contains(".")) {
            throw new ServiceException("重置链接格式无效");
        }
        int dotIndex = signedToken.lastIndexOf(".");
        String tokenId = signedToken.substring(0, dotIndex);
        String hmac = signedToken.substring(dotIndex + 1);
        if (tokenId.isEmpty() || hmac.isEmpty()) {
            throw new ServiceException("重置链接格式无效");
        }
        return new String[]{tokenId, hmac};
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 计算失败", e);
        }
    }

    private String buildResetEmailHtml(String username, String resetLink) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
            "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1)'>" +
            "<h2 style='color:#333;margin-bottom:8px'>Password Reset / 密码重置</h2>" +
            "<p style='color:#666'>Hi " + username + ",</p>" +
            "<p style='color:#666'>We received a request to reset your password for your Life Strategy AI account.<br>" +
            "我们收到了您重置 Life Strategy AI 账户密码的请求。</p>" +
            "<p style='color:#666'>Click the button below to reset your password. This link is valid for <strong>5 minutes</strong>.<br>" +
            "点击下方按钮重置密码，此链接 <strong>5分钟内</strong> 有效。</p>" +
            "<div style='text-align:center;margin:32px 0'>" +
            "<a href='" + resetLink + "' style='background:#6366f1;color:#fff;padding:14px 32px;border-radius:6px;text-decoration:none;font-size:16px;font-weight:bold'>Reset Password / 重置密码</a>" +
            "</div>" +
            "<p style='color:#999;font-size:13px'>If the button doesn't work, copy and paste this link into your browser:<br>" +
            "如果按钮无效，请复制以下链接到浏览器：</p>" +
            "<p style='color:#6366f1;font-size:13px;word-break:break-all'>" + resetLink + "</p>" +
            "<hr style='border:none;border-top:1px solid #eee;margin:24px 0'>" +
            "<p style='color:#999;font-size:12px'>If you did not request a password reset, please ignore this email. Your password will not be changed.<br>" +
            "如果您没有申请重置密码，请忽略此邮件，您的密码不会被更改。</p>" +
            "<p style='color:#999;font-size:12px'>Life Strategy AI Team</p>" +
            "</div></body></html>";
    }
}
