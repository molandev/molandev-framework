package com.molandev.framework.spring.xss;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * jackson xss 配置
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnProperty(prefix = XssProperties.PREFIX, name = "enabled", havingValue = "true")
public class XssAutoConfiguration {

    private final XssProperties xssProperties;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter(xssProperties, requestMappingHandlerMapping));
        registration.addUrlPatterns(xssProperties.getUrlPattern());
        registration.setName("xssFilter");
        registration.setOrder(xssProperties.getOrder());
        return registration;
    }

}