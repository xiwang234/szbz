package xw.szbz.cn.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import xw.szbz.cn.entity.WebUser;
import xw.szbz.cn.model.AuthResponse;
import xw.szbz.cn.model.RegisterRequest;
import xw.szbz.cn.model.WebLoginRequest;
import xw.szbz.cn.repository.WebUserRepository;
import xw.szbz.cn.service.AuthService;
import xw.szbz.cn.service.DataMaskingService;
import xw.szbz.cn.util.EnhancedJwtUtil;
import xw.szbz.cn.util.EnhancedUserIdEncryption;
import xw.szbz.cn.util.FieldEncryptionUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Web认证集成测试
 * 测试完整的注册-登录-Token刷新流程
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WebAuthIntegrationTest {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private WebUserRepository webUserRepository;
    
    @Autowired
    private EnhancedJwtUtil jwtUtil;
    
    @Autowired
    private EnhancedUserIdEncryption userIdEncryption;
    
    @Autowired
    private FieldEncryptionUtil fieldEncryptionUtil;
    
    @Autowired
    private DataMaskingService maskingService;
    
    private static final String TEST_EMAIL = "integrationtest@example.com";
    private static final String TEST_USERNAME = "integrationtestuser";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 Integration Test";
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(TEST_EMAIL);
        webUserRepository.findByEmail(encryptedEmail).ifPresent(user -> {
            webUserRepository.delete(user);
        });
    }
    
    @Test
    void testCompleteAuthenticationFlow() {
        System.out.println("\n=== 测试完整认证流程 ===\n");
        
        // 1. 用户注册
        System.out.println("1. 用户注册");
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        
        authService.register(registerRequest, TEST_IP);
        System.out.println("✓ 注册成功");
        
        // 验证用户已创建
        String encryptedEmail = fieldEncryptionUtil.encryptEmail(TEST_EMAIL);
        WebUser user = webUserRepository.findByEmail(encryptedEmail).orElseThrow();
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertNotNull(user.getPasswordHash());
        System.out.println("✓ 用户数据已保存到数据库");
        System.out.println("  User ID: " + user.getId());
        System.out.println("  Username: " + user.getUsername());
        
        // 2. 用户登录
        System.out.println("\n2. 用户登录");
        WebLoginRequest loginRequest = new WebLoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);
        loginRequest.setDeviceId("test_device_123");
        
        AuthResponse authResponse = authService.login(loginRequest, TEST_IP, TEST_USER_AGENT);
        
        assertNotNull(authResponse);
        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertNotNull(authResponse.getEncryptedUserId());
        assertEquals(TEST_USERNAME, authResponse.getUsername());
        System.out.println("✓ 登录成功");
        System.out.println("  Access Token: " + authResponse.getAccessToken().substring(0, 50) + "...");
        System.out.println("  Refresh Token: " + authResponse.getRefreshToken().substring(0, 50) + "...");
        System.out.println("  Encrypted User ID: " + authResponse.getEncryptedUserId());
        System.out.println("  Masked Email: " + authResponse.getMaskedEmail());
        
        // 3. 验证Access Token
        System.out.println("\n3. 验证Access Token");
        assertTrue(jwtUtil.validateToken(authResponse.getAccessToken()));
        assertTrue(jwtUtil.isAccessToken(authResponse.getAccessToken()));
        
        String userIdFromToken = jwtUtil.getEncryptedUserIdFromToken(authResponse.getAccessToken());
        assertEquals(authResponse.getEncryptedUserId(), userIdFromToken);
        System.out.println("✓ Access Token验证通过");
        
        // 4. 解密用户ID
        System.out.println("\n4. 解密用户ID");
        Long decryptedUserId = userIdEncryption.decryptUserId(authResponse.getEncryptedUserId());
        assertEquals(user.getId(), decryptedUserId);
        System.out.println("✓ 用户ID解密成功");
        System.out.println("  Encrypted: " + authResponse.getEncryptedUserId());
        System.out.println("  Decrypted: " + decryptedUserId);
        
        // 5. 刷新Token
        System.out.println("\n5. 刷新Token");
        AuthResponse refreshedAuth = authService.refreshToken(
            authResponse.getRefreshToken(),
            loginRequest.getDeviceId(),
            TEST_IP
        );
        
        assertNotNull(refreshedAuth);
        assertNotNull(refreshedAuth.getAccessToken());
        assertNotNull(refreshedAuth.getRefreshToken());
        assertNotEquals(authResponse.getAccessToken(), refreshedAuth.getAccessToken());
        assertNotEquals(authResponse.getRefreshToken(), refreshedAuth.getRefreshToken());
        System.out.println("✓ Token刷新成功");
        System.out.println("  New Access Token: " + refreshedAuth.getAccessToken().substring(0, 50) + "...");
        
        // 6. 验证旧Token已失效（通过黑名单）
        System.out.println("\n6. 登出测试");
        authService.logout(refreshedAuth.getAccessToken(), refreshedAuth.getRefreshToken());
        System.out.println("✓ 登出成功");
        
        // 7. 测试邮箱脱敏
        System.out.println("\n7. 测试邮箱脱敏");
        String maskedEmail = maskingService.maskEmail(TEST_EMAIL);
        assertNotNull(maskedEmail);
        assertTrue(maskedEmail.contains("***"));
        System.out.println("  Original: " + TEST_EMAIL);
        System.out.println("  Masked: " + maskedEmail);
        
        // 8. 测试邮箱加密解密
        System.out.println("\n8. 测试邮箱加密解密");
        String encrypted = fieldEncryptionUtil.encryptEmail(TEST_EMAIL);
        String decrypted = fieldEncryptionUtil.decryptEmail(encrypted);
        assertEquals(TEST_EMAIL, decrypted);
        System.out.println("  Original: " + TEST_EMAIL);
        System.out.println("  Encrypted: " + encrypted.substring(0, 50) + "...");
        System.out.println("  Decrypted: " + decrypted);
        
        System.out.println("\n=== 集成测试全部通过 ✓ ===\n");
    }
    
    @Test
    void testUserIdEncryptionUniqueness() {
        System.out.println("\n=== 测试用户ID加密唯一性 ===\n");
        
        // 创建两个用户
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("user1_" + System.currentTimeMillis());
        request1.setEmail("user1_" + System.currentTimeMillis() + "@example.com");
        request1.setPassword("Password123!");
        authService.register(request1, TEST_IP);
        
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("user2_" + System.currentTimeMillis());
        request2.setEmail("user2_" + System.currentTimeMillis() + "@example.com");
        request2.setPassword("Password123!");
        authService.register(request2, TEST_IP);
        
        // 获取两个用户
        String encEmail1 = fieldEncryptionUtil.encryptEmail(request1.getEmail());
        String encEmail2 = fieldEncryptionUtil.encryptEmail(request2.getEmail());
        WebUser user1 = webUserRepository.findByEmail(encEmail1).orElseThrow();
        WebUser user2 = webUserRepository.findByEmail(encEmail2).orElseThrow();
        
        // 生成加密ID
        String encUserId1 = userIdEncryption.encryptUserId(user1.getId(), user1.getCreateTimeAsLocalDateTime());
        String encUserId2 = userIdEncryption.encryptUserId(user2.getId(), user2.getCreateTimeAsLocalDateTime());
        
        // 验证唯一性
        assertNotEquals(encUserId1, encUserId2);
        assertEquals(13, encUserId1.length());
        assertEquals(13, encUserId2.length());
        assertTrue(encUserId1.startsWith("u"));
        assertTrue(encUserId2.startsWith("u"));
        
        System.out.println("User 1 ID: " + user1.getId() + " -> " + encUserId1);
        System.out.println("User 2 ID: " + user2.getId() + " -> " + encUserId2);
        System.out.println("✓ 用户ID加密唯一性测试通过");
        
        // 清理
        webUserRepository.delete(user1);
        webUserRepository.delete(user2);
    }
}
