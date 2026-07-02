package com.smartquantify.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 配置Caffeine本地缓存和Redis分布式缓存
 * RedisCacheManager设置为@Primary，支持按缓存名单独TTL配置
 */
@EnableCaching
@Configuration
public class CacheConfig {

    /**
     * 创建Jackson ObjectMapper，支持Java 8日期时间类型和泛型
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * 获取默认的Redis缓存配置
     * 使用JSON序列化，默认TTL为5分钟
     */
    private RedisCacheConfiguration getDefaultCacheConfig() {
        ObjectMapper objectMapper = createObjectMapper();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));
    }

    /**
     * 获取指定TTL的Redis缓存配置
     */
    private RedisCacheConfiguration getCacheConfigWithTtl(Duration ttl) {
        return getDefaultCacheConfig().entryTtl(ttl);
    }

    /**
     * 配置Caffeine本地缓存管理器
     * 设置初始容量500，最大容量50000，写入后10秒过期，访问后5秒过期
     * 用于热点数据的二级缓存
     * @return CaffeineCacheManager实例
     */
    @Bean(name = "caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(500)
                .maximumSize(50000)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .recordStats());
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "klines",
                "orderBook",
                "ticker",
                "instruments",
                "riskRules",
                "riskState"
        ));
        return cacheManager;
    }

    /**
     * 配置Redis分布式缓存管理器（主缓存）
     * 为不同缓存设置独立的过期时间，支持动态创建缓存
     * @param redisConnectionFactory Redis连接工厂
     * @return RedisCacheManager实例
     */
    @Bean(name = "redisCacheManager")
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("klines", getCacheConfigWithTtl(Duration.ofSeconds(30)));
        cacheConfigurations.put("orderBook", getCacheConfigWithTtl(Duration.ofSeconds(15)));
        cacheConfigurations.put("ticker", getCacheConfigWithTtl(Duration.ofSeconds(10)));
        cacheConfigurations.put("instruments", getCacheConfigWithTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("riskRules", getCacheConfigWithTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("riskState", getCacheConfigWithTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(getDefaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
