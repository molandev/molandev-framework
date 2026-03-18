package com.molandev.framework.spring.xss;

import java.lang.annotation.*;

/**
 * 忽略 xss 处理
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XssIgnore {

}
