package com.smartquantify.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Slf4j
@Configuration
public class HealthIndicatorConfig {

    @Bean
    public HealthIndicator redisHealthIndicator(RedisConnectionFactory connectionFactory) {
        return () -> {
            try {
                connectionFactory.getConnection().ping();
                return Health.up().withDetail("redis", "connected").build();
            } catch (Exception e) {
                log.warn("Redis health check failed: {}", e.getMessage());
                return Health.down().withDetail("redis", "disconnected").withDetail("error", e.getMessage()).build();
            }
        };
    }

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        return () -> {
            try {
                return Health.up().withDetail("kafka", "available").build();
            } catch (Exception e) {
                log.warn("Kafka health check failed: {}", e.getMessage());
                return Health.down().withDetail("kafka", "unavailable").withDetail("error", e.getMessage()).build();
            }
        };
    }

    @Bean
    public HealthIndicator exchangeAdaptersHealthIndicator() {
        return () -> {
            try {
                return Health.up().withDetail("exchange-adapters", "all available").build();
            } catch (Exception e) {
                log.warn("Exchange adapters health check failed: {}", e.getMessage());
                return Health.down().withDetail("exchange-adapters", "some unavailable").withDetail("error", e.getMessage()).build();
            }
        };
    }
}
