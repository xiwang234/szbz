package xw.szbz.cn.model;

import java.util.Map;

/**
 * 缓存信息响应
 */
public class CacheInfoResponse {

    private String cacheName;           // 缓存名称
    private String cacheType;           // 缓存类型（Guava/ConcurrentHashMap）
    private Integer size;               // 当前缓存大小
    private Long maxSize;               // 最大缓存大小
    private String expiration;          // 过期策略
    private Map<String, Object> entries; // 缓存条目（key-value）
    private String stats;               // 缓存统计信息

    public CacheInfoResponse() {
    }

    public CacheInfoResponse(String cacheName, String cacheType, Integer size, Long maxSize,
                            String expiration, Map<String, Object> entries, String stats) {
        this.cacheName = cacheName;
        this.cacheType = cacheType;
        this.size = size;
        this.maxSize = maxSize;
        this.expiration = expiration;
        this.entries = entries;
        this.stats = stats;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public Map<String, Object> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, Object> entries) {
        this.entries = entries;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    @Override
    public String toString() {
        return "CacheInfoResponse{" +
                "cacheName='" + cacheName + '\'' +
                ", cacheType='" + cacheType + '\'' +
                ", size=" + size +
                ", maxSize=" + maxSize +
                ", expiration='" + expiration + '\'' +
                ", stats='" + stats + '\'' +
                '}';
    }
}
