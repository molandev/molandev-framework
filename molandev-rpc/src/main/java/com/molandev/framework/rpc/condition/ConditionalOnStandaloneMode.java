package com.molandev.framework.rpc.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * 仅当 run-mode=single 时生效（独立模式，本地调用）
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnMissingClass(value = "org.springframework.cloud.openfeign.FeignClientFactoryBean")
@ConditionalOnProperty(prefix = "molandev", name = "run-mode", havingValue = "single")
public @interface ConditionalOnStandaloneMode {
}
