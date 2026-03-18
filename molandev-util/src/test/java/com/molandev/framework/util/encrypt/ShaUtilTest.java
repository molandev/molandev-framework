package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SHA工具类测试")
class ShaUtilTest {

    private static final String TEST_STRING = "Hello World";
    private static final String TEST_STRING_SHA1 = "0a4d55a8d778e5022fab701977c5d840bbc486d0";
    private static final String TEST_STRING_SHA256 = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e";
    private static final String TEST_STRING_SHA512 = "2c74fd17edafd80e8447b0d46741ee243b7eb74dd2149a0ab1b9246fb30382f27e853d8585719e0e67cbda0daa8f51671064615d645ae27acb15bfb1447f459b";

    @Nested
    @DisplayName("SHA1测试")
    class Sha1Test {

        @Test
        @DisplayName("测试SHA1加密")
        void testSha1() {
            String sha1 = ShaUtil.sha1(TEST_STRING);
            assertEquals(TEST_STRING_SHA1, sha1, "SHA1加密结果应正确");
        }

        @Test
        @DisplayName("测试空字符串SHA1")
        void testSha1EmptyString() {
            String sha1 = ShaUtil.sha1("");
            assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", sha1, "空字符串SHA1加密结果应正确");
        }
    }

    @Nested
    @DisplayName("SHA256测试")
    class Sha256Test {

        @Test
        @DisplayName("测试SHA256加密")
        void testSha256() {
            String sha256 = ShaUtil.sha256(TEST_STRING);
            assertEquals(TEST_STRING_SHA256, sha256, "SHA256加密结果应正确");
        }

        @Test
        @DisplayName("测试空字符串SHA256")
        void testSha256EmptyString() {
            String sha256 = ShaUtil.sha256("");
            assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256, "空字符串SHA256加密结果应正确");
        }
    }

    @Nested
    @DisplayName("SHA512测试")
    class Sha512Test {

        @Test
        @DisplayName("测试SHA512加密")
        void testSha512() {
            String sha512 = ShaUtil.sha512(TEST_STRING);
            assertEquals(TEST_STRING_SHA512, sha512, "SHA512加密结果应正确");
        }

        @Test
        @DisplayName("测试空字符串SHA512")
        void testSha512EmptyString() {
            String sha512 = ShaUtil.sha512("");
            assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", sha512, "空字符串SHA512加密结果应正确");
        }
    }

    @Nested
    @DisplayName("通用加密方法测试")
    class EncryptMethodTest {

        @Test
        @DisplayName("测试通用加密方法")
        void testEncrypt() {
            String sha256 = ShaUtil.encrypt(TEST_STRING, ShaUtil.SHA_256);
            assertEquals(TEST_STRING_SHA256, sha256, "通用加密方法结果应正确");
        }

        @Test
        @DisplayName("测试空内容加密")
        void testEncryptEmptyContent() {
            String sha256 = ShaUtil.encrypt("", ShaUtil.SHA_256);
            assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256, "空内容加密结果应正确");
        }

        @Test
        @DisplayName("测试null内容加密")
        void testEncryptNullContent() {
            assertThrows(IllegalArgumentException.class, () -> {
                ShaUtil.encrypt(null, ShaUtil.SHA_256);
            }, "null内容加密应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("测试null类型加密")
        void testEncryptWithNullType() {
            assertThrows(RuntimeException.class, () -> {
                ShaUtil.encrypt(TEST_STRING, null);
            }, "null类型应抛出RuntimeException");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("测试无效算法类型")
        void testInvalidAlgorithmType() {
            assertThrows(RuntimeException.class, () -> {
                ShaUtil.encrypt(TEST_STRING, "invalid_algorithm");
            }, "使用无效算法类型应抛出RuntimeException");
        }
    }

    @Nested
    @DisplayName("文件加密测试")
    class FileEncryptTest {

        @Test
        @DisplayName("测试文件SHA256加密")
        void testEncryptFile() throws IOException {
            // 创建临时文件
            Path tempFile = Files.createTempFile("sha_test", ".txt");
            Files.write(tempFile, TEST_STRING.getBytes());

            String fileSha256 = ShaUtil.encryptFile(tempFile.toFile(), ShaUtil.SHA_256);
            String stringSha256 = ShaUtil.sha256(TEST_STRING);

            assertEquals(stringSha256, fileSha256, "文件加密结果应与字符串加密结果一致");

            // 清理临时文件
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("测试空文件SHA256加密")
        void testEncryptEmptyFile() throws IOException {
            // 创建空临时文件
            Path tempFile = Files.createTempFile("sha_empty_test", ".txt");

            String fileSha256 = ShaUtil.encryptFile(tempFile.toFile(), ShaUtil.SHA_256);
            String stringSha256 = ShaUtil.sha256("");

            assertEquals(stringSha256, fileSha256, "空文件加密结果应与空字符串加密结果一致");

            // 清理临时文件
            Files.deleteIfExists(tempFile);
        }

        @Test
        @DisplayName("测试null文件加密")
        void testEncryptNullFile() {
            assertThrows(IllegalArgumentException.class, () -> {
                ShaUtil.encryptFile(null, ShaUtil.SHA_256);
            }, "null文件加密应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("测试不存在文件加密")
        void testEncryptNonExistentFile() {
            File nonExistentFile = new File("non_existent_file.txt");
            assertThrows(IllegalArgumentException.class, () -> {
                ShaUtil.encryptFile(nonExistentFile, ShaUtil.SHA_256);
            }, "不存在文件加密应抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("测试无效算法文件加密")
        void testEncryptFileWithInvalidAlgorithm() throws IOException {
            // 创建临时文件
            Path tempFile = Files.createTempFile("sha_invalid_test", ".txt");
            Files.write(tempFile, TEST_STRING.getBytes());

            assertThrows(RuntimeException.class, () -> {
                ShaUtil.encryptFile(tempFile.toFile(), "invalid_algorithm");
            }, "使用无效算法类型加密文件应抛出RuntimeException");

            // 清理临时文件
            Files.deleteIfExists(tempFile);
        }
    }
}