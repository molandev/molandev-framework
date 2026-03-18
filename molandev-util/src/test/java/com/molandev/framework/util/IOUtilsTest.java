package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IO工具类测试")
class IOUtilsTest {

    @Nested
    @DisplayName("读取操作测试")
    class ReadOperationTest {

        @Test
        @DisplayName("readToString默认编码方法测试")
        void readToString() {
            // 测试正常读取
            String content = "Hello, World!";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            assertEquals(content, IOUtils.readToString(inputStream));

            // 测试空流
            InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
            assertEquals("", IOUtils.readToString(emptyStream));

            // 测试null输入
            assertThrows(NullPointerException.class, () -> IOUtils.readToString(null));
        }

        @Test
        @DisplayName("readToString指定编码方法测试")
        void readToStringWithCharset() {
            // 测试指定字符集读取
            String content = "你好，世界!";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            assertEquals(content, IOUtils.readToString(inputStream, "UTF-8"));

            // 测试不支持的字符集
            InputStream stream = new ByteArrayInputStream("test".getBytes());
            assertThrows(RuntimeException.class, () -> IOUtils.readToString(stream, "invalid-charset"));
        }

        @Test
        @DisplayName("readToBytes方法测试")
        void readToBytes() {
            // 测试正常读取
            byte[] data = {1, 2, 3, 4, 5};
            InputStream inputStream = new ByteArrayInputStream(data);
            assertArrayEquals(data, IOUtils.readToBytes(inputStream));

            // 测试空流
            InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
            assertArrayEquals(new byte[0], IOUtils.readToBytes(emptyStream));

            // 测试null输入
            assertThrows(NullPointerException.class, () -> IOUtils.readToBytes(null));
        }
    }

    @Nested
    @DisplayName("写入操作测试")
    class WriteOperationTest {

        @Test
        @DisplayName("writeToStream字符串方法测试")
        void writeToStream() {
            // 测试正常写入
            String content = "Hello, World!";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.writeToStream(content, outputStream);
            assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), outputStream.toByteArray());

            // 测试空字符串
            ByteArrayOutputStream emptyOutputStream = new ByteArrayOutputStream();
            IOUtils.writeToStream("", emptyOutputStream);
            assertArrayEquals(new byte[0], emptyOutputStream.toByteArray());
        }

        @Test
        @DisplayName("writeToStream指定编码方法测试")
        void writeToStreamWithCharset() {
            // 测试指定字符集写入
            String content = "你好，世界!";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.writeToStream(content, outputStream, "UTF-8");
            assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), outputStream.toByteArray());

            // 测试不支持的字符集
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertThrows(RuntimeException.class, () -> IOUtils.writeToStream("test", baos, "invalid-charset"));
        }

        @Test
        @DisplayName("writeToStream字节数组方法测试")
        void writeToStreamWithBytes() {
            // 测试字节数组写入
            byte[] data = {1, 2, 3, 4, 5};
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.writeToStream(data, outputStream);
            assertArrayEquals(data, outputStream.toByteArray());

            // 测试空数组
            byte[] emptyData = new byte[0];
            ByteArrayOutputStream emptyOutputStream = new ByteArrayOutputStream();
            IOUtils.writeToStream(emptyData, emptyOutputStream);
            assertArrayEquals(emptyData, emptyOutputStream.toByteArray());
        }
    }

    @Nested
    @DisplayName("读写操作测试")
    class ReadWriteOperationTest {

        @Test
        @DisplayName("readAndWrite方法测试")
        void readAndWrite() {
            // 测试读写操作
            String content = "Hello, World!";
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            IOUtils.readAndWrite(inputStream, outputStream);
            assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), outputStream.toByteArray());

            // 测试空流读写
            InputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
            ByteArrayOutputStream emptyOutputStream = new ByteArrayOutputStream();
            IOUtils.readAndWrite(emptyInputStream, emptyOutputStream);
            assertArrayEquals(new byte[0], emptyOutputStream.toByteArray());
        }

        @Test
        @DisplayName("toInputStream方法测试")
        void toInputStream() {
            // 测试字符串转输入流
            String content = "Hello, World!";
            InputStream inputStream = IOUtils.toInputStream(content, "UTF-8");
            assertNotNull(inputStream);
            assertEquals(content, IOUtils.readToString(inputStream));

            // 测试不支持的字符集
            assertThrows(RuntimeException.class, () -> IOUtils.toInputStream("test", "invalid-charset"));
        }
    }

    @Nested
    @DisplayName("资源关闭测试")
    class ResourceCloseTest {

        @Test
        @DisplayName("closeQuietly方法测试")
        void closeQuietly() {
            // 测试正常关闭
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> IOUtils.closeQuietly(outputStream));

            // 测试关闭null
            assertDoesNotThrow(() -> IOUtils.closeQuietly(null));

            // 测试关闭抛出异常的流
            Closeable closeable = () -> {
                throw new IOException("Test exception");
            };
            assertDoesNotThrow(() -> IOUtils.closeQuietly(closeable));
        }
    }

    @Nested
    @DisplayName("类路径资源测试")
    class ClasspathResourceTest {

        @Test
        @DisplayName("readFromClassPath方法测试")
        void readFromClassPath() {
            // 创建一个测试资源文件
            String testContent = "This is a test file content.";
            InputStream testStream = new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8));

            // 由于我们无法直接模拟类路径资源加载，这里仅测试异常情况
            assertThrows(RuntimeException.class, () -> IOUtils.readFromClassPath("non-existent-file.txt"));
        }
    }
}