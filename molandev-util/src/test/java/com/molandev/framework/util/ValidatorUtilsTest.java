package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("验证工具类测试")
class ValidatorUtilsTest {

    @Nested
    @DisplayName("手机号验证测试")
    class PhoneValidationTest {

        @Test
        @DisplayName("isPhone有效号码测试")
        void isPhone_shouldReturnTrueForValidPhoneNumbers() {
            // 测试有效的手机号码
            assertTrue(ValidatorUtils.isPhone("13812345678"));
            assertTrue(ValidatorUtils.isPhone("15912345678"));
            assertTrue(ValidatorUtils.isPhone("19912345678"));
            assertTrue(ValidatorUtils.isPhone("17712345678"));
        }

        @Test
        @DisplayName("isPhone无效号码测试")
        void isPhone_shouldReturnFalseForInvalidPhoneNumbers() {
            // 测试无效的手机号码
            assertFalse(ValidatorUtils.isPhone("12812345678")); // 以12开头
            assertFalse(ValidatorUtils.isPhone("1381234567"));  // 位数不足
            assertFalse(ValidatorUtils.isPhone("138123456789")); // 位数过多
            assertFalse(ValidatorUtils.isPhone("abcd1234567")); // 包含字母
            assertFalse(ValidatorUtils.isPhone("")); // 空字符串
            assertFalse(ValidatorUtils.isPhone(null)); // null值
        }
    }

    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTest {

        @Test
        @DisplayName("isEmail有效地址测试")
        void isEmail_shouldReturnTrueForValidEmailAddresses() {
            // 测试有效的邮箱地址
            assertTrue(ValidatorUtils.isEmail("test@example.com"));
            assertTrue(ValidatorUtils.isEmail("user.name@domain.co.uk"));
            assertTrue(ValidatorUtils.isEmail("user123@test-domain.org"));
            assertTrue(ValidatorUtils.isEmail("a@b.com"));
        }

        @Test
        @DisplayName("isEmail无效地址测试")
        void isEmail_shouldReturnFalseForInvalidEmailAddresses() {
            // 测试无效的邮箱地址
            assertFalse(ValidatorUtils.isEmail("test.example.com")); // 缺少@符号
            assertFalse(ValidatorUtils.isEmail("test@@example.com")); // 双@符号
            assertFalse(ValidatorUtils.isEmail("@example.com")); // 缺少用户名
            assertFalse(ValidatorUtils.isEmail("test@.com")); // 域名格式错误
            assertFalse(ValidatorUtils.isEmail("test@example.")); // 缺少顶级域名
            assertFalse(ValidatorUtils.isEmail("")); // 空字符串
            assertFalse(ValidatorUtils.isEmail(null)); // null值
        }
    }

    @Nested
    @DisplayName("IP地址验证测试")
    class IpAddressValidationTest {

        @Test
        @DisplayName("isIpAddress有效地址测试")
        void isIpAddress_shouldReturnTrueForValidIpAddresses() {
            // 测试有效的IP地址
            assertTrue(ValidatorUtils.isIpAddress("192.168.1.1"));
            assertTrue(ValidatorUtils.isIpAddress("127.0.0.1"));
            assertTrue(ValidatorUtils.isIpAddress("255.255.255.255"));
            assertTrue(ValidatorUtils.isIpAddress("0.0.0.0"));
            assertTrue(ValidatorUtils.isIpAddress("10.0.0.1"));
        }

        @Test
        @DisplayName("isIpAddress无效地址测试")
        void isIpAddress_shouldReturnFalseForInvalidIpAddresses() {
            // 测试无效的IP地址
            assertFalse(ValidatorUtils.isIpAddress("256.1.1.1")); // 超出范围
            assertFalse(ValidatorUtils.isIpAddress("192.168.1")); // 缺少段
            assertFalse(ValidatorUtils.isIpAddress("192.168.1.1.1")); // 段数过多
            assertFalse(ValidatorUtils.isIpAddress("192.168.1.-1")); // 负数
            assertFalse(ValidatorUtils.isIpAddress("192.168.1.abc")); // 包含字母
            assertFalse(ValidatorUtils.isIpAddress("")); // 空字符串
            assertFalse(ValidatorUtils.isIpAddress(null)); // null值
        }
    }

    @Nested
    @DisplayName("URL验证测试")
    class UrlValidationTest {

        @Test
        @DisplayName("isUrl有效URL测试")
        void isUrl_shouldReturnTrueForValidUrls() {
            // 测试有效的URL
            assertTrue(ValidatorUtils.isUrl("http://www.example.com"));
            assertTrue(ValidatorUtils.isUrl("https://example.com"));
            assertTrue(ValidatorUtils.isUrl("http://example.com:8080"));
            assertTrue(ValidatorUtils.isUrl("https://www.example.com/path"));
            assertTrue(ValidatorUtils.isUrl("http://example.com:8080/path?query=value"));
            assertTrue(ValidatorUtils.isUrl("https://subdomain.example.com"));
            assertTrue(ValidatorUtils.isUrl("http://192.168.1.1:3000"));
            assertTrue(ValidatorUtils.isUrl("www.example.com")); // 没有协议
        }

        @Test
        @DisplayName("isUrl无效URL测试")
        void isUrl_shouldReturnFalseForInvalidUrls() {
            // 测试无效的URL
            assertFalse(ValidatorUtils.isUrl("http://")); // 缺少域名
            assertFalse(ValidatorUtils.isUrl("http://.")); // 无效域名
            assertFalse(ValidatorUtils.isUrl("http://..")); // 无效域名
            assertFalse(ValidatorUtils.isUrl("http://example..com")); // 域名格式错误
            assertFalse(ValidatorUtils.isUrl("")); // 空字符串
            assertFalse(ValidatorUtils.isUrl(null)); // null值
        }
    }

    @Nested
    @DisplayName("数字字符串验证测试")
    class NumericValidationTest {

        @Test
        @DisplayName("isNumeric纯数字测试")
        void isNumeric_shouldReturnTrueForNumericStrings() {
            // 测试纯数字字符串
            assertTrue(ValidatorUtils.isNumeric("123"));
            assertTrue(ValidatorUtils.isNumeric("0"));
            assertTrue(ValidatorUtils.isNumeric("999999"));
        }

        @Test
        @DisplayName("isNumeric非数字测试")
        void isNumeric_shouldReturnFalseForNonNumericStrings() {
            // 测试非纯数字字符串
            assertFalse(ValidatorUtils.isNumeric("123a"));
            assertFalse(ValidatorUtils.isNumeric("12.3")); // 小数点
            assertFalse(ValidatorUtils.isNumeric("-123")); // 负数
            assertFalse(ValidatorUtils.isNumeric("")); // 空字符串
        }
    }

    @Nested
    @DisplayName("字母字符串验证测试")
    class AlphabeticValidationTest {

        @Test
        @DisplayName("isAlphabetic纯字母测试")
        void isAlphabetic_shouldReturnTrueForAlphabeticStrings() {
            // 测试纯字母字符串
            assertTrue(ValidatorUtils.isAlphabetic("abc"));
            assertTrue(ValidatorUtils.isAlphabetic("ABC"));
            assertTrue(ValidatorUtils.isAlphabetic("AbC"));
        }

        @Test
        @DisplayName("isAlphabetic非字母测试")
        void isAlphabetic_shouldReturnFalseForNonAlphabeticStrings() {
            // 测试非纯字母字符串
            assertFalse(ValidatorUtils.isAlphabetic("abc123"));
            assertFalse(ValidatorUtils.isAlphabetic("abc_"));
            assertFalse(ValidatorUtils.isAlphabetic("")); // 空字符串
        }
    }

    @Nested
    @DisplayName("字母数字下划线验证测试")
    class AlphanumericWithUnderscoreValidationTest {

        @Test
        @DisplayName("isAlphanumericWithUnderscore有效组合测试")
        void isAlphanumericWithUnderscore_shouldReturnTrueForValidStrings() {
            // 测试数字、字母和下划线组合
            assertTrue(ValidatorUtils.isAlphanumericWithUnderscore("abc123"));
            assertTrue(ValidatorUtils.isAlphanumericWithUnderscore("ABC_123"));
            assertTrue(ValidatorUtils.isAlphanumericWithUnderscore("_abc"));
            assertTrue(ValidatorUtils.isAlphanumericWithUnderscore("123"));
            assertTrue(ValidatorUtils.isAlphanumericWithUnderscore("___"));
        }

        @Test
        @DisplayName("isAlphanumericWithUnderscore无效组合测试")
        void isAlphanumericWithUnderscore_shouldReturnFalseForInvalidStrings() {
            // 测试包含特殊字符的字符串
            assertFalse(ValidatorUtils.isAlphanumericWithUnderscore("abc-123"));
            assertFalse(ValidatorUtils.isAlphanumericWithUnderscore("abc.123"));
            assertFalse(ValidatorUtils.isAlphanumericWithUnderscore("abc 123"));
            assertFalse(ValidatorUtils.isAlphanumericWithUnderscore("")); // 空字符串
        }
    }

    @Nested
    @DisplayName("特殊字符检测测试")
    class SpecialCharactersCheckTest {

        @Test
        @DisplayName("containsSpecialCharacters包含特殊字符测试")
        void containsSpecialCharacters_shouldReturnTrueForStringsWithSpecialCharacters() {
            // 测试包含特殊字符的字符串
            assertTrue(ValidatorUtils.containsSpecialCharacters("abc-123"));
            assertTrue(ValidatorUtils.containsSpecialCharacters("abc.123"));
            assertTrue(ValidatorUtils.containsSpecialCharacters("abc 123"));
            assertTrue(ValidatorUtils.containsSpecialCharacters("abc@123"));
            assertTrue(ValidatorUtils.containsSpecialCharacters("abc#123"));
        }

        @Test
        @DisplayName("containsSpecialCharacters不包含特殊字符测试")
        void containsSpecialCharacters_shouldReturnFalseForStringsWithoutSpecialCharacters() {
            // 测试不包含特殊字符的字符串
            assertFalse(ValidatorUtils.containsSpecialCharacters("abc123"));
            assertFalse(ValidatorUtils.containsSpecialCharacters("ABC_123"));
            assertFalse(ValidatorUtils.containsSpecialCharacters("_abc"));
            assertFalse(ValidatorUtils.containsSpecialCharacters("")); // 空字符串
        }
    }
}