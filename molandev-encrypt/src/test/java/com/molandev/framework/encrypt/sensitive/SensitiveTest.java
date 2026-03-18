package com.molandev.framework.encrypt.sensitive;

import com.molandev.framework.spring.json.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("@Sensitive注解测试")
class SensitiveTest {

    @Getter
    @Setter
    static class TestUser {
        private String name;

        @Sensitive(type = SensitiveTypes.CHINESE_NAME)
        private String chineseName;

        @Sensitive(type = SensitiveTypes.ID_CARD)
        private String idCard;

        @Sensitive(type = SensitiveTypes.FIXED_PHONE)
        private String fixedPhone;

        @Sensitive(type = SensitiveTypes.MOBILE_PHONE)
        private String mobilePhone;

        @Sensitive(type = SensitiveTypes.ADDRESS)
        private String address;

        @Sensitive(type = SensitiveTypes.EMAIL)
        private String email;

        @Sensitive(type = SensitiveTypes.BANK_CARD)
        private String bankCard;

        @Sensitive(type = SensitiveTypes.PASSWORD)
        private String password;

        @Sensitive(type = SensitiveTypes.KEY)
        private String key;

        @Sensitive(type = SensitiveTypes.CUSTOMER, preLength = 2, postLength = 3, maskStr = "#")
        private String customField;
    }

    @Nested
    @DisplayName("JSON序列化脱敏测试")
    class JsonSerializationTest {

        @Test
        @DisplayName("测试中文姓名脱敏")
        void testChineseNameSensitive() {
            TestUser user = new TestUser();
            user.setChineseName("张三丰");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"chineseName\":\"**丰\""));
        }

        @Test
        @DisplayName("测试身份证号脱敏")
        void testIdCardSensitive() {
            TestUser user = new TestUser();
            user.setIdCard("123456789012345678");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"idCard\":\"123456********5678\""));
        }

        @Test
        @DisplayName("测试固定电话脱敏")
        void testFixedPhoneSensitive() {
            TestUser user = new TestUser();
            user.setFixedPhone("010-12345678");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"fixedPhone\":\"********5678\""));
        }

        @Test
        @DisplayName("测试手机号码脱敏")
        void testMobilePhoneSensitive() {
            TestUser user = new TestUser();
            user.setMobilePhone("13812345678");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"mobilePhone\":\"138****5678\""));
        }

        @Test
        @DisplayName("测试地址脱敏")
        void testAddressSensitive() {
            TestUser user = new TestUser();
            user.setAddress("北京市海淀区中关村大街1号");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"address\":\"北京市海淀区*******\""));
        }

        @Test
        @DisplayName("测试电子邮箱脱敏")
        void testEmailSensitive() {
            TestUser user = new TestUser();
            user.setEmail("example@126.com");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"email\":\"e******@126.com\""));
        }

        @Test
        @DisplayName("测试银行卡号脱敏")
        void testBankCardSensitive() {
            TestUser user = new TestUser();
            user.setBankCard("6222601234567890123");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"bankCard\":\"622260*********0123\""));
        }

        @Test
        @DisplayName("测试密码脱敏")
        void testPasswordSensitive() {
            TestUser user = new TestUser();
            user.setPassword("password123");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"password\":\"******\""));
        }

        @Test
        @DisplayName("测试密钥脱敏")
        void testKeySensitive() {
            TestUser user = new TestUser();
            user.setKey("abcdefg123456");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"key\":\"***456\""));
        }

        @Test
        @DisplayName("测试自定义脱敏")
        void testCustomSensitive() {
            TestUser user = new TestUser();
            user.setCustomField("1234567890");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"customField\":\"12#####890\""));
        }

        @Test
        @DisplayName("测试null值处理")
        void testNullValueHandling() {
            TestUser user = new TestUser();
            // 不设置任何字段值

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"chineseName\":null"));
        }

        @Test
        @DisplayName("测试混合字段序列化")
        void testMixedFieldsSerialization() {
            TestUser user = new TestUser();
            user.setName("张三");
            user.setChineseName("张三丰");
            user.setMobilePhone("13812345678");
            user.setEmail("example@126.com");

            String json = JSONUtils.toJsonString(user);
            assertTrue(json.contains("\"name\":\"张三\""));
            assertTrue(json.contains("\"chineseName\":\"**丰\""));
            assertTrue(json.contains("\"mobilePhone\":\"138****5678\""));
            assertTrue(json.contains("\"email\":\"e******@126.com\""));
        }
    }
}