package com.molandev.framework.lock.locktest.service;

import com.molandev.framework.lock.annotation.GlobalLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TestLockService {

    private int counter = 0;

    @Autowired
    @Lazy
    private TestLockService self;

    @GlobalLock(key = "#key", waitTime = 3, leaseTime = 10)
    public int incrementCounter(String key) {
        int newCount = counter;
        // 模拟一些处理时间
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return counter = newCount + 1;
    }

    @GlobalLock(key = "#key", waitTime = 3, leaseTime = 10)
    public int incrementCounterWithReentrant(String key) {
        // 可重入调用 - 通过self引用调用，确保经过代理
        return self.incrementCounterInternal(key) + (++counter);
    }

    @GlobalLock(key = "#key", waitTime = 3, leaseTime = 10)
    public int incrementCounterInternal(String key) {
        return ++counter;
    }

    @GlobalLock(key = "#key", waitTime = 1, leaseTime = 10)
    public int incrementCounterWithTimeout(String key) {
        // 模拟长时间运行的任务
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ++counter;
    }

    // 添加用于测试leaseTime功能的方法
    @GlobalLock(key = "#key", waitTime = 1, leaseTime = 3)
    public int incrementCounterWithShortLeaseTime(String key) {
        // 模拟一个超过leaseTime的长时间任务
        try {
            TimeUnit.SECONDS.sleep(6); // 休眠6秒，超过leaseTime=3秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ++counter;
    }

    @GlobalLock(key = "#key", waitTime = 3, leaseTime = 10, timeoutFallback = "handleLockTimeout")
    public int incrementWithFallback(String key) {
        // 模拟长时间运行的任务
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ++counter;
    }

    public int handleLockTimeout(String key) {
        // 锁超时的降级处理逻辑
        return -1;
    }

    // 添加用于测试不同方法间互斥的新方法
    @GlobalLock(key = "#key", waitTime = 3, leaseTime = 10)
    public int anotherMethodWithSameKey(String key) {
        int newCount = counter;
        // 模拟一些处理时间
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return counter = newCount + 1;
    }

    public int getCounterValue() {
        return counter;
    }

    public void resetCounter() {
        counter = 0;
    }
}