package com.molandev.framework.lock.support.redisson;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class RedissonLock implements Lock {
    /**
     * redisson客户
     */
    protected RedissonClient redissonClient;

    /**
     * 锁信息
     */
    protected LockInfo lockInfo;

    /**
     * r锁
     */
    protected RLock rLock;

    public RedissonLock(RedissonClient client, LockInfo lockInfo) {
        this.redissonClient = client;
        this.lockInfo = lockInfo;
    }

    @Override
    public boolean acquire() {
        try {
            rLock = redissonClient.getLock(lockInfo.getKey());
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if (rLock != null && rLock.isHeldByCurrentThread()) {
            try {
                rLock.unlock();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


}
