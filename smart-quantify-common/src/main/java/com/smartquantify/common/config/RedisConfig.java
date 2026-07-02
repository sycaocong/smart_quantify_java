package com.smartquantify.common.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis配置类
 * 配置Redisson客户端和StringRedisTemplate
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Redis主机地址
     */
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    /**
     * Redis端口
     */
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * 配置Redisson客户端
     * 用于分布式锁和高级Redis功能
     * @return RedissonClient实例
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectionMinimumIdleSize(2)
                .setConnectionPoolSize(16)
                .setConnectTimeout(3000)
                .setTimeout(3000);
        RedissonClient client = Redisson.create(config);
        log.info("Redisson client initialized: {}:{}", redisHost, redisPort);
        return client;
    }

    /**
     * 配置StringRedisTemplate
     * 用于简单的字符串操作和限流计数
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
