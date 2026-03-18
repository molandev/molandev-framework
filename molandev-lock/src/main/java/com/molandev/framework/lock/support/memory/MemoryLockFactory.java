package com.molandev.framework.lock.support.memory;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于内存的锁工厂实现，建议仅做本地调试时无 Redis 时使用
 */
public class MemoryLockFactory implements LockFactory {

    /**
     * 用于保护 clean 操作的锁
     */
    private final ReentrantLock cleanLock = new ReentrantLock();

    /**
     * 锁 Map
     */
    private final Map<String, MemoryLock.ReentrantLockWithLease> lockMap = new ConcurrentHashMap<>();

    /**
     * 最后一次清理时间
     */
    volatile long lastClean = System.currentTimeMillis();

    @Override
    public Lock getLock(LockInfo lockInfo) {
        clean();
        MemoryLock.ReentrantLockWithLease reentrantLock = getReentrantLock(lockInfo);
        return new MemoryLock(reentrantLock, lockInfo);
    }

    /**
     * 获取可重入锁
     *
     * @param lockInfo 锁信息
     * @return 带租约的可重入锁
     */
    private MemoryLock.ReentrantLockWithLease getReentrantLock(LockInfo lockInfo) {
        // 在获取锁之前检查租约是否过期
        lockMap.computeIfPresent(lockInfo.getKey(), (key, lock) -> {
            if (lock.isExpired()) {
                return null; // 移除过期的锁
            }
            return lock;
        });

        return lockMap.computeIfAbsent(lockInfo.getKey(), key -> new MemoryLock.ReentrantLockWithLease());
    }

    /**
     * 清理过期的锁
     */
    private void clean() {
        // 每隔2小时执行一次清理
        if (System.currentTimeMillis() - lastClean > 2 * 60 * 60 * 1000) {
            if (cleanLock.tryLock()) {
                try {
                    if (System.currentTimeMillis() - lastClean > 2 * 60 * 60 * 1000) {
                        Set<String> keysToRemove = new HashSet<>();
                        for (Map.Entry<String, MemoryLock.ReentrantLockWithLease> entry : lockMap.entrySet()) {
                            // 清理过期的锁和未被持有的锁
                            if (entry.getValue().isExpired() || !entry.getValue().isLocked()) {
                                keysToRemove.add(entry.getKey());
                            }
                        }
                        keysToRemove.forEach(lockMap::remove);
                        lastClean = System.currentTimeMillis();
                    }
                } finally {
                    cleanLock.unlock();
                }
            }
        }
    }
}
