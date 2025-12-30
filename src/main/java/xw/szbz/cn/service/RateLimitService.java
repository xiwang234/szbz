package xw.szbz.cn.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * API调用频率限制服务
 * 使用Caffeine缓存实现内存级限流
 */
@Service
public class RateLimitService {

    @Autowired
    private Cache<String, Integer> rateLimitCache;

    @Value("${rate.limit.wenji.daily:5}")
    private int wenjiDailyLimit;

    /**
     * 检查是否超过调用限制
     *
     * @param openId 用户OpenId
     * @param apiName API名称（如: "wenji"）
     * @return true=可以调用, false=已超过限制
     */
    public boolean checkLimit(String openId, String apiName) {
        String key = buildKey(openId, apiName);
        Integer count = rateLimitCache.getIfPresent(key);

        int limit = getLimit(apiName);

        if (count == null) {
            return true; // 首次调用
        }

        return count < limit;
    }

    /**
     * 增加调用次数
     *
     * @param openId 用户OpenId
     * @param apiName API名称
     * @return 当前调用次数
     */
    public int incrementCount(String openId, String apiName) {
        String key = buildKey(openId, apiName);
        Integer count = rateLimitCache.getIfPresent(key);

        if (count == null) {
            count = 0;
        }

        count++;
        rateLimitCache.put(key, count);

        return count;
    }

    /**
     * 获取剩余调用次数
     *
     * @param openId 用户OpenId
     * @param apiName API名称
     * @return 剩余次数
     */
    public int getRemainingCount(String openId, String apiName) {
        String key = buildKey(openId, apiName);
        Integer count = rateLimitCache.getIfPresent(key);

        int limit = getLimit(apiName);

        if (count == null) {
            return limit;
        }

        return Math.max(0, limit - count);
    }

    /**
     * 获取已使用次数
     *
     * @param openId 用户OpenId
     * @param apiName API名称
     * @return 已使用次数
     */
    public int getUsedCount(String openId, String apiName) {
        String key = buildKey(openId, apiName);
        Integer count = rateLimitCache.getIfPresent(key);
        return count == null ? 0 : count;
    }

    /**
     * 构建缓存Key
     */
    private String buildKey(String openId, String apiName) {
        return openId + ":" + apiName;
    }

    /**
     * 根据API名称获取限制次数
     */
    private int getLimit(String apiName) {
        if ("wenji".equals(apiName)) {
            return wenjiDailyLimit;
        }
        return 5; // 默认值
    }

    /**
     * 获取API的每日限制次数（用于返回给前端）
     *
     * @param apiName API名称
     * @return 每日限制次数
     */
    public int getDailyLimit(String apiName) {
        return getLimit(apiName);
    }
}
