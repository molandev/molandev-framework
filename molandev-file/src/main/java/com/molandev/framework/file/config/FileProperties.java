package com.molandev.framework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置属性
 *
 * @author molandev
 */
@Data
@ConfigurationProperties(prefix = "molandev.file")
public class FileProperties {

    /**
     * 存储类型：local（本地）、s3（AWS S3）
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * 默认bucket名称
     */
    private String defaultBucket = "default";

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * S3存储配置
     */
    private S3Config s3 = new S3Config();

    /**
     * 存储类型枚举
     */
    public enum StorageType {
        LOCAL, S3
    }

    /**
     * 本地存储配置
     */
    @Data
    public static class LocalConfig {
        /**
         * 本地存储根路径
         */
        private String basePath = "./files";
    }

    /**
     * S3存储配置
     */
    @Data
    public static class S3Config {
        /**
         * S3 Endpoint（如：https://s3.amazonaws.com）
         */
        private String endpoint;

        /**
         * S3 Region（如：us-east-1）
         */
        private String region = "us-east-1";

        /**
         * Access Key
         */
        private String accessKey;

        /**
         * Secret Key
         */
        private String secretKey;

        /**
         * 默认Bucket
         */
        private String bucket;

        /**
         * 是否使用路径风格访问（PathStyle）
         */
        private boolean pathStyleAccess = false;
    }
}
