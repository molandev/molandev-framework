package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RSA工具类测试")
class RsaUtilTest {

    private static final String TEST_STRING = "This is a test string for RSA encryption.";
    private static String[] keyPair;
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    @BeforeAll
    static void setUp() {
        // 创建密钥对
        keyPair = RsaUtil.createKeys(1024);
        publicKey = RsaUtil.getPublicKey(keyPair[0]);
        privateKey = RsaUtil.getPrivateKey(keyPair[1]);
    }

    @Nested
    @DisplayName("密钥生成测试")
    class KeyGenerationTest {

        @Test
        @DisplayName("测试密钥对生成")
        void testCreateKeys() {
            String[] keys = RsaUtil.createKeys(1024);
            assertNotNull(keys, "生成的密钥对不应为null");
            assertEquals(2, keys.length, "密钥对应包含公钥和私钥");
            assertNotNull(keys[0], "公钥不应为null");
            assertNotNull(keys[1], "私钥不应为null");
        }

        @Test
        @DisplayName("测试获取公钥")
        void testGetPublicKey() {
            RSAPublicKey pubKey = RsaUtil.getPublicKey(keyPair[0]);
            assertNotNull(pubKey, "获取的公钥不应为null");
            assertEquals("RSA", pubKey.getAlgorithm(), "公钥算法应为RSA");
        }

        @Test
        @DisplayName("测试获取私钥")
        void testGetPrivateKey() {
            RSAPrivateKey privKey = RsaUtil.getPrivateKey(keyPair[1]);
            assertNotNull(privKey, "获取的私钥不应为null");
            assertEquals("RSA", privKey.getAlgorithm(), "私钥算法应为RSA");
        }
    }

    @Nested
    @DisplayName("公钥加密私钥解密测试")
    class PublicEncryptPrivateDecryptTest {

        @Test
        @DisplayName("测试公钥加密私钥解密")
        void testPublicEncryptPrivateDecrypt() {
            String encrypted = RsaUtil.publicEncrypt(TEST_STRING, publicKey);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = RsaUtil.privateDecrypt(encrypted, privateKey);
            assertEquals(TEST_STRING, decrypted, "解密后的内容应与原文相同");
        }

        @Test
        @DisplayName("测试空字符串公钥加密")
        void testPublicEncryptEmptyString() {
            String encrypted = RsaUtil.publicEncrypt("", publicKey);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = RsaUtil.privateDecrypt(encrypted, privateKey);
            assertEquals("", decrypted, "解密空字符串应得到空字符串");
        }
    }

    @Nested
    @DisplayName("私钥加密公钥解密测试")
    class PrivateEncryptPublicDecryptTest {

        @Test
        @DisplayName("测试私钥加密公钥解密")
        void testPrivateEncryptPublicDecrypt() {
            String encrypted = RsaUtil.privateEncrypt(TEST_STRING, privateKey);
            assertNotNull(encrypted, "加密结果不应为null");

            String decrypted = RsaUtil.publicDecrypt(encrypted, publicKey);
            assertEquals(TEST_STRING, decrypted, "解密后的内容应与原文相同");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("测试无效公钥字符串")
        void testInvalidPublicKey() {
            assertThrows(RsaUtil.RsaException.class, () -> {
                RsaUtil.getPublicKey("invalid_public_key");
            }, "使用无效公钥字符串应抛出RsaException");
        }

        @Test
        @DisplayName("测试无效私钥字符串")
        void testInvalidPrivateKey() {
            assertThrows(RsaUtil.RsaException.class, () -> {
                RsaUtil.getPrivateKey("invalid_private_key");
            }, "使用无效私钥字符串应抛出RsaException");
        }

        @Test
        @DisplayName("测试用错误私钥解密")
        void testDecryptWithWrongPrivateKey() {
            String encrypted = RsaUtil.publicEncrypt(TEST_STRING, publicKey);

            // 生成另一对密钥
            String[] anotherKeyPair = RsaUtil.createKeys(1024);
            RSAPrivateKey anotherPrivateKey = RsaUtil.getPrivateKey(anotherKeyPair[1]);

            assertThrows(RuntimeException.class, () -> {
                RsaUtil.privateDecrypt(encrypted, anotherPrivateKey);
            }, "使用错误私钥解密应抛出异常");
        }
    }
}