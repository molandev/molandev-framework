package com.molandev.framework.util;

import java.util.regex.Pattern;

public class ValidatorUtils {

    // 手机号码正则表达式（简单示例，根据实际需求调整）
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    // 邮箱地址正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // IP地址正则表达式
    private static final String IP_ADDRESS_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    // URL正则表达式
    private static final String URL_REGEX =
            "^(https?://)?" + // 协议部分
                    "(" +
                    // 域名部分
                    "([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}" +
                    "|" +
                    // IP地址部分
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
                    ")" +
                    "(:[0-9]{1,5})?" + // 端口号（可选）
                    "(/[^\\s]*)?$"; // 路径部分（可选）

    // 校验手机号码
    public static boolean isPhone(String str) {
        return str != null && !str.isEmpty() && Pattern.matches(PHONE_REGEX, str);
    }

    // 校验邮箱地址
    public static boolean isEmail(String str) {
        return str != null && !str.isEmpty() && Pattern.matches(EMAIL_REGEX, str);
    }

    // 校验IP地址
    public static boolean isIpAddress(String str) {
        return str != null && !str.isEmpty() && Pattern.matches(IP_ADDRESS_REGEX, str);
    }

    // 校验URL
    public static boolean isUrl(String str) {
        return str != null && !str.isEmpty() && Pattern.matches(URL_REGEX, str);
    }

    // 校验是否是纯数字
    public static boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && str.matches("\\d+");
    }

    // 校验是否是纯文本（只包含字母）
    public static boolean isAlphabetic(String str) {
        return str != null && !str.isEmpty() && str.matches("[a-zA-Z]+");
    }

    // 校验是否是数字加文本加下划线
    public static boolean isAlphanumericWithUnderscore(String str) {
        return str != null && !str.isEmpty() && str.matches("\\w+");
    }

    // 校验是否包含特殊字符
    public static boolean containsSpecialCharacters(String str) {
        return str != null && !str.isEmpty() && !str.matches("[a-zA-Z0-9_]+");
    }

}