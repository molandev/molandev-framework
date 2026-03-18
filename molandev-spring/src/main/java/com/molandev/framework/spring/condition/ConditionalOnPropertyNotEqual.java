package com.molandev.framework.spring.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 当属性不等于某个值时匹配
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional({OnPropertyNotEqualCondition.class})
public @interface ConditionalOnPropertyNotEqual {

    /**
     * 前缀
     *
     * @return {@link String}
     */
    String name() default "";

    String havingValue() default "";

}
