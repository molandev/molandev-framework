package com.molandev.framework.spring.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("任务工具类测试")
class TaskUtilTest {

    private TaskScheduler originalTaskScheduler;

    @BeforeEach
    void setUp() {
        // 创建测试用的任务调度器
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
        ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler(executor);
        TaskUtil.setTaskScheduler(taskScheduler);
    }

    @Nested
    @DisplayName("立即执行任务测试")
    class InvokeNowTest {

        @Test
        @DisplayName("invokeNow立即执行任务测试")
        void invokeNow_shouldExecuteTaskImmediately() throws InterruptedException {
            // 测试立即执行任务
            AtomicBoolean executed = new AtomicBoolean(false);

            TaskUtil.invokeNow(() -> executed.set(true));

            // 等待任务执行完成
            Thread.sleep(100);

            assertTrue(executed.get(), "任务应该立即执行");
        }
    }

    @Nested
    @DisplayName("延时执行任务测试")
    class InvokeLaterTest {

        @Test
        @DisplayName("invokeLater使用Cron表达式调度任务测试")
        void invokeLater_withCron_shouldScheduleTaskWithCronExpression() throws InterruptedException {
            // 测试使用cron表达式调度任务
            AtomicInteger executionCount = new AtomicInteger(0);

            // 使用每秒执行一次的cron表达式
            TaskUtil.invokeLater(executionCount::incrementAndGet, "* * * * * ?");

            // 等待一段时间让任务有机会执行
            Thread.sleep(1100);

            // 验证任务至少执行了一次
            assertTrue(executionCount.get() > 0, "任务应该根据cron表达式执行");
        }

        @Test
        @DisplayName("invokeLater使用Duration延时执行任务测试")
        void invokeLater_withDuration_shouldScheduleTaskWithDelay() throws InterruptedException {
            // 测试延时执行任务
            AtomicBoolean executed = new AtomicBoolean(false);
            Duration delay = Duration.ofMillis(200);

            TaskUtil.invokeLater(() -> executed.set(true), delay);

            // 初始应该还未执行
            assertFalse(executed.get(), "任务在延时前不应该执行");

            // 等待足够时间让任务执行
            Thread.sleep(300);

            assertTrue(executed.get(), "任务应该在延时后执行");
        }
    }

    @Nested
    @DisplayName("循环执行任务测试")
    class InvokeLoopTest {

        @Test
        @DisplayName("invokeLaterLoopDelay固定延迟循环执行任务测试")
        void invokeLaterLoopDelay_shouldScheduleTaskWithFixedDelay() throws InterruptedException {
            // 测试固定延迟循环执行任务
            AtomicInteger executionCount = new AtomicInteger(0);
            Duration firstDelay = Duration.ofMillis(100);
            Duration delay = Duration.ofMillis(200);

            TaskUtil.invokeLaterLoopDelay(executionCount::incrementAndGet, firstDelay, delay);

            // 初始应该执行次数为0
            assertEquals(0, executionCount.get(), "任务在初始时不应该执行");

            // 等待第一次执行
            Thread.sleep(150);
            assertEquals(1, executionCount.get(), "任务应该执行一次");

            // 等待第二次执行
            Thread.sleep(250);
            assertEquals(2, executionCount.get(), "任务应该执行两次");
        }

        @Test
        @DisplayName("invokeLoopDelay立即开始固定延迟循环执行任务测试")
        void invokeLoopDelay_shouldScheduleTaskWithFixedDelayStartingImmediately() throws InterruptedException {
            // 测试立即开始并以固定延迟循环执行任务
            AtomicInteger executionCount = new AtomicInteger(0);
            Duration delay = Duration.ofMillis(200);

            TaskUtil.invokeLoopDelay(executionCount::incrementAndGet, delay);

            // 立即检查
            assertEquals(0, executionCount.get(), "任务在初始时不应该执行");

            // 等待第一次执行
            Thread.sleep(100);
            assertEquals(1, executionCount.get(), "任务应该执行一次");

            // 等待第二次执行
            Thread.sleep(250);
            assertTrue(executionCount.get() >= 2, "任务应该至少执行两次");
        }

        @Test
        @DisplayName("invokeLoopRate固定频率执行任务测试")
        void invokeLoopRate_shouldScheduleTaskAtFixedRate() throws InterruptedException {
            // 测试以固定频率执行任务
            AtomicInteger executionCount = new AtomicInteger(0);
            Duration period = Duration.ofMillis(200);

            TaskUtil.invokeLoopRate(executionCount::incrementAndGet, period);

            // 立即检查
            assertEquals(0, executionCount.get(), "任务在初始时不应该执行");

            // 等待足够时间让任务执行几次
            Thread.sleep(500);
            assertTrue(executionCount.get() >= 2, "任务应该至少执行两次");
        }
    }

    @Nested
    @DisplayName("任务调度器测试")
    class TaskSchedulerTest {

        @Test
        @DisplayName("setTaskScheduler设置任务调度器测试")
        void setTaskScheduler_shouldUpdateTaskScheduler() {
            // 测试设置任务调度器
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);
            ConcurrentTaskScheduler newTaskScheduler = new ConcurrentTaskScheduler(executor);

            TaskUtil.setTaskScheduler(newTaskScheduler);

            assertEquals(newTaskScheduler, TaskUtil.getTaskScheduler(), "任务调度器应该被正确设置");
        }
    }
}