package com.molandev.framework.encrypt.params;

import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.encrypt.AesUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {EncryptedParamsTest.TestConfig.class, EncryptedParamsTest.TestController.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "molandev.encrypt.params.enabled=true",
        "molandev.encrypt.params.key=test1234567890ab",
        "molandev.encrypt.params.type=AES"
})
@DisplayName("@EncryptedParam注解测试")
class EncryptedParamsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    EncryptProperties encryptProperties;

    @SpringBootApplication
    @ComponentScan(basePackages = {"com.molandev.framework.encrypt.params"})
    @Import({ EncryptedParamsConfiguration.class})
    static class TestConfig {

    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/decrypt-param")
        public String EncryptedParam(@EncryptedParam("data") String data) {
            return data;
        }

        // 测试参数名自动推断（不指定value）
        @GetMapping("/decrypt-param-auto")
        public String decryptParamAuto(@EncryptedParam String username) {
            return "auto:" + username;
        }

        // 测试混合使用
        @GetMapping("/decrypt-param-mixed")
        public String decryptParamMixed(
                @EncryptedParam String password,  // 自动推断为 password
                @EncryptedParam("encryptedEmail") String email  // 显式指定为 encryptedEmail
        ) {
            return "password:" + password + ",email:" + email;
        }

    }

    @Nested
    @DisplayName("参数解密测试")
    class EncryptedParamTests {

        @Test
        @DisplayName("测试使用默认密钥解密参数")
        void testEncryptedParamWithDefaultKey() throws Exception {
            // 使用默认密钥加密测试数据
            String encryptedData = AesUtil.encrypt("Hello World", encryptProperties.getParams().getKey());

            mockMvc.perform(get("/test/decrypt-param")
                            .param("data", encryptedData)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello World"));
        }

        @Test
        @DisplayName("测试解密空参数")
        void testDecryptEmptyParam() throws Exception {
            mockMvc.perform(get("/test/decrypt-param")
                            .param("data", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("测试解密null参数")
        void testDecryptNullParam() throws Exception {
            mockMvc.perform(get("/test/decrypt-param")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("测试参数名自动推断")
        void testAutoInferredParamName() throws Exception {
            // 使用默认密钥加密测试数据
            String encryptedUsername = AesUtil.encrypt("testuser", encryptProperties.getParams().getKey());

            mockMvc.perform(get("/test/decrypt-param-auto")
                            .param("username", encryptedUsername)  // 参数名自动匹配方法参数名
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().string("auto:testuser"));
        }

        @Test
        @DisplayName("测试混合使用：自动推断+显式指定")
        void testMixedParamNames() throws Exception {
            // 加密参数
            String encryptedPassword = AesUtil.encrypt("secret123", encryptProperties.getParams().getKey());
            String encryptedEmail = AesUtil.encrypt("test@example.com", encryptProperties.getParams().getKey());

            mockMvc.perform(get("/test/decrypt-param-mixed")
                            .param("password", encryptedPassword)  // 自动推断
                            .param("encryptedEmail", encryptedEmail)  // 显式指定
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(content().string("password:secret123,email:test@example.com"));
        }
    }
}