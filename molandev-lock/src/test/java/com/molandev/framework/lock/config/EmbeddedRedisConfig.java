package com.molandev.framework.lock.config;

import jakarta.annotation.PreDestroy;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfig {
    private RedisServer redisServer;
    private RedissonClient redissonClient;

    public EmbeddedRedisConfig() throws IOException {
        // 启动嵌入式Redis服务器
        redisServer = RedisServer.builder()
                .port(6381)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();

        // 创建Redisson客户端
        org.redisson.config.Config config = new org.redisson.config.Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6381")
                .setDatabase(0);
        redissonClient = org.redisson.Redisson.create(config);
    }

    @PreDestroy
    public void tearDown() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public RedissonClient redissonClient() {
        return redissonClient;
    }
}