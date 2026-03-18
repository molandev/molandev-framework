package com.molandev.framework.lock.locktest;

import com.molandev.framework.lock.config.EmbeddedRedisConfig;
import com.molandev.framework.lock.config.LockAutoConfiguration;
import com.molandev.framework.lock.locktest.service.TestLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EmbeddedRedisConfig.class,
        TestLockService.class,
        LockAutoConfiguration.class
})
@DisplayName("MolanLock注解测试基类")
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "molandev.lock.type=redisson",
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public  class GlobalLockTest {

    @Autowired
    protected TestLockService testLockService;

    @BeforeEach
    void setUp() {
        testLockService.resetCounter();
    }

    @Nested
    @DisplayName("单线程可重入性测试")
    class ReentrancyTest {

        @Test
        @DisplayName("测试同一方法的可重入调用")
        void testReentrantCall() {
            int result = testLockService.incrementCounterWithReentrant("reentrant-key");
            assertEquals(3, result, "可重入调用应该成功执行");
            assertEquals(2, testLockService.getCounterValue(), "计数器应该增加2次");
        }

        @Test
        @DisplayName("测试多次获取同一把锁")
        void testMultipleAcquireSameLock() {
            int result1 = testLockService.incrementCounter("same-key");
            int result2 = testLockService.incrementCounter("same-key");
            assertEquals(1, result1, "第一次调用结果应为1");
            assertEquals(2, result2, "第二次调用结果应为2");
            assertEquals(2, testLockService.getCounterValue(), "计数器应该增加2次");
        }
    }

    @Nested
    @DisplayName("多线程并发测试")
    class ConcurrencyTest {

        /**
         * 多个线程竞争同一把锁，最终结果必然是10，但是每个线程的唤起并不一定保证顺序
         */
        @Test
        @DisplayName("测试多线程竞争同一把锁")
        void testMultipleThreadsCompetingForSameLock() throws InterruptedException, ExecutionException, TimeoutException {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Future<Integer>> futures = new ArrayList<>();

            // 启动多个线程同时访问同一把锁保护的资源
            for (int i = 0; i < threadCount; i++) {
                Future<Integer> future = executor.submit(() -> {
                    try {
                        return testLockService.incrementCounter("concurrent-key");
                    } finally {
                        latch.countDown();
                    }
                });
                futures.add(future);
            }

            // 等待所有线程执行完毕
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // 验证所有线程都成功执行
            for (int i = 0; i < futures.size(); i++) {
                try {
                    int result = futures.get(i).get(5, TimeUnit.SECONDS);
                    assertTrue(result >= 1 && result <= threadCount,
                            "每个线程都应该得到一个有效的结果，实际结果: " + result);
                } catch (ExecutionException e) {
                    fail("线程执行异常: " + e.getCause());
                }
            }

            // 验证最终计数器值
            assertEquals(threadCount, testLockService.getCounterValue(),
                    "计数器值应该等于线程数");
        }

        /**
         * 每个线程不同的key，所以每个线程都应获取到锁并执行，由于实际的方法先获取到counter=0，然后sleep，最终在加1
         * 所以，所有的线程结果都应该是1
         */
        @Test
        @DisplayName("测试不同线程使用不同锁key")
        void testDifferentThreadsDifferentKeys() throws InterruptedException, ExecutionException, TimeoutException {
            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Future<Integer>> futures = new ArrayList<>();

            // 启动多个线程，每个线程使用不同的锁key
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                Future<Integer> future = executor.submit(() -> {
                    try {
                        return testLockService.incrementCounter("different-key-" + index);
                    } finally {
                        latch.countDown();
                    }
                });
                futures.add(future);
            }

            // 等待所有线程执行完毕
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // 验证所有线程都成功执行
            for (int i = 0; i < futures.size(); i++) {
                try {
                    int result = futures.get(i).get(5, TimeUnit.SECONDS);
                    assertEquals(1, result, "每个线程都应该得到结果1");
                } catch (ExecutionException e) {
                    fail("线程执行异常: " + e.getCause());
                }
            }
        }
    }

    @Nested
    @DisplayName("跨方法互斥测试")
    class CrossMethodMutexTest {

        @Test
        @DisplayName("测试不同方法使用相同key时的互斥性")
        void testDifferentMethodsWithSameKey() throws InterruptedException, ExecutionException, TimeoutException {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch latch = new CountDownLatch(2);

            // 启动一个线程执行第一个方法
            Future<Integer> future1 = executor.submit(() -> {
                try {
                    return testLockService.incrementCounter("cross-method-key");
                } finally {
                    latch.countDown();
                }
            });

            // 确保第一个线程先获取到锁
            Thread.sleep(50);

            // 启动另一个线程执行第二个方法，使用相同的key
            Future<Integer> future2 = executor.submit(() -> {
                try {
                    return testLockService.anotherMethodWithSameKey("cross-method-key");
                } finally {
                    latch.countDown();
                }
            });

            // 等待所有线程执行完毕
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // 获取两个线程的执行结果
            int result1 = future1.get(5, TimeUnit.SECONDS);
            int result2 = future2.get(5, TimeUnit.SECONDS);

            // 验证两个方法都成功执行
            assertTrue(result1 >= 1, "第一个方法应该成功执行");
            assertTrue(result2 >= 1, "第二个方法应该成功执行");

            // 验证计数器值为2，说明两个方法都成功执行且互不干扰
            assertEquals(2, testLockService.getCounterValue(),
                    "计数器值应该为2，表示两个方法都成功执行");
        }
    }

    @Nested
    @DisplayName("异常情况测试")
    class ExceptionTest {

        @Test
        @DisplayName("测试锁超时情况")
        void testLockTimeout() throws InterruptedException, ExecutionException, TimeoutException {
            // 先获取锁并保持一段时间
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);

            // 启动一个线程先获取锁
            Future<Integer> future = executor.submit(() -> {
                try {
                    // 此方法内部执行时间5秒
                    return testLockService.incrementCounterWithTimeout("timeout-key");
                } finally {
                    latch.countDown();
                }
            });

            // 等待一段时间确保锁已被获取
            Thread.sleep(100);

            // 尝试获取同一把锁，应该超时
            assertThrows(RuntimeException.class, () -> {
                // 此方法timeout3秒
                testLockService.incrementCounter("timeout-key");
            }, "获取已被占用且超时的锁应该抛出异常");

            // 等待第一个线程执行完毕
            latch.await(10, TimeUnit.SECONDS);
            future.get(5, TimeUnit.SECONDS);

            executor.shutdown();
        }

        @Test
        @DisplayName("测试锁超时降级处理")
        void testLockTimeoutFallback() throws InterruptedException, ExecutionException, TimeoutException {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);

            // 启动一个线程先获取锁
            Future<Integer> future = executor.submit(() -> {
                try {
                    return testLockService.incrementWithFallback("fallback-key");
                } finally {
                    latch.countDown();
                }
            });

            // 等待一段时间确保锁已被获取
            Thread.sleep(100);

            // 尝试获取同一把锁，应该触发降级处理
            int result = testLockService.incrementWithFallback("fallback-key");
            assertEquals(-1, result, "锁超时应该触发降级处理，返回-1");

            // 等待第一个线程执行完毕
            latch.await(10, TimeUnit.SECONDS);
            int firstResult = future.get(5, TimeUnit.SECONDS);
            assertTrue(firstResult > 0, "第一个线程应该正常执行，结果应大于0");

            executor.shutdown();
        }

        @Test
        @DisplayName("测试leaseTime自动释放锁功能")
        void testLeaseTimeAutoRelease() throws InterruptedException, ExecutionException, TimeoutException {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);

            // 启动一个线程先获取锁，但该线程执行时间将超过leaseTime
            Future<Integer> future = executor.submit(() -> {
                try {
                    // 此方法内部执行时间5秒，但leaseTime设置为3秒
                    return testLockService.incrementCounterWithShortLeaseTime("lease-time-key");
                } finally {
                    latch.countDown();
                }
            });

            // 等待一段时间确保锁已被获取
            Thread.sleep(100);

            // 等待超过leaseTime的时间（3秒）
            Thread.sleep(3500);

            // 尝试获取同一把锁，应该能够成功获取因为锁已自动释放
            int result = testLockService.incrementCounter("lease-time-key");
            assertEquals(1, result, "锁应该已经被自动释放，所以可以成功获取并执行");

            // 等待第一个线程执行完毕
            latch.await(10, TimeUnit.SECONDS);
            int firstResult = future.get(5, TimeUnit.SECONDS);
            assertEquals(2, firstResult, "第一个线程应该正常执行，结果应为1");

            executor.shutdown();
        }
    }
}