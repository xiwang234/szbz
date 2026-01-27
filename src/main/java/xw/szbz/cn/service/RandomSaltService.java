package xw.szbz.cn.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;
import xw.szbz.cn.exception.ServiceException;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 随机盐管理服务
 * 使用Guava Cache管理随机盐，5分钟过期
 */
@Service
public class RandomSaltService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 盐状态枚举
     */
    public enum SaltStatus {
        UNUSED,  // 未使用
        USED     // 已使用
    }

    /**
     * 盐信息类
     */
    public static class SaltInfo {
        private final String salt;
        private SaltStatus status;
        private final long createTime;

        public SaltInfo(String salt) {
            this.salt = salt;
            this.status = SaltStatus.UNUSED;
            this.createTime = System.currentTimeMillis();
        }

        public String getSalt() {
            return salt;
        }

        public SaltStatus getStatus() {
            return status;
        }

        public void setStatus(SaltStatus status) {
            this.status = status;
        }

        public long getCreateTime() {
            return createTime;
        }
    }

    // Guava Cache: 5分钟过期，最多存储10000个盐
    private final Cache<String, SaltInfo> saltCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

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

        String saltStr = salt.toString();
        SaltInfo saltInfo = new SaltInfo(saltStr);
        saltCache.put(saltStr, saltInfo);

        return saltStr;
    }

    /**
     * 验证随机盐是否有效（未使用且在5分钟有效期内）
     *
     * @param salt 随机盐
     * @return 是否有效
     */
    public boolean validateSalt(String salt) {
        if (salt == null || salt.isEmpty()) {
            return false;
        }

        SaltInfo saltInfo = saltCache.getIfPresent(salt);
        if (saltInfo == null) {
            return false; // 盐不存在或已过期
        }

        return saltInfo.getStatus() == SaltStatus.UNUSED;
    }

    /**
     * 标记盐为已使用
     *
     * @param salt 随机盐
     */
    public void markSaltAsUsed(String salt) {
        SaltInfo saltInfo = saltCache.getIfPresent(salt);
        if (saltInfo != null) {
            saltInfo.setStatus(SaltStatus.USED);
        }
    }

    /**
     * 验证并标记盐为已使用（原子操作）
     *
     * @param salt 随机盐
     * @throws ServiceException 如果盐无效或已使用
     */
    public void validateAndMarkSaltAsUsed(String salt) {
        if (salt == null || salt.isEmpty()) {
            throw new ServiceException("随机盐不能为空");
        }

        SaltInfo saltInfo = saltCache.getIfPresent(salt);
        if (saltInfo == null) {
            throw new ServiceException("随机盐无效或已过期");
        }

        if (saltInfo.getStatus() == SaltStatus.USED) {
            throw new ServiceException("随机盐已被使用");
        }

        saltInfo.setStatus(SaltStatus.USED);
    }

    /**
     * 获取缓存统计信息（用于监控）
     */
    public String getCacheStats() {
        return String.format("Salt Cache - Size: %d, Stats: %s",
            saltCache.size(),
            saltCache.stats().toString());
    }

    /**
     * 获取所有缓存条目（用于缓存管理）
     */
    public Map<String, Object> getAllSaltEntries() {
        Map<String, Object> entries = new java.util.HashMap<>();
        saltCache.asMap().forEach((key, value) -> {
            Map<String, Object> saltData = new java.util.HashMap<>();
            saltData.put("salt", value.getSalt());
            saltData.put("status", value.getStatus().toString());
            saltData.put("createTime", value.getCreateTime());
            saltData.put("age", System.currentTimeMillis() - value.getCreateTime() + "ms");
            entries.put(key, saltData);
        });
        return entries;
    }
}
