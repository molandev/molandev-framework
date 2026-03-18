package com.molandev.framework.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * List工具类
 */
public class ListUtils {


    /**
     * 创建可变列表，接收可变参数
     *
     * @param elements 元素
     * @param <T>      泛型类型
     * @return 可变列表
     */
    @SafeVarargs
    public static <T> List<T> toList(T... elements) {
        if (elements == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * 安全获取列表大小，避免空指针异常
     *
     * @param list 列表
     * @return 列表大小
     */
    public static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * 判断列表是否为空（null或size为0）
     *
     * @param list 列表
     * @return 是否为空
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 判断列表是否非空
     *
     * @param list 列表
     * @return 是否非空
     */
    public static boolean isNotEmpty(List<?> list) {
        return !isEmpty(list);
    }

    /**
     * 安全获取列表元素，避免索引越界异常
     *
     * @param list  列表
     * @param index 索引
     * @param <T>   泛型类型
     * @return 元素，如果索引无效则返回null
     */
    public static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 安全获取列表第一个元素
     *
     * @param list 列表
     * @param <T>  泛型类型
     * @return 第一个元素，如果列表为空则返回null
     */
    public static <T> T getFirst(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 安全获取列表最后一个元素
     *
     * @param list 列表
     * @param <T>  泛型类型
     * @return 最后一个元素，如果列表为空则返回null
     */
    public static <T> T getLast(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * 将两个列表合并成一个新列表
     *
     * @param list1 列表1
     * @param list2 列表2
     * @param <T>   泛型类型
     * @return 合并后的新列表
     */
    public static <T> List<T> merge(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>();
        if (isNotEmpty(list1)) {
            result.addAll(list1);
        }
        if (isNotEmpty(list2)) {
            result.addAll(list2);
        }
        return result;
    }

    /**
     * 去除列表中的重复元素，保持原有顺序
     *
     * @param list 原列表
     * @param <T>  泛型类型
     * @return 去重后的新列表
     */
    public static <T> List<T> distinct(List<T> list) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        Set<T> seen = new LinkedHashSet<>();
        return list.stream()
                .filter(seen::add)
                .collect(Collectors.toList());
    }

    /**
     * 截取列表的一部分
     *
     * @param list  原列表
     * @param start 起始索引（包含）
     * @param end   结束索引（不包含）
     * @param <T>   泛型类型
     * @return 截取后的新列表
     */
    public static <T> List<T> subList(List<T> list, int start, int end) {
        if (isEmpty(list)) {
            return new ArrayList<>();
        }
        if (start < 0) start = 0;
        if (end > list.size()) end = list.size();
        if (start >= end) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list.subList(start, end));
    }
}