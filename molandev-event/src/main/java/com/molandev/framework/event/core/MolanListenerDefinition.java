package com.molandev.framework.event.core;

import lombok.Data;
import java.lang.reflect.Method;

/**
 * 内部监听器定义
 */
@Data
public class MolanListenerDefinition {
    private final Object bean;
    private final Method method;
    private final Class<?> eventType;
    private final String group;
    private final boolean async;

    public void invoke(Object event) {
        try {
            method.setAccessible(true);
            method.invoke(bean, event);
        } catch (Exception e) {
            // 将反射异常包装为运行时异常
            throw new RuntimeException("调用监听器方法失败: " + method, e);
        }
    }
}
