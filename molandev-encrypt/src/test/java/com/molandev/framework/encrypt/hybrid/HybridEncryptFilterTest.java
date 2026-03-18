package com.molandev.framework.encrypt.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.spring.json.JSONUtils;
import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.MapUtil;
import com.molandev.framework.util.encrypt.AesUtil;
import com.molandev.framework.util.encrypt.RsaUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {HybridEncryptFilterTest.TestConfig.class, HybridEncryptFilterTest.TestController.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "molandev.encrypt.hybrid.enabled=true",
        "molandev.encrypt.hybrid.public-key=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkIzC1o6Em6dt5RsmHn2Y/lMAvRMA1uwwOvyyDcIcWono9M0gYUKJ2vcKGs82rGV76eTMDdWISsawiIjS+1GL7i9Z3kfkzXJIZ/W/bDzewBjMxYftgyl6D2KJeljXP0Vb8Qxs7pCmTIWVkolA5Z/mGQZuYjNJ2bdUDDNm/dPqFxwIDAQAB",
        "molandev.encrypt.hybrid.private-key=MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKQjMLWjoSbp23lGyYefZj+UwC9EwDW7DA6/LINwhxaiej0zSBhQona9woazzasZXvp5MwN1YhKxrCIiNL7UYvuL1neR+TNckhn9b9sPN7AGMzFh+2DKXoPYol6WNc/RVvxDGzukKZMhZWSiUDln+YZBm5iM0nZt1QMM2b90+oXHAgMBAAECgYAIrrtsV3bEihd9tdkCLX77DO3m/OFD0s4v7KBOEXDLDSwfwcSXxZJppAmpekTFd+/GYbeT5Akq21KIuXwNd0rq1SI2dhR4JBZUgFr0vL4y2E6mZHIbUbNTWdf4s85G4PfWBjAee6NVuUK5++nw3HxEGZwvvCz3fojvz8KuELIU8QJBANzLoM1DvDYRPtnOfTeh5zMN4yD6ndZ506qXZDdXdfKHx2R6V9sQ6UNGTSIdfLe/FLawtbNk3GuXmTgwsiMkeAkCQQC+TulryiZl5n7VQ8RPcDQsNsmuS8NedbBhdJNI4Yy0vU/xpR1vN6BppyjdPRuzuwcoyFfACA7VZrttA1W3XmNPAkEAyPxPhypWhH0UUl5F28N0WyQUEewwi5DgXdRrtHjX9AsnNU6s9MwjGks/YYOoCPdWmqqm7AMvJn2cSusA7T6ASQJBAKHah1dvU/zybgh/XCSbryOzbQ/Zy804lswr01/2xC6rm6RO45vFWZ7B1lVjNX5EoLAkw8zEjakdeGYcofgURK0CQEvXdLU32K6OGHURtX6IhYK/vL1v1UYmUs8XqC8xeBM3xjvL8HBDkSOgjEqzMEWs2NUQPh6yjnJyyoK03TeZxkI=",
        "molandev.encrypt.hybrid.whitelist=/test/public/**"
})
@DisplayName("双层加密功能测试")
class HybridEncryptFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptProperties encryptProperties;

    @Autowired
    private ObjectMapper objectMapper;


    private static final String TEST_AES_KEY = "test1234567890ab";

    @BeforeAll
    static void setup() {
    }

    @SpringBootApplication
    @ComponentScan(basePackages = {"com.molandev.framework.encrypt.hybrid"})
    @Import({HybridEncryptAutoConfiguration.class})
    static class TestConfig {
        @Bean
        @SuppressWarnings("all")
        public FilterRegistrationBean exceptionFilterRegistration(EncryptProperties encryptProperties) {
            FilterRegistrationBean registration = new FilterRegistrationBean<>();
            registration.setFilter(new Filter() {
                @Override
                public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                    try {
                        filterChain.doFilter(servletRequest, servletResponse);
                    } catch (HybridEncryptException e) {
                        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                        httpResponse.setStatus(400);
                        servletResponse.setContentType("application/json;charset=utf-8");
                        servletResponse.getWriter().write(JSONUtils.toJsonString(MapUtil.toMap("code", 400, "message", e.getMessage())));
                    }
                }
            });
            registration.addUrlPatterns("/*");
            registration.setName("signFilter");
            registration.setOrder(Integer.MIN_VALUE);
            return registration;
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/secure")
        public Map<String, Object> secureEndpoint(@RequestBody Map<String, Object> requestData) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("receivedData", requestData);
            response.put("message", "数据处理成功");
            return response;
        }

        @GetMapping("/public/info")
        public Map<String, String> publicEndpoint() {
            Map<String, String> response = new HashMap<>();
            response.put("message", "这是公开接口");
            return response;
        }
    }

    @Nested
    @DisplayName("双层加密通信成功场景")
    class HybridEncryptSuccessTests {

        @Test
        @DisplayName("测试完整的加密-解密流程")
        void testFullEncryptDecryptFlow() throws Exception {
            // 1. 准备原始数据
            Map<String, Object> originalData = new HashMap<>();
            originalData.put("username", "testUser");
            originalData.put("action", "query");
            originalData.put("userId", 12345);

            String originalJsonData = objectMapper.writeValueAsString(originalData);

            // 2. 使用AES加密数据
            String encryptedData = AesUtil.encrypt(originalJsonData, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 3. 使用RSA公钥加密AES密钥
            String encryptedAesKey = RsaUtil.publicEncrypt(TEST_AES_KEY,
                    encryptProperties.getHybrid().getPublicKey());

            // 4. 构建加密请求体
            HybridEncryptedRequest encryptedRequest = new HybridEncryptedRequest();
            encryptedRequest.setData(encryptedData);
            encryptedRequest.setEncryptedKey(encryptedAesKey);

            String requestBody = objectMapper.writeValueAsString(encryptedRequest);

            // 5. 发送请求并验证响应
            MvcResult result = mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andReturn();

            // 6. 解密响应数据
            String encryptedResponse = result.getResponse().getContentAsString();
            assertNotNull(encryptedResponse);
            assertTrue(encryptedResponse.length() > 0, "响应数据不应为空");

            // 7. 使用相同的AES密钥解密响应
            String decryptedResponse = AesUtil.decrypt(encryptedResponse, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 8. 验证解密后的响应数据
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(decryptedResponse, Map.class);
            assertEquals("success", responseData.get("status"));
            assertNotNull(responseData.get("receivedData"));
        }

        @Test
        @DisplayName("测试白名单路径不进行加密处理")
        void testWhitelistedPath() throws Exception {
            mockMvc.perform(get("/test/public/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("这是公开接口"));
        }

        @Test
        @DisplayName("测试不同AES密钥的加密")
        void testDifferentAesKeys() throws Exception {
            // 使用不同的AES密钥
            String customAesKey = "custom9876543210";

            Map<String, Object> data = new HashMap<>();
            data.put("test", "data");

            String jsonData = objectMapper.writeValueAsString(data);
            String encryptedData = AesUtil.encrypt(jsonData, customAesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());

            String encryptedAesKey = RsaUtil.publicEncrypt(customAesKey,
                    encryptProperties.getHybrid().getPublicKey());

            HybridEncryptedRequest request = new HybridEncryptedRequest(encryptedData, encryptedAesKey);
            String requestBody = objectMapper.writeValueAsString(request);

            MvcResult result = mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andReturn();

            // 使用相同的自定义AES密钥解密响应
            String encryptedResponse = result.getResponse().getContentAsString();
            String decryptedResponse = AesUtil.decrypt(encryptedResponse, customAesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(decryptedResponse, Map.class);
            assertEquals("success", responseData.get("status"));
        }
    }

    @Nested
    @DisplayName("双层加密通信失败场景")
    class HybridEncryptFailureTests {

        @Test
        @DisplayName("测试请求体格式错误")
        void testInvalidRequestFormat() throws Exception {
            mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("测试缺少encryptedKey字段")
        void testMissingEncryptedKey() throws Exception {
            Map<String, String> invalidRequest = new HashMap<>();
            invalidRequest.put("data", "some_encrypted_data");
            // 缺少 encryptedKey 字段

            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("测试使用错误的RSA公钥加密AES密钥")
        void testWrongRsaKey() throws Exception {
            // 使用错误的RSA公钥
            String wrongPublicKey = RsaUtil.createKeys(1024)[0];

            Map<String, Object> data = new HashMap<>();
            data.put("test", "data");

            String jsonData = objectMapper.writeValueAsString(data);
            String encryptedData = AesUtil.encrypt(jsonData, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 使用错误的公钥加密AES密钥
            String encryptedAesKey = RsaUtil.publicEncrypt(TEST_AES_KEY, wrongPublicKey);

            HybridEncryptedRequest request = new HybridEncryptedRequest(encryptedData, encryptedAesKey);
            String requestBody = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("请求解密失败")));
        }

        @Test
        @DisplayName("测试数据字段被篡改")
        void testTamperedData() throws Exception {
            String originalData = "{\"test\":\"data\"}";
            String encryptedData = AesUtil.encrypt(originalData, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 篡改加密数据
            String tamperedData = encryptedData.substring(0, encryptedData.length() - 5) + "XXXXX";

            String encryptedAesKey = RsaUtil.publicEncrypt(TEST_AES_KEY,
                    encryptProperties.getHybrid().getPublicKey());

            HybridEncryptedRequest request = new HybridEncryptedRequest(tamperedData, encryptedAesKey);
            String requestBody = objectMapper.writeValueAsString(request);

            mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("测试空请求体")
        void testEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("加密算法兼容性测试")
    class EncryptionAlgorithmTests {

        @Test
        @DisplayName("测试AES加密解密的对称性")
        void testAesSymmetry() throws Exception {
            String originalText = "测试数据123!@#";
            String aesKey = "1234567890123456";

            // 加密
            String encrypted = AesUtil.encrypt(originalText, aesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());
            assertNotEquals(originalText, encrypted);

            // 解密
            String decrypted = AesUtil.decrypt(encrypted, aesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());
            assertEquals(originalText, decrypted);
        }

        @Test
        @DisplayName("测试RSA加密解密的对称性")
        void testRsaSymmetry() throws Exception {
            String originalText = "test_aes_key_1234";

            // 使用公钥加密
            String encrypted = RsaUtil.publicEncrypt(originalText,
                    encryptProperties.getHybrid().getPublicKey());
            assertNotEquals(originalText, encrypted);

            // 使用私钥解密
            String decrypted = RsaUtil.privateDecrypt(encrypted,
                    encryptProperties.getHybrid().getPrivateKey());
            assertEquals(originalText, decrypted);
        }

        @Test
        @DisplayName("测试复杂JSON数据的加密传输")
        void testComplexJsonData() throws Exception {
            Map<String, Object> complexData = new HashMap<>();
            complexData.put("string", "测试字符串");
            complexData.put("number", 12345);
            complexData.put("boolean", true);
            complexData.put("null", null);

            Map<String, Object> nested = new HashMap<>();
            nested.put("nested1", "value1");
            nested.put("nested2", 999);
            complexData.put("nested", nested);

            String jsonData = objectMapper.writeValueAsString(complexData);
            String encryptedData = AesUtil.encrypt(jsonData, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            String encryptedAesKey = RsaUtil.publicEncrypt(TEST_AES_KEY,
                    encryptProperties.getHybrid().getPublicKey());

            HybridEncryptedRequest request = new HybridEncryptedRequest(encryptedData, encryptedAesKey);
            String requestBody = objectMapper.writeValueAsString(request);

            MvcResult result = mockMvc.perform(post("/test/secure")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andReturn();

            String encryptedResponse = result.getResponse().getContentAsString();
            String decryptedResponse = AesUtil.decrypt(encryptedResponse, TEST_AES_KEY,
                    encryptProperties.getHybrid().getAesAlgorithm());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(decryptedResponse, Map.class);
            assertEquals("success", responseData.get("status"));

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedData = (Map<String, Object>) responseData.get("receivedData");
            assertEquals("测试字符串", receivedData.get("string"));
            assertEquals(12345, receivedData.get("number"));
            assertEquals(true, receivedData.get("boolean"));
        }
    }
}
