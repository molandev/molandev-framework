package com.molandev.framework.event.annotation;

import java.lang.annotation.*;

/**
 * Molan 事件监听注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MolanListener {

    /**
     * 监听的事件类型
     * 如果不指定（默认为 Void.class），则自动从方法的第一个参数类型推断
     */
    Class<?> value() default Void.class;

    /**
     * 消费组名称
     * 为空时视为广播模式；非空时视为争抢模式
     */
    String group() default "";

    /**
     * 是否异步执行（默认为 true）
     */
    boolean async() default true;
}
