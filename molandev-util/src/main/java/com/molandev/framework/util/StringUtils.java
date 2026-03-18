package com.molandev.framework.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class StringUtils {

    /**
     * 空字符判断
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    /**
     * 非空判断
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * null转空字符串
     */
    public static String nullToEmpty(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    /**
     * 空字符串转默认值
     */
    public static String emptyToDefault(String str, String def) {
        if (isEmpty(str)) {
            return def;
        }
        return str;
    }

    /**
     * 前置填充， 例如 num =1 空位为0 总长为10 结果为0000000001
     */
    public static String fillEmpty(long num, char c, int length) {
        String numStr = String.valueOf(num);
        int numLen = numStr.length();
        if (numLen >= length) {
            return numStr.substring(numLen - length);
        }
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length - numLen) {
            sb.append(c);
        }
        sb.append(numStr);
        return sb.toString();
    }

    /**
     * 去掉首字符串和未字符串
     *
     * @param str      字符串
     * @param startStr 首串
     * @param endStr   尾串
     */
    public static String removeStartEnd(String str, String startStr, String endStr) {
        return removeEnd(removeStart(str, startStr), endStr);
    }

    /**
     * 去掉末尾字符串
     */
    public static String removeEnd(String str, String endStr) {
        if (str == null) {
            return str;
        }
        if (!str.endsWith(endStr)) {
            return str;
        }
        return str.substring(0, str.lastIndexOf(endStr));
    }

    /**
     * 去掉首字符串
     */
    public static String removeStart(String str, String startStr) {
        if (str == null) {
            return str;
        }
        if (!str.startsWith(startStr)) {
            return str;
        }
        return str.substring(str.indexOf(startStr) + startStr.length());
    }

    /**
     * 分隔字符串
     */
    public static List<String> split(String str, String split) {
        String[] split1 = str.split(split);
        return new ArrayList<>(Arrays.asList(split1));
    }

    /**
     * 集合转字符串
     */
    public static String join(Collection<String> list, String split) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (split == null) {
            split = ",";
        }
        StringBuilder sb = new StringBuilder();
        for (Object s : list) {
            sb.append(s).append(split);
        }
        return removeEnd(sb.toString(), split);
    }

    /**
     * 数组join成字符串
     */
    public static String join(String[] array, String split) {
        if (array == null || array.length == 0) {
            return "";
        }
        if (split == null) {
            split = ",";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length - 1) {
                sb.append(split);
            }
        }
        return sb.toString();
    }


    /**
     * 寻找匹配项,返回List
     *
     * @param str   字符串
     * @param regex 正则式
     */
    public static List<String> findMatches(String str, String regex) {
        List<String> l = new LinkedList<String>();
        Matcher m = Pattern.compile(regex).matcher(str);
        while (m.find()) {
            l.add(m.group());
        }
        return l;
    }

    /**
     * 填满空的str
     *
     * @param length 长度
     * @param str    str
     * @param num    数量
     * @return {@link String}
     */
    public static String fillEmptyWithStr(long num, int length, String str) {
        String strNum = String.valueOf(num);
        int numLen = strNum.length();
        if (numLen > length) {
            return strNum.substring(numLen - length);
        } else {
            String repeat = getRepeatStrs(str, length - numLen);
            return repeat + strNum;
        }
    }

    /**
     * 获取重复str
     *
     * @param repeatStr   重复str
     * @param repeatCount 重复计数
     * @return {@link String}
     */
    public static String getRepeatStrs(String repeatStr, int repeatCount) {
        if (repeatCount <= 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < repeatCount; i++) {
                sb.append(repeatStr);
            }
            return sb.toString();
        }
    }

    /**
     * 首字母大写
     *
     * @param str str
     * @return {@link String}
     */
    public static String upperFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return String.valueOf(str.charAt(0)).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     *
     * @param str str
     * @return {@link String}
     */
    public static String lowerFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return String.valueOf(str.charAt(0)).toLowerCase() + str.substring(1);
    }

    /**
     * 下划线转驼峰法(默认小驼峰)
     *
     * @param line 源字符串
     * @return 转换后的字符串
     */
    public static String underline2Camel(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher = pattern.matcher(line);
        // 匹配正则表达式
        while (matcher.find()) {
            String word = matcher.group();
            // 当是true 或则是空的情况
            if (matcher.start() == 0) {
                sb.append(word.charAt(0));
            } else {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }

            int index = word.lastIndexOf('_');
            if (index > 0) {
                sb.append(word.substring(1, index).toLowerCase());
            } else {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰法转下划线
     *
     * @param line 源字符串
     * @return 转换后的字符串
     */
    public static String camel2Underline(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        line = String.valueOf(line.charAt(0)).toUpperCase().concat(line.substring(1));
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("[A-Z]([a-z\\d]+)?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.toLowerCase());
            sb.append(matcher.end() == line.length() ? "" : "_");
        }
        return sb.toString();
    }


}
