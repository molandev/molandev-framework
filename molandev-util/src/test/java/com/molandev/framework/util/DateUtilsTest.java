package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils 功能集成测试
 */
@DisplayName("日期工具类测试")
public class DateUtilsTest {

    @Nested
    @DisplayName("1. 当前时间获取测试")
    class CurrentTimeTest {

        @Test
        @DisplayName("验证获取当前日期时间字符串")
        void testNow() {
            String now = DateUtils.now();
            assertNotNull(now);
            // 验证格式 yyyy-MM-dd HH:mm:ss
            assertTrue(now.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

            String customNow = DateUtils.now("yyyyMMddHHmmss");
            assertTrue(customNow.matches("\\d{14}"));
        }

        @Test
        @DisplayName("验证获取当前日期字符串")
        void testGetDate() {
            String date = DateUtils.getDate();
            assertNotNull(date);
            // 验证格式 yyyy-MM-dd
            assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
        }
    }

    @Nested
    @DisplayName("2. 格式化测试 (Object -> String)")
    class FormatTest {

        @Test
        @DisplayName("验证 Date 对象格式化")
        void testDateToStr() {
            // 2023-10-24 10:00:00
            long timestamp = 1698112800000L; 
            Date date = new Date(timestamp);
            
            String str = DateUtils.toStr(date, "yyyy-MM-dd");
            assertEquals("2023-10-24", str);
        }

        @Test
        @DisplayName("验证 LocalDateTime 对象格式化")
        void testLocalDateTimeToStr() {
            LocalDateTime ldt = LocalDateTime.of(2023, 10, 24, 10, 0, 0);
            assertEquals("2023-10-24 10:00:00", DateUtils.toStr(ldt));
            assertEquals("2023/10/24", DateUtils.toStr(ldt, "yyyy/MM/dd"));
        }
    }

    @Nested
    @DisplayName("3. 解析测试 (String -> Object)")
    class ParseTest {

        @Test
        @DisplayName("验证日期时间字符串解析")
        void testToLocalDateTime() {
            String str = "2023-10-24 10:00:00";
            LocalDateTime ldt = DateUtils.toLocalDateTime(str);
            assertNotNull(ldt);
            assertEquals(2023, ldt.getYear());
            assertEquals(10, ldt.getMonthValue());
            assertEquals(24, ldt.getDayOfMonth());
            assertEquals(10, ldt.getHour());
        }

        @Test
        @DisplayName("验证纯日期字符串自动补全时间解析")
        void testOnlyDateParse() {
            String str = "2023-10-24";
            // 自动识别 yyyy-MM-dd 并补全为 00:00:00
            LocalDateTime ldt = DateUtils.toLocalDateTime(str, "yyyy-MM-dd");
            assertNotNull(ldt);
            assertEquals(0, ldt.getHour());
            assertEquals(0, ldt.getMinute());
        }

        @Test
        @DisplayName("验证字符串转 Date 对象")
        void testToDate() {
            String str = "2023-10-24 10:00:00";
            Date date = DateUtils.toDate(str);
            assertNotNull(date);
            // 反向验证
            assertEquals(str, DateUtils.toStr(date));
        }
    }

    @Nested
    @DisplayName("4. 相互转换与毫秒值测试")
    class ConversionTest {

        @Test
        @DisplayName("验证 LocalDateTime 转毫秒值")
        void testToMillis() {
            LocalDateTime ldt = LocalDateTime.of(2023, 10, 24, 10, 0, 0);
            long millis = DateUtils.toMillis(ldt);
            assertTrue(millis > 0);
            
            // 验证一致性
            Date date = DateUtils.toDate(ldt);
            assertEquals(millis, date.getTime());
        }

        @Test
        @DisplayName("验证 Date 与 LocalDateTime 互转")
        void testDateAndLdtConversion() {
            Date originalDate = new Date();
            LocalDateTime ldt = DateUtils.toLocalDateTime(originalDate);
            Date convertedDate = DateUtils.toDate(ldt);
            
            // 毫秒精度可能会有微小差异，通常验证到秒级
            assertEquals(originalDate.getTime() / 1000, convertedDate.getTime() / 1000);
        }
    }
}
