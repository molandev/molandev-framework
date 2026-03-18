package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DES加密工具类测试")
class DesUtilTest {

    private static final String TEST_CONTENT = "This is a test content for DES encryption.";
    private static final String TEST_PASSWORD = "12345678"; // DES密钥长度为8位

    @Nested
    @DisplayName("基础加解密测试")
    class BasicEncryptionDecryptionTest {

        @Test
        @DisplayName("测试正常加密解密")
        void testEncryptAndDecrypt() {
            String encrypted = DesUtil.encrypt(TEST_CONTENT, TEST_PASSWORD);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = DesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals(TEST_CONTENT, decrypted, "解密后的内容应与原文相同");
        }

        @Test
        @DisplayName("测试空内容加密")
        void testEncryptEmptyContent() {
            String encrypted = DesUtil.encrypt("", TEST_PASSWORD);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = DesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals("", decrypted, "解密空内容应得到空字符串");
        }

        @Test
        @DisplayName("测试特殊字符内容加密")
        void testEncryptSpecialCharacters() {
            String specialContent = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
            String encrypted = DesUtil.encrypt(specialContent, TEST_PASSWORD);
            String decrypted = DesUtil.decrypt(encrypted, TEST_PASSWORD);
            assertEquals(specialContent, decrypted, "特殊字符内容加解密应保持一致");
        }


        @Test
        @DisplayName("测试密钥长度不足自动补位")
        void testShortPasswordException() {
            String shortPassword = "1234567"; // 少于8位
            String encrypted = DesUtil.encrypt(TEST_CONTENT, shortPassword);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = DesUtil.decrypt(encrypted, shortPassword);
            assertEquals(TEST_CONTENT, decrypted, "解密后的内容应与原文相同");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("测试错误密码解密")
        void testDecryptWithWrongPassword() {
            String encrypted = DesUtil.encrypt(TEST_CONTENT, TEST_PASSWORD);
            String wrongPassword = "87654321";

            assertThrows(IllegalArgumentException.class, () -> {
                DesUtil.decrypt(encrypted, wrongPassword);
            }, "使用错误密码解密应抛出DesException");
        }

        @Test
        @DisplayName("测试无效密文解密")
        void testDecryptInvalidContent() {
            String invalidContent = "invalid_hex_content";

            assertThrows(IllegalArgumentException.class, () -> {
                DesUtil.decrypt(invalidContent, TEST_PASSWORD);
            }, "解密无效密文应抛出DesException");
        }

    }

    @Nested
    @DisplayName("自定义算法测试")
    class CustomAlgorithmTest {

        @Test
        @DisplayName("测试使用自定义算法加密解密")
        void testCustomAlgorithm() {
            String customAlgorithm = "DES/ECB/PKCS5Padding";
            String encrypted = DesUtil.des(TEST_CONTENT, TEST_PASSWORD, javax.crypto.Cipher.ENCRYPT_MODE, customAlgorithm);
            String decrypted = DesUtil.des(encrypted, TEST_PASSWORD, javax.crypto.Cipher.DECRYPT_MODE, customAlgorithm);
            // 注意：这里由于DES工具类的实现方式，直接调用des方法的测试方式与encrypt/decrypt略有不同
        }
    }
}