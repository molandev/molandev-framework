package com.molandev.framework.event.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类继承体系解析器，用于处理事件冒泡
 */
public class EventHierarchyResolver {

    private static final Map<Class<?>, Set<Class<?>>> HIERARCHY_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取指定类及其所有父类和接口
     */
    public static Set<Class<?>> getHierarchy(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return Collections.emptySet();
        }
        return HIERARCHY_CACHE.computeIfAbsent(clazz, k -> {
            Set<Class<?>> result = new LinkedHashSet<>();
            traverse(k, result);
            return result;
        });
    }

    private static void traverse(Class<?> clazz, Set<Class<?>> result) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        result.add(clazz);
        // 递归父类
        traverse(clazz.getSuperclass(), result);
        // 递归接口
        for (Class<?> iface : clazz.getInterfaces()) {
            traverse(iface, result);
        }
    }
}
