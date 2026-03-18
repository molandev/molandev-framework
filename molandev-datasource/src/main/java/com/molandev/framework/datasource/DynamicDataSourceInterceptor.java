package com.molandev.framework.datasource;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;

/**
 * 动态数据源拦截器，基于Mapper包名切换数据源
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class DynamicDataSourceInterceptor implements Interceptor {

    private final Map<String, String> packageMapping;
    private final String defaultDataSource;

    public DynamicDataSourceInterceptor(Map<String, String> packageMapping, String defaultDataSource) {
        this.packageMapping = packageMapping;
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        
        // 获取Mapper方法的完整路径，例如：com.molandev.mapper.UserMapper.selectById
        String id = ms.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        
        String dataSourceName = lookupDataSource(className);
        
        DataSourceContextHolder.setDataSourceName(dataSourceName);
        try {
            return invocation.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceName();
        }
    }

    /**
     * 根据类名查找对应的数据源
     *
     * @param className 类名
     * @return 数据源名称
     */
    private String lookupDataSource(String className) {
        if (packageMapping == null || packageMapping.isEmpty()) {
            return defaultDataSource;
        }

        // 尝试最精确的匹配
        String bestMatch = null;
        int maxMatchLength = -1;

        for (Map.Entry<String, String> entry : packageMapping.entrySet()) {
            String packageName = entry.getKey();
            if (className.startsWith(packageName)) {
                if (packageName.length() > maxMatchLength) {
                    maxMatchLength = packageName.length();
                    bestMatch = entry.getValue();
                }
            }
        }

        return bestMatch != null ? bestMatch : defaultDataSource;
    }
}
