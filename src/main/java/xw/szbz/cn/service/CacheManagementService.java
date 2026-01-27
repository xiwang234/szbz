package xw.szbz.cn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xw.szbz.cn.config.SecurityFilter;
import xw.szbz.cn.model.CacheInfoResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存管理服务
 * 统一管理所有本地缓存信息
 */
@Service
public class CacheManagementService {

    @Autowired
    private RandomSaltService randomSaltService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SecurityFilter securityFilter;

    /**
     * 获取所有缓存信息
     */
    public List<CacheInfoResponse> getAllCacheInfo() {
        List<CacheInfoResponse> cacheInfoList = new ArrayList<>();

        // 1. 随机盐缓存（Guava Cache）
        cacheInfoList.add(getRandomSaltCacheInfo());

        // 2. Token黑名单缓存（ConcurrentHashMap）
        cacheInfoList.add(getTokenBlacklistCacheInfo());

        // 3. IP限流缓存（ConcurrentHashMap）
        cacheInfoList.addAll(getSecurityCacheInfo());

        return cacheInfoList;
    }

    /**
     * 获取随机盐缓存信息
     */
    private CacheInfoResponse getRandomSaltCacheInfo() {
        Map<String, Object> entries = randomSaltService.getAllSaltEntries();

        CacheInfoResponse info = new CacheInfoResponse();
        info.setCacheName("RandomSaltCache");
        info.setCacheType("Guava Cache");
        info.setSize(entries.size());
        info.setMaxSize(10000L);
        info.setExpiration("5分钟（expireAfterWrite）");
        info.setEntries(entries);
        info.setStats(randomSaltService.getCacheStats());

        return info;
    }

    /**
     * 获取Token黑名单缓存信息
     */
    private CacheInfoResponse getTokenBlacklistCacheInfo() {
        Map<String, Object> entries = authService.getTokenBlacklistEntries();

        CacheInfoResponse info = new CacheInfoResponse();
        info.setCacheName("TokenBlacklist");
        info.setCacheType("ConcurrentHashMap");
        info.setSize(entries.size());
        info.setMaxSize(null); // ConcurrentHashMap 无固定大小限制
        info.setExpiration("根据Token过期时间自动清理");
        info.setEntries(entries);
        info.setStats("活跃Token黑名单数量: " + entries.size());

        return info;
    }

    /**
     * 获取安全相关缓存信息（IP限流、IP黑名单）
     */
    private List<CacheInfoResponse> getSecurityCacheInfo() {
        List<CacheInfoResponse> securityCaches = new ArrayList<>();

        // IP请求计数缓存
        CacheInfoResponse ipRequestCountsInfo = new CacheInfoResponse();
        ipRequestCountsInfo.setCacheName("IpRequestCounts");
        ipRequestCountsInfo.setCacheType("ConcurrentHashMap");
        Map<String, Object> requestCountEntries = securityFilter.getIpRequestCountEntries();
        ipRequestCountsInfo.setSize(requestCountEntries.size());
        ipRequestCountsInfo.setMaxSize(null);
        ipRequestCountsInfo.setExpiration("每分钟重置");
        ipRequestCountsInfo.setEntries(requestCountEntries);
        ipRequestCountsInfo.setStats("当前监控IP数量: " + requestCountEntries.size());
        securityCaches.add(ipRequestCountsInfo);

        // IP封禁计数缓存
        CacheInfoResponse ipBlockCountsInfo = new CacheInfoResponse();
        ipBlockCountsInfo.setCacheName("IpBlockCounts");
        ipBlockCountsInfo.setCacheType("ConcurrentHashMap");
        Map<String, Object> blockCountEntries = securityFilter.getIpBlockCountEntries();
        ipBlockCountsInfo.setSize(blockCountEntries.size());
        ipBlockCountsInfo.setMaxSize(null);
        ipBlockCountsInfo.setExpiration("永久保存（需手动清理）");
        ipBlockCountsInfo.setEntries(blockCountEntries);
        ipBlockCountsInfo.setStats("累计拦截记录数量: " + blockCountEntries.size());
        securityCaches.add(ipBlockCountsInfo);

        // IP黑名单缓存
        CacheInfoResponse blockedIpsInfo = new CacheInfoResponse();
        blockedIpsInfo.setCacheName("BlockedIps");
        blockedIpsInfo.setCacheType("ConcurrentHashMap.KeySet");
        Map<String, Object> blockedIpEntries = securityFilter.getBlockedIpEntries();
        blockedIpsInfo.setSize(blockedIpEntries.size());
        blockedIpsInfo.setMaxSize(null);
        blockedIpsInfo.setExpiration("永久封禁（需手动解封）");
        blockedIpsInfo.setEntries(blockedIpEntries);
        blockedIpsInfo.setStats("当前封禁IP数量: " + blockedIpEntries.size());
        securityCaches.add(blockedIpsInfo);

        return securityCaches;
    }
}
