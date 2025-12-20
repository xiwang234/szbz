package xw.szbz.cn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xw.szbz.cn.model.BaZiAnalysisResponse;

/**
 * 八字缓存服务
 * 使用 Caffeine 本地缓存管理八字分析结果
 * Key: openId
 * Value: BaZiAnalysisResponse (JSON 字符串)
 */
@Service
public class BaZiCacheService {

    private final Cache<String, String> baziCache;
    private final ObjectMapper objectMapper;

    @Autowired
    public BaZiCacheService(Cache<String, String> baziCache, ObjectMapper objectMapper) {
        this.baziCache = baziCache;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成缓存 Key
     * 格式: openId
     * 
     * @param openId 用户openId
     * @return 缓存Key
     */
    public String generateCacheKey(String openId) {
        return openId;
    }

    /**
     * 从缓存获取八字分析结果
     * 
     * @param openId 用户openId
     * @return 八字分析响应，不存在返回null
     */
    public BaZiAnalysisResponse get(String openId) {
        try {
            String cacheKey = generateCacheKey(openId);
            String jsonValue = baziCache.getIfPresent(cacheKey);
            
            if (jsonValue != null) {
                return objectMapper.readValue(jsonValue, BaZiAnalysisResponse.class);
            }
            return null;
        } catch (JsonProcessingException e) {
            System.err.println("缓存数据解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将八字分析结果存入缓存
     * 
     * @param openId 用户openId
     * @param response 八字分析响应
     */
    public void put(String openId, BaZiAnalysisResponse response) {
        try {
            String cacheKey = generateCacheKey(openId);
            String jsonValue = objectMapper.writeValueAsString(response);
            baziCache.put(cacheKey, jsonValue);
            System.out.println("缓存已更新: " + cacheKey);
        } catch (JsonProcessingException e) {
            System.err.println("缓存数据序列化失败: " + e.getMessage());
        }
    }

    /**
     * 删除缓存
     * 
     * @param openId 用户openId
     */
    public void invalidate(String openId) {
        String cacheKey = generateCacheKey(openId);
        baziCache.invalidate(cacheKey);
        System.out.println("缓存已清除: " + cacheKey);
    }

    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        baziCache.invalidateAll();
        System.out.println("所有缓存已清除");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息字符串
     */
    public String getCacheStats() {
        var stats = baziCache.stats();
        return String.format(
            "缓存统计 - 命中率: %.2f%%, 总请求: %d, 命中: %d, 未命中: %d, 驱逐: %d",
            stats.hitRate() * 100,
            stats.requestCount(),
            stats.hitCount(),
            stats.missCount(),
            stats.evictionCount()
        );
    }
}
