package com.smartquantify.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Redis缓存服务
 * 提供统一的缓存操作接口，支持按缓存名独立TTL配置
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final CacheManager cacheManager;

    /**
     * 获取缓存
     * @param cacheName 缓存名
     * @return Cache实例，如果不存在返回null
     */
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * 设置缓存
     * 使用对应缓存名的TTL配置
     * @param cacheName 缓存名
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String cacheName, String key, Object value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                log.debug("Put cache: name={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to put cache: name={}, key={}, error={}", cacheName, key, e.getMessage());
        }
    }

    /**
     * 获取缓存值
     * @param cacheName 缓存名
     * @param key 缓存键
     * @param type 返回类型
     * @return 缓存值，如果不存在返回null
     */
    public <T> T get(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    return type.cast(wrapper.get());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get cache: name={}, key={}, error={}", cacheName, key, e.getMessage());
        }
        return null;
    }

    /**
     * 获取缓存值（通用类型）
     * @param cacheName 缓存名
     * @param key 缓存键
     * @return 缓存值，如果不存在返回null
     */
    public Object get(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    return wrapper.get();
                }
            }
        } catch (Exception e) {
            log.error("Failed to get cache: name={}, key={}, error={}", cacheName, key, e.getMessage());
        }
        return null;
    }

    /**
     * 删除缓存
     * @param cacheName 缓存名
     * @param key 缓存键
     */
    public void evict(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("Evicted cache: name={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to evict cache: name={}, key={}, error={}", cacheName, key, e.getMessage());
        }
    }

    /**
     * 清除整个缓存名的所有缓存
     * @param cacheName 缓存名
     */
    public void clear(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("Failed to clear cache: name={}, error={}", cacheName, e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     * @param cacheName 缓存名
     * @param key 缓存键
     * @return 如果存在返回true，否则返回false
     */
    public boolean exists(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache.get(key) != null;
            }
        } catch (Exception e) {
            log.error("Failed to check cache existence: name={}, key={}, error={}", cacheName, key, e.getMessage());
        }
        return false;
    }

    /**
     * 获取缓存键的剩余TTL（秒）
     * 注意：此方法依赖于具体的缓存实现，对于RedisCacheManager，
     * 可以通过RedisTemplate直接获取TTL
     * @param cacheName 缓存名
     * @param key 缓存键
     * @return 剩余TTL（秒），如果不存在返回-1
     */
    public long getTtl(String cacheName, String key) {
        return -1;
    }
}