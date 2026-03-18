package com.molandev.framework.lock.support.redisson;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基于redisson的lockFactory
 */
public class RedissonLockFactory implements LockFactory {

    /**
     * redisson
     */
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取锁
     *
     * @param lockInfo 锁信息
     * @return {@link Lock}
     */
    @Override
    public Lock getLock(LockInfo lockInfo) {
        return new RedissonLock(redissonClient, lockInfo);
    }


}
