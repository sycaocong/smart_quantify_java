package com.smartquantify.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 网关限流配置
 * 定义多种限流Key解析器，用于基于不同维度进行API限流
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    /**
     * Redis模板，用于存储限流计数器
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 默认限流Key解析器
     * 基于客户端IP地址进行限流
     * @return KeyResolver实例
     */
    @Bean
    @Primary
    public KeyResolver defaultKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    /**
     * 基于客户端IP地址的限流Key解析器
     * @return KeyResolver实例
     */
    @Bean("remoteAddressKeyResolver")
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    /**
     * 基于用户ID的限流Key解析器
     * 从请求头 X-User-Id 获取用户ID
     * @return KeyResolver实例
     */
    @Bean("userIdKeyResolver")
    public KeyResolver userIdKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-User-Id")
        ).defaultIfEmpty("anonymous");
    }

    /**
     * 基于API Key的限流Key解析器
     * 从请求头 X-API-Key 获取API密钥
     * @return KeyResolver实例
     */
    @Bean("apiKeyResolver")
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-API-Key")
        ).defaultIfEmpty("no-api-key");
    }

    /**
     * 基于策略ID的限流Key解析器
     * 从请求路径中提取策略ID进行限流
     * @return KeyResolver实例
     */
    @Bean("strategyKeyResolver")
    public KeyResolver strategyKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().toString();
            String strategyId = extractStrategyId(path);
            return Mono.just(strategyId != null ? strategyId : "default");
        };
    }

    /**
     * 从路径中提取策略ID
     * 支持路径格式：/api/v1/strategies/{strategyId}/...
     * @param path 请求路径
     * @return 策略ID，未找到返回null
     */
    private String extractStrategyId(String path) {
        List<String> parts = Arrays.asList(path.split("/"));
        int idx = parts.indexOf("strategies");
        if (idx >= 0 && idx + 1 < parts.size()) {
            return parts.get(idx + 1);
        }
        return null;
    }
}
