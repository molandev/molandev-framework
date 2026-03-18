package com.molandev.framework.spring.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 当匹配到某个前缀时加载，例如spring.cache.type=redis
 * ConditionalOnPropertyPrefix("spring.cache")时加载
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional({OnPropertyPrefixCondition.class})
public @interface ConditionalOnPropertyPrefix {

    /**
     * 前缀
     */
    String prefix() default "";

    /**
     * 当没有这个配置的时候才匹配，默认情况下为有这个配置的时候匹配
     */
    boolean matchIfMissing() default false;

}
