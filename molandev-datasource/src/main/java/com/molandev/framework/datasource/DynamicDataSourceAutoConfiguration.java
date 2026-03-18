package com.molandev.framework.datasource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源自动配置
 */
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DynamicDataSourceAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMolandevDataSource
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(DataSourceProperties properties) {
        Map<String, DataSourceProperties.DataSourceConfig> datasourceConfigs = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource defaultDataSource = null;
        String primaryName = null;

        for (Map.Entry<String, DataSourceProperties.DataSourceConfig> entry : datasourceConfigs.entrySet()) {
            String name = entry.getKey();
            DataSourceProperties.DataSourceConfig config = entry.getValue();

            DataSource dataSource = createDataSource(config);
            targetDataSources.put(name, dataSource);

            if (config.isPrimary()) {
                if (defaultDataSource != null && primaryName != null) {
                    throw new RuntimeException("找到多个主数据源：" + primaryName + " 和 " + name);
                }
                defaultDataSource = dataSource;
                primaryName = name;
            }
        }

        if (defaultDataSource == null && !targetDataSources.isEmpty()) {
            // 如果没有指定默认数据源，取第一个
            Map.Entry<String, DataSourceProperties.DataSourceConfig> firstEntry = datasourceConfigs.entrySet().iterator().next();
            defaultDataSource = (DataSource) targetDataSources.get(firstEntry.getKey());
            primaryName = firstEntry.getKey();
        }

        if (defaultDataSource == null) {
            throw new RuntimeException("未在 molandev.datasource 下配置数据源");
        }

        return new DynamicDataSource(defaultDataSource, targetDataSources);
    }

    private DataSource createDataSource(DataSourceProperties.DataSourceConfig config) {
        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(config.getUrl())
                .username(config.getUsername())
                .password(config.getPassword())
                .driverClassName(config.getDriverClassName());

        // 指定连接池类型
        if (config.getType() != null) {
            try {
                Class<?> typeClass = Class.forName(config.getType());
                builder.type((Class<? extends DataSource>) typeClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("找不到数据源类型类：" + config.getType(), e);
            }
        }

        DataSource dataSource = builder.build();

        // 通过反射应用通用连接池配置（支持任意连接池）
        if (config.getPool() != null && !config.getPool().isEmpty()) {
            applyPoolProperties(dataSource, config.getPool());
        }

        return dataSource;
    }

    /**
     * 通过反射应用连接池属性，支持任意连接池类型
     */
    private void applyPoolProperties(DataSource dataSource, Map<String, String> properties) {
        BeanWrapper wrapper = new BeanWrapperImpl(dataSource);
        PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();
        Map<String, PropertyDescriptor> propertyMap = new HashMap<>();
        
        for (PropertyDescriptor descriptor : descriptors) {
            propertyMap.put(descriptor.getName().toLowerCase(), descriptor);
        }

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            
            // 支持 kebab-case 和 camelCase
            String normalizedName = toCamelCase(propertyName);
            PropertyDescriptor descriptor = propertyMap.get(normalizedName.toLowerCase());
            
            if (descriptor != null && descriptor.getWriteMethod() != null) {
                try {
                    Class<?> propertyType = descriptor.getPropertyType();
                    Object convertedValue = convertValue(propertyValue, propertyType);
                    wrapper.setPropertyValue(descriptor.getName(), convertedValue);
                } catch (Exception e) {
                    throw new RuntimeException("在数据源上设置属性 '" + propertyName + "' 失败", e);
                }
            }
        }
    }

    /**
     * 将 kebab-case 转换为 camelCase
     */
    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : input.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 类型转换
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        
        return value;
    }

    @Bean
    @ConditionalOnMolandevDataSource
    @ConditionalOnMissingBean
    public DynamicDataSourceInterceptor dynamicDataSourceInterceptor(DataSourceProperties properties) {
        Map<String, String> packageMapping = new HashMap<>();
        String primaryName = null;

        for (Map.Entry<String, DataSourceProperties.DataSourceConfig> entry : properties.getDatasource().entrySet()) {
            String dsName = entry.getKey();
            DataSourceProperties.DataSourceConfig config = entry.getValue();

            if (config.isPrimary()) {
                primaryName = dsName;
            }

            if (config.getPackages() != null) {
                for (String pkg : config.getPackages()) {
                    if (packageMapping.containsKey(pkg)) {
                        throw new RuntimeException("重复的包配置：包 '" + pkg + 
                                "' 同时分配给了 '" + packageMapping.get(pkg) + "' 和 '" + dsName + "'");
                    }
                    packageMapping.put(pkg, dsName);
                }
            }
        }

        if (primaryName == null && !properties.getDatasource().isEmpty()) {
            primaryName = properties.getDatasource().keySet().iterator().next();
        }

        return new DynamicDataSourceInterceptor(packageMapping, primaryName);
    }
}
