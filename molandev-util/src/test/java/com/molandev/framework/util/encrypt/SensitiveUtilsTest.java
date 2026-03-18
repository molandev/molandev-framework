package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("脱敏工具类测试")
class SensitiveUtilsTest {

    @Nested
    @DisplayName("基础脱敏方法测试")
    class DesValueTest {

        @Test
        @DisplayName("测试正常字符串脱敏")
        void testDesValueNormal() {
            String origin = "1234567890";
            String result = SensitiveUtils.desValue(origin, 3, 4, "*");
            assertEquals("123***7890", result);
        }

        @Test
        @DisplayName("测试空字符串脱敏")
        void testDesValueEmpty() {
            String origin = "";
            String result = SensitiveUtils.desValue(origin, 3, 4, "*");
            assertEquals("", result);
        }

        @Test
        @DisplayName("测试null字符串脱敏")
        void testDesValueNull() {
            String result = SensitiveUtils.desValue(null, 3, 4, "*");
            assertNull(result);
        }

        @Test
        @DisplayName("测试前缀长度超过字符串长度")
        void testDesValuePrefixTooLong() {
            String origin = "123";
            String result = SensitiveUtils.desValue(origin, 5, 1, "*");
            assertEquals("123", result);
        }

        @Test
        @DisplayName("测试后缀长度超过字符串长度")
        void testDesValueSuffixTooLong() {
            String origin = "123";
            String result = SensitiveUtils.desValue(origin, 1, 5, "*");
            assertEquals("123", result);
        }

        @Test
        @DisplayName("测试自定义遮罩字符")
        void testDesValueCustomMask() {
            String origin = "1234567890";
            String result = SensitiveUtils.desValue(origin, 2, 3, "#");
            assertEquals("12#####890", result);
        }
    }

    @Nested
    @DisplayName("中文姓名脱敏测试")
    class ChineseNameTest {

        @Test
        @DisplayName("测试正常中文姓名脱敏")
        void testChineseNameNormal() {
            String name = "张三丰";
            String result = SensitiveUtils.chineseName(name);
            assertEquals("**丰", result);
        }

        @Test
        @DisplayName("测试单字姓名脱敏")
        void testChineseNameSingleChar() {
            String name = "华";
            String result = SensitiveUtils.chineseName(name);
            assertEquals("*", result);
        }

        @Test
        @DisplayName("测试空姓名脱敏")
        void testChineseNameEmpty() {
            String name = "";
            String result = SensitiveUtils.chineseName(name);
            assertEquals("", result);
        }

        @Test
        @DisplayName("测试null姓名脱敏")
        void testChineseNameNull() {
            String result = SensitiveUtils.chineseName(null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("身份证号脱敏测试")
    class IdCardNumTest {

        @Test
        @DisplayName("测试18位身份证号脱敏")
        void testIdCardNum18Digits() {
            String id = "123456789012345678";
            String result = SensitiveUtils.idCardNum(id);
            assertEquals("123456********5678", result);
        }

        @Test
        @DisplayName("测试15位身份证号脱敏")
        void testIdCardNum15Digits() {
            String id = "123456789012345";
            String result = SensitiveUtils.idCardNum(id);
            assertEquals("123456*****2345", result);
        }

        @Test
        @DisplayName("测试null身份证号脱敏")
        void testIdCardNumNull() {
            String result = SensitiveUtils.idCardNum(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短身份证号脱敏")
        void testIdCardNumShort() {
            String id = "12345";
            String result = SensitiveUtils.idCardNum(id);
            assertEquals("12345", result);
        }
    }

    @Nested
    @DisplayName("固定电话脱敏测试")
    class FixedPhoneTest {

        @Test
        @DisplayName("测试正常固定电话脱敏")
        void testFixedPhoneNormal() {
            String phone = "010-12345678";
            String result = SensitiveUtils.fixedPhone(phone);
            assertEquals("********5678", result);
        }

        @Test
        @DisplayName("测试null固定电话脱敏")
        void testFixedPhoneNull() {
            String result = SensitiveUtils.fixedPhone(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短固定电话脱敏")
        void testFixedPhoneShort() {
            String phone = "123";
            String result = SensitiveUtils.fixedPhone(phone);
            assertEquals("123", result);
        }
    }

    @Nested
    @DisplayName("手机号码脱敏测试")
    class MobilePhoneTest {

        @Test
        @DisplayName("测试正常手机号码脱敏")
        void testMobilePhoneNormal() {
            String phone = "13812345678";
            String result = SensitiveUtils.mobilePhone(phone);
            assertEquals("138****5678", result);
        }

        @Test
        @DisplayName("测试null手机号码脱敏")
        void testMobilePhoneNull() {
            String result = SensitiveUtils.mobilePhone(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短手机号码脱敏")
        void testMobilePhoneShort() {
            String phone = "123";
            String result = SensitiveUtils.mobilePhone(phone);
            assertEquals("123", result);
        }
    }

    @Nested
    @DisplayName("地址脱敏测试")
    class AddressTest {

        @Test
        @DisplayName("测试正常地址脱敏")
        void testAddressNormal() {
            String address = "北京市海淀区中关村大街1号";
            String result = SensitiveUtils.address(address);
            assertEquals("北京市海淀区*******", result);
        }

        @Test
        @DisplayName("测试null地址脱敏")
        void testAddressNull() {
            String result = SensitiveUtils.address(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短地址脱敏")
        void testAddressShort() {
            String address = "北京";
            String result = SensitiveUtils.address(address);
            assertEquals("北京", result);
        }
    }

    @Nested
    @DisplayName("电子邮箱脱敏测试")
    class EmailTest {

        @Test
        @DisplayName("测试正常电子邮箱脱敏")
        void testEmailNormal() {
            String email = "example@126.com";
            String result = SensitiveUtils.email(email);
            assertEquals("e******@126.com", result);
        }

        @Test
        @DisplayName("测试null电子邮箱脱敏")
        void testEmailNull() {
            String result = SensitiveUtils.email(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短电子邮箱脱敏")
        void testEmailShort() {
            String email = "a@126.com";
            String result = SensitiveUtils.email(email);
            assertEquals("a@126.com", result);
        }

        @Test
        @DisplayName("测试无@符号邮箱脱敏")
        void testEmailWithoutAt() {
            String email = "example";
            String result = SensitiveUtils.email(email);
            assertEquals("example", result);
        }
    }

    @Nested
    @DisplayName("银行卡号脱敏测试")
    class BankCardTest {

        @Test
        @DisplayName("测试正常银行卡号脱敏")
        void testBankCardNormal() {
            String card = "6222601234567890123";
            String result = SensitiveUtils.bankCard(card);
            assertEquals("622260*********0123", result);
        }

        @Test
        @DisplayName("测试null银行卡号脱敏")
        void testBankCardNull() {
            String result = SensitiveUtils.bankCard(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短银行卡号脱敏")
        void testBankCardShort() {
            String card = "123456";
            String result = SensitiveUtils.bankCard(card);
            assertEquals("123456", result);
        }
    }

    @Nested
    @DisplayName("密码脱敏测试")
    class PasswordTest {

        @Test
        @DisplayName("测试正常密码脱敏")
        void testPasswordNormal() {
            String password = "password123";
            String result = SensitiveUtils.password(password);
            assertEquals("******", result);
        }

        @Test
        @DisplayName("测试null密码脱敏")
        void testPasswordNull() {
            String result = SensitiveUtils.password(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试空密码脱敏")
        void testPasswordEmpty() {
            String password = "";
            String result = SensitiveUtils.password(password);
            assertEquals("******", result);
        }
    }

    @Nested
    @DisplayName("密钥脱敏测试")
    class KeyTest {

        @Test
        @DisplayName("测试正常密钥脱敏")
        void testKeyNormal() {
            String key = "abcdefg123456";
            String result = SensitiveUtils.key(key);
            assertEquals("***456", result);
        }

        @Test
        @DisplayName("测试null密钥脱敏")
        void testKeyNull() {
            String result = SensitiveUtils.key(null);
            assertNull(result);
        }

        @Test
        @DisplayName("测试短密钥脱敏")
        void testKeyShort() {
            String key = "ab";
            String result = SensitiveUtils.key(key);
            assertEquals("****ab", result);
        }

        @Test
        @DisplayName("测试长度刚好6位的密钥脱敏")
        void testKeyLength6() {
            String key = "123456";
            String result = SensitiveUtils.key(key);
            assertEquals("***456", result);
        }
    }
}