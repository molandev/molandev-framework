package com.molandev.framework.file.config;

import com.molandev.framework.file.FileStorage;
import com.molandev.framework.file.storage.LocalFileStorage;
import com.molandev.framework.file.storage.S3FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动配置
 *
 * @author molandev
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileProperties.class)
public class FileAutoConfiguration {

    /**
     * 本地文件存储配置
     */
    @Configuration
    @ConditionalOnProperty(name = "molandev.file.type", havingValue = "local", matchIfMissing = true)
    static class LocalFileStorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public FileStorage fileStorage(FileProperties properties) {
            log.info("初始化本地文件存储，基础路径: {}",
                    properties.getLocal().getBasePath());
            return new LocalFileStorage(properties);
        }
    }

    /**
     * S3对象存储配置
     */
    @Configuration
    @ConditionalOnProperty(name = "molandev.file.type", havingValue = "s3")
    @ConditionalOnClass(name = "software.amazon.awssdk.services.s3.S3Client")
    static class S3FileStorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public FileStorage fileStorage(FileProperties properties) {
            log.info("初始化 S3 对象存储，Endpoint: {}",
                    properties.getS3().getEndpoint());
            return new S3FileStorage(properties);
        }
    }
}
