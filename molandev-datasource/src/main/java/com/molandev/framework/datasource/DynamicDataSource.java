package com.molandev.framework.datasource;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 动态数据源：支持事务内多数据源切换
 * 通过连接代理实现，每个数据源维护一个连接，统一管理事务
 */
public class DynamicDataSource extends AbstractDataSource {

    private final DataSource defaultDataSource;
    private final Map<Object, Object> targetDataSourcesMap;

    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        this.defaultDataSource = defaultTargetDataSource;
        this.targetDataSourcesMap = targetDataSources;
    }

    /**
     * 获取目标数据源Map（用于测试）
     */
    public Map<Object, Object> getTargetDataSourcesMap() {
        return targetDataSourcesMap;
    }
    
    /**
     * 根据数据源名称获取实际数据源
     * 
     * @param dataSourceName 数据源名称
     * @return 数据源对象，如果找不到则返回默认数据源
     */
    public DataSource getTargetDataSource(String dataSourceName) {
        if (dataSourceName == null) {
            return defaultDataSource;
        }
        DataSource dataSource = (DataSource) targetDataSourcesMap.get(dataSourceName);
        return dataSource != null ? dataSource : defaultDataSource;
    }
    
    /**
     * 覆盖 getConnection 方法，返回代理连接
     * 代理连接会在真正执行 SQL 时才获取实际连接
     */
    @Override
    public Connection getConnection() throws SQLException {
        return createProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return createProxyConnection();
    }
    
    /**
     * 创建代理连接
     * 
     * @return 代理后的 Connection 对象
     */
    private Connection createProxyConnection() {
        ConnectionProxy handler = new ConnectionProxy(this);
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                handler
        );
    }
}
