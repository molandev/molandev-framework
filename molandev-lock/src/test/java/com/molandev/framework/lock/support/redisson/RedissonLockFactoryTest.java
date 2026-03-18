package com.molandev.framework.lock.support.redisson;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;
import org.junit.jupiter.api.*;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
//@TestPropertySource(properties = {
//    "spring.redis.host=localhost",
//    "spring.redis.port=6380",
//    "spring.redis.password=",
//    "spring.redis.database=0"
//})
@DisplayName("RedissonLockFactory测试")
class RedissonLockFactoryTest {

    private static RedisServer redisServer;
    private RedissonClient redissonClient;
    private RedissonLockFactory redissonLockFactory;

    @BeforeAll
    static void setUpRedis() throws IOException {
        // 启动嵌入式Redis服务器
        redisServer = RedisServer.builder()
                .port(6380)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
    }

    @AfterAll
    static void tearDownRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        // 创建Redisson客户端
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6380")
                .setDatabase(0);
        redissonClient = Redisson.create(config);

        // 创建RedissonLockFactory实例
        redissonLockFactory = new RedissonLockFactory();

        // 使用反射设置redissonClient字段
        try {
            java.lang.reflect.Field field = RedissonLockFactory.class.getDeclaredField("redissonClient");
            field.setAccessible(true);
            field.set(redissonLockFactory, redissonClient);
        } catch (Exception e) {
            fail("无法设置RedissonLockFactory的redissonClient字段: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (redissonClient != null) {
            redissonClient.shutdown();
        }
    }

    @Nested
    @DisplayName("锁创建测试")
    class LockCreationTest {

        @Test
        @DisplayName("测试创建默认锁")
        void testCreateDefaultLock() {
            LockInfo lockInfo = new LockInfo("test-key", 10, 30);
            Lock lock = redissonLockFactory.getLock(lockInfo);
            assertNotNull(lock, "锁实例不应为null");
        }
    }

    @Nested
    @DisplayName("默认锁功能测试")
    class DefaultLockFunctionalityTest {

        @Test
        @DisplayName("测试默认锁获取和释放")
        void testDefaultLockAcquireAndRelease() {
            LockInfo lockInfo = new LockInfo("default-lock-test", 5, 10);
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 获取锁
            boolean acquired = lock.acquire();
            assertTrue(acquired, "应能成功获取锁");

            // 释放锁
            boolean released = lock.release();
            assertTrue(released, "应能成功释放锁");
        }

        @Test
        @DisplayName("测试默认锁可重入性")
        void testDefaultLockReentrant() {
            LockInfo lockInfo = new LockInfo("reentrant-lock-test", 5, 10);
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 第一次获取锁
            boolean acquired1 = lock.acquire();
            assertTrue(acquired1, "第一次应能成功获取锁");

            // 第二次获取锁（可重入）
            boolean acquired2 = lock.acquire();
            assertTrue(acquired2, "第二次应能成功获取锁（可重入）");

            // 释放锁两次
            boolean released1 = lock.release();
            assertTrue(released1, "第一次应能成功释放锁");

            boolean released2 = lock.release();
            assertTrue(released2, "第二次应能成功释放锁");
        }

        @Test
        @DisplayName("测试默认锁互斥性")
        void testDefaultLockMutex() throws InterruptedException {
            LockInfo lockInfo = new LockInfo("mutex-lock-test", 5, 10);
            Lock lock1 = redissonLockFactory.getLock(lockInfo);

            // lock1获取锁
            boolean acquired1 = lock1.acquire();
            assertTrue(acquired1, "lock1应能成功获取锁");

            // 在另一个线程中尝试获取锁，应该失败（因为lock1已持有）
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] acquired2 = {false};
            Thread thread = new Thread(() -> {
                Lock lock2 = redissonLockFactory.getLock(lockInfo);
                acquired2[0] = lock2.acquire();
                latch.countDown();
            });
            thread.start();
            latch.await();

            assertFalse(acquired2[0], "lock2应无法获取已被占用的锁");

            // 释放lock1的锁
            boolean released1 = lock1.release();
            assertTrue(released1, "lock1应能成功释放锁");

            // 现在在另一个线程中获取锁
            CountDownLatch latch2 = new CountDownLatch(1);
            final boolean[] acquired3 = {false};
            final boolean[] released2 = {false};
            final Lock[] lock3Holder = {null};
            Thread thread2 = new Thread(() -> {
                Lock lock3 = redissonLockFactory.getLock(lockInfo);
                lock3Holder[0] = lock3;
                acquired3[0] = lock3.acquire();
                released2[0] = lock3.release();
                latch2.countDown();
            });
            thread2.start();
            latch2.await();

            assertTrue(acquired3[0], "lock3现在应能成功获取锁");

            // 在同一线程中释放锁
//            released2[0] = lock3Holder[0].release();
            assertTrue(released2[0], "lock3应能成功释放锁");
        }

        @Test
        @DisplayName("测试跨线程释放锁")
        void testCrossThreadRelease() throws InterruptedException {
            LockInfo lockInfo = new LockInfo("cross-thread-release-test", 5, 10);
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 在一个线程中获取锁
            CountDownLatch acquireLatch = new CountDownLatch(1);
            final boolean[] acquired = {false};
            acquired[0] = lock.acquire();
            acquireLatch.countDown();
            assertTrue(acquired[0], "应在第一个线程中成功获取锁");

            // 在另一个线程中尝试释放锁，应该失败
            CountDownLatch releaseLatch = new CountDownLatch(1);
            final boolean[] released = {false};
            Thread releaseThread = new Thread(() -> {
                released[0] = lock.release();
                releaseLatch.countDown();
            });
            releaseThread.start();
            releaseLatch.await();
            assertFalse(released[0], "在另一个线程中应无法释放不持有的锁");

            // 在原始线程中释放锁，应该成功
            CountDownLatch releaseLatch2 = new CountDownLatch(1);
            final boolean[] released2 = {false};
            released2[0] = lock.release();
            releaseLatch2.countDown();
            releaseLatch2.await();
            assertTrue(released2[0], "在原始线程中应能成功释放锁");
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

            // 创建多个线程竞争锁
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        for (int j = 0; j < iterations; j++) {
                            Lock lock = redissonLockFactory.getLock(lockInfo);
                            if (lock.acquire()) {
                                try {
                                    // 模拟临界区操作
                                    int currentValue = sharedCounter.get();
                                    // 模拟一些处理时间
                                    TimeUnit.MILLISECONDS.sleep(1);
                                    sharedCounter.set(currentValue + 1);
                                } finally {
                                    lock.release();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
                thread.start();
            }

            // 等待所有线程完成
            latch.await(30, TimeUnit.SECONDS);

            // 验证结果
            assertEquals(threadCount * iterations, sharedCounter.get(),
                    "共享计数器的值应该等于线程数乘以迭代次数");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionsTest {

        @Test
        @DisplayName("测试锁超时")
        void testLockTimeout() throws InterruptedException {
            LockInfo lockInfo = new LockInfo("timeout-test", 1, 10); // 等待时间1秒
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 获取锁
            boolean acquired1 = lock.acquire();
            assertTrue(acquired1, "应能成功获取锁");

            // 在另一个线程中尝试获取锁，应该超时失败
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] acquired2 = {false};
            Thread thread = new Thread(() -> {
                Lock lock2 = redissonLockFactory.getLock(lockInfo);
                acquired2[0] = lock2.acquire();
                latch.countDown();
            });
            thread.start();

            // 等待子线程执行完毕
            latch.await();

            // 验证另一个线程未能获取锁
            assertFalse(acquired2[0], "在锁被占用且超时时间内其他线程应无法获取锁");

            // 释放锁
            boolean released = lock.release();
            assertTrue(released, "应能成功释放锁");
        }

        @Test
        @DisplayName("测试锁的可重入性")
        void testLockReentrancy() {
            LockInfo lockInfo = new LockInfo("reentrant-test", 5, 10);
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 第一次获取锁
            boolean acquired1 = lock.acquire();
            assertTrue(acquired1, "应能成功获取锁");

            // 同一线程再次获取锁（可重入）
            boolean acquired2 = lock.acquire();
            assertTrue(acquired2, "同一线程应能再次获取锁（可重入）");

            // 释放锁 - 需要释放两次才能真正释放
            boolean released1 = lock.release();
            assertTrue(released1, "应能成功释放锁第一次");

            boolean released2 = lock.release();
            assertTrue(released2, "应能成功释放锁第二次");
        }

        @Test
        @DisplayName("测试释放未持有的锁")
        void testReleaseUnheldLock() {
            LockInfo lockInfo = new LockInfo("unheld-lock-test", 1, 10);
            Lock lock = redissonLockFactory.getLock(lockInfo);

            // 尝试释放未持有的锁
            boolean released = lock.release();
            assertFalse(released, "应无法释放未持有的锁");
        }
    }
}