package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base64工具类测试")
class Base64UtilsTest {

    private static final String TEST_STRING = "Hello World";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("编码测试")
    class EncodeTest {

        @Test
        @DisplayName("测试正常字符串编码")
        void testEncode() {
            String encoded = Base64Utils.encode(TEST_BYTES);
            assertNotNull(encoded, "编码结果不应为null");
            assertEquals("SGVsbG8gV29ybGQ=", encoded, "Base64编码结果应正确");
        }

        @Test
        @DisplayName("测试空字节数组编码")
        void testEncodeEmptyBytes() {
            String encoded = Base64Utils.encode(new byte[0]);
            assertEquals("", encoded, "空字节数组编码应为空字符串");
        }

        @Test
        @DisplayName("测试特殊字符编码")
        void testEncodeSpecialChars() {
            byte[] specialBytes = "!@#$%^&*()".getBytes(StandardCharsets.UTF_8);
            String encoded = Base64Utils.encode(specialBytes);
            assertEquals("IUAjJCVeJiooKQ==", encoded, "特殊字符编码应正确");
        }
    }

    @Nested
    @DisplayName("解码测试")
    class DecodeTest {

        @Test
        @DisplayName("测试正常字符串解码")
        void testDecode() {
            byte[] decoded = Base64Utils.decode("SGVsbG8gV29ybGQ=");
            assertNotNull(decoded, "解码结果不应为null");
            assertArrayEquals(TEST_BYTES, decoded, "解码结果应与原字节数组一致");
        }

        @Test
        @DisplayName("测试空字符串解码")
        void testDecodeEmptyString() {
            byte[] decoded = Base64Utils.decode("");
            assertArrayEquals(new byte[0], decoded, "空字符串解码应得到空字节数组");
        }

        @Test
        @DisplayName("测试特殊字符解码")
        void testDecodeSpecialChars() {
            byte[] decoded = Base64Utils.decode("IUAjJCVeJiooKQ==");
            byte[] expected = "!@#$%^&*()".getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expected, decoded, "特殊字符解码应正确");
        }

        @Test
        @DisplayName("测试无效Base64字符串解码")
        void testDecodeInvalidBase64() {
            assertThrows(IllegalArgumentException.class, () -> {
                Base64Utils.decode("InvalidBase64!!");
            }, "解码无效Base64字符串应抛出异常");
        }
    }

    @Nested
    @DisplayName("编解码一致性测试")
    class EncodeDecodeConsistencyTest {

        @Test
        @DisplayName("测试编解码一致性")
        void testEncodeDecodeConsistency() {
            String original = "Test String for Encode/Decode";
            byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);

            String encoded = Base64Utils.encode(originalBytes);
            byte[] decoded = Base64Utils.decode(encoded);

            assertArrayEquals(originalBytes, decoded, "编解码后应保持数据一致性");
        }
    }
}