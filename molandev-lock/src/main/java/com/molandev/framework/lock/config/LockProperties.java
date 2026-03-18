package com.molandev.framework.lock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 锁配置属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "molandev.lock")
public class LockProperties {

    /**
     * 锁类型: redis(基于RedisTemplate), memory
     */
    private LockFactoryType type = LockFactoryType.memory;

    public enum LockFactoryType {
        redis, memory, redisson
    }

}
