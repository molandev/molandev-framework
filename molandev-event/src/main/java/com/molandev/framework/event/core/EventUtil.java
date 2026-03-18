package com.molandev.framework.event.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件发布静态工具类
 */
@Slf4j
public class EventUtil {

    @Setter
    private static MolanEventPublisher publisher;

    /**
     * 发布事件
     * @param event 事件对象
     */
    public static void publish(Object event) {
        if (publisher == null) {
            log.warn("MolanEventPublisher 尚未初始化，事件已被忽略: {}", event);
            return;
        }
        publisher.publish(event);
    }
}
