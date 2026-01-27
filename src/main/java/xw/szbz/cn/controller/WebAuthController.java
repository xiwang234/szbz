package xw.szbz.cn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.LifeAIRequest;
import xw.szbz.cn.model.LifeAIResponse;
import xw.szbz.cn.model.PasswordResetRequest;
import xw.szbz.cn.model.RandomSaltResponse;
import xw.szbz.cn.model.RefreshTokenRequest;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.ResetPasswordRequest;
import xw.szbz.cn.model.UserInfoResponse;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.service.AuthService;
import xw.szbz.cn.service.DataMaskingService;
import xw.szbz.cn.service.RandomSaltService;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.FieldEncryptionUtil;

/**
 * Web应用认证Controller
 * 提供注册、登录、Token刷新、登出等接口
 */
@RestController
@RequestMapping("/api/web-auth")
public class WebAuthController {
    
    @Autowired
    private AuthService authService;

    @Autowired
    private EnhancedJwtUtil jwtUtil;

    @Autowired
    private FieldEncryptionUtil fieldEncryptionUtil;

    @Autowired
    private DataMaskingService maskingService;

    @Autowired
    private RandomSaltService randomSaltService;
    
    /**
     * 用户注册
     * POST /api/web-auth/register
     * 前端需要先将密码进行 SHA256(原密码 + 固定盐) 后再传入
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);

        try {
            authService.register(request, ipAddress);
            return ResponseEntity.ok(ApiResponse.success(
                "注册成功，请验证邮箱", null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取随机盐（用于登录）
     * GET /api/web-auth/random-salt
     * 返回32位随机字符串，5分钟有效期
     */
    @GetMapping("/random-salt")
    public ResponseEntity<ApiResponse<RandomSaltResponse>> getRandomSalt() {
        try {
            String randomSalt = randomSaltService.generateRandomSalt();
            Long expiresAt = System.currentTimeMillis() + (5 * 60 * 1000); // 5分钟后过期

            RandomSaltResponse response = new RandomSaltResponse(randomSalt, expiresAt);
            return ResponseEntity.ok(ApiResponse.success(response, "获取随机盐成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     * POST /api/web-auth/login
     * 前端需要先调用 /random-salt 获取随机盐
     * 然后将密码进行 SHA256(原密码 + 固定盐 + 随机盐) 后再传入
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody WebLoginRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        try {
            AuthResponse response = authService.login(request, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success(response,"登录成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 刷新Token
     * POST /api/web-auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        
        try {
            AuthResponse response = authService.refreshToken(
                request.getRefreshToken(),
                request.getDeviceId(),
                ipAddress
            );
            return ResponseEntity.ok(ApiResponse.success(response,"Token刷新成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 登出
     * POST /api/web-auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) RefreshTokenRequest request) {
        
        try {
            String accessToken = extractToken(authHeader);
            String refreshToken = request != null ? request.getRefreshToken() : null;
            
            authService.logout(accessToken, refreshToken);
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     * GET /api/web-auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = extractToken(authHeader);
            
            // 验证Token
            if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid token"));
            }
            
            // 获取用户信息
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);
            WebUser user = authService.getUserByEncryptedId(encryptedUserId);
            
            // 解密并脱敏邮箱
            String plainEmail = fieldEncryptionUtil.decryptEmail(user.getEmail());
            String maskedEmail = maskingService.maskEmail(plainEmail);
            
            // 构建响应
            UserInfoResponse response = new UserInfoResponse();
            response.setEncryptedUserId(encryptedUserId);
            response.setUsername(user.getUsername());
            response.setMaskedEmail(maskedEmail);
            response.setEmailVerified(user.getEmailVerified());
            response.setActive(user.getActive());
            response.setCreateTime(user.getCreateTime());
            response.setLastLoginTime(user.getLastLoginTime());
            
            return ResponseEntity.ok(ApiResponse.success(response,"success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 请求密码重置
     * POST /api/web-auth/request-reset
     */
    @PostMapping("/request-reset")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(
            @RequestBody PasswordResetRequest request) {
        
        try {
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                "如果该邮箱存在，我们已发送重置链接", null
            ));
        } catch (Exception e) {
            // 为了安全，不透露用户是否存在
            return ResponseEntity.ok(ApiResponse.success(
                "如果该邮箱存在，我们已发送重置链接", null
            ));
        }
    }
    
    /**
     * 重置密码
     * POST /api/web-auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 验证邮箱
     * POST /api/web-auth/verify-email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam String token) {

        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(ApiResponse.success("邮箱验证成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * LifeAI 接口
     * POST /api/web-auth/lifeai
     * 需要登录，提供人生建议和咨询服务
     */
    @PostMapping("/lifeai")
    public ResponseEntity<ApiResponse<LifeAIResponse>> lifeAI(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LifeAIRequest request) {

        try {
            // 1. Token 验证
            String token = extractToken(authHeader);

            if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("无效的访问令牌"));
            }

            // 2. 获取用户信息（确认用户已登录）
            String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);
            WebUser user = authService.getUserByEncryptedId(encryptedUserId);

            if (!user.getActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("账户已被禁用"));
            }

            // 3. 参数验证
            validateLifeAIRequest(request);

            // 4. TODO: 业务逻辑待定
            // 这里将来会调用 AI 服务进行处理
            String requestId = java.util.UUID.randomUUID().toString();
            String answer = "业务逻辑待实现"; // 占位符

            // 5. 构建响应
            LifeAIResponse response = new LifeAIResponse(
                requestId,
                request.getBackground(),
                request.getQuestion(),
                request.getBirthYear(),
                request.getGender(),
                request.getCategory(),
                answer,
                System.currentTimeMillis()
            );

            return ResponseEntity.ok(ApiResponse.success(response, "请求成功"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("服务器内部错误：" + e.getMessage()));
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 验证 LifeAI 请求参数
     */
    private void validateLifeAIRequest(LifeAIRequest request) {
        if (request.getBackground() == null || request.getBackground().trim().isEmpty()) {
            throw new IllegalArgumentException("背景描述不能为空");
        }

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        if (request.getBirthYear() == null) {
            throw new IllegalArgumentException("出生年份不能为空");
        }

        // 验证出生年份范围
        int currentYear = java.time.Year.now().getValue();
        if (request.getBirthYear() < 1900 || request.getBirthYear() > currentYear) {
            throw new IllegalArgumentException("出生年份必须在1900-" + currentYear + "之间");
        }

        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("性别不能为空");
        }

        // 验证性别格式
        String gender = request.getGender().toLowerCase();
        if (!gender.equals("男") && !gender.equals("女") &&
            !gender.equals("male") && !gender.equals("female")) {
            throw new IllegalArgumentException("性别格式错误，只支持：男/女/male/female");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }

        // 验证字段长度
        if (request.getBackground().length() > 1000) {
            throw new IllegalArgumentException("背景描述不能超过1000字符");
        }

        if (request.getQuestion().length() > 500) {
            throw new IllegalArgumentException("问题不能超过500字符");
        }

        if (request.getCategory().length() > 50) {
            throw new IllegalArgumentException("分类不能超过50字符");
        }
    }

    // ========== 私有辅助方法 ==========
    
    /**
     * 从请求中获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 从Authorization header中提取Token
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
