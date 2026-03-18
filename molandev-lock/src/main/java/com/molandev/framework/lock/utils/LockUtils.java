package com.molandev.framework.lock.utils;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.exception.LockTimeoutException;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;
import org.springframework.core.NamedThreadLocal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * 锁工具类
 */
public class LockUtils {

    /**
     * 当前线程内的锁栈
     */
    private static final ThreadLocal<Deque<LockInfo>> CURRENT_LOCK = new NamedThreadLocal<Deque<LockInfo>>("current-lock") {
        @Override
        protected Deque<LockInfo> initialValue() {
            return new ArrayDeque<>();
        }
    };

    /**
     * 锁工厂（静态持有，通过配置类注入）
     */
    private static LockFactory lockFactory;

    private LockUtils() {
        // 工具类不允许实例化
    }

    /**
     * 设置锁工厂（由配置类调用）
     *
     * @param factory 锁工厂
     */
    public static void setLockFactory(LockFactory factory) {
        lockFactory = factory;
    }

    /**
     * 在一个分布式锁内执行操作，使用默认的等待时间和租约时间
     *
     * @param key      锁的key值，会在前面统一加上前缀
     * @param supplier 业务逻辑
     * @return 业务逻辑返回值
     */
    public static <T> T runInLock(String key, Supplier<T> supplier) {
        return runInLock(key, 30, 60, supplier);
    }

    /**
     * 在一个分布式锁内执行操作
     *
     * @param key       锁的key值，会在前面统一加上前缀
     * @param waitTime  获取锁的超时时间，单位秒
     * @param leaseTime 上锁以后自动解锁的时间，单位秒
     * @param supplier  业务逻辑
     * @return 业务逻辑返回值
     */
    public static <T> T runInLock(String key, long waitTime, long leaseTime, Supplier<T> supplier) {
        LockInfo lockInfo = LockInfoUtils.createLockInfo(key, waitTime, leaseTime);
        return runInLock(lockInfo, supplier);
    }

    /**
     * 在一个分布式锁内执行操作
     *
     * @param lockInfo 锁信息
     * @param supplier 业务逻辑
     * @return 业务逻辑返回值
     */
    public static <T> T runInLock(LockInfo lockInfo, Supplier<T> supplier) {
        if (lockFactory == null) {
            throw new IllegalStateException("LockFactory not initialized. Please check your configuration.");
        }

        Lock lock = lockFactory.getLock(lockInfo);
        boolean lockRes = lock.acquire();

        if (!lockRes) {
            throw new LockTimeoutException("获取锁超时：" + lockInfo.getKey());
        }

        CURRENT_LOCK.get().push(lockInfo);
        try {
            return supplier.get();
        } finally {
            try {
                lock.release();
            } finally {
                CURRENT_LOCK.get().poll();
            }
        }
    }

    /**
     * 在一个分布式锁内执行操作，仅接受Runnable（无返回值的操作）
     *
     * @param key      锁的key值，会在前面统一加上前缀
     * @param runnable 业务逻辑
     */
    public static void runInLock(String key, Runnable runnable) {
        runInLock(key, 30, 60, runnable);
    }

    /**
     * 在一个分布式锁内执行操作，仅接受Runnable（无返回值的操作），允许用户自定义等待时间和租约时间
     *
     * @param key       锁的key值，会在前面统一加上前缀
     * @param waitTime  获取锁的超时时间，单位秒
     * @param leaseTime 上锁以后自动解锁的时间，单位秒
     * @param runnable  业务逻辑
     */
    public static void runInLock(String key, long waitTime, long leaseTime, Runnable runnable) {
        runInLock(key, waitTime, leaseTime, () -> {
            runnable.run();
            return null;
        });
    }

}
