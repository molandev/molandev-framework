package com.molandev.framework.datasource;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 条件注解：当配置了 molandev.datasource 时生效
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMolandevDataSourceCondition.class)
public @interface ConditionalOnMolandevDataSource {
}
