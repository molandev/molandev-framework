package com.molandev.framework.util;

import java.util.List;
import java.util.Random;

/**
 * 随机工具
 */
public class RandomUtils {

    /**
     * 所有字符
     */
    private static final String ALL_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 字母字符
     */
    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 数字字符
     */
    private static final String NUMBER_CHAR = "0123456789";
    
    /**
     * 中文字符范围
     */
    private static final int MIN_CHINESE_CHAR = 0x4e00;
    private static final int MAX_CHINESE_CHAR = 0x9fff;
    
    /**
     * 手机号前缀
     */
    private static final String[] MOBILE_PREFIXES = {
        "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
        "145", "147", "149",
        "150", "151", "152", "153", "155", "156", "157", "158", "159",
        "166",
        "170", "171", "172", "173", "175", "176", "177", "178", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
        "191", "198", "199"
    };
    
    /**
     * 邮箱后缀
     */
    private static final String[] EMAIL_SUFFIXES = {
        "@gmail.com", "@yahoo.com", "@hotmail.com", "@outlook.com", "@qq.com", 
        "@163.com", "@126.com", "@sina.com", "@sohu.com", "@aliyun.com"
    };
    
    /**
     * 随机
     */
    static Random random = new Random();

    /**
     * 随机整数
     *
     * @param bound 绑定
     * @return int
     */
    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }

    /**
     * 随机字符串 获取定长的随机数，包含大小写、数字
     *
     * @param length 随机数长度
     * @return {@link String}
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 随机混合字符串 获取定长的随机数,包含大小写字母
     *
     * @param length 随机数长度
     * @return {@link String}
     */
    public static String randomMixString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(LETTER_CHAR.charAt(random.nextInt(LETTER_CHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 获取定长的随机数，只包含小写字母
     *
     * @param length 随机数长度
     * @return {@link String}
     */
    public static String randomLowerString(int length) {
        return randomMixString(length).toLowerCase();
    }

    /**
     * 获取定长的随机数,只包含大写字母
     *
     * @param length 随机数长度
     * @return {@link String}
     */
    public static String randomUpperString(int length) {
        return randomMixString(length).toUpperCase();
    }

    /**
     * 获取定长的随机数,只包含数字
     *
     * @param length 随机数长度
     * @return {@link String}
     */
    public static String randomNumberString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_CHAR.charAt(random.nextInt(NUMBER_CHAR.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成指定范围内的随机长整型数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return long
     */
    public static long randomLong(long min, long max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return min + (long)(random.nextDouble() * (max - min + 1));
    }
    
    /**
     * 生成随机布尔值
     *
     * @return boolean
     */
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }
    
    /**
     * 从给定数组中随机选择元素
     *
     * @param array 数组
     * @return T
     */
    @SafeVarargs
    public static <T> T randomElement(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }
    
    /**
     * 从给定列表中随机选择元素
     *
     * @param list 列表
     * @return T
     */
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * 生成随机浮点数（指定范围）
     *
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return float
     */
    public static double randomDouble(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return min + random.nextDouble() * (max - min);
    }
    
    /**
     * 生成随机的中文字符串
     *
     * @param length 随机中文字符串长度
     * @return {@link String}
     */
    public static String randomChineseString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomChar = MIN_CHINESE_CHAR + random.nextInt(MAX_CHINESE_CHAR - MIN_CHINESE_CHAR + 1);
            sb.append((char) randomChar);
        }
        return sb.toString();
    }
    
    /**
     * 生成随机手机号
     *
     * @return {@link String}
     */
    public static String randomMobileNumber() {
        String prefix = randomElement(MOBILE_PREFIXES);
        String suffix = randomNumberString(8); // 后8位数字
        return prefix + suffix;
    }
    
    /**
     * 生成随机邮箱地址
     *
     * @return {@link String}
     */
    public static String randomEmail() {
        String username = randomString(randomInt(5) + 5); // 用户名5-9位
        String suffix = randomElement(EMAIL_SUFFIXES);
        return username + suffix;
    }
}