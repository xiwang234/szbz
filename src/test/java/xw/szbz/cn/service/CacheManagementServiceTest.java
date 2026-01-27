package xw.szbz.cn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import xw.szbz.cn.config.SecurityFilter;
import xw.szbz.cn.model.CacheInfoResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheManagementService 单元测试
 */
class CacheManagementServiceTest {

    @Mock
    private RandomSaltService randomSaltService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityFilter securityFilter;

    @InjectMocks
    private CacheManagementService cacheManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCacheInfo_ShouldReturnAllCaches() {
        // 1. 准备 RandomSaltService 的 mock 数据
        Map<String, Object> saltEntries = new HashMap<>();
        Map<String, Object> salt1Data = new HashMap<>();
        salt1Data.put("salt", "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6");
        salt1Data.put("status", "UNUSED");
        salt1Data.put("createTime", System.currentTimeMillis());
        salt1Data.put("age", "15000ms");
        saltEntries.put("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6", salt1Data);

        when(randomSaltService.getAllSaltEntries()).thenReturn(saltEntries);
        when(randomSaltService.getCacheStats()).thenReturn("Salt Cache - Size: 1, Stats: CacheStats{}");

        // 2. 准备 AuthService 的 mock 数据
        Map<String, Object> tokenEntries = new HashMap<>();
        Map<String, Object> token1Data = new HashMap<>();
        token1Data.put("expiryTime", System.currentTimeMillis() + 3600000);
        token1Data.put("remainingTime", "3600000ms");
        token1Data.put("isExpired", false);
        tokenEntries.put("jti-123456", token1Data);

        when(authService.getTokenBlacklistEntries()).thenReturn(tokenEntries);

        // 3. 准备 SecurityFilter 的 mock 数据
        Map<String, Object> ipRequestEntries = new HashMap<>();
        Map<String, Object> ip1Data = new HashMap<>();
        ip1Data.put("requestCount", 15);
        ip1Data.put("resetTime", System.currentTimeMillis() + 60000);
        ip1Data.put("remainingTime", "60000ms");
        ipRequestEntries.put("192.168.1.100", ip1Data);

        Map<String, Object> ipBlockEntries = new HashMap<>();
        Map<String, Object> block1Data = new HashMap<>();
        block1Data.put("blockCount", 3);
        block1Data.put("isBlocked", false);
        block1Data.put("threshold", 5);
        ipBlockEntries.put("192.168.1.200", block1Data);

        Map<String, Object> blockedIpEntries = new HashMap<>();
        Map<String, Object> blockedData = new HashMap<>();
        blockedData.put("blocked", true);
        blockedData.put("totalBlockCount", 5);
        blockedIpEntries.put("10.0.0.100", blockedData);

        when(securityFilter.getIpRequestCountEntries()).thenReturn(ipRequestEntries);
        when(securityFilter.getIpBlockCountEntries()).thenReturn(ipBlockEntries);
        when(securityFilter.getBlockedIpEntries()).thenReturn(blockedIpEntries);

        // 4. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 5. 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(5, result.size(), "应该返回5个缓存");

        // 6. 验证每个缓存的基本信息
        CacheInfoResponse randomSaltCache = findCacheByName(result, "RandomSaltCache");
        assertNotNull(randomSaltCache, "应该包含 RandomSaltCache");
        assertEquals("Guava Cache", randomSaltCache.getCacheType());
        assertEquals(1, randomSaltCache.getSize());
        assertEquals(10000L, randomSaltCache.getMaxSize());
        assertEquals("5分钟（expireAfterWrite）", randomSaltCache.getExpiration());
        assertNotNull(randomSaltCache.getEntries());
        assertTrue(randomSaltCache.getEntries().containsKey("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"));

        CacheInfoResponse tokenBlacklistCache = findCacheByName(result, "TokenBlacklist");
        assertNotNull(tokenBlacklistCache, "应该包含 TokenBlacklist");
        assertEquals("ConcurrentHashMap", tokenBlacklistCache.getCacheType());
        assertEquals(1, tokenBlacklistCache.getSize());
        assertNull(tokenBlacklistCache.getMaxSize());
        assertEquals("根据Token过期时间自动清理", tokenBlacklistCache.getExpiration());
        assertTrue(tokenBlacklistCache.getEntries().containsKey("jti-123456"));

        CacheInfoResponse ipRequestCountsCache = findCacheByName(result, "IpRequestCounts");
        assertNotNull(ipRequestCountsCache, "应该包含 IpRequestCounts");
        assertEquals("ConcurrentHashMap", ipRequestCountsCache.getCacheType());
        assertEquals(1, ipRequestCountsCache.getSize());
        assertEquals("每分钟重置", ipRequestCountsCache.getExpiration());
        assertTrue(ipRequestCountsCache.getEntries().containsKey("192.168.1.100"));

        CacheInfoResponse ipBlockCountsCache = findCacheByName(result, "IpBlockCounts");
        assertNotNull(ipBlockCountsCache, "应该包含 IpBlockCounts");
        assertEquals("ConcurrentHashMap", ipBlockCountsCache.getCacheType());
        assertEquals(1, ipBlockCountsCache.getSize());
        assertEquals("永久保存（需手动清理）", ipBlockCountsCache.getExpiration());
        assertTrue(ipBlockCountsCache.getEntries().containsKey("192.168.1.200"));

        CacheInfoResponse blockedIpsCache = findCacheByName(result, "BlockedIps");
        assertNotNull(blockedIpsCache, "应该包含 BlockedIps");
        assertEquals("ConcurrentHashMap.KeySet", blockedIpsCache.getCacheType());
        assertEquals(1, blockedIpsCache.getSize());
        assertEquals("永久封禁（需手动解封）", blockedIpsCache.getExpiration());
        assertTrue(blockedIpsCache.getEntries().containsKey("10.0.0.100"));

        // 7. 验证 mock 方法被调用
        verify(randomSaltService, times(1)).getAllSaltEntries();
        verify(randomSaltService, times(1)).getCacheStats();
        verify(authService, times(1)).getTokenBlacklistEntries();
        verify(securityFilter, times(1)).getIpRequestCountEntries();
        verify(securityFilter, times(1)).getIpBlockCountEntries();
        verify(securityFilter, times(1)).getBlockedIpEntries();
    }

    @Test
    void testGetAllCacheInfo_WithEmptyCaches() {
        // 1. 准备空的 mock 数据
        when(randomSaltService.getAllSaltEntries()).thenReturn(new HashMap<>());
        when(randomSaltService.getCacheStats()).thenReturn("Salt Cache - Size: 0, Stats: CacheStats{}");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证结果
        assertNotNull(result);
        assertEquals(5, result.size(), "即使缓存为空，也应该返回5个缓存对象");

        // 4. 验证每个缓存的大小都为0
        for (CacheInfoResponse cache : result) {
            assertEquals(0, cache.getSize(), cache.getCacheName() + " 的大小应该为0");
            assertNotNull(cache.getEntries(), cache.getCacheName() + " 的 entries 不应为 null");
            assertTrue(cache.getEntries().isEmpty(), cache.getCacheName() + " 的 entries 应该为空");
        }
    }

    @Test
    void testGetAllCacheInfo_WithMultipleEntries() {
        // 1. 准备多条数据的 mock
        Map<String, Object> saltEntries = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> saltData = new HashMap<>();
            saltData.put("salt", "salt" + i);
            saltData.put("status", i % 2 == 0 ? "UNUSED" : "USED");
            saltData.put("createTime", System.currentTimeMillis());
            saltData.put("age", (i * 1000) + "ms");
            saltEntries.put("salt" + i, saltData);
        }

        when(randomSaltService.getAllSaltEntries()).thenReturn(saltEntries);
        when(randomSaltService.getCacheStats()).thenReturn("Salt Cache - Size: 5, Stats: CacheStats{}");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证结果
        CacheInfoResponse randomSaltCache = findCacheByName(result, "RandomSaltCache");
        assertNotNull(randomSaltCache);
        assertEquals(5, randomSaltCache.getSize());
        assertEquals(5, randomSaltCache.getEntries().size());

        // 4. 验证具体数据
        for (int i = 0; i < 5; i++) {
            assertTrue(randomSaltCache.getEntries().containsKey("salt" + i));
            @SuppressWarnings("unchecked")
            Map<String, Object> saltData = (Map<String, Object>) randomSaltCache.getEntries().get("salt" + i);
            assertEquals("salt" + i, saltData.get("salt"));
            assertEquals(i % 2 == 0 ? "UNUSED" : "USED", saltData.get("status"));
        }
    }

    @Test
    void testGetAllCacheInfo_CacheNames() {
        // 1. 准备 mock 数据
        when(randomSaltService.getAllSaltEntries()).thenReturn(new HashMap<>());
        when(randomSaltService.getCacheStats()).thenReturn("stats");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证缓存名称
        List<String> expectedNames = List.of(
            "RandomSaltCache",
            "TokenBlacklist",
            "IpRequestCounts",
            "IpBlockCounts",
            "BlockedIps"
        );

        assertEquals(expectedNames.size(), result.size());

        for (String expectedName : expectedNames) {
            CacheInfoResponse cache = findCacheByName(result, expectedName);
            assertNotNull(cache, "应该包含缓存: " + expectedName);
            assertEquals(expectedName, cache.getCacheName());
        }
    }

    @Test
    void testGetAllCacheInfo_CacheTypes() {
        // 1. 准备 mock 数据
        when(randomSaltService.getAllSaltEntries()).thenReturn(new HashMap<>());
        when(randomSaltService.getCacheStats()).thenReturn("stats");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证缓存类型
        assertEquals("Guava Cache", findCacheByName(result, "RandomSaltCache").getCacheType());
        assertEquals("ConcurrentHashMap", findCacheByName(result, "TokenBlacklist").getCacheType());
        assertEquals("ConcurrentHashMap", findCacheByName(result, "IpRequestCounts").getCacheType());
        assertEquals("ConcurrentHashMap", findCacheByName(result, "IpBlockCounts").getCacheType());
        assertEquals("ConcurrentHashMap.KeySet", findCacheByName(result, "BlockedIps").getCacheType());
    }

    @Test
    void testGetAllCacheInfo_ExpirationPolicies() {
        // 1. 准备 mock 数据
        when(randomSaltService.getAllSaltEntries()).thenReturn(new HashMap<>());
        when(randomSaltService.getCacheStats()).thenReturn("stats");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证过期策略
        assertEquals("5分钟（expireAfterWrite）",
            findCacheByName(result, "RandomSaltCache").getExpiration());
        assertEquals("根据Token过期时间自动清理",
            findCacheByName(result, "TokenBlacklist").getExpiration());
        assertEquals("每分钟重置",
            findCacheByName(result, "IpRequestCounts").getExpiration());
        assertEquals("永久保存（需手动清理）",
            findCacheByName(result, "IpBlockCounts").getExpiration());
        assertEquals("永久封禁（需手动解封）",
            findCacheByName(result, "BlockedIps").getExpiration());
    }

    @Test
    void testGetAllCacheInfo_MaxSizes() {
        // 1. 准备 mock 数据
        when(randomSaltService.getAllSaltEntries()).thenReturn(new HashMap<>());
        when(randomSaltService.getCacheStats()).thenReturn("stats");
        when(authService.getTokenBlacklistEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpRequestCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getIpBlockCountEntries()).thenReturn(new HashMap<>());
        when(securityFilter.getBlockedIpEntries()).thenReturn(new HashMap<>());

        // 2. 执行测试
        List<CacheInfoResponse> result = cacheManagementService.getAllCacheInfo();

        // 3. 验证最大容量
        assertEquals(10000L, findCacheByName(result, "RandomSaltCache").getMaxSize());
        assertNull(findCacheByName(result, "TokenBlacklist").getMaxSize());
        assertNull(findCacheByName(result, "IpRequestCounts").getMaxSize());
        assertNull(findCacheByName(result, "IpBlockCounts").getMaxSize());
        assertNull(findCacheByName(result, "BlockedIps").getMaxSize());
    }

    // ========== 辅助方法 ==========

    /**
     * 根据缓存名称查找缓存对象
     */
    private CacheInfoResponse findCacheByName(List<CacheInfoResponse> caches, String name) {
        return caches.stream()
            .filter(cache -> cache.getCacheName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
