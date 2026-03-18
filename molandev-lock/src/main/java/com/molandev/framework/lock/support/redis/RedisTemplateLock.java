package com.molandev.framework.lock.support.redis;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 RedisTemplate 的锁实现（支持可重入）
 */
public class RedisTemplateLock implements Lock {

    /**
     * 加锁 Lua 脚本
     * 尝试设置锁，返回 1 表示成功，0 表示失败
     */
    private static final String LOCK_SCRIPT =
            "local result = redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) " +
                    "if result then " +
                    "    return 1 " +
                    "else " +
                    "    return 0 " +
                    "end";
    /**
     * 解锁 Lua 脚本
     * 只有锁的持有者才能解锁
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";
    private final RedisTemplate<String, Object> redisTemplate;
    private final LockInfo lockInfo;
    private final String lockValue;
    /**
     * 本地重入计数器，避免重复与Redis交互
     */
    private final ThreadLocal<Integer> reentrantCount = new ThreadLocal<>();

    public RedisTemplateLock(RedisTemplate<String, Object> redisTemplate,
                             LockInfo lockInfo) {
        this.redisTemplate = redisTemplate;
        this.lockInfo = lockInfo;
        // 使用 UUID + 线程ID 作为锁的值，确保锁的唯一性
        this.lockValue = UUID.randomUUID().toString() + "_" + Thread.currentThread().getId();
    }

    @Override
    public boolean acquire() {
        // 检查本地重入计数
        Integer count = reentrantCount.get();
        if (count != null && count > 0) {
            // 已经持有锁，直接增加计数，无需与Redis交互
            reentrantCount.set(count + 1);
            return true;
        }

        // 第一次获取锁，需要与Redis交互，支持等待超时
        long waitTime = lockInfo.getWaitTime();
        long startTime = System.currentTimeMillis();
        long waitMillis = TimeUnit.SECONDS.toMillis(waitTime);

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        // 在超时时间内循环尝试获取锁
        while (true) {
            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockInfo.getKey()),
                    lockValue,
                    String.valueOf(TimeUnit.SECONDS.toMillis(lockInfo.getLeaseTime()))
            );

            boolean acquired = result != null && result.equals(1L);
            if (acquired) {
                // 获取锁成功，初始化计数器为1
                reentrantCount.set(1);
                return true;
            }

            // 检查是否超时
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= waitMillis) {
                // 超时，获取锁失败
                return false;
            }

            // 短暂休眠后重试，避免CPU空转
            try {
                // 计算剩余等待时间和重试间隔，取较小值
                long remainingTime = waitMillis - elapsedTime;
                long sleepTime = Math.min(100, remainingTime); // 最多休眠100ms
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    @Override
    public boolean release() {
        // 检查本地重入计数
        Integer count = reentrantCount.get();
        if (count == null || count <= 0) {
            // 没有持有锁
            return false;
        }

        // 计数器减1
        count--;
        if (count > 0) {
            // 还有重入次数，只更新计数器，不释放Redis锁
            reentrantCount.set(count);
            return true;
        }

        // 计数器为0，需要释放Redis锁
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(lockInfo.getKey()),
                lockValue
        );

        boolean released = result != null && result.equals(1L);
        if (released) {
            // 释放成功，清除计数器
            reentrantCount.remove();
        }
        return released;
    }
}
