package xw.szbz.cn.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * 字段加密工具类
 * 用于加密敏感字段（如邮箱、手机号等）
 * 算法：AES-256-GCM
 * 特点：认证加密，防止密文被篡改
 */
@Component
public class FieldEncryptionUtil {
    
    @Value("${field.encryption.key:your-field-encryption-key-32-chars}")
    private String encryptionKey;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    /**
     * 加密字段
     * @param plainText 明文
     * @return Base64编码的密文
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // 初始化密钥（确保32字节）
            byte[] keyBytes = prepareKey(encryptionKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            // 初始化Cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            
            // 加密
            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // 将IV和密文拼接（IV + 密文）
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            
            // Base64编码
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt field", e);
        }
    }
    
    /**
     * 解密字段
     * @param encryptedText Base64编码的密文
     * @return 明文
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // Base64解码
            byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
            
            if (encryptedData.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Encrypted data too short");
            }
            
            // 提取IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
            
            // 初始化密钥（确保32字节）
            byte[] keyBytes = prepareKey(encryptionKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            
            // 初始化Cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            
            // 解密
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt field", e);
        }
    }
    
    /**
     * 准备密钥：确保32字节（256位）
     */
    private byte[] prepareKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 不足32字节，补零
            keyBytes = Arrays.copyOf(keyBytes, 32);
        } else if (keyBytes.length > 32) {
            // 超过32字节，截断
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return keyBytes;
    }
    
    /**
     * 加密邮箱（封装方法，语义更清晰）
     */
    public String encryptEmail(String email) {
        return encrypt(email);
    }
    
    /**
     * 解密邮箱
     */
    public String decryptEmail(String encryptedEmail) {
        return decrypt(encryptedEmail);
    }
    
    /**
     * 加密手机号
     */
    public String encryptPhone(String phone) {
        return encrypt(phone);
    }
    
    /**
     * 解密手机号
     */
    public String decryptPhone(String encryptedPhone) {
        return decrypt(encryptedPhone);
    }
}
