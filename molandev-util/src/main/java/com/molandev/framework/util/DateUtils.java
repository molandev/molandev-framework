package com.molandev.framework.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * 日期工具类
 * <p>
 * 提供基于 Java 8 java.time 包的日期处理能力，弃用线程不安全的 SimpleDateFormat。
 * 包含日期格式化、解析、当前时间获取及常用对象转换功能。
 *
 * @author molandev
 */
public class DateUtils {

    // ==================================================================================
    // 1. 常量定义
    // ==================================================================================

    /**
     * 默认日期时间格式: yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式: yyyy-MM-dd
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认日期时间格式化器
     */
    public static final DateTimeFormatter DEFAULT_LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

    /**
     * 默认日期格式化器
     */
    public static final DateTimeFormatter DEFAULT_LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    /**
     * 上海时区 (GMT+8)
     */
    public static final String ASIA_SHANGHAI = "GMT+8";

    /**
     * 一秒钟的毫秒数
     */
    public static final int ONE_SECOND_MILLSECOND = 1000;

    /**
     * 默认时区偏移量
     */
    public static ZoneOffset defaultZoneOffset = ZoneOffset.of("+8");

    // ==================================================================================
    // 2. 当前时间获取
    // ==================================================================================

    /**
     * 获取当前日期时间字符串，格式: yyyy-MM-dd HH:mm:ss
     *
     * @return 当前日期时间
     */
    public static String now() {
        return now(DEFAULT_TIME_FORMAT);
    }

    /**
     * 获取指定格式的当前日期时间字符串
     *
     * @param pattern 格式模式
     * @return 格式化后的当前日期时间
     */
    public static String now(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前日期字符串，格式: yyyy-MM-dd
     *
     * @return 当前日期
     */
    public static String getDate() {
        return getDate(DEFAULT_DATE_FORMAT);
    }

    /**
     * 获取指定格式的当前日期字符串
     *
     * @param pattern 格式模式
     * @return 格式化后的当前日期
     */
    public static String getDate(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================================================================================
    // 3. 格式化 (Date/LocalDateTime -> String)
    // ==================================================================================

    /**
     * Date 转字符串，使用默认格式: yyyy-MM-dd HH:mm:ss
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String toStr(Date date) {
        return toStr(date, DEFAULT_TIME_FORMAT);
    }

    /**
     * Date 转字符串，使用指定格式
     *
     * @param date    日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String toStr(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return toLocalDateTime(date).format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDateTime 转字符串，使用默认格式: yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 日期时间对象
     * @return 格式化后的字符串
     */
    public static String toStr(LocalDateTime localDateTime) {
        return toStr(localDateTime, DEFAULT_TIME_FORMAT);
    }

    /**
     * LocalDateTime 转字符串，使用指定格式
     *
     * @param localDateTime 日期时间对象
     * @param pattern       格式模式
     * @return 格式化后的字符串
     */
    public static String toStr(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ==================================================================================
    // 4. 解析 (String -> Date/LocalDateTime)
    // ==================================================================================

    /**
     * 字符串转 Date，使用默认格式: yyyy-MM-dd HH:mm:ss
     *
     * @param dateStr 日期字符串
     * @return Date 对象
     */
    public static Date toDate(String dateStr) {
        return toDate(dateStr, DEFAULT_TIME_FORMAT);
    }

    /**
     * 字符串转 Date，使用指定格式
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return Date 对象
     */
    public static Date toDate(String dateStr, String pattern) {
        LocalDateTime localDateTime = toLocalDateTime(dateStr, pattern);
        return localDateTime != null ? toDate(localDateTime) : null;
    }

    /**
     * 字符串转 LocalDateTime，使用默认格式: yyyy-MM-dd HH:mm:ss
     *
     * @param dateStr 日期字符串
     * @return LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(String dateStr) {
        return toLocalDateTime(dateStr, DEFAULT_TIME_FORMAT);
    }

    /**
     * 字符串转 LocalDateTime
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            TemporalAccessor temporalAccessor = formatter.parse(dateStr);
            if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
                return LocalDateTime.from(temporalAccessor);
            } else {
                return LocalDate.from(temporalAccessor).atStartOfDay();
            }
        } catch (Exception e) {
            throw new RuntimeException("Parse date error: " + dateStr + " with pattern: " + pattern, e);
        }
    }

    // ==================================================================================
    // 5. 相互转换
    // ==================================================================================

    /**
     * LocalDateTime 转毫秒数
     *
     * @param localDateTime 日期时间对象
     * @return 毫秒数
     */
    public static long toMillis(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L;
        }
        return localDateTime.toInstant(defaultZoneOffset).toEpochMilli();
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date Date 对象
     * @return LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime LocalDateTime 对象
     * @return Date 对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
