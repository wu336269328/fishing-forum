package com.fishforum.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置 - 使用 Caffeine 内存缓存，避免 Redis 在单机部署下的额外内存与运维成本。
 *
 * 不同热点的过期时间不同：
 *   - sections / wikiCategories：基本不变，缓存 30 分钟
 *   - hotTags / hotPosts / statistics：内容会动，5 分钟
 *   - weather：外部接口结果，15 分钟以减少调用频次
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(defaultSpec());
        manager.setCacheNames(List.of(
                "sections",
                "wikiCategories",
                "hotTags",
                "hotPosts",
                "statistics",
                "weather"
        ));
        manager.registerCustomCache("sections",        longLived());
        manager.registerCustomCache("wikiCategories",  longLived());
        manager.registerCustomCache("weather",         midLived());
        return manager;
    }

    private static Caffeine<Object, Object> defaultSpec() {
        return Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats();
    }

    private static com.github.benmanes.caffeine.cache.Cache<Object, Object> longLived() {
        return Caffeine.newBuilder()
                .maximumSize(64)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private static com.github.benmanes.caffeine.cache.Cache<Object, Object> midLived() {
        return Caffeine.newBuilder()
                .maximumSize(64)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
