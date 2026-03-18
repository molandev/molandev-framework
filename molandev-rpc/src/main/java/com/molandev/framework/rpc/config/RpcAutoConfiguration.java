package com.molandev.framework.rpc.config;

import com.molandev.framework.rpc.condition.ConditionalOnCloudMode;
import com.molandev.framework.rpc.feign.LocalFeignResolver;
import com.molandev.framework.rpc.feign.FeignMappingRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * RPC 模块自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(RpcProperties.class)
@Import({FeignMappingRegistrar.class, FeignJsonConfiguration.class})
public class RpcAutoConfiguration {

    /**
     * 注册 FeignClientBeanDefinitionPostProcessor 用于拦截 FeignClientFactoryBean
     * 必须为 static 确保在 Bean 定义阶段执行
     */
    @Bean
    @ConditionalOnCloudMode
    @ConditionalOnMissingBean
    public static LocalFeignResolver feignClientBeanDefinitionPostProcessor() {
        return new LocalFeignResolver();
    }
}
