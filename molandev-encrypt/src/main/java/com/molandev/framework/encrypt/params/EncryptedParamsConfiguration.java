package com.molandev.framework.encrypt.params;

import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 加密参数解析器配置
 */
@Configuration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "molandev.encrypt.params", name = "enabled", havingValue = "true")
public class EncryptedParamsConfiguration implements WebMvcConfigurer {

    @Bean
    public EncryptedParamHandlerMethodArgumentResolver decryptParamHandlerMethodArgumentResolver() {
        return new EncryptedParamHandlerMethodArgumentResolver();
    }

    /**
     * 创建 BeanPostProcessor，用于调整参数解析器的优先级
     * 使用静态方法避免循环依赖警告
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static EncryptedParamBeanPostProcessor encryptParamBeanPostProcessor() {
        return new EncryptedParamBeanPostProcessor();
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, decryptParamHandlerMethodArgumentResolver());
    }

    /**
     * 在非静态方法中验证配置，确保在适当的时候检查配置
     */
    @Autowired
    public void validateConfig(EncryptProperties encryptProperties) {
        if (StringUtils.isEmpty(encryptProperties.getParams().getKey())) {
            throw new IllegalArgumentException("请配置参数加密的密钥!");
        }
    }

}