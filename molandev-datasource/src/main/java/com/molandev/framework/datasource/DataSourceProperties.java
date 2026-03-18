package com.molandev.framework.datasource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源属性配置
 */
@Data
@ConfigurationProperties(prefix = "molandev")
public class DataSourceProperties {

    /**
     * 数据源配置，key为数据源名称，value为具体配置
     */
    private Map<String, DataSourceConfig> datasource = new LinkedHashMap<>();

    @Data
    public static class DataSourceConfig {
        /**
         * 数据库URL
         */
        private String url;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 驱动类名
         */
        private String driverClassName;
        /**
         * 连接池类型（默认为 HikariCP）
         */
        private String type = "com.zaxxer.hikari.HikariDataSource";
        /**
         * 是否为主数据源
         */
        private boolean primary = false;
        /**
         * 该数据源关联的包名列表
         */
        private List<String> packages = new ArrayList<>();
        /**
         * 通用连接池配置（支持任意连接池的配置项）
         */
        private Map<String, String> pool = new LinkedHashMap<>();
    }
}
