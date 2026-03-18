package com.molandev.framework.spring.task;

import com.molandev.framework.util.DoubleCheckUtils;
import com.molandev.framework.util.ThreadUtil;
import lombok.Setter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 *
 */
public class TaskUtil {

    @Setter
    private static volatile TaskScheduler taskScheduler;

    public static TaskScheduler getTaskScheduler() {
        DoubleCheckUtils.doubleCheck(
                () -> taskScheduler == null,
                () -> taskScheduler = createTaskScheduler()
        );
        return taskScheduler;
    }

    /**
     * 在任务线程池中执行，立即执行（线程池空闲的话）
     *
     * @param runnable 可运行的
     */
    public static void invokeNow(Runnable runnable) {
        getTaskScheduler().schedule(runnable, Instant.now());
    }

    /**
     * 在任务线程池中执行，根据cron表达式执行
     *
     * @param runnable 可运行的
     */
    public static void invokeLater(Runnable runnable, String cron) {
        getTaskScheduler().schedule(runnable, new CronTrigger(cron));
    }

    /**
     * 延迟一段时间后执行
     *
     * @param runnable 可运行的
     * @param duration 持续时间
     */
    public static void invokeLater(Runnable runnable, Duration duration) {
        Instant from = LocalDateTime.now().plusSeconds(duration.getSeconds()).plusNanos(duration.getNano()).atZone(ZoneId.systemDefault()).toInstant();
        getTaskScheduler().schedule(runnable, from);
    }

    /**
     * 延迟一段时间执行，并在执行结束后间隔一段时间执行
     *
     * @param runnable           可运行的
     * @param firstDelayDuration 第一次开始执行延迟的时间
     * @param duration           持续时间
     */
    public static void invokeLaterLoopDelay(Runnable runnable, Duration firstDelayDuration, Duration duration) {
        Instant fromInstant = LocalDateTime.now().plusSeconds(firstDelayDuration.getSeconds()).plusNanos(firstDelayDuration.getNano()).atZone(ZoneId.systemDefault()).toInstant();
        getTaskScheduler().scheduleWithFixedDelay(runnable, fromInstant, duration);
    }

    /**
     * 立即执行，并在执行结束后间隔一段时间执行
     *
     * @param runnable 可运行的
     * @param duration 持续时间
     */
    public static void invokeLoopDelay(Runnable runnable, Duration duration) {
        getTaskScheduler().scheduleWithFixedDelay(runnable, duration);
    }

    /**
     * 立即执行，并每个一段时间执行一次
     *
     * @param runnable 可运行的
     * @param duration 持续时间
     */
    public static void invokeLoopRate(Runnable runnable, Duration duration) {
        getTaskScheduler().scheduleAtFixedRate(runnable, duration);
    }

    protected static TaskScheduler createTaskScheduler() {
        ThreadFactory threadFactory;
        // 检查Java版本并使用反射创建虚拟线程（仅当Java版本大于21时）
        if (ThreadUtil.isVirtualThreadsSupported()) {
            threadFactory = ThreadUtil.createVirtualThreadFactory();
        } else {
            // Java版本小于21时使用普通线程工厂
            threadFactory = r -> {
                Thread thread = new Thread(r, "scheduler-thread-" + r.hashCode());
                thread.setDaemon(true);
                return thread;
            };
        }

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(10, threadFactory);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.allowCoreThreadTimeOut(true);
        ConcurrentTaskScheduler concurrentTaskScheduler = new ConcurrentTaskScheduler(scheduler);

        TaskUtil.setTaskScheduler(concurrentTaskScheduler);
        return concurrentTaskScheduler;
    }


}
