package com.molandev.framework.encrypt.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.encrypt.common.EncryptProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 双层加密自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "molandev.encrypt.hybrid", name = "enabled", havingValue = "true")
public class HybridEncryptAutoConfiguration {

    @Bean
    public FilterRegistrationBean<HybridEncryptFilter> hybridEncryptFilterRegistration(
            EncryptProperties encryptProperties, ObjectMapper objectMapper) {
        FilterRegistrationBean<HybridEncryptFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HybridEncryptFilter(encryptProperties, objectMapper));
        registration.addUrlPatterns(encryptProperties.getHybrid().getUrlPattern());
        registration.setName("hybridEncryptFilter");
        registration.setOrder(encryptProperties.getHybrid().getOrder());
        return registration;
    }

}
