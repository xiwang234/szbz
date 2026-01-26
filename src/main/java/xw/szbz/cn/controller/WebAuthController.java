package xw.szbz.cn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.model.*;
import xw.szbz.cn.service.AuthService;
import xw.szbz.cn.service.DataMaskingService;
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
    
    /**
     * 用户注册
     * POST /api/web-auth/register
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
     * 用户登录
     * POST /api/web-auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody WebLoginRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        try {
            AuthResponse response = authService.login(request, ipAddress, userAgent);
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
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
            return ResponseEntity.ok(ApiResponse.success("Token刷新成功", response));
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
            
            return ResponseEntity.ok(ApiResponse.success("success", response));
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
