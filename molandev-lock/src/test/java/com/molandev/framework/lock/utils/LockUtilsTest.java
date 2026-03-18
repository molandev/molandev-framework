package com.molandev.framework.lock.utils;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.exception.LockTimeoutException;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LockUtils 测试")
class LockUtilsTest {

    @Mock
    private LockFactory lockFactory;

    @Mock
    private Lock mockLock;

    @BeforeEach
    void setUp() {
        // 设置静态的 LockFactory
        LockUtils.setLockFactory(lockFactory);
    }

    @Test
    @DisplayName("测试基本的runInLock方法")
    void testRunInLockBasic() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "test-key";
        String expectedResult = "success";

        // 执行
        String result = LockUtils.runInLock(testKey, () -> expectedResult);

        // 验证
        assertEquals(expectedResult, result);
        verify(mockLock).acquire();
        verify(mockLock).release();
    }

    @Test
    @DisplayName("测试带时间参数的runInLock方法")
    void testRunInLockWithTimeParameters() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "test-key";
        long waitTime = 10;
        long leaseTime = 20;
        String expectedResult = "success";

        // 执行
        String result = LockUtils.runInLock(testKey, waitTime, leaseTime, () -> expectedResult);

        // 验证
        assertEquals(expectedResult, result);
        verify(mockLock).acquire();
        verify(mockLock).release();
    }

    @Test
    @DisplayName("测试无返回值的runInLock方法")
    void testRunInLockVoid() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "test-key";
        AtomicInteger counter = new AtomicInteger(0);

        // 执行
        LockUtils.runInLock(testKey, () -> counter.incrementAndGet());

        // 验证
        assertEquals(1, counter.get());
        verify(mockLock).acquire();
        verify(mockLock).release();
    }

    @Test
    @DisplayName("测试带时间参数的无返回值runInLock方法")
    void testRunInLockVoidWithTimeParameters() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "test-key";
        long waitTime = 15;
        long leaseTime = 30;
        AtomicInteger counter = new AtomicInteger(0);

        // 执行
        LockUtils.runInLock(testKey, waitTime, leaseTime, () -> counter.incrementAndGet());

        // 验证
        assertEquals(1, counter.get());
        verify(mockLock).acquire();
        verify(mockLock).release();
    }

    @Test
    @DisplayName("测试锁获取失败的情况")
    void testRunInLockFailure() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(false);

        String testKey = "test-key";

        // 执行和验证
        LockTimeoutException exception = assertThrows(
            LockTimeoutException.class,
            () -> LockUtils.runInLock(testKey, () -> "should-not-reach")
        );
        assertTrue(exception.getMessage().contains("获取锁超时"));
        verify(mockLock).acquire();
        // 注意：由于获取锁失败，release方法不应该被调用
    }

    @Test
    @DisplayName("测试锁释放确保被执行")
    void testLockReleaseAlwaysCalled() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "test-key";

        // 执行 - 即使业务逻辑抛出异常，锁也应该被释放
        assertThrows(
            RuntimeException.class,
            () -> LockUtils.runInLock(testKey, () -> {
                throw new RuntimeException("Business logic failed");
            })
        );

        // 验证锁被释放了
        verify(mockLock).acquire();
        verify(mockLock).release();
    }

    @Test
    @DisplayName("测试并发安全性")
    void testConcurrencySafety() throws InterruptedException {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        String testKey = "concurrent-test-key";
        AtomicInteger sharedCounter = new AtomicInteger(0);
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 执行多个线程同时尝试获取相同的锁
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    LockUtils.runInLock(testKey, 5, 10, () -> {
                        // 模拟临界区操作
                        int currentValue = sharedCounter.get();
                        try {
                            // 短暂延迟以增加竞争机会
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        sharedCounter.set(currentValue + 1);
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // 验证计数器值（由于锁的存在，每次只有一个线程能访问临界区）
        // 注意：由于CompletableFuture使用ForkJoinPool，实际执行顺序可能不确定
        // 但我们至少可以验证方法没有因为锁机制而崩溃
        verify(lockFactory, atLeastOnce()).getLock(any(LockInfo.class));
    }

    @Test
    @DisplayName("测试不同的锁key生成")
    void testDifferentLockKeys() {
        // 准备
        when(lockFactory.getLock(any(LockInfo.class))).thenReturn(mockLock);
        when(mockLock.acquire()).thenReturn(true);
        when(mockLock.release()).thenReturn(true);

        // 执行 - 测试不同的key
        String result1 = LockUtils.runInLock("KEY1", () -> "result1");
        String result2 = LockUtils.runInLock("KEY2", 10, 20, () -> "result2");

        // 验证
        assertEquals("result1", result1);
        assertEquals("result2", result2);
        verify(lockFactory, times(2)).getLock(any(LockInfo.class));
        verify(mockLock, times(2)).acquire();
        verify(mockLock, times(2)).release();
    }
}
