package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.repository.WebUserRepository;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.EnhancedUserIdEncryption;
import xw.szbz.cn.util.FieldEncryptionUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 */
class AuthServiceTest {
    
    @Mock
    private WebUserRepository webUserRepository;
    
    @Mock
    private EnhancedJwtUtil enhancedJwtUtil;
    
    @Mock
    private EnhancedUserIdEncryption userIdEncryption;
    
    @Mock
    private FieldEncryptionUtil fieldEncryptionUtil;
    
    @Mock
    private DataMaskingService maskingService;
    
    @InjectMocks
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }
    
    @Test
    void testRegisterSuccess() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        
        String ipAddress = "192.168.1.1";
        String encryptedEmail = "encrypted_email_base64";
        
        when(fieldEncryptionUtil.encryptEmail(request.getEmail())).thenReturn(encryptedEmail);
        when(webUserRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(webUserRepository.existsByEmail(encryptedEmail)).thenReturn(false);
        when(webUserRepository.save(any(WebUser.class))).thenAnswer(invocation -> {
            WebUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When
        authService.register(request, ipAddress);
        
        // Then
        verify(webUserRepository, times(1)).save(any(WebUser.class));
        verify(fieldEncryptionUtil, times(1)).encryptEmail(request.getEmail());
    }
    
    @Test
    void testRegisterWithExistingUsername() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        
        when(fieldEncryptionUtil.encryptEmail(anyString())).thenReturn("encrypted");
        when(webUserRepository.existsByUsername(request.getUsername())).thenReturn(true);
        
        // When & Then
        assertThrows(ServiceException.class, () -> {
            authService.register(request, "192.168.1.1");
        });
    }
    
    @Test
    void testRegisterWithExistingEmail() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("Password123!");
        
        String encryptedEmail = "encrypted_email";
        
        when(fieldEncryptionUtil.encryptEmail(request.getEmail())).thenReturn(encryptedEmail);
        when(webUserRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(webUserRepository.existsByEmail(encryptedEmail)).thenReturn(true);
        
        // When & Then
        assertThrows(ServiceException.class, () -> {
            authService.register(request, "192.168.1.1");
        });
    }
    
    @Test
    void testLoginSuccess() {
        // Given
        WebLoginRequest request = new WebLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setDeviceId("device_123");
        
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        String encryptedEmail = "encrypted_email";
        String encryptedUserId = "u1a2b3c4d5e6f";
        
        WebUser mockUser = new WebUser();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail(encryptedEmail);
        mockUser.setPasswordHash("$2a$10$hashedpassword");
        mockUser.setEmailVerified(true);
        mockUser.setActive(true);
        mockUser.setCreateTime(System.currentTimeMillis());
        
        when(fieldEncryptionUtil.encryptEmail(request.getEmail())).thenReturn(encryptedEmail);
        when(webUserRepository.findByEmail(encryptedEmail)).thenReturn(mockUser);
        when(userIdEncryption.encryptUserId(anyLong(), any(LocalDateTime.class))).thenReturn(encryptedUserId);
        when(enhancedJwtUtil.generateSessionId()).thenReturn("session_123");
        when(enhancedJwtUtil.generateAccessToken(anyString(), anyString(), anyBoolean(), 
            anyString(), anyString(), anyString())).thenReturn("access_token");
        when(enhancedJwtUtil.generateRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("refresh_token");
        when(fieldEncryptionUtil.decryptEmail(encryptedEmail)).thenReturn(request.getEmail());
        when(maskingService.maskEmail(request.getEmail())).thenReturn("t***@example.com");
        
        // When
        AuthResponse response = authService.login(request, ipAddress, userAgent);
        
        // Then
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals(encryptedUserId, response.getEncryptedUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("t***@example.com", response.getMaskedEmail());
        
        verify(webUserRepository, times(1)).save(any(WebUser.class));
    }
    
    @Test
    void testLoginWithInvalidEmail() {
        // Given
        WebLoginRequest request = new WebLoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123!");
        
        when(fieldEncryptionUtil.encryptEmail(anyString())).thenReturn("encrypted");
        when(webUserRepository.findByEmail(anyString())).thenReturn(null);
        
        // When & Then
        assertThrows(ServiceException.class, () -> {
            authService.login(request, "192.168.1.1", "Mozilla/5.0");
        });
    }
    
    @Test
    void testLoginWithInactiveAccount() {
        // Given
        WebLoginRequest request = new WebLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        
        WebUser inactiveUser = new WebUser();
        inactiveUser.setActive(false);
        inactiveUser.setPasswordHash("$2a$10$hashedpassword");
        
        when(fieldEncryptionUtil.encryptEmail(anyString())).thenReturn("encrypted");
        when(webUserRepository.findByEmail(anyString())).thenReturn(inactiveUser);
        
        // When & Then
        assertThrows(ServiceException.class, () -> {
            authService.login(request, "192.168.1.1", "Mozilla/5.0");
        });
    }
    
    @Test
    void testRefreshTokenSuccess() {
        // Given
        String refreshToken = "valid_refresh_token";
        String deviceId = "device_123";
        String ipAddress = "192.168.1.1";
        String encryptedUserId = "u1a2b3c4d5e6f";
        String sessionId = "session_123";
        
        WebUser mockUser = new WebUser();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("encrypted_email");
        mockUser.setEmailVerified(true);
        mockUser.setActive(true);
        mockUser.setCreateTime(System.currentTimeMillis());
        
        when(enhancedJwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(enhancedJwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(enhancedJwtUtil.getJtiFromToken(refreshToken)).thenReturn("jti_123");
        when(enhancedJwtUtil.validateDeviceId(refreshToken, deviceId)).thenReturn(true);
        when(enhancedJwtUtil.getEncryptedUserIdFromToken(refreshToken)).thenReturn(encryptedUserId);
        when(enhancedJwtUtil.getSessionIdFromToken(refreshToken)).thenReturn(sessionId);
        when(userIdEncryption.decryptUserId(encryptedUserId)).thenReturn(1L);
        when(webUserRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(enhancedJwtUtil.generateAccessToken(anyString(), anyString(), anyBoolean(),
            anyString(), anyString(), anyString())).thenReturn("new_access_token");
        when(enhancedJwtUtil.generateRefreshToken(anyString(), anyString(), anyString()))
            .thenReturn("new_refresh_token");
        when(fieldEncryptionUtil.decryptEmail(anyString())).thenReturn("test@example.com");
        when(maskingService.maskEmail(anyString())).thenReturn("t***@example.com");
        
        // When
        AuthResponse response = authService.refreshToken(refreshToken, deviceId, ipAddress);
        
        // Then
        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
    }
    
    @Test
    void testLogout() {
        // Given
        String accessToken = "access_token";
        String refreshToken = "refresh_token";
        
        when(enhancedJwtUtil.validateToken(anyString())).thenReturn(true);
        when(enhancedJwtUtil.getJtiFromToken(accessToken)).thenReturn("jti_access");
        when(enhancedJwtUtil.getJtiFromToken(refreshToken)).thenReturn("jti_refresh");
        
        // When
        authService.logout(accessToken, refreshToken);
        
        // Then
        verify(enhancedJwtUtil, times(2)).validateToken(anyString());
        verify(enhancedJwtUtil, times(2)).getJtiFromToken(anyString());
    }
    
    @Test
    void testGetUserByEncryptedId() {
        // Given
        String encryptedUserId = "u1a2b3c4d5e6f";
        Long userId = 1L;
        
        WebUser mockUser = new WebUser();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");
        
        when(userIdEncryption.decryptUserId(encryptedUserId)).thenReturn(userId);
        when(webUserRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // When
        WebUser result = authService.getUserByEncryptedId(encryptedUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
    }
}
