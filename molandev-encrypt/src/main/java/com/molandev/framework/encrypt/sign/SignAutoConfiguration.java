package com.molandev.framework.encrypt.sign;

import com.molandev.framework.encrypt.common.EncryptProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 签名校验自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "molandev.encrypt.sign", name = "enabled", havingValue = "true")
public class SignAutoConfiguration {

    @Bean
    public FilterRegistrationBean<SignFilter> signFilterRegistration(EncryptProperties encryptProperties) {
        FilterRegistrationBean<SignFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SignFilter(encryptProperties));
        registration.addUrlPatterns(encryptProperties.getSign().getUrlPattern());
        registration.setName("signFilter");
        registration.setOrder(encryptProperties.getSign().getOrder());
        return registration;
    }

}
