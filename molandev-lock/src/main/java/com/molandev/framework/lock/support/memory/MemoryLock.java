package com.molandev.framework.lock.support.memory;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于内存的锁实现
 */
public class MemoryLock implements Lock {

    private final ReentrantLockWithLease reentrantLock;
    private final LockInfo lockInfo;

    public MemoryLock(ReentrantLockWithLease reentrantLock, LockInfo lockInfo) {
        this.reentrantLock = reentrantLock;
        this.lockInfo = lockInfo;
    }

    @Override
    public boolean acquire() {
        try {
            return reentrantLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean release() {
        if (reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
            return true;
        }
        return false;
    }

    /**
     * 带租约时间的可重入锁
     */
    static class ReentrantLockWithLease extends ReentrantLock {

        private volatile long leaseExpirationTime = 0;

        /**
         * 尝试获取锁，并设置租约时间
         *
         * @param waitTime  等待时间
         * @param leaseTime 租约时间
         * @param unit      时间单位
         * @return 是否获取成功
         * @throws InterruptedException 中断异常
         */
        public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
            boolean acquired = super.tryLock(waitTime, unit);
            if (acquired && leaseTime > 0) {
                // 设置租约过期时间
                this.leaseExpirationTime = System.currentTimeMillis() + unit.toMillis(leaseTime);
            }
            return acquired;
        }

        /**
         * 检查锁是否已过期
         *
         * @return 是否已过期
         */
        public boolean isExpired() {
            return leaseExpirationTime > 0 && System.currentTimeMillis() >= leaseExpirationTime;
        }
    }
}
