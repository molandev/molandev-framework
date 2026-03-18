package com.molandev.framework.datasource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 连接代理，使用链式事务管理多个数据源
 * 核心思想：为每个数据源维护一个独立的连接，统一管理 commit/rollback
 */
public class ConnectionProxy implements InvocationHandler {

    /**
     * 事务管理相关方法名常量
     */
    private static final String METHOD_CLOSE = "close";
    private static final String METHOD_COMMIT = "commit";
    private static final String METHOD_ROLLBACK = "rollback";
    private static final String METHOD_SET_AUTO_COMMIT = "setAutoCommit";
    private static final String METHOD_GET_AUTO_COMMIT = "getAutoCommit";

    /**
     * 动态数据源
     */
    private final DynamicDataSource dynamicDataSource;
    
    /**
     * 连接缓存：key=数据源名称，value=该数据源的连接
     */
    private final Map<String, Connection> cachedConnectionMap = new HashMap<>(8);
    
    /**
     * 自动提交标志
     */
    private boolean autoCommit = true;

    /**
     * 构造连接代理
     *
     * @param dynamicDataSource 动态数据源
     */
    public ConnectionProxy(DynamicDataSource dynamicDataSource) {
        this.dynamicDataSource = dynamicDataSource;
    }

    /**
     * 代理调用：拦截所有 Connection 接口的方法调用
     *
     * @param proxy  代理对象
     * @param method 被调用的方法
     * @param args   方法参数
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        // 特殊方法统一处理所有缓存的连接
        if (METHOD_CLOSE.equals(methodName)) {
            close();
            return null;
        } else if (METHOD_COMMIT.equals(methodName)) {
            commit();
            return null;
        } else if (METHOD_ROLLBACK.equals(methodName)) {
            rollback();
            return null;
        } else if (METHOD_SET_AUTO_COMMIT.equals(methodName)) {
            setAutoCommit((Boolean) args[0]);
            return null;
        } else if (METHOD_GET_AUTO_COMMIT.equals(methodName)) {
            return this.autoCommit;
        }
        
        // 其他方法：根据当前上下文获取对应的连接
        String currentDataSource = DataSourceContextHolder.getDataSourceName();
        if (currentDataSource == null) {
            // 如果没有设置，使用默认数据源
            currentDataSource = "default";
        }
        
        Connection connection = getOrCreateConnection(currentDataSource);
        return method.invoke(connection, args);
    }

    /**
     * 获取或创建连接：如果该数据源的连接已经存在，直接返回；否则创建新连接
     *
     * @param dataSourceName 数据源名称
     * @return 连接对象
     * @throws SQLException SQL异常
     */
    private Connection getOrCreateConnection(String dataSourceName) throws SQLException {
        Connection connection = cachedConnectionMap.get(dataSourceName);
        if (connection == null) {
            // 从 DynamicDataSource 中获取实际的数据源
            DataSource targetDataSource = dynamicDataSource.getTargetDataSource(dataSourceName);
            if (targetDataSource == null) {
                throw new SQLException("找不到数据源: " + dataSourceName);
            }
            
            connection = targetDataSource.getConnection();
            
            // 设置自动提交模式
            if (!autoCommit) {
                connection.setAutoCommit(false);
            }
            
            // 缓存连接
            cachedConnectionMap.put(dataSourceName, connection);
        }
        return connection;
    }

    /**
     * 设置自动提交模式：应用到所有已缓存的连接
     *
     * @param autoCommit 自动提交标志
     * @throws SQLException SQL异常
     */
    private void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection connection : cachedConnectionMap.values()) {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }

    /**
     * 提交所有缓存的连接
     * 注意：这不是原子性操作，如果某个数据源提交失败，其他已提交的无法回滚
     *
     * @throws SQLException SQL异常
     */
    private void commit() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection connection : cachedConnectionMap.values()) {
            try {
                connection.commit();
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }

    /**
     * 回滚所有缓存的连接
     *
     * @throws SQLException SQL异常
     */
    private void rollback() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection connection : cachedConnectionMap.values()) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }

    /**
     * 关闭所有缓存的连接
     *
     * @throws SQLException SQL异常
     */
    private void close() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection connection : cachedConnectionMap.values()) {
            try {
                connection.close();
            } catch (SQLException e) {
                exceptions.add(e);
            }
        }
        cachedConnectionMap.clear();
        throwSQLExceptionIfNecessary(exceptions);
    }

    /**
     * 如果有异常，抛出聚合异常
     *
     * @param exceptions 异常集合
     * @throws SQLException SQL异常
     */
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("多数据源事务操作失败");
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
}
