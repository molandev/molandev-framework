package com.molandev.framework.encrypt.db;

import com.molandev.framework.encrypt.common.EncryptProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * db配置
 */
@Configuration
@EnableConfigurationProperties(EncryptProperties.class)
@ConditionalOnProperty(prefix = "molandev.encrypt.db", name = "enabled", havingValue = "true")
public class DbEncryptAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DbEncryptService encryptService() {
        return new DbEncryptService();
    }

    @Bean
    public DbEncryptInterceptor parameterInterceptor() {
        return new DbEncryptInterceptor();
    }

    @Bean
    public DbEncryptResultInterceptor resultInterceptor() {
        return new DbEncryptResultInterceptor();
    }

}
