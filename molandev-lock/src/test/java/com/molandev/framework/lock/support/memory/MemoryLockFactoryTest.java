package com.molandev.framework.lock.support.memory;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryLockFactory测试")
class MemoryLockFactoryTest {

    private MemoryLockFactory memoryLockFactory;

    @BeforeEach
    void setUp() {
        memoryLockFactory = new MemoryLockFactory();
    }

    @Nested
    @DisplayName("锁创建测试")
    class LockCreationTest {

        @Test
        @DisplayName("测试创建默认锁")
        void testCreateDefaultLock() {
            LockInfo lockInfo = new LockInfo("test-key", 10, 30);
            Lock lock = memoryLockFactory.getLock(lockInfo);
            assertNotNull(lock, "锁实例不应为null");
        }
    }

    @Nested
    @DisplayName("锁功能测试")
    class LockFunctionalityTest {

        @Test
        @DisplayName("测试锁获取和释放")
        void testLockAcquireAndRelease() {
            LockInfo lockInfo = new LockInfo("acquire-release-test", 5, 10);
            Lock lock = memoryLockFactory.getLock(lockInfo);

            // 获取锁
            boolean acquired = lock.acquire();
            assertTrue(acquired, "应能成功获取锁");

            // 释放锁
            boolean released = lock.release();
            assertTrue(released, "应能成功释放锁");
        }

        @Test
        @DisplayName("测试锁可重入性")
        void testLockReentrant() {
            LockInfo lockInfo = new LockInfo("reentrant-test", 5, 10);
            Lock lock = memoryLockFactory.getLock(lockInfo);

            // 第一次获取锁
            boolean acquired1 = lock.acquire();
            assertTrue(acquired1, "第一次应能成功获取锁");

            // 第二次获取锁（可重入）
            boolean acquired2 = lock.acquire();
            assertTrue(acquired2, "第二次应能成功获取锁（可重入）");

            // 第三次获取锁（可重入）
            boolean acquired3 = lock.acquire();
            assertTrue(acquired3, "第三次应能成功获取锁（可重入）");

            // 释放锁三次
            boolean released1 = lock.release();
            assertTrue(released1, "第一次应能成功释放锁");

            boolean released2 = lock.release();
            assertTrue(released2, "第二次应能成功释放锁");

            boolean released3 = lock.release();
            assertTrue(released3, "第三次应能成功释放锁");
        }

        @Test
        @DisplayName("测试释放未持有的锁")
        void testReleaseUnheldLock() {
            LockInfo lockInfo = new LockInfo("unheld-lock-test", 1, 10);
            Lock lock = memoryLockFactory.getLock(lockInfo);

            // 尝试释放未持有的锁
            boolean released = lock.release();
            assertFalse(released, "应无法释放未持有的锁");
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTest {

        @Test
        @DisplayName("测试多个线程竞争同一把锁")
        void testMultipleThreadsCompetingForLock() throws InterruptedException {
            final int threadCount = 10;
            final int iterations = 100;
            final AtomicInteger sharedCounter = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            LockInfo lockInfo = new LockInfo("concurrent-lock-test", 5, 10);

            // 创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

            // 创建多个线程竞争锁
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        for (int j = 0; j < iterations; j++) {
                            Lock lock = memoryLockFactory.getLock(lockInfo);
                            if (lock.acquire()) {
                                try {
                                    // 模拟临界区操作
                                    int currentValue = sharedCounter.get();
                                    // 模拟一些处理时间
                                    TimeUnit.MILLISECONDS.sleep(1);
                                    sharedCounter.set(currentValue + 1);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    lock.release();
                                }
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 关闭线程池
            executorService.shutdown();

            // 等待所有线程完成
            boolean finished = latch.await(30, TimeUnit.SECONDS);
            assertTrue(finished, "所有线程应在规定时间内完成");

            // 验证结果
            assertEquals(threadCount * iterations, sharedCounter.get(),
                    "共享计数器的值应该等于线程数乘以迭代次数");
        }

        @Test
        @DisplayName("测试锁超时")
        void testLockTimeout() throws InterruptedException {
            LockInfo lockInfo = new LockInfo("timeout-test", 1, 10); // 等待时间1秒

            // 线程1获取锁
            Lock lock1 = memoryLockFactory.getLock(lockInfo);
            boolean acquired1 = lock1.acquire();
            assertTrue(acquired1, "线程1应能成功获取锁");

            // 启动线程2尝试获取锁
            CountDownLatch thread2Finished = new CountDownLatch(1);
            AtomicBoolean thread2Acquired = new AtomicBoolean(false);

            Thread thread2 = new Thread(() -> {
                try {
                    Lock lock2 = memoryLockFactory.getLock(lockInfo);
                    thread2Acquired.set(lock2.acquire());
                } finally {
                    thread2Finished.countDown();
                }
            });

            thread2.start();

            // 等待线程2完成（应该超时失败）
            boolean finished = thread2Finished.await(2, TimeUnit.SECONDS);
            assertTrue(finished, "线程2应在超时时间内完成");
            assertFalse(thread2Acquired.get(), "线程2应无法获取已被占用的锁");

            // 释放线程1的锁
            boolean released1 = lock1.release();
            assertTrue(released1, "线程1应能成功释放锁");
        }
    }

    @Nested
    @DisplayName("锁清理测试")
    class LockCleanTest {

        @Test
        @DisplayName("测试锁清理功能")
        void testLockClean() throws InterruptedException {
            // 修改lastClean时间，使清理条件满足
            memoryLockFactory.lastClean = System.currentTimeMillis() - 3 * 60 * 60 * 1000;

            LockInfo lockInfo = new LockInfo("clean-test", 5, 10);
            Lock lock = memoryLockFactory.getLock(lockInfo);

            // 获取并释放锁，使锁进入map中
            boolean acquired = lock.acquire();
            assertTrue(acquired, "应能成功获取锁");

            boolean released = lock.release();
            assertTrue(released, "应能成功释放锁");

            // 在新线程中再次获取相同的锁，确保清理机制不影响正常使用
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean acquiredInThread = new AtomicBoolean(false);

            Thread thread = new Thread(() -> {
                try {
                    Lock lock2 = memoryLockFactory.getLock(lockInfo);
                    acquiredInThread.set(lock2.acquire());
                    if (acquiredInThread.get()) {
                        lock2.release();
                    }
                } finally {
                    latch.countDown();
                }
            });

            thread.start();
            latch.await();

            assertTrue(acquiredInThread.get(), "在新线程中应能成功获取和释放锁");
        }
    }
}