package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MD5工具类测试")
class Md5UtilsTest {

    private static final String TEST_STRING = "Hello World";
    private static final String TEST_STRING_MD5 = "b10a8db164e0754105b7a99be72e3fe5"; // "Hello World"的MD5值

    @Nested
    @DisplayName("字符串MD5测试")
    class StringMd5Test {

        @Test
        @DisplayName("测试默认字符集MD5")
        void testMd5DefaultCharset() {
            String md5 = Md5Utils.md5(TEST_STRING);
            assertEquals(TEST_STRING_MD5, md5, "字符串MD5计算应正确");
        }

        @Test
        @DisplayName("测试指定字符集MD5")
        void testMd5WithCharset() {
            String md5 = Md5Utils.md5(TEST_STRING, "UTF-8");
            assertEquals(TEST_STRING_MD5, md5, "指定UTF-8字符集MD5计算应正确");
        }

        @Test
        @DisplayName("测试空字符串MD5")
        void testMd5EmptyString() {
            String md5 = Md5Utils.md5("");
            assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5, "空字符串MD5计算应正确");
        }

        @Test
        @DisplayName("测试特殊字符MD5")
        void testMd5SpecialChars() {
            String specialString = "!@#$%^&*()";
            String md5 = Md5Utils.md5(specialString);
            assertEquals("05b28d17a7b6e7024b6e5d8cc43a8bf7", md5, "特殊字符MD5计算应正确");
        }
    }

    @Nested
    @DisplayName("字节数组MD5测试")
    class BytesMd5Test {

        @Test
        @DisplayName("测试字节数组MD5")
        void testMd5Bytes() {
            byte[] bytes = TEST_STRING.getBytes(StandardCharsets.UTF_8);
            String md5 = Md5Utils.md5(bytes);
            assertEquals(TEST_STRING_MD5, md5, "字节数组MD5计算应正确");
        }

        @Test
        @DisplayName("测试空字节数组MD5")
        void testMd5EmptyBytes() {
            byte[] bytes = new byte[0];
            String md5 = Md5Utils.md5(bytes);
            assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5, "空字节数组MD5计算应正确");
        }
    }

    @Nested
    @DisplayName("文件MD5测试")
    class FileMd5Test {

        @Test
        @DisplayName("测试文件MD5")
        void testMd5File() throws IOException {
            // 创建临时文件
            File tempFile = File.createTempFile("md5_test", ".txt");
            tempFile.deleteOnExit();

            // 写入测试内容
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(TEST_STRING.getBytes(StandardCharsets.UTF_8));
            }

            String md5 = Md5Utils.md5(tempFile);
            assertEquals(TEST_STRING_MD5, md5, "文件MD5计算应正确");
        }

        @Test
        @DisplayName("测试空文件MD5")
        void testMd5EmptyFile() throws IOException {
            // 创建空临时文件
            File tempFile = File.createTempFile("md5_test_empty", ".txt");
            tempFile.deleteOnExit();

            String md5 = Md5Utils.md5(tempFile);
            assertEquals("d41d8cd98f00b204e9800998ecf8427e", md5, "空文件MD5计算应正确");
        }
    }

    @Nested
    @DisplayName("十六进制转换测试")
    class ToHexTest {

        @Test
        @DisplayName("测试字节数组转十六进制")
        void testToHex() {
            byte[] bytes = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
            String hex = Md5Utils.toHex(bytes);
            assertEquals("deadbeef", hex, "字节数组转十六进制应正确");
        }

        @Test
        @DisplayName("测试空字节数组转十六进制")
        void testToHexEmptyBytes() {
            byte[] bytes = new byte[0];
            String hex = Md5Utils.toHex(bytes);
            assertEquals("", hex, "空字节数组转十六进制应为空字符串");
        }
    }
}