package com.molandev.framework.spring.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.spring.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {JacksonConfigTest.TestConfig.class, TestController.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.main.banner-mode=off",
        "logging.level.org.springframework.web=DEBUG",
        "molandev.autoconfig.json.enabled=true"
})
@DisplayName("Jackson配置测试")
public class JacksonConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FormattingConversionService mvcConversionService;
    private ObjectMapper objectMapper = JSONUtils.getJsonMapper();

    @Configuration
    @EnableWebMvc
    @ComponentScan(basePackages = "com.molandev.framework.spring.json")
    static class TestConfig {
    }

    @Nested
    @DisplayName("JSON序列化测试")
    class JsonSerializationTest {

        @Test
        @DisplayName("测试LocalDateTime序列化")
        public void testLocalDateTimeSerialization() throws Exception {
            String json = "{\"localDateTime\":\"2023-01-01 12:00:00\"}";

            mockMvc.perform(post("/test/datetime")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data.localDateTime", is("2023-01-01 12:00:00")));
        }

        @Test
        @DisplayName("测试LocalDate序列化")
        public void testLocalDateSerialization() throws Exception {
            String json = "{\"localDate\":\"2023-01-01\"}";

            mockMvc.perform(post("/test/date")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data.localDate", is("2023-01-01")));
        }

        @Test
        @DisplayName("测试LocalTime序列化")
        public void testLocalTimeSerialization() throws Exception {
            String json = "{\"localTime\":\"12:00:00\"}";

            mockMvc.perform(post("/test/time")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data.localTime", is("12:00:00")));
        }
    }

    @Nested
    @DisplayName("参数转换测试")
    class ParameterConversionTest {

        @Test
        @DisplayName("测试GET请求中LocalDateTime参数转换")
        public void testLocalDateTimeParameterConversion() throws Exception {
            mockMvc.perform(get("/test/param/datetime")
                            .param("dateTime", "2023-01-01 12:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data", is("2023-01-01 12:00:00")));
        }

        @Test
        @DisplayName("测试GET请求中LocalDate参数转换")
        public void testLocalDateParameterConversion() throws Exception {
            mockMvc.perform(get("/test/param/date")
                            .param("date", "2023-01-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data", is("2023-01-01")));
        }

        @Test
        @DisplayName("测试GET请求中LocalTime参数转换")
        public void testLocalTimeParameterConversion() throws Exception {
            mockMvc.perform(get("/test/param/time")
                            .param("time", "12:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code", is("0000")))
                    .andExpect(jsonPath("$.data", is("12:00:00")));
        }
    }

    @Nested
    @DisplayName("ObjectMapper配置测试")
    class ObjectMapperConfigurationTest {

        @Test
        @DisplayName("测试ObjectMapper是否注册了时间处理模块")
        public void testObjectMapperHasTimeModule() {
            // 检查ObjectMapper是否注册了处理Java 8时间的模块
            boolean hasJavaTimeModule = objectMapper.getRegisteredModuleIds().stream()
                    .anyMatch(id -> id.toString().contains("JavaTimeModule") || id.toString().contains("JsonJavaTimeModule"));
        }
    }
}