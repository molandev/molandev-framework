package com.molandev.framework.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学工具
 */
public class MathUtils {

    /**
     * 求最小值
     */
    public static int min(int... nums) {
        int num = nums[0];
        for (int i = 1; i < nums.length; i++) {
            num = Math.min(num, nums[i]);
        }
        return num;
    }

    /**
     * 求最大值
     */
    public static int max(int... nums) {
        int num = nums[0];
        for (int i = 1; i < nums.length; i++) {
            num = Math.max(num, nums[i]);
        }
        return num;
    }

    /**
     * 加法
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.add(b2).doubleValue();
    }

    /**
     * 减法
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 乘法
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 除法
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, 10);
    }

    /**
     * 除法，保留小数位
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留小数位有误");
        } else {
            BigDecimal b1 = BigDecimal.valueOf(v1);
            BigDecimal b2 = BigDecimal.valueOf(v2);
            return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
        }
    }

    /**
     * 四舍五入多少位
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("保留小数位有误");
        } else {
            BigDecimal b = BigDecimal.valueOf(v);
            BigDecimal one = new BigDecimal("1");
            return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
        }
    }

    /**
     * 判断两个double是否相等
     */
    public static boolean equals(double a, double b, double scale) {
        return Math.abs(a - b) < scale;
    }

    /**
     * 相等判断
     */
    public static boolean equals(double a, double b) {
        return equals(a, b, 1e-6);
    }

}
