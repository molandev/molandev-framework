package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 双重判定锁工具类测试
 */
@DisplayName("双重判定锁工具类测试")
class DoubleCheckUtilsTest {

    @Nested
    @DisplayName("单线程测试")
    class SingleThreadTest {

        @Test
        @DisplayName("测试基本初始化功能")
        void testBasicInitialization() {
            AtomicReference<String> value = new AtomicReference<>();
            
            // 首次调用，条件为真，应该执行初始化
            DoubleCheckUtils.doubleCheck(
                () -> value.get() == null,
                () -> value.set("initialized")
            );
            
            assertEquals("initialized", value.get(), "值应该被初始化");
            
            // 再次调用，条件为假，不应该执行初始化
            DoubleCheckUtils.doubleCheck(
                () -> value.get() == null,
                () -> value.set("changed")
            );
            
            assertEquals("initialized", value.get(), "值不应该被改变");
        }

        @Test
        @DisplayName("测试条件判断功能")
        void testConditionEvaluation() {
            AtomicInteger counter = new AtomicInteger(0);
            AtomicReference<String> value = new AtomicReference<>();
            
            // 条件始终为真，每次都会尝试初始化
            DoubleCheckUtils.doubleCheck(
                () -> true,
                () -> {
                    value.set("value" + counter.incrementAndGet());
                }
            );
            
            assertEquals("value1", value.get(), "第一次调用应该设置值");
            
            // 条件始终为假，不会执行初始化
            DoubleCheckUtils.doubleCheck(
                () -> false,
                () -> {
                    value.set("should_not_happen");
                }
            );
            
            assertEquals("value1", value.get(), "第二次调用不应该改变值");
        }
    }

    @Nested
    @DisplayName("多线程测试")
    class MultiThreadTest {

        @Test
        @DisplayName("测试线程安全性 - 单例模式模拟")
        void testThreadSafetySingleton() throws InterruptedException {
            AtomicReference<String> singleton = new AtomicReference<>();
            AtomicInteger initializationCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(10);
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // 启动多个线程同时尝试初始化
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    try {
                        // 使用双重检查确保只初始化一次
                        DoubleCheckUtils.doubleCheck(
                            () -> singleton.get() == null,
                            () -> {
                                singleton.set(Thread.currentThread().getName() + "_initialized");
                                initializationCount.incrementAndGet();
                            }
                        );
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            assertTrue(latch.await(5, TimeUnit.SECONDS), "所有线程应该在5秒内完成");
            executor.shutdown();

            // 验证只初始化了一次
            assertEquals(1, initializationCount.get(), "应该只初始化一次");
            assertNotNull(singleton.get(), "单例对象应该被初始化");
        }

        @Test
        @DisplayName("测试高并发场景下的线程安全性")
        void testHighConcurrency() throws InterruptedException {
            AtomicReference<Integer> sharedResource = new AtomicReference<>();
            AtomicInteger initializationCounter = new AtomicInteger(0);
            int threadCount = 50;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(20);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        // 模拟资源初始化
                        DoubleCheckUtils.doubleCheck(
                            () -> sharedResource.get() == null,
                            () -> {
                                sharedResource.set(initializationCounter.incrementAndGet());
                            }
                        );
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS), "所有线程应该在5秒内完成");
            executor.shutdown();

            // 验证只初始化了一次
            assertEquals(1, initializationCounter.get(), "在高并发下也应该只初始化一次");
            assertEquals(Integer.valueOf(1), sharedResource.get(), "资源应该被正确初始化");
        }

        @Test
        @DisplayName("测试使用自定义锁键的双检查")
        void testDoubleCheckWithCustomLockKey() throws InterruptedException {
            AtomicReference<String> resource1 = new AtomicReference<>();
            AtomicReference<String> resource2 = new AtomicReference<>();
            AtomicInteger initCount1 = new AtomicInteger(0);
            AtomicInteger initCount2 = new AtomicInteger(0);
            
            String lockKey1 = "lock1";
            String lockKey2 = "lock2";
            
            CountDownLatch latch = new CountDownLatch(10);
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // 同时对两个不同资源进行初始化
            for (int i = 0; i < 10; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        if (index % 2 == 0) {
                            // 初始化资源1
                            DoubleCheckUtils.doubleCheck(
                                lockKey1,
                                () -> resource1.get() == null,
                                () -> {
                                    resource1.set("resource1_initialized_by_" + Thread.currentThread().getName());
                                    initCount1.incrementAndGet();
                                }
                            );
                        } else {
                            // 初始化资源2
                            DoubleCheckUtils.doubleCheck(
                                lockKey2,
                                () -> resource2.get() == null,
                                () -> {
                                    resource2.set("resource2_initialized_by_" + Thread.currentThread().getName());
                                    initCount2.incrementAndGet();
                                }
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(5, TimeUnit.SECONDS), "所有线程应该在5秒内完成");
            executor.shutdown();

            // 验证每个资源都只被初始化一次
            assertEquals(1, initCount1.get(), "资源1应该只初始化一次");
            assertEquals(1, initCount2.get(), "资源2应该只初始化一次");
            assertNotNull(resource1.get(), "资源1应该被初始化");
            assertNotNull(resource2.get(), "资源2应该被初始化");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("测试快速连续调用")
        void testRapidSequentialCalls() {
            AtomicReference<String> value = new AtomicReference<>();
            AtomicInteger initCount = new AtomicInteger(0);

            // 快速连续调用，验证只初始化一次
            for (int i = 0; i < 100; i++) {
                DoubleCheckUtils.doubleCheck(
                    () -> value.get() == null,
                    () -> {
                        value.set("initialized");
                        initCount.incrementAndGet();
                    }
                );
            }

            assertEquals(1, initCount.get(), "即使快速连续调用，也只应初始化一次");
            assertEquals("initialized", value.get(), "值应该被正确设置");
        }

        @Test
        @DisplayName("测试异常处理")
        void testExceptionHandling() {
            AtomicReference<String> value = new AtomicReference<>();
            AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

            // 在初始化过程中抛出异常，但仍应保持线程安全
            try {
                DoubleCheckUtils.doubleCheck(
                    () -> value.get() == null,
                    () -> {
                        value.set("about_to_fail");
                        throw new RuntimeException("初始化过程中模拟异常");
                    }
                );
            } catch (RuntimeException e) {
                exceptionOccurred.set(true);
                assertEquals("初始化过程中模拟异常", e.getMessage(), "异常消息应该匹配");
            }

            assertTrue(exceptionOccurred.get(), "应该捕获到异常");
            assertEquals("about_to_fail", value.get(), "即使发生异常，之前的赋值也应该生效");

            // 尝试再次初始化，由于条件已变为false，不应该再执行初始化
            AtomicInteger secondInitCount = new AtomicInteger(0);
            try {
                DoubleCheckUtils.doubleCheck(
                    () -> value.get() == null,
                        secondInitCount::incrementAndGet
                );
            } catch (Exception e) {
                // 忽略异常，因为我们知道上面的值已经设置了，条件为false
            }

            assertEquals(0, secondInitCount.get(), "第二次调用不应执行初始化，因为条件为false");
        }

        @Test
        @DisplayName("测试复杂条件判断")
        void testComplexCondition() {
            AtomicReference<String> value = new AtomicReference<>();
            AtomicInteger initCount = new AtomicInteger(0);
            String targetValue = "target";

            // 复杂的条件判断
            DoubleCheckUtils.doubleCheck(
                () -> value.get() == null || !value.get().equals(targetValue),
                () -> {
                    value.set(targetValue);
                    initCount.incrementAndGet();
                }
            );

            assertEquals(targetValue, value.get(), "值应该被设置为目标值");
            assertEquals(1, initCount.get(), "应该只初始化一次");

            // 再次调用，条件为false（因为值已经是目标值），不应该再次初始化
            DoubleCheckUtils.doubleCheck(
                () -> value.get() == null || !value.get().equals(targetValue),
                () -> {
                    value.set("another_value");
                    initCount.incrementAndGet();
                }
            );

            assertEquals(targetValue, value.get(), "值不应该被改变");
            assertEquals(1, initCount.get(), "不应该再次初始化");
        }
    }
}