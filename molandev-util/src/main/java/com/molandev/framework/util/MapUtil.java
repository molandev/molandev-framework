package com.molandev.framework.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

/**
 * Map工具类，提供便捷的Map创建和操作方法
 */
public class MapUtil {
    

    /**
     * 创建包含指定键值对的不可变Map
     * 使用方式：MapUtil.of("key1", "value1", "key2", "value2")
     *
     * @param args 键值对参数，必须是偶数个，奇数位为键，偶数位为值
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 包含指定键值对的新Map
     * @throws IllegalArgumentException 如果参数数量不是偶数或键为null
     */
    public static <K, V> Map<K, V> toMap(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("参数数量必须是偶数");
        }
        
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            K key = (K) args[i];
            if (key == null) {
                throw new IllegalArgumentException("键不能为null，索引：" + i);
            }
            V value = (V) args[i + 1];
            map.put(key, value);
        }
        return map;
    }
    
    /**
     * 创建包含单个键值对的Map
     *
     * @param key 键
     * @param value 值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的新Map
     */
    public static <K, V> Map<K, V> toMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
    
    /**
     * 创建包含两个键值对的Map
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的新Map
     */
    public static <K, V> Map<K, V> toMap(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
    
    /**
     * 创建包含三个键值对的Map
     *
     * @param k1 第一个键
     * @param v1 第一个值
     * @param k2 第二个键
     * @param v2 第二个值
     * @param k3 第三个键
     * @param v3 第三个值
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 包含指定键值对的新Map
     */
    public static <K, V> Map<K, V> toMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
    
    /**
     * 检查Map是否为空
     *
     * @param map 要检查的Map
     * @return 如果Map为null或空则返回true，否则返回false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * 检查Map是否不为空
     *
     * @param map 要检查的Map
     * @return 如果Map不为null且不为空则返回true，否则返回false
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
    
    /**
     * 安全地获取Map中的值，如果Map为null或键不存在则返回默认值
     *
     * @param map        要获取值的Map
     * @param key        要获取的键
     * @param defaultVal 默认值
     * @param <K>        键类型
     * @param <V>        值类型
     * @return Map中对应键的值，如果Map为null或键不存在则返回默认值
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultVal) {
        if (isEmpty(map)) {
            return defaultVal;
        }
        return map.getOrDefault(key, defaultVal);
    }
    
    /**
     * 合并多个Map，后面的Map会覆盖前面相同的键
     *
     * @param maps 要合并的Map数组
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 合并后的新Map
     */
    @SafeVarargs
    public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
        Map<K, V> result = new HashMap<>();
        for (Map<K, V> map : maps) {
            if (isNotEmpty(map)) {
                result.putAll(map);
            }
        }
        return result;
    }
    

    /**
     * 获取Map的大小，如果Map为null则返回0
     *
     * @param map 要计算大小的Map
     * @return Map的大小
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }
    
    /**
     * 判断Map是否包含指定的键
     *
     * @param map 要检查的Map
     * @param key 要查找的键
     * @return 如果Map不为null且包含指定键则返回true，否则返回false
     */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return map != null && map.containsKey(key);
    }
    
    /**
     * 判断Map是否包含指定的值
     *
     * @param map   要检查的Map
     * @param value 要查找的值
     * @return 如果Map不为null且包含指定值则返回true，否则返回false
     */
    public static boolean containsValue(Map<?, ?> map, Object value) {
        return map != null && map.containsValue(value);
    }

}