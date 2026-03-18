package com.molandev.framework.event;

import com.molandev.framework.event.annotation.MolanListener;
import com.molandev.framework.event.core.EventUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(classes = EventIntegrationTest.TestConfig.class, properties = {
        "molandev.run-mode=single",
        "logging.level.com.molandev.framework.event=DEBUG"
})
public class EventIntegrationTest {

    // --- 事件对象 ---

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BaseEvent {
        private String message;
    }

    @Data
    @NoArgsConstructor
    public static class ChildEvent extends BaseEvent {
        public ChildEvent(String message) {
            super(message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimpleEvent {
        private String data;
    }

    // --- 监听器 Bean ---

    @Slf4j
    @Component
    public static class TestListener {
        
        // 记录执行次数和结果，用于断言
        public final Set<String> executionLog = Collections.newSetFromMap(new ConcurrentHashMap<>());
        public final AtomicInteger g1Count = new AtomicInteger(0);
        public CountDownLatch latch;

        // 1. 基础广播监听
        @MolanListener(SimpleEvent.class)
        public void onSimpleEvent(SimpleEvent e) {
            log.info("Received SimpleEvent");
            executionLog.add("simple");
            if (latch != null) latch.countDown();
        }

        // 2. 监听父类（冒泡测试）
        @MolanListener(BaseEvent.class)
        public void onBaseEvent(BaseEvent e) {
            log.info("Received BaseEvent: {}", e.getMessage());
            executionLog.add("base:" + e.getMessage());
            if (latch != null) latch.countDown();
        }

        // 3. 监听子类
        @MolanListener(ChildEvent.class)
        public void onChildEvent(ChildEvent e) {
            log.info("Received ChildEvent: {}", e.getMessage());
            executionLog.add("child:" + e.getMessage());
            if (latch != null) latch.countDown();
        }

        // 4. 同组争抢监听 A (Group1)
        @MolanListener(value = SimpleEvent.class, group = "g1")
        public void onG1_A(SimpleEvent e) {
            log.info("G1_A matched");
            g1Count.incrementAndGet();
            if (latch != null) latch.countDown();
        }

        // 5. 同组争抢监听 B (Group1)
        @MolanListener(value = SimpleEvent.class, group = "g1")
        public void onG1_B(SimpleEvent e) {
            log.info("G1_B matched");
            g1Count.incrementAndGet();
            if (latch != null) latch.countDown();
        }

        // 6. 同步执行测试
        @MolanListener(value = SimpleEvent.class, async = false)
        public void onSync(SimpleEvent e) {
            log.info("Sync execution on thread: {}", Thread.currentThread().getName());
            executionLog.add("sync:" + Thread.currentThread().getName());
        }

        // 7. 隔离性测试 - 监听器 A 修改对象
        @MolanListener(value = SimpleEvent.class, async = false)
        public void onIsolationA(SimpleEvent e) {
            e.setData("modified-by-A");
        }

        // 8. 隔离性测试 - 监听器 B 检查对象
        @MolanListener(value = SimpleEvent.class, async = false)
        public void onIsolationB(SimpleEvent e) {
            executionLog.add("isolation:" + e.getData());
        }

        // 9. 测试参数类型自动推断（不指定事件类型）
        @MolanListener // 自动从方法参数推断为 BaseEvent
        public void onAutoInferred(BaseEvent e) {
            log.info("Auto-inferred listener received: {}", e.getMessage());
            executionLog.add("auto:" + e.getMessage());
            if (latch != null) latch.countDown();
        }
    }

    @SpringBootApplication
    @Configuration
    @org.springframework.context.annotation.Import(TestListener.class)
    public static class TestConfig {
    }

    @Autowired
    private TestListener testListener;

    @Test
    public void testBasicAndSync() {
        testListener.executionLog.clear();
        String currentThread = Thread.currentThread().getName();

        // 发布简单事件
        EventUtil.publish(new SimpleEvent("test-data"));

        // 校验同步执行结果（应该立即生效且线程名相同）
        Assertions.assertTrue(testListener.executionLog.contains("sync:" + currentThread));
    }

    @Test
    public void testInheritanceBubbling() throws InterruptedException {
        testListener.executionLog.clear();
        testListener.latch = new CountDownLatch(3); // 期望：onChildEvent、onBaseEvent 和 onAutoInferred 都会触发

        // 发布子类事件
        EventUtil.publish(new ChildEvent("hello"));

        boolean completed = testListener.latch.await(2, TimeUnit.SECONDS);
        Assertions.assertTrue(completed, "Events were not processed in time");
        Assertions.assertTrue(testListener.executionLog.contains("child:hello"));
        Assertions.assertTrue(testListener.executionLog.contains("base:hello"));
        Assertions.assertTrue(testListener.executionLog.contains("auto:hello"));
    }

    @Test
    public void testCompetitiveConsumption() throws InterruptedException {
        testListener.g1Count.set(0);
        // 期望：SimpleEvent 会触发：onSimpleEvent(广播) + [onG1_A 或 onG1_B](争抢)
        // 所以 g1Count 应该恰好为 1
        testListener.latch = new CountDownLatch(2);

        EventUtil.publish(new SimpleEvent("test-competitive"));

        testListener.latch.await(2, TimeUnit.SECONDS);
        Assertions.assertEquals(1, testListener.g1Count.get(), "Competitive group should only be invoked once");
    }

    @Test
    public void testEventIsolation() {
        testListener.executionLog.clear();
        
        // 发布一个初始事件
        EventUtil.publish(new SimpleEvent("original"));

        // 校验：虽然 IsolationA 修改了对象，但由于是深克隆，IsolationB 拿到的是自己的副本，应该还是 original
        // 注意：监听器的执行顺序是不确定的，但无论顺序如何，每个监听器拿到的都应该是副本
        boolean hasOriginal = testListener.executionLog.contains("isolation:original");
        Assertions.assertTrue(hasOriginal, "Listener should receive original data even if another listener modified its own copy");
    }

    @Test
    public void testAutoInferredEventType() throws InterruptedException {
        testListener.executionLog.clear();
        testListener.latch = new CountDownLatch(2); // onBaseEvent + onAutoInferred

        // 发布基础事件
        EventUtil.publish(new BaseEvent("auto-test"));

        boolean completed = testListener.latch.await(2, TimeUnit.SECONDS);
        Assertions.assertTrue(completed, "监听器未及时执行");
        Assertions.assertTrue(testListener.executionLog.contains("base:auto-test"), "显式指定监听器未执行");
        Assertions.assertTrue(testListener.executionLog.contains("auto:auto-test"), "自动推断监听器未执行");
    }
}
