package com.molandev.framework.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * 双重判定锁的工具
 * 对应与类似这种初始化逻辑
 * <p>
 * private volatile A a;
 * if(a == null) {
 * synchronized(this.getClass) {
 * if(a == null){
 * a = 1;
 * }
 * }
 * }
 * <p>
 * 使用ReentrantLock以兼容jdk21的线程固定问题，虽然jdk25不再有线程固定问题，但为了兼容jdk21，还是使用ReentrantLock
 * <p>
 * 调用方式示例：
 * DoubleCheckUtils.init(() -> a, () -> a = new A());
 */
public class DoubleCheckUtils {
    // 全局锁缓存：保证同一初始化逻辑共用唯一锁
    private static final Map<Object, ReentrantLock> LOCK_CACHE = new ConcurrentHashMap<>();

    private DoubleCheckUtils() {
    }

    /**
     * 核心方法：线程安全的双重判定锁初始化
     * DoubleCheckUtils.doubleCheck(() -> a== null, () -> a = 1;)
     * @param predicate   判断逻辑，如 () -> a==null , () -> map.get(key)==null
     * @param initializer 初始化+赋值逻辑（创建对象并自行赋值给外部volatile变量）
     */
    public static void doubleCheck(BooleanSupplier predicate, Runnable initializer) {
        // 第一次判定：读取外部对象，避免不必要加锁
        if (predicate.getAsBoolean()) {
            // 自动创建/获取锁：基于初始化逻辑的Class保证唯一性
            ReentrantLock lock = LOCK_CACHE.computeIfAbsent(
                    initializer.getClass(),
                    k -> new ReentrantLock()
            );

            lock.lock();
            try {
                // 第二次判定：加锁后再次读取，防止并发重复初始化
                boolean asBoolean = predicate.getAsBoolean();
                if (asBoolean) {
                    // 执行初始化逻辑（内部已完成对外部变量的赋值）
                    initializer.run();
                }
            } finally {
                // 确保锁释放，避免死锁
                lock.unlock();
            }
        }
    }

    /**
     * 重载方法：支持自定义锁key（多实例/多场景）
     */
    public static void doubleCheck(Object lockKey, BooleanSupplier booleanSupplier, Runnable initializer) {
        if (booleanSupplier.getAsBoolean()) {
            ReentrantLock lock = LOCK_CACHE.computeIfAbsent(lockKey, k -> new ReentrantLock());
            lock.lock();
            try {
                if (booleanSupplier.getAsBoolean()) {
                    initializer.run();
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
