package com.molandev.framework.lock.annotation;

import java.lang.annotation.*;

/**
 * 幂等
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等操作的唯一标识，使用spring el表达式 用#来引用方法参数 默认使用当前请求的uri + 参数
     *
     * @return Spring-EL expression
     */
    String key() default "";

    /**
     * 有效期 默认：1分钟 有效期要大于程序执行时间，否则请求还是可能会进来
     *
     * @return expireTime
     */
    int expireTime() default 60;

    /**
     * 提示信息，可自定义
     *
     * @return String
     */
    String msg() default "请勿重复请求";

}
