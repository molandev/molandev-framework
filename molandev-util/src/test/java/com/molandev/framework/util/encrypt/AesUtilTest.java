package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AES加密工具类测试")
class AesUtilTest {

    private static final String TEST_CONTENT = "This is a test content for AES encryption.";
    private static final String TEST_PASSWORD = "1234567890123456"; // 16位密码

    @Nested
    @DisplayName("基础加解密测试")
    class BasicEncryptionDecryptionTest {

        @Test
        @DisplayName("测试正常加密解密")
        void testEncryptAndDecrypt() {
            String encrypted = AesUtil.encrypt(TEST_CONTENT, TEST_PASSWORD);
            assertNotNull(encrypted, "加密结果不应为null");
            assertNotEquals(TEST_CONTENT, encrypted, "加密后的内容应与原文不同");

            String decrypted = AesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals(TEST_CONTENT, decrypted, "解密后的内容应与原文相同");
        }

        @Test
        @DisplayName("测试空内容加密")
        void testEncryptEmptyContent() {
            String encrypted = AesUtil.encrypt("", TEST_PASSWORD);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = AesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals("", decrypted, "解密空内容应得到空字符串");
        }

        @Test
        @DisplayName("测试特殊字符内容加密")
        void testEncryptSpecialCharacters() {
            String specialContent = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
            String encrypted = AesUtil.encrypt(specialContent, TEST_PASSWORD);
            String decrypted = AesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals(specialContent, decrypted, "特殊字符内容加解密应保持一致");
        }
    }

    @Nested
    @DisplayName("密码处理测试")
    class PasswordHandlingTest {

        @Test
        @DisplayName("测试密码长度不足16位时的处理")
        void testPasswordPadding() {
            String shortPassword = "123456"; // 少于16位
            String fixedPassword = AesUtil.fixPassword16(shortPassword);
            assertEquals(16, fixedPassword.length(), "修复后的密码长度应为16位");
            assertTrue(fixedPassword.startsWith(shortPassword), "修复后的密码应以原密码开头");
        }

        @Test
        @DisplayName("测试16位密码不需要填充")
        void testExact16CharPassword() {
            String exactPassword = "1234567890123456"; // 正好16位
            String fixedPassword = AesUtil.fixPassword16(exactPassword);
            assertEquals(exactPassword, fixedPassword, "16位密码不应被修改");
        }

        @Test
        @DisplayName("测试空密码异常")
        void testEmptyPasswordException() {
            assertThrows(IllegalArgumentException.class, () -> {
                AesUtil.fixPassword16(null);
            }, "空密码应抛出IllegalArgumentException");

            assertThrows(IllegalArgumentException.class, () -> {
                AesUtil.fixPassword16("");
            }, "空字符串密码应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("测试密码过长异常")
        void testLongPasswordException() {
            String longPassword = "12345678901234567"; // 超过16位
            assertThrows(IllegalArgumentException.class, () -> {
                AesUtil.fixPassword16(longPassword);
            }, "超过16位的密码应抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("测试错误密码解密")
        void testDecryptWithWrongPassword() {
            String encrypted = AesUtil.encrypt(TEST_CONTENT, TEST_PASSWORD);
            String wrongPassword = "wrongpassword123";

            assertThrows(IllegalArgumentException.class, () -> {
                AesUtil.decrypt(encrypted, wrongPassword);
            }, "使用错误密码解密应抛出AesException");
        }

        @Test
        @DisplayName("测试无效密文解密")
        void testDecryptInvalidContent() {
            String invalidContent = "invalid_base64_content";

            assertThrows(IllegalArgumentException.class, () -> {
                AesUtil.decrypt(invalidContent, TEST_PASSWORD);
            }, "解密无效密文应抛出AesException");
        }

        @Test
        @DisplayName("测试getKey方法")
        void testGetKey() {
            String password = "1234567890123456";
            javax.crypto.spec.SecretKeySpec key = AesUtil.getKey(password);
            assertNotNull(key, "获取的密钥不应为null");
            assertEquals("AES", key.getAlgorithm(), "密钥算法应为AES");
        }
    }

    @Nested
    @DisplayName("自定义算法测试")
    class CustomAlgorithmTest {

        @Test
        @DisplayName("测试使用自定义算法加密解密")
        void testCustomAlgorithm() {
            String customAlgorithm = "AES/ECB/PKCS5Padding";
            String encrypted = AesUtil.encrypt(TEST_CONTENT, TEST_PASSWORD, customAlgorithm);
            String decrypted = AesUtil.decrypt(encrypted, TEST_PASSWORD, customAlgorithm);
            assertEquals(TEST_CONTENT, decrypted, "使用自定义算法加解密应保持内容一致");
        }
    }
}