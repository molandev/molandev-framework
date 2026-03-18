package com.molandev.framework.lock.utils;

import com.molandev.framework.lock.annotation.GlobalLock;
import com.molandev.framework.lock.support.model.LockInfo;
import org.aspectj.lang.JoinPoint;

/**
 * 锁信息工具类
 */
public class LockInfoUtils {

    /**
     * 锁名称前缀
     */
    public static final String LOCK_NAME_PREFIX = "LOCK:";

    private LockInfoUtils() {
        // 工具类不允许实例化
    }

    /**
     * 从注解和切点获取锁信息
     *
     * @param joinPoint 连接点
     * @param globalLock     锁注解
     * @return 锁信息
     */
    public static LockInfo getLockInfo(JoinPoint joinPoint, GlobalLock globalLock) {
        String businessKeyName = LockKeyUtils.resolveKey(joinPoint, globalLock.key());
        // 锁的key，根据注解中的表达式获取，锁的粒度就是这里控制的
        String lockKey = LOCK_NAME_PREFIX + businessKeyName;
        long waitTime = globalLock.waitTime();
        long leaseTime = globalLock.leaseTime();

        // 占用锁的时间设计不合理
        if (leaseTime < waitTime) {
            throw new IllegalArgumentException("Lock leaseTime must be greater than or equal to waitTime");
        }

        return new LockInfo(lockKey, waitTime, leaseTime);
    }

    /**
     * 创建锁信息
     *
     * @param key       业务key
     * @param waitTime  等待时间（秒）
     * @param leaseTime 租约时间（秒）
     * @return 锁信息
     */
    public static LockInfo createLockInfo(String key, long waitTime, long leaseTime) {
        String lockKey = LOCK_NAME_PREFIX + key;
        return new LockInfo(lockKey, waitTime, leaseTime);
    }

}
