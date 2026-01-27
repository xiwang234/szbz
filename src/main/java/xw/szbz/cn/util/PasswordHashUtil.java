package xw.szbz.cn.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码哈希工具类
 * 使用SHA-256算法进行密码哈希
 */
@Component
public class PasswordHashUtil {

    /**
     * 使用SHA-256对密码进行哈希
     *
     * @param password 原始密码
     * @return 十六进制格式的哈希值
     */
    public String sha256Hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param hashedPassword 已哈希的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        String hash = sha256Hash(rawPassword);
        return hash.equals(hashedPassword);
    }
}
