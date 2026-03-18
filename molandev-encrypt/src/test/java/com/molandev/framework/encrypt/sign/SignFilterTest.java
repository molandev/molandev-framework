package com.molandev.framework.encrypt.sign;

import com.molandev.framework.spring.json.JSONUtils;
import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.MapUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {SignFilterTest.TestConfig.class, SignFilterTest.TestController.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "molandev.encrypt.sign.enabled=true",
        "molandev.encrypt.sign.secret=test_secret_key_123456",
        "molandev.encrypt.sign.expire-time=300",
        "molandev.encrypt.sign.whitelist=/test/public/**,/health"
})
@DisplayName("签名校验功能测试")
class SignFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EncryptProperties encryptProperties;

    @SpringBootApplication
    @ComponentScan(basePackages = {"com.molandev.framework.encrypt.sign"})
    @Import({SignAutoConfiguration.class})
    static class TestConfig {
        @Bean
        @SuppressWarnings("unchecked")
        public FilterRegistrationBean exceptionFilterRegistration(EncryptProperties encryptProperties) {
            FilterRegistrationBean  registration = new FilterRegistrationBean<>();
            registration.setFilter(new Filter(){
                @Override
                public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                    try {
                        filterChain.doFilter(servletRequest, servletResponse);
                    }catch (SignException e){
                        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                        httpResponse.setStatus(401);
                        servletResponse.setContentType("application/json;charset=utf-8");
                        servletResponse.getWriter().write(JSONUtils.toJsonString(MapUtil.toMap("code", 401, "message", e.getMessage())));
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

        @GetMapping("/secure")
        public String secureEndpoint(@RequestParam String data) {
            return "success: " + data;
        }

        @GetMapping("/public/info")
        public String publicEndpoint() {
            return "public info";
        }
    }

    @Nested
    @DisplayName("签名校验成功场景")
    class SignVerificationSuccessTests {

        @Test
        @DisplayName("测试正确签名的请求通过验证")
        void testValidSignature() throws Exception {
            // 准备参数
            Map<String, String> params = new HashMap<>();
            params.put("data", "test_data");
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("nonce", UUID.randomUUID().toString());

            // 生成签名
            String sign = SignUtils.generateSign(params, encryptProperties.getSign().getSecret());
            params.put("sign", sign);

            // 发送请求
            mockMvc.perform(get("/test/secure")
                            .param("data", params.get("data"))
                            .param("timestamp", params.get("timestamp"))
                            .param("nonce", params.get("nonce"))
                            .param("sign", sign))
                    .andExpect(status().isOk())
                    .andExpect(content().string("success: test_data"));
        }

        @Test
        @DisplayName("测试白名单路径不需要签名验证")
        void testWhitelistedPath() throws Exception {
            mockMvc.perform(get("/test/public/info"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("public info"));
        }

        @Test
        @DisplayName("测试多个参数的签名验证")
        void testMultipleParameters() throws Exception {
            Map<String, String> params = new HashMap<>();
            params.put("data", "test_data");
            params.put("userId", "12345");
            params.put("action", "query");
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("nonce", UUID.randomUUID().toString());

            String sign = SignUtils.generateSign(params, encryptProperties.getSign().getSecret());

            mockMvc.perform(get("/test/secure")
                            .param("data", params.get("data"))
                            .param("userId", params.get("userId"))
                            .param("action", params.get("action"))
                            .param("timestamp", params.get("timestamp"))
                            .param("nonce", params.get("nonce"))
                            .param("sign", sign))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("签名校验失败场景")
    class SignVerificationFailureTests {

        @Test
        @DisplayName("测试缺少签名参数")
        void testMissingSignature() throws Exception {
            mockMvc.perform(get("/test/secure")
                            .param("data", "test_data")
                            .param("timestamp", String.valueOf(System.currentTimeMillis()))
                            .param("nonce", UUID.randomUUID().toString()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("签名参数[sign]不能为空"));
        }

        @Test
        @DisplayName("测试缺少时间戳参数")
        void testMissingTimestamp() throws Exception {
            mockMvc.perform(get("/test/secure")
                            .param("data", "test_data")
                            .param("nonce", UUID.randomUUID().toString())
                            .param("sign", "fake_sign"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("时间戳参数[timestamp]不能为空"));
        }

        @Test
        @DisplayName("测试缺少随机数参数")
        void testMissingNonce() throws Exception {
            mockMvc.perform(get("/test/secure")
                            .param("data", "test_data")
                            .param("timestamp", String.valueOf(System.currentTimeMillis()))
                            .param("sign", "fake_sign"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("随机数参数[nonce]不能为空"));
        }

        @Test
        @DisplayName("测试错误的签名")
        void testInvalidSignature() throws Exception {
            mockMvc.perform(get("/test/secure")
                            .param("data", "test_data")
                            .param("timestamp", String.valueOf(System.currentTimeMillis()))
                            .param("nonce", UUID.randomUUID().toString())
                            .param("sign", "invalid_signature"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("签名验证失败"));
        }

        @Test
        @DisplayName("测试过期的时间戳")
        void testExpiredTimestamp() throws Exception {
            // 使用10分钟前的时间戳（超过配置的5分钟有效期）
            long expiredTimestamp = System.currentTimeMillis() - (10 * 60 * 1000);

            Map<String, String> params = new HashMap<>();
            params.put("data", "test_data");
            params.put("timestamp", String.valueOf(expiredTimestamp));
            params.put("nonce", UUID.randomUUID().toString());

            String sign = SignUtils.generateSign(params, encryptProperties.getSign().getSecret());

            mockMvc.perform(get("/test/secure")
                            .param("data", params.get("data"))
                            .param("timestamp", params.get("timestamp"))
                            .param("nonce", params.get("nonce"))
                            .param("sign", sign))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("请求已过期"));
        }

        @Test
        @DisplayName("测试重复的nonce（防重放攻击）")
        void testDuplicateNonce() throws Exception {
            String nonce = UUID.randomUUID().toString();

            Map<String, String> params = new HashMap<>();
            params.put("data", "test_data");
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("nonce", nonce);

            String sign = SignUtils.generateSign(params, encryptProperties.getSign().getSecret());

            // 第一次请求应该成功
            mockMvc.perform(get("/test/secure")
                            .param("data", params.get("data"))
                            .param("timestamp", params.get("timestamp"))
                            .param("nonce", params.get("nonce"))
                            .param("sign", sign))
                    .andExpect(status().isOk());

            // 使用相同nonce的第二次请求应该失败
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            sign = SignUtils.generateSign(params, encryptProperties.getSign().getSecret());

            mockMvc.perform(get("/test/secure")
                            .param("data", params.get("data"))
                            .param("timestamp", params.get("timestamp"))
                            .param("nonce", params.get("nonce"))
                            .param("sign", sign))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("请求重复，nonce已使用"));
        }

        @Test
        @DisplayName("测试时间戳格式错误")
        void testInvalidTimestampFormat() throws Exception {
            mockMvc.perform(get("/test/secure")
                            .param("data", "test_data")
                            .param("timestamp", "invalid_timestamp")
                            .param("nonce", UUID.randomUUID().toString())
                            .param("sign", "fake_sign"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("时间戳格式错误"));
        }
    }

    @Nested
    @DisplayName("签名生成工具测试")
    class SignUtilsTests {

        @Test
        @DisplayName("测试签名生成的一致性")
        void testSignGenerationConsistency() {
            Map<String, String> params = new HashMap<>();
            params.put("data", "test_data");
            params.put("timestamp", "1234567890");
            params.put("nonce", "test_nonce");

            String sign1 = SignUtils.generateSign(params, "test_secret");
            String sign2 = SignUtils.generateSign(params, "test_secret");

            // 相同参数应该生成相同的签名
            assert sign1.equals(sign2);
        }

        @Test
        @DisplayName("测试参数顺序不影响签名结果")
        void testSignOrderIndependence() {
            Map<String, String> params1 = new HashMap<>();
            params1.put("a", "1");
            params1.put("b", "2");
            params1.put("c", "3");

            Map<String, String> params2 = new HashMap<>();
            params2.put("c", "3");
            params2.put("a", "1");
            params2.put("b", "2");

            String sign1 = SignUtils.generateSign(params1, "test_secret");
            String sign2 = SignUtils.generateSign(params2, "test_secret");

            // 参数顺序不同，但内容相同，应该生成相同的签名
            assert sign1.equals(sign2);
        }

        @Test
        @DisplayName("测试时间戳验证逻辑")
        void testTimestampVerification() {
            long currentTime = System.currentTimeMillis();
            long expireTime = 300; // 5分钟

            // 当前时间应该有效
            assert SignUtils.verifyTimestamp(currentTime, expireTime);

            // 4分钟前应该有效
            assert SignUtils.verifyTimestamp(currentTime - (4 * 60 * 1000), expireTime);

            // 6分钟前应该无效
            assert !SignUtils.verifyTimestamp(currentTime - (6 * 60 * 1000), expireTime);
        }
    }
}
