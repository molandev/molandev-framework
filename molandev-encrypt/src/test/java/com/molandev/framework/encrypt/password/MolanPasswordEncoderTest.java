package com.molandev.framework.encrypt.password;

import com.molandev.framework.encrypt.common.EncryptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("密码编码器测试")
class MolanPasswordEncoderTest {

    private MolanPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new MolanPasswordEncoder("12345657");
    }

    @Nested
    @DisplayName("密码编码测试")
    class EncodeTest {

        @Test
        @DisplayName("测试正常密码编码")
        void testEncodeNormalPassword() {
            String password = "password123";
            String encoded = passwordEncoder.encode(password);
            
            assertNotNull(encoded, "编码结果不应为null");
            assertEquals(80, encoded.length(), "编码后的密码长度应为80位");
            
            // 验证前16位是DES加密的盐值
            String saltPart = encoded.substring(0, 16);
            // 验证后64位是SHA256加密结果
            String shaPart = encoded.substring(16);
            assertEquals(64, shaPart.length(), "SHA256部分长度应为64位");
        }

        @Test
        @DisplayName("测试空密码编码")
        void testEncodeEmptyPassword() {
            String password = "";
            String encoded = passwordEncoder.encode(password);
            
            assertNotNull(encoded, "编码结果不应为null");
            assertEquals(80, encoded.length(), "编码后的密码长度应为80位");
        }

        @Test
        @DisplayName("测试特殊字符密码编码")
        void testEncodeSpecialCharPassword() {
            String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
            String encoded = passwordEncoder.encode(password);
            
            assertNotNull(encoded, "编码结果不应为null");
            assertEquals(80, encoded.length(), "编码后的密码长度应为80位");
        }
    }

    @Nested
    @DisplayName("密码匹配测试")
    class NotMatchTest {

        @Test
        @DisplayName("测试密码匹配")
        void testPasswordMatch() {
            String password = "password123";
            String encoded = passwordEncoder.encode(password);
            
            assertFalse(passwordEncoder.notMatch(password, encoded), "密码应该匹配");
        }

        @Test
        @DisplayName("测试密码不匹配")
        void testPasswordNotMatch() {
            String password = "password123";
            String wrongPassword = "wrongpassword";
            String encoded = passwordEncoder.encode(password);
            
            assertTrue(passwordEncoder.notMatch(wrongPassword, encoded), "密码应该不匹配");
        }

        @Test
        @DisplayName("测试空密码匹配")
        void testEmptyPasswordMatch() {
            String password = "";
            String encoded = passwordEncoder.encode(password);
            
            assertFalse(passwordEncoder.notMatch(password, encoded), "空密码应该匹配");
        }

        @Test
        @DisplayName("测试null密码")
        void testNullPassword() {
            String password = "password123";
            String encoded = passwordEncoder.encode(password);
            
            assertTrue(passwordEncoder.notMatch(password, null), "null密码应该不匹配");
        }

        @Test
        @DisplayName("测试错误长度密码")
        void testWrongLengthPassword() {
            String password = "password123";
            String wrongEncoded = "short";
            
            assertTrue(passwordEncoder.notMatch(password, wrongEncoded), "长度错误的密码应该不匹配");
        }

        @Test
        @DisplayName("测试异常情况处理")
        void testExceptionHandling() {
            String password = "password123";
            String encoded = passwordEncoder.encode(password);
            
            // 修改编码后的密码使其无法解密
            String brokenEncoded = "invalid" + encoded.substring(6);
            
            assertTrue(passwordEncoder.notMatch(password, brokenEncoded), 
                "解密异常时应该返回不匹配");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTest {

        @Test
        @DisplayName("测试多次编码结果不同")
        void testMultipleEncodeResults() {
            String password = "password123";
            String encoded1 = passwordEncoder.encode(password);
            String encoded2 = passwordEncoder.encode(password);
            
            assertNotEquals(encoded1, encoded2, "多次编码同一密码结果应该不同");
            assertFalse(passwordEncoder.notMatch(password, encoded1), "第一次编码应该匹配");
            assertFalse(passwordEncoder.notMatch(password, encoded2), "第二次编码应该匹配");
        }
    }
}