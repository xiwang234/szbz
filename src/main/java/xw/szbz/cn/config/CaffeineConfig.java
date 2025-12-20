package xw.szbz.cn.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xw.szbz.cn.model.BaZiAnalysisResponse;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * 替代 Redis，降低云服务成本
 */
@Configuration
public class CaffeineConfig {

    /**
     * 八字分析结果缓存
     * - 最大条目数：10000（约占用 50-100MB 内存）
     * - 过期时间：3天
     * - 过期策略：写入后过期
     * - Key: openId
     * - Value: BaZiAnalysisResponse (JSON格式)
     */
    @Bean
    public Cache<String, String> baziCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.DAYS)
                .recordStats() // 启用统计信息
                .build();
    }
}
