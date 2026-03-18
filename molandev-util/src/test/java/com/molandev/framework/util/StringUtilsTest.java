package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("字符串工具类测试")
class StringUtilsTest {

    @Nested
    @DisplayName("空值判断测试")
    class EmptyCheckTest {

        @Test
        @DisplayName("isEmpty方法测试")
        void isEmpty() {
            // 测试null值
            assertTrue(StringUtils.isEmpty(null));

            // 测试空字符串
            assertTrue(StringUtils.isEmpty(""));

            // 测试只有空格的字符串
            assertTrue(StringUtils.isEmpty(" "));
            assertTrue(StringUtils.isEmpty("   "));

            // 测试非空字符串
            assertFalse(StringUtils.isEmpty("a"));
            assertFalse(StringUtils.isEmpty(" a "));
            assertFalse(StringUtils.isEmpty("test"));
        }

        @Test
        @DisplayName("isNotEmpty方法测试")
        void isNotEmpty() {
            // 测试null值
            assertFalse(StringUtils.isNotEmpty(null));

            // 测试空字符串
            assertFalse(StringUtils.isNotEmpty(""));

            // 测试只有空格的字符串
            assertFalse(StringUtils.isNotEmpty(" "));
            assertFalse(StringUtils.isNotEmpty("   "));

            // 测试非空字符串
            assertTrue(StringUtils.isNotEmpty("a"));
            assertTrue(StringUtils.isNotEmpty(" a "));
            assertTrue(StringUtils.isNotEmpty("test"));
        }
    }

    @Nested
    @DisplayName("空值处理测试")
    class EmptyHandleTest {

        @Test
        @DisplayName("nullToEmpty方法测试")
        void nullToEmpty() {
            // 测试null值
            assertEquals("", StringUtils.nullToEmpty(null));

            // 测试空字符串
            assertEquals("", StringUtils.nullToEmpty(""));

            // 测试非空字符串
            assertEquals("test", StringUtils.nullToEmpty("test"));
            assertEquals(" a ", StringUtils.nullToEmpty(" a "));
        }

        @Test
        @DisplayName("emptyToDefault方法测试")
        void emptyToDefault() {
            // 测试null值
            assertEquals("default", StringUtils.emptyToDefault(null, "default"));

            // 测试空字符串
            assertEquals("default", StringUtils.emptyToDefault("", "default"));

            // 测试只有空格的字符串
            assertEquals("default", StringUtils.emptyToDefault(" ", "default"));
            assertEquals("default", StringUtils.emptyToDefault("   ", "default"));

            // 测试非空字符串
            assertEquals("test", StringUtils.emptyToDefault("test", "default"));
            assertEquals(" a ", StringUtils.emptyToDefault(" a ", "default"));
        }
    }

    @Nested
    @DisplayName("字符串填充测试")
    class StringFillTest {

        @Test
        @DisplayName("fillEmpty方法测试")
        void fillEmpty() {
            // 测试正常情况
            assertEquals("0000000001", StringUtils.fillEmpty(1, '0', 10));
            assertEquals("0000000123", StringUtils.fillEmpty(123, '0', 10));

            // 测试长度等于数字长度
            assertEquals("123", StringUtils.fillEmpty(123, '0', 3));

            // 测试长度小于数字长度
            assertEquals("23", StringUtils.fillEmpty(123, '0', 2));

            // 测试其他字符填充
            assertEquals("aaaaa123", StringUtils.fillEmpty(123, 'a', 8));
        }

        @Test
        @DisplayName("fillEmptyWithStr方法测试")
        void fillEmptyWithStr() {
            // 测试正常情况
            assertEquals("0000000001", StringUtils.fillEmptyWithStr(1, 10, "0"));
            assertEquals("0000000123", StringUtils.fillEmptyWithStr(123, 10, "0"));

            // 测试长度等于数字长度
            assertEquals("123", StringUtils.fillEmptyWithStr(123, 3, "0"));

            // 测试长度小于数字长度
            assertEquals("23", StringUtils.fillEmptyWithStr(123, 2, "0"));

            // 测试其他字符串填充
            assertEquals("aaaaa123", StringUtils.fillEmptyWithStr(123, 8, "a"));
        }

        @Test
        @DisplayName("getRepeatStrs方法测试")
        void getRepeatStrs() {
            // 测试正常情况
            assertEquals("aaaa", StringUtils.getRepeatStrs("a", 4));
            assertEquals("abcdabcd", StringUtils.getRepeatStrs("abcd", 2));

            // 测试0次重复
            assertEquals("", StringUtils.getRepeatStrs("a", 0));

            // 测试负数次重复
            assertEquals("", StringUtils.getRepeatStrs("a", -1));

            // 测试空字符串
            assertEquals("", StringUtils.getRepeatStrs("", 5));
        }
    }

    @Nested
    @DisplayName("字符串移除测试")
    class StringRemoveTest {

        @Test
        @DisplayName("removeStartEnd方法测试")
        void removeStartEnd() {
            // 测试正常情况
            assertEquals("bc", StringUtils.removeStartEnd("abcd", "a", "d"));
            assertEquals("hello", StringUtils.removeStartEnd("[hello]", "[", "]"));

            // 测试只匹配开始
            assertEquals("bcd", StringUtils.removeStartEnd("abcd", "a", "x"));

            // 测试只匹配结束
            assertEquals("abc", StringUtils.removeStartEnd("abcd", "x", "d"));

            // 测试都不匹配
            assertEquals("abcd", StringUtils.removeStartEnd("abcd", "x", "y"));
        }

        @Test
        @DisplayName("removeEnd方法测试")
        void removeEnd() {
            // 测试正常情况
            assertEquals("abc", StringUtils.removeEnd("abcd", "d"));
            assertEquals("[hello", StringUtils.removeEnd("[hello]", "]"));

            // 测试不匹配
            assertEquals("abcd", StringUtils.removeEnd("abcd", "x"));

            // 测试null值
            assertNull(StringUtils.removeEnd(null, "d"));

            // 测试空字符串
            assertEquals("", StringUtils.removeEnd("", "d"));
        }

        @Test
        @DisplayName("removeStart方法测试")
        void removeStart() {
            // 测试正常情况
            assertEquals("bcd", StringUtils.removeStart("abcd", "a"));
            assertEquals("hello]", StringUtils.removeStart("[hello]", "["));

            // 测试不匹配
            assertEquals("abcd", StringUtils.removeStart("abcd", "x"));

            // 测试null值
            assertNull(StringUtils.removeStart(null, "a"));

            // 测试空字符串
            assertEquals("", StringUtils.removeStart("", "a"));
        }
    }

    @Nested
    @DisplayName("字符串分割与连接测试")
    class StringSplitJoinTest {

        @Test
        @DisplayName("split方法测试")
        void split() {
            // 测试正常分割
            List<String> result = StringUtils.split("a,b,c", ",");
            assertEquals(3, result.size());
            assertEquals("a", result.get(0));
            assertEquals("b", result.get(1));
            assertEquals("c", result.get(2));

            // 测试无分割符
            result = StringUtils.split("abc", ",");
            assertEquals(1, result.size());
            assertEquals("abc", result.get(0));

            // 测试空字符串
            result = StringUtils.split("", ",");
            assertEquals(1, result.size());
            assertEquals("", result.get(0));
        }

        @Test
        @DisplayName("join集合方法测试")
        void joinCollection() {
            // 测试正常情况
            List<String> list = ListUtils.toList("a", "b", "c");
            assertEquals("a,b,c", StringUtils.join(list, ","));

            // 测试自定义分隔符
            assertEquals("a|b|c", StringUtils.join(list, "|"));

            // 测试null分隔符
            assertEquals("a,b,c", StringUtils.join(list, null));

            // 测试空集合
            assertEquals("", StringUtils.join(ListUtils.toList(), ","));

            // 测试null集合
            List<String> nullList = null;
            assertEquals("", StringUtils.join(nullList, ","));
        }

        @Test
        @DisplayName("join数组方法测试")
        void joinArray() {
            // 测试正常情况
            String[] array = {"a", "b", "c"};
            assertEquals("a,b,c", StringUtils.join(array, ","));

            // 测试自定义分隔符
            assertEquals("a|b|c", StringUtils.join(array, "|"));

            // 测试null分隔符
            assertEquals("a,b,c", StringUtils.join(array, null));

            // 测试空数组
            assertEquals("", StringUtils.join(new String[0], ","));

            // 测试null数组
            assertEquals("", StringUtils.join((String[]) null, ","));
        }
    }

    @Nested
    @DisplayName("正则表达式测试")
    class RegexTest {

        @Test
        @DisplayName("findMatches方法测试")
        void findMatches() {
            // 测试正常情况
            List<String> result = StringUtils.findMatches("abc123def456", "\\d+");
            assertEquals(2, result.size());
            assertEquals("123", result.get(0));
            assertEquals("456", result.get(1));

            // 测试无匹配
            result = StringUtils.findMatches("abc", "\\d+");
            assertTrue(result.isEmpty());

            // 测试空字符串
            result = StringUtils.findMatches("", "\\d+");
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("大小写转换测试")
    class CaseConvertTest {

        @Test
        @DisplayName("upperFirst方法测试")
        void upperFirst() {
            // 测试正常情况
            assertEquals("Test", StringUtils.upperFirst("test"));
            assertEquals("Hello", StringUtils.upperFirst("hello"));

            // 测试单个字符
            assertEquals("A", StringUtils.upperFirst("a"));

            // 测试已经是大写
            assertEquals("TEST", StringUtils.upperFirst("tEST"));

            // 测试空字符串
            assertNull(StringUtils.upperFirst(null));
            assertEquals("", StringUtils.upperFirst(""));
        }

        @Test
        @DisplayName("lowerFirst方法测试")
        void lowerFirst() {
            // 测试正常情况
            assertEquals("test", StringUtils.lowerFirst("Test"));
            assertEquals("hello", StringUtils.lowerFirst("Hello"));

            // 测试单个字符
            assertEquals("a", StringUtils.lowerFirst("A"));

            // 测试已经是小写
            assertEquals("test", StringUtils.lowerFirst("Test"));

            // 测试空字符串
            assertNull(StringUtils.lowerFirst(null));
            assertEquals("", StringUtils.lowerFirst(""));
        }
    }

    @Nested
    @DisplayName("命名转换测试")
    class NamingConvertTest {

        @Test
        @DisplayName("underline2Camel方法测试")
        void underline2Camel() {
            // 测试正常情况
            assertEquals("userName", StringUtils.underline2Camel("user_name"));
            assertEquals("firstName", StringUtils.underline2Camel("first_name"));
            assertEquals("userId", StringUtils.underline2Camel("user_id"));

            // 测试多个下划线
            assertEquals("userDetailInfo", StringUtils.underline2Camel("user_detail_info"));

            // 测试无下划线
            assertEquals("user", StringUtils.underline2Camel("user"));
            assertEquals("User", StringUtils.underline2Camel("User"));

            // 测试空字符串
            assertEquals("", StringUtils.underline2Camel(null));
            assertEquals("", StringUtils.underline2Camel(""));
        }

        @Test
        @DisplayName("camel2Underline方法测试")
        void camel2Underline() {
            // 测试正常情况
            assertEquals("user_name", StringUtils.camel2Underline("userName"));
            assertEquals("first_name", StringUtils.camel2Underline("firstName"));
            assertEquals("user_id", StringUtils.camel2Underline("userId"));

            // 测试多个驼峰
            assertEquals("user_detail_info", StringUtils.camel2Underline("userDetailInfo"));

            // 测试已经是大写开头
            assertEquals("user", StringUtils.camel2Underline("User"));

            // 测试无驼峰
            assertEquals("user", StringUtils.camel2Underline("user"));

            // 测试空字符串
            assertEquals("", StringUtils.camel2Underline(null));
            assertEquals("", StringUtils.camel2Underline(""));
        }
    }
}