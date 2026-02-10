package xw.szbz.cn.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

/**
 * 随机盐生成服务
 * 注意：随机盐的存储和验证已改为数据库方式，见 UserSaltInfo 表
 * 此服务仅负责生成随机盐字符串
 */
@Service
public class RandomSaltService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成32位随机盐
     *
     * @return 随机盐字符串
     */
    public String generateRandomSalt() {
        StringBuilder salt = new StringBuilder(SALT_LENGTH);
        for (int i = 0; i < SALT_LENGTH; i++) {
            salt.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return salt.toString();
    }
}
