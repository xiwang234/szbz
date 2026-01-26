package xw.szbz.cn.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 增强型JWT工具类
 * 支持Access Token和Refresh Token
 * 包含丰富的安全特性：会话管理、设备绑定、IP验证等
 */
@Component
public class EnhancedJwtUtil {
    
    @Value("${jwt.secret:szbz-api-secret-key-for-wechat-miniprogram-authentication-2024}")
    private String secret;
    
    @Value("${jwt.access-token.expiration:3600000}")
    private Long accessTokenExpiration; // 默认1小时
    
    @Value("${jwt.refresh-token.expiration:604800000}")
    private Long refreshTokenExpiration; // 默认7天
    
    private static final String ISSUER = "szbz-web-app";
    private static final String AUDIENCE = "szbz-web-client";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    
    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成Access Token
     * 
     * @param encryptedUserId 加密用户ID (13位)
     * @param username 用户名
     * @param emailVerified 邮箱验证状态
     * @param sessionId 会话ID
     * @param deviceId 设备指纹
     * @param ipAddress 登录IP地址
     * @return Access Token
     */
    public String generateAccessToken(String encryptedUserId, String username, 
                                     boolean emailVerified, String sessionId,
                                     String deviceId, String ipAddress) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("email_verified", emailVerified);
        claims.put("token_type", TOKEN_TYPE_ACCESS);
        claims.put("session_id", sessionId);
        claims.put("device_id", deviceId);
        claims.put("ip_hash", hashIp(ipAddress));
        claims.put("jti", UUID.randomUUID().toString());
        
        return Jwts.builder()
                .subject(encryptedUserId)
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 生成Refresh Token
     * 
     * @param encryptedUserId 加密用户ID (13位)
     * @param sessionId 会话ID
     * @param deviceId 设备指纹
     * @return Refresh Token
     */
    public String generateRefreshToken(String encryptedUserId, String sessionId, String deviceId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", TOKEN_TYPE_REFRESH);
        claims.put("session_id", sessionId);
        claims.put("device_id", deviceId);
        claims.put("jti", UUID.randomUUID().toString());
        
        return Jwts.builder()
                .subject(encryptedUserId)
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 从Token中获取加密用户ID
     */
    public String getEncryptedUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    /**
     * 从Token中获取会话ID
     */
    public String getSessionIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("session_id", String.class);
    }
    
    /**
     * 从Token中获取设备ID
     */
    public String getDeviceIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("device_id", String.class);
    }
    
    /**
     * 从Token中获取IP哈希
     */
    public String getIpHashFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("ip_hash", String.class);
    }
    
    /**
     * 从Token中获取Token类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("token_type", String.class);
    }
    
    /**
     * 从Token中获取JTI（Token唯一ID）
     */
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("jti", String.class);
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证Token是否为Access Token
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return TOKEN_TYPE_ACCESS.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证Token是否为Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return TOKEN_TYPE_REFRESH.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 验证设备ID是否匹配
     */
    public boolean validateDeviceId(String token, String deviceId) {
        try {
            String tokenDeviceId = getDeviceIdFromToken(token);
            return deviceId != null && deviceId.equals(tokenDeviceId);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证IP是否匹配（允许一定的容差）
     */
    public boolean validateIp(String token, String ipAddress) {
        try {
            String tokenIpHash = getIpHashFromToken(token);
            String currentIpHash = hashIp(ipAddress);
            return currentIpHash != null && currentIpHash.equals(tokenIpHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取Token剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 解析Token获取Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 对IP地址进行哈希处理
     */
    private String hashIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash IP address", e);
        }
    }
    
    /**
     * 生成会话ID
     */
    public String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成设备指纹ID（简单实现，实际应该从前端获取）
     */
    public String generateDeviceId(String userAgent, String ipAddress) {
        String deviceInfo = userAgent + "|" + ipAddress;
        return hashIp(deviceInfo).substring(0, 16);
    }
}
