package xw.szbz.cn.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户ID加密工具测试
 */
class EnhancedUserIdEncryptionTest {
    
    private EnhancedUserIdEncryption encryption;
    
    @BeforeEach
    void setUp() {
        encryption = new EnhancedUserIdEncryption();
        // 设置测试密钥（32字节）
        ReflectionTestUtils.setField(encryption, "encryptionKey", 
            "test-encryption-key-32-bytes!!");
    }
    
    @Test
    void testEncryptAndDecryptUserId() {
        // Given
        Long userId = 12345L;
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        String encryptedId = encryption.encryptUserId(userId, createdAt);
        Long decryptedId = encryption.decryptUserId(encryptedId);
        
        // Then
        assertNotNull(encryptedId);
        assertEquals(13, encryptedId.length());
        assertTrue(encryptedId.startsWith("u"));
        assertEquals(userId, decryptedId);
        
        System.out.println("Original User ID: " + userId);
        System.out.println("Encrypted User ID: " + encryptedId);
        System.out.println("Decrypted User ID: " + decryptedId);
    }
    
    @Test
    void testEncryptedIdFormat() {
        // Given
        Long userId = 999L;
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        String encryptedId = encryption.encryptUserId(userId, createdAt);
        
        // Then
        assertTrue(encryptedId.matches("^u[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{12}$"));
        System.out.println("Encrypted ID format test: " + encryptedId);
    }
    
    @Test
    void testDifferentUserIdsProduceDifferentEncryptedIds() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        String encrypted1 = encryption.encryptUserId(1L, createdAt);
        String encrypted2 = encryption.encryptUserId(2L, createdAt);
        
        // Then
        assertNotEquals(encrypted1, encrypted2);
        System.out.println("User ID 1: " + encrypted1);
        System.out.println("User ID 2: " + encrypted2);
    }
    
    @Test
    void testSameUserIdWithDifferentTimestampsProducesDifferentEncryptedIds() {
        // Given
        Long userId = 12345L;
        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = time1.plusDays(1);
        
        // When
        String encrypted1 = encryption.encryptUserId(userId, time1);
        String encrypted2 = encryption.encryptUserId(userId, time2);
        
        // Then
        assertNotEquals(encrypted1, encrypted2);
        System.out.println("Same user ID, different timestamps:");
        System.out.println("Time 1: " + encrypted1);
        System.out.println("Time 2: " + encrypted2);
    }
    
    @Test
    void testDecryptInvalidIdShouldThrowException() {
        // Given
        String invalidId = "uINVALIDID123";
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            encryption.decryptUserId(invalidId);
        });
    }
    
    @Test
    void testDecryptTamperedIdShouldThrowException() {
        // Given
        Long userId = 12345L;
        LocalDateTime createdAt = LocalDateTime.now();
        String validEncryptedId = encryption.encryptUserId(userId, createdAt);
        
        // Tamper with the encrypted ID (change last character)
        String tamperedId = validEncryptedId.substring(0, 12) + "X";
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            encryption.decryptUserId(tamperedId);
        });
    }
    
    @Test
    void testLargeUserIds() {
        // Given
        Long largeUserId = Long.MAX_VALUE;
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        String encryptedId = encryption.encryptUserId(largeUserId, createdAt);
        Long decryptedId = encryption.decryptUserId(encryptedId);
        
        // Then
        assertEquals(largeUserId, decryptedId);
        System.out.println("Large User ID: " + largeUserId);
        System.out.println("Encrypted: " + encryptedId);
    }
}
