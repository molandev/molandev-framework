package com.molandev.framework.event.core;

/**
 * 事件发布接口
 */
public interface MolanEventPublisher {

    /**
     * 发布事件
     * @param event 事件对象
     */
    void publish(Object event);
}
