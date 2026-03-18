package com.molandev.framework.rpc.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

/**
 * 仅当 run-mode=cloud 时生效（云模式，微服务远程调用）
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignClientFactoryBean")
@ConditionalOnProperty(prefix = "molandev", name = "run-mode", havingValue = "cloud")
public @interface ConditionalOnCloudMode {
}
