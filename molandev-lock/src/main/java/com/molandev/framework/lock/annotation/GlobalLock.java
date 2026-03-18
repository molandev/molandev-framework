package com.molandev.framework.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局锁
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GlobalLock {

    /**
     * 获取锁的超时时间，单位秒。默认30秒
     *
     * @return waitTime
     */
    long waitTime() default 30;

    /**
     * 兜底的上锁以后自动解锁的时间，单位秒。防止程序未能解锁
     * 防止服务宕机等情况，导致锁无法自动解锁
     *
     * @return leaseTime
     */
    long leaseTime() default 60;

    /**
     * 锁的key，使用springEL表达式 建议使用 'name' + #xxx , 即给每个加锁的地方起个名字，然后再加参数控制粒度
     * 默认使用当前的类型加方法名
     *
     * @return keys
     */
    String key() default "";

    /**
     * 加锁超时的处理策略，可在当前类下写一个与当前方法参数签名一致的其他方法 默认抛出异常
     *
     * @return lockTimeoutStrategy
     */
    String timeoutFallback() default "";

}