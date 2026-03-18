package com.molandev.framework.datasource;

/**
 * 数据源上下文持有者
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前数据源名称
     *
     * @param dataSourceName 数据源名称
     */
    public static void setDataSourceName(String dataSourceName) {
        CONTEXT_HOLDER.set(dataSourceName);
    }

    /**
     * 获取当前数据源名称
     *
     * @return 数据源名称
     */
    public static String getDataSourceName() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前数据源名称
     */
    public static void clearDataSourceName() {
        CONTEXT_HOLDER.remove();
    }
}
