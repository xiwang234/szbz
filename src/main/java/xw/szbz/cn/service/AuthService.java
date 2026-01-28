package xw.szbz.cn.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.repository.WebUserRepository;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.EnhancedUserIdEncryption;
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
    private EnhancedUserIdEncryption userIdEncryption;
    
    @Autowired
    private FieldEncryptionUtil fieldEncryptionUtil;
    
    @Autowired
    private DataMaskingService maskingService;

    @Autowired
    private RandomSaltService randomSaltService;

    @Autowired
    private PasswordHashUtil passwordHashUtil;

    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration;

    @Value("${password.fixed.salt:szbz-fixed-salt-2024-secure-password}")
    private String fixedSalt;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Token黑名单（用于登出），生产环境应使用Redis
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    
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

        // 2. 验证随机盐（检查是否未使用且在5分钟有效期内）
        randomSaltService.validateAndMarkSaltAsUsed(request.getRandomSalt());

        // 3. 加密邮箱查询用户
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(request.getEmail());
        WebUser user = webUserRepository.findByEmail(encryptedEmail);
        if (user == null) {
            throw new ServiceException("邮箱或密码错误");
        }

        // 4. 验证密码
        // 数据库中存储的是：SHA256(原密码 + 固定盐)
        // 前端传入的是：SHA256(原密码 + 固定盐 + 随机盐)
        // 需要计算：SHA256(数据库密码 + 随机盐) 并与前端传入的密码比较
        String expectedPassword = passwordHashUtil.sha256Hash(user.getPasswordHash() + request.getRandomSalt());
        if (!expectedPassword.equals(request.getPassword())) {
            throw new ServiceException("邮箱或密码错误");
        }

        // 5. 检查账户状态
        if (!user.getActive()) {
            throw new ServiceException("账户已被禁用");
        }

        // 6. 生成加密用户ID
        String encryptedUserId = userIdEncryption.encryptUserId(
            user.getId(),
            user.getCreateTimeAsLocalDateTime()
        );

        // 7. 生成会话ID和设备ID
        String sessionId = enhancedJwtUtil.generateSessionId();
        String deviceId = request.getDeviceId() != null ?
            request.getDeviceId() :
            enhancedJwtUtil.generateDeviceId(userAgent, ipAddress);

        // 8. 生成Token
        String accessToken = enhancedJwtUtil.generateAccessToken(
            encryptedUserId,
            user.getUsername(),
            user.getEmailVerified(),
            sessionId,
            deviceId,
            ipAddress
        );

        String refreshToken = enhancedJwtUtil.generateRefreshToken(
            encryptedUserId,
            sessionId,
            deviceId
        );

        // 9. 更新最后登录信息
        user.setLastLoginTime(System.currentTimeMillis());
        user.setLastLoginIp(ipAddress);
        webUserRepository.save(user);

        // 10. 脱敏邮箱
        String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
        String maskedEmail = maskingService.maskEmail(plainEmail);

        // 11. 返回响应
        return new AuthResponse(
            accessToken,
            refreshToken,
            System.currentTimeMillis() + accessTokenExpiration,
            System.currentTimeMillis() + refreshTokenExpiration,
            encryptedUserId,
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
        
        // 4. 获取用户信息
        String encryptedUserId = enhancedJwtUtil.getEncryptedUserIdFromToken(refreshToken);
        Long userId = userIdEncryption.decryptUserId(encryptedUserId);
        
        Optional<WebUser> userOpt = webUserRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ServiceException("User not found");
        }
        
        WebUser user = userOpt.get();
        
        if (!user.getActive()) {
            throw new ServiceException("Account is disabled");
        }
        
        // 5. 生成新的Token（使用相同的会话ID）
        String sessionId = enhancedJwtUtil.getSessionIdFromToken(refreshToken);
        
        String newAccessToken = enhancedJwtUtil.generateAccessToken(
            encryptedUserId,
            user.getUsername(),
            user.getEmailVerified(),
            sessionId,
            deviceId,
            ipAddress
        );
        
        String newRefreshToken = enhancedJwtUtil.generateRefreshToken(
            encryptedUserId,
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
            encryptedUserId,
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
     * 根据加密用户ID获取用户
     */
    public WebUser getUserByEncryptedId(String encryptedUserId) {
        Long userId = userIdEncryption.decryptUserId(encryptedUserId);
        return webUserRepository.findById(userId)
            .orElseThrow(() -> new ServiceException("User not found"));
    }
    
    /**
     * 密码重置请求（生成重置令牌并发送邮件）
     */
    @Transactional
    public void requestPasswordReset(String email) {
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(email);
        WebUser user = webUserRepository.findByEmail(encryptedEmail);

        if (user == null) {
            // 为了安全，不透露用户是否存在
            return;
        }

        // 生成密码重置令牌（1小时有效）
        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpiry(System.currentTimeMillis() + 60 * 60 * 1000L); // 1小时
        webUserRepository.save(user);

        // TODO: 发送密码重置邮件
    }
    
    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        WebUser user = webUserRepository.findByPasswordResetToken(resetToken);

        if (user == null) {
            throw new ServiceException("Invalid reset token");
        }

        // 检查令牌是否过期
        if (user.getPasswordResetExpiry() == null ||
            user.getPasswordResetExpiry() < System.currentTimeMillis()) {
            throw new ServiceException("Reset token has expired");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        webUserRepository.save(user);
    }
    
    /**
     * 验证邮箱
     */
    @Transactional
    public void verifyEmail(String verificationToken) {
        WebUser user = webUserRepository.findByEmailVerificationToken(verificationToken);

        if (user == null) {
            throw new ServiceException("Invalid verification token");
        }

        // 检查令牌是否过期
        if (user.getEmailVerificationExpiry() == null ||
            user.getEmailVerificationExpiry() < System.currentTimeMillis()) {
            throw new ServiceException("Verification token has expired");
        }

        // 更新验证状态
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
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
}
