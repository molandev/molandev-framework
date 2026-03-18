package com.molandev.framework.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 线程相关工具类
 */
public class ThreadUtil {

    /**
     * 创建虚拟线程工厂（通过反射）
     */
    public static ThreadFactory createVirtualThreadFactory() {
        try {
            // 使用反射访问Thread.Builder类和相关方法
            Class<?> threadClass = Class.forName("java.lang.Thread");
            java.lang.reflect.Method ofVirtualMethod = threadClass.getDeclaredMethod("ofVirtual");
            Object builder = ofVirtualMethod.invoke(null);

            // 获取Builder类
            Class<?> builderClass = builder.getClass();
            java.lang.reflect.Method nameMethod = builderClass.getDeclaredMethod("name", String.class, long.class);
            Object namedBuilder = nameMethod.invoke(builder, "virtual-", 0L);

            java.lang.reflect.Method factoryMethod = builderClass.getDeclaredMethod("factory");
            return (ThreadFactory) factoryMethod.invoke(namedBuilder);
        } catch (Exception e) {
            // 如果反射失败，回退到普通线程工厂
            return Executors.defaultThreadFactory();
        }
    }

    /**
     * 获取当前 Java 主版本号
     *
     * @return 主版本号，如果获取失败则返回 0
     */
    public static int getJavaVersion() {
        return extractMajorVersion(System.getProperty("java.version"));
    }

    /**
     * 检查当前运行时是否支持虚拟线程 (Java 19+)，但仅在Java版本大于21时启用
     */
    public static boolean isVirtualThreadsSupported() {
        try {
            // 根据用户要求，仅在Java版本大于21时启用虚拟线程
            return getJavaVersion() > 21;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Java版本字符串中提取主版本号
     */
    public static int extractMajorVersion(String version) {
        if (version == null || version.isEmpty()) {
            return 0;
        }

        // 处理不同格式的版本号，例如 "1.8.0_321", "11.0.13", "17.0.2", "21.0.1" 等
        if (version.startsWith("1.")) {
            // 旧格式，如 1.8.0_321
            return Integer.parseInt(version.substring(2, version.indexOf('.', 2)));
        } else {
            // 新格式，如 11.0.13, 17.0.2
            int dotIndex = version.indexOf('.');
            if (dotIndex > 0) {
                return Integer.parseInt(version.substring(0, dotIndex));
            } else {
                // 如果没有点号，尝试直接解析整个字符串
                int endIndex = 0;
                while (endIndex < version.length() && Character.isDigit(version.charAt(endIndex))) {
                    endIndex++;
                }
                if (endIndex > 0) {
                    return Integer.parseInt(version.substring(0, endIndex));
                }
                return 0;
            }
        }
    }
}