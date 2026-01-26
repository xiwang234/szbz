package xw.szbz.cn.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段加密工具测试
 */
class FieldEncryptionUtilTest {
    
    private FieldEncryptionUtil encryptionUtil;
    
    @BeforeEach
    void setUp() {
        encryptionUtil = new FieldEncryptionUtil();
        // 设置测试密钥（32字节）
        ReflectionTestUtils.setField(encryptionUtil, "encryptionKey", 
            "field-encryption-key-32-bytes!!");
    }
    
    @Test
    void testEncryptAndDecryptEmail() {
        // Given
        String email = "user@example.com";
        
        // When
        String encrypted = encryptionUtil.encryptEmail(email);
        String decrypted = encryptionUtil.decryptEmail(encrypted);
        
        // Then
        assertNotNull(encrypted);
        assertNotEquals(email, encrypted);
        assertEquals(email, decrypted);
        
        System.out.println("Original Email: " + email);
        System.out.println("Encrypted Email: " + encrypted);
        System.out.println("Decrypted Email: " + decrypted);
    }
    
    @Test
    void testEncryptAndDecryptPhone() {
        // Given
        String phone = "13812345678";
        
        // When
        String encrypted = encryptionUtil.encryptPhone(phone);
        String decrypted = encryptionUtil.decryptPhone(encrypted);
        
        // Then
        assertEquals(phone, decrypted);
        System.out.println("Original Phone: " + phone);
        System.out.println("Encrypted Phone: " + encrypted);
    }
    
    @Test
    void testSameEmailProducesDifferentEncryptedValues() {
        // Given
        String email = "test@example.com";
        
        // When
        String encrypted1 = encryptionUtil.encrypt(email);
        String encrypted2 = encryptionUtil.encrypt(email);
        
        // Then
        // 由于使用随机IV，相同明文应产生不同密文
        assertNotEquals(encrypted1, encrypted2);
        
        // 但解密后应该相同
        assertEquals(email, encryptionUtil.decrypt(encrypted1));
        assertEquals(email, encryptionUtil.decrypt(encrypted2));
        
        System.out.println("Same email, different encrypted values:");
        System.out.println("Encrypted 1: " + encrypted1);
        System.out.println("Encrypted 2: " + encrypted2);
    }
    
    @Test
    void testEncryptEmptyString() {
        // Given
        String empty = "";
        
        // When
        String encrypted = encryptionUtil.encrypt(empty);
        
        // Then
        assertEquals(empty, encrypted);
    }
    
    @Test
    void testEncryptNull() {
        // Given
        String nullValue = null;
        
        // When
        String encrypted = encryptionUtil.encrypt(nullValue);
        
        // Then
        assertNull(encrypted);
    }
    
    @Test
    void testDecryptInvalidDataShouldThrowException() {
        // Given
        String invalidEncrypted = "invalid-base64-data";
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            encryptionUtil.decrypt(invalidEncrypted);
        });
    }
    
    @Test
    void testEncryptLongText() {
        // Given
        String longText = "This is a very long email address or text that needs to be encrypted: " +
                         "very.long.email.address.for.testing@example.subdomain.domain.com";
        
        // When
        String encrypted = encryptionUtil.encrypt(longText);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        // Then
        assertEquals(longText, decrypted);
        System.out.println("Long text length: " + longText.length());
        System.out.println("Encrypted length: " + encrypted.length());
    }
    
    @Test
    void testEncryptSpecialCharacters() {
        // Given
        String specialChars = "email+tag@example.com!#$%&*()";
        
        // When
        String encrypted = encryptionUtil.encrypt(specialChars);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        // Then
        assertEquals(specialChars, decrypted);
        System.out.println("Special characters: " + specialChars);
        System.out.println("Encrypted: " + encrypted);
    }
}
