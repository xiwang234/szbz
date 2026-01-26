package xw.szbz.cn.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 增强型JWT工具测试
 */
class EnhancedJwtUtilTest {
    
    private EnhancedJwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new EnhancedJwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", 
            "szbz-api-secret-key-for-testing-jwt-token-generation-2024");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L); // 7 days
    }
    
    @Test
    void testGenerateAndValidateAccessToken() {
        // Given
        String encryptedUserId = "u1a2b3c4d5e6f";
        String username = "testuser";
        boolean emailVerified = true;
        String sessionId = "session_123";
        String deviceId = "device_456";
        String ipAddress = "192.168.1.1";
        
        // When
        String token = jwtUtil.generateAccessToken(
            encryptedUserId, username, emailVerified, sessionId, deviceId, ipAddress
        );
        
        // Then
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.isAccessToken(token));
        assertFalse(jwtUtil.isRefreshToken(token));
        assertEquals(encryptedUserId, jwtUtil.getEncryptedUserIdFromToken(token));
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
        assertEquals(sessionId, jwtUtil.getSessionIdFromToken(token));
        assertEquals(deviceId, jwtUtil.getDeviceIdFromToken(token));
        
        System.out.println("Access Token: " + token);
        System.out.println("Token Type: Access");
        System.out.println("Encrypted User ID: " + jwtUtil.getEncryptedUserIdFromToken(token));
        System.out.println("Username: " + jwtUtil.getUsernameFromToken(token));
    }
    
    @Test
    void testGenerateAndValidateRefreshToken() {
        // Given
        String encryptedUserId = "u1a2b3c4d5e6f";
        String sessionId = "session_123";
        String deviceId = "device_456";
        
        // When
        String token = jwtUtil.generateRefreshToken(encryptedUserId, sessionId, deviceId);
        
        // Then
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.isRefreshToken(token));
        assertFalse(jwtUtil.isAccessToken(token));
        assertEquals(encryptedUserId, jwtUtil.getEncryptedUserIdFromToken(token));
        assertEquals(sessionId, jwtUtil.getSessionIdFromToken(token));
        assertEquals(deviceId, jwtUtil.getDeviceIdFromToken(token));
        
        System.out.println("\nRefresh Token: " + token);
        System.out.println("Token Type: Refresh");
    }
    
    @Test
    void testValidateDeviceId() {
        // Given
        String token = jwtUtil.generateAccessToken(
            "u1a2b3c4d5e6f", "testuser", true, "session_123", "device_456", "192.168.1.1"
        );
        
        // When & Then
        assertTrue(jwtUtil.validateDeviceId(token, "device_456"));
        assertFalse(jwtUtil.validateDeviceId(token, "device_789"));
    }
    
    @Test
    void testValidateIp() {
        // Given
        String ipAddress = "192.168.1.1";
        String token = jwtUtil.generateAccessToken(
            "u1a2b3c4d5e6f", "testuser", true, "session_123", "device_456", ipAddress
        );
        
        // When & Then
        assertTrue(jwtUtil.validateIp(token, ipAddress));
        assertFalse(jwtUtil.validateIp(token, "192.168.1.2"));
    }
    
    @Test
    void testGetTokenRemainingTime() {
        // Given
        String token = jwtUtil.generateAccessToken(
            "u1a2b3c4d5e6f", "testuser", true, "session_123", "device_456", "192.168.1.1"
        );
        
        // When
        long remainingTime = jwtUtil.getTokenRemainingTime(token);
        
        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= 3600); // 应该小于等于1小时（秒）
        
        System.out.println("\nToken remaining time: " + remainingTime + " seconds");
    }
    
    @Test
    void testInvalidTokenShouldFailValidation() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When & Then
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
    
    @Test
    void testGenerateSessionId() {
        // When
        String sessionId1 = jwtUtil.generateSessionId();
        String sessionId2 = jwtUtil.generateSessionId();
        
        // Then
        assertNotNull(sessionId1);
        assertNotNull(sessionId2);
        assertNotEquals(sessionId1, sessionId2);
        assertTrue(sessionId1.startsWith("session_"));
        
        System.out.println("\nGenerated Session IDs:");
        System.out.println("Session ID 1: " + sessionId1);
        System.out.println("Session ID 2: " + sessionId2);
    }
    
    @Test
    void testGenerateDeviceId() {
        // Given
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        String ipAddress = "192.168.1.1";
        
        // When
        String deviceId = jwtUtil.generateDeviceId(userAgent, ipAddress);
        
        // Then
        assertNotNull(deviceId);
        assertEquals(16, deviceId.length());
        
        System.out.println("\nGenerated Device ID: " + deviceId);
    }
    
    @Test
    void testGetJtiFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(
            "u1a2b3c4d5e6f", "testuser", true, "session_123", "device_456", "192.168.1.1"
        );
        
        // When
        String jti = jwtUtil.getJtiFromToken(token);
        
        // Then
        assertNotNull(jti);
        System.out.println("\nJWT Token ID (JTI): " + jti);
    }
}
