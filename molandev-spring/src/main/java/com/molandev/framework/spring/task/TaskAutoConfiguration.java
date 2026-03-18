package com.molandev.framework.spring.task;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 *
 */
@EnableScheduling
@AutoConfiguration
@ConditionalOnProperty(prefix = "molandev.autoconfig.task", name = "enabled", havingValue = "true")
public class TaskAutoConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {
        // 线程池对拒绝任务(无线程可用)的处理策略
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler() {

        return TaskUtil.getTaskScheduler();
    }


}
