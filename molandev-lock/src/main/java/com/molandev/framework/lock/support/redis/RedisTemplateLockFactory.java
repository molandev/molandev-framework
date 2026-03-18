package com.molandev.framework.lock.support.redis;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 RedisTemplate 的锁工厂
 */
public class RedisTemplateLockFactory implements LockFactory {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTemplateLockFactory(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Lock getLock(LockInfo lockInfo) {
        return new RedisTemplateLock(redisTemplate, lockInfo);
    }
}
