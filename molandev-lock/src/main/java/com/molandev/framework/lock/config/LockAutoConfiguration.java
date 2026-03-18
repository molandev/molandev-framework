package com.molandev.framework.lock.config;

import com.molandev.framework.lock.aspect.IdempotentAspect;
import com.molandev.framework.lock.aspect.LockAspect;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.memory.MemoryLockFactory;
import com.molandev.framework.lock.support.redis.RedisTemplateLockFactory;
import com.molandev.framework.lock.support.redisson.RedissonLockFactory;
import com.molandev.framework.lock.utils.LockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 锁自动配置类
 */
@Slf4j
@EnableAspectJAutoProxy
@Configuration
@EnableConfigurationProperties(LockProperties.class)
public class LockAutoConfiguration {

    /**
     * RedisTemplate 锁工厂
     *
     * @param redisConnectionFactory Redis 连接工厂
     * @return RedisTemplate 锁工厂
     */
    @Bean
    @ConditionalOnProperty(name = "molandev.lock.type", havingValue = "redis")
    public LockFactory redisTemplateLockFactory(org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory) {
        log.info("初始化 RedisTemplateLockFactory");
        
        // 创建专用于锁的 RedisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        
        // 配置序列化器
        org.springframework.data.redis.serializer.StringRedisSerializer stringSerializer = 
            new org.springframework.data.redis.serializer.StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        
        redisTemplate.afterPropertiesSet();
        
        RedisTemplateLockFactory factory = new RedisTemplateLockFactory(redisTemplate);
        // 设置到 LockUtils 静态工具类
        LockUtils.setLockFactory(factory);
        return factory;
    }

    /**
     * 使用Redisson而不是RedisTempalte
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "molandev.lock.type", havingValue = "redisson")
    public LockFactory redissonLockFactory() {
        log.info("初始化 RedissonLockFactory");
        RedissonLockFactory factory = new RedissonLockFactory();
        // 设置到 LockUtils 静态工具类
        LockUtils.setLockFactory(factory);
        return factory;
    }

    /**
     * 内存锁工厂
     *
     * @return 内存锁工厂
     */
    @Bean
    @ConditionalOnProperty(name = "molandev.lock.type", havingValue = "memory", matchIfMissing = true)
    public LockFactory memoryLockFactory() {
        log.info("初始化 MemoryLockFactory");
        MemoryLockFactory factory = new MemoryLockFactory();
        // 设置到 LockUtils 静态工具类
        LockUtils.setLockFactory(factory);
        return factory;
    }

    /**
     * 锁切面
     *
     * @return 锁切面
     */
    @Bean
    public LockAspect lockAspect() {
        return new LockAspect();
    }

    /**
     * 幂等切面
     *
     * @return 幂等切面
     */
    @Bean
    public IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

}
