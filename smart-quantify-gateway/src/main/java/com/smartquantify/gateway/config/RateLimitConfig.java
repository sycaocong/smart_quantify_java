package com.smartquantify.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final StringRedisTemplate redisTemplate;

    @Bean
    public KeyResolver remoteAddressKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    @Bean
    public KeyResolver userIdKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-User-Id")
        ).defaultIfEmpty("anonymous");
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("X-API-Key")
        ).defaultIfEmpty("no-api-key");
    }

    @Bean
    public KeyResolver strategyKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().toString();
            String strategyId = extractStrategyId(path);
            return Mono.just(strategyId != null ? strategyId : "default");
        };
    }

    private String extractStrategyId(String path) {
        List<String> parts = Arrays.asList(path.split("/"));
        int idx = parts.indexOf("strategies");
        if (idx >= 0 && idx + 1 < parts.size()) {
            return parts.get(idx + 1);
        }
        return null;
    }
}
