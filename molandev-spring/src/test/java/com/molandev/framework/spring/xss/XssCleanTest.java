package com.molandev.framework.spring.xss;

import com.molandev.framework.spring.json.JSONUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {XssCleanTest.TestConfig.class,
        TestController.class,
        XssIgnoredController.class,
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "molandev.autoconfig.xss.enabled=true",
})
@DisplayName("XSS过滤器测试")
public class XssCleanTest {

    @Autowired
    private MockMvc mockMvc;

    @Configuration
    @EnableWebMvc
    @ComponentScan(basePackages = "com.molandev.framework.spring.xss")
    static class TestConfig {
    }

    @Nested
    @DisplayName("GET请求测试")
    class GetRequestsTest {
        @Test
        @DisplayName("测试启用XSS保护")
        public void testXssProtection_enabled() throws Exception {
            mockMvc.perform(get("/test/xss?input=<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
        }
    }

    @Nested
    @DisplayName("POST请求测试")
    class PostRequestsTest {

        @Test
        @DisplayName("测试表单数据")
        public void testXssProtection_formData() throws Exception {
            mockMvc.perform(post("/test/xss-form")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("input", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
        }

        @Test
        @DisplayName("测试JSON请求体")
        public void testXssProtection_jsonBody() throws Exception {
            TestController.TestData data = new TestController.TestData();
            data.setValue("<script>alert('xss')</script>");
            String json = JSONUtils.toJsonString(data);

            mockMvc.perform(post("/test/xss-json")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"value\":\"&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;\"}"));
        }

        @Test
        @DisplayName("测试字符串请求体")
        public void testXssProtection_requestBodyString() throws Exception {
            String requestBody = "<script>alert('xss')</script>";

            mockMvc.perform(post("/test/xss-requestbody-string")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(content().string("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
        }
    }

    @Nested
    @DisplayName("文件上传测试")
    class FileUploadTest {
        @Test
        @DisplayName("测试文件上传参数")
        public void testXssProtection_multipart() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "test content".getBytes()
            );

            mockMvc.perform(multipart("/test/xss-file")
                            .file(file)
                            .param("description", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
        }
    }

    @Nested
    @DisplayName("方法级别XssIgnore注解测试")
    class MethodLevelXssIgnoreTest {
        @Test
        @DisplayName("测试GET请求中方法级别@XssIgnore注解")
        public void testXssIgnoreOnMethodLevelForGet() throws Exception {
            mockMvc.perform(get("/test/xss-ignore-method?input=<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<script>alert('xss')</script>"));
        }

        @Test
        @DisplayName("测试POST请求中方法级别@XssIgnore注解")
        public void testXssIgnoreOnMethodLevelForPost() throws Exception {
            mockMvc.perform(post("/test/xss-ignore-method-post")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("input", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<script>alert('xss')</script>"));
        }
    }

    @Nested
    @DisplayName("类级别XssIgnore注解测试")
    class ClassLevelXssIgnoreTest {
        @Test
        @DisplayName("测试GET请求中类级别@XssIgnore注解")
        public void testXssIgnoreOnClassLevelForGet() throws Exception {
            mockMvc.perform(get("/test/xss-ignore-class?input=<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<script>alert('xss')</script>"));
        }

        @Test
        @DisplayName("测试POST请求中类级别@XssIgnore注解")
        public void testXssIgnoreOnClassLevelForPost() throws Exception {
            mockMvc.perform(post("/test/xss-ignore-class-post")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("input", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<script>alert('xss')</script>"));
        }

        @Test
        @DisplayName("测试文件上传中类级别@XssIgnore注解")
        public void testXssIgnoreOnClassLevelForFileUpload() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "test content".getBytes()
            );

            mockMvc.perform(multipart("/test/xss-ignore-class-file")
                            .file(file)
                            .param("description", "<script>alert('xss')</script>"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("<script>alert('xss')</script>"));
        }
    }
}