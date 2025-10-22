package com.finance.aiexpense.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
public class CacheConfig {

    // Default in-memory cache (for development)
    @Bean
    @Profile("dev")
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "transactions",
                "insights",
                "userStats",
                "aiResponses"
        );
    }
}