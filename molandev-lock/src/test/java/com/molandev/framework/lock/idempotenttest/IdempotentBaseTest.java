package com.molandev.framework.lock.idempotenttest;

import com.molandev.framework.spring.json.JSONUtils;
import com.molandev.framework.lock.config.EmbeddedRedisConfig;
import com.molandev.framework.lock.config.JsonResult;
import com.molandev.framework.lock.config.LockAutoConfiguration;
import com.molandev.framework.lock.idempotenttest.controller.IdempotentTestController;
import com.molandev.framework.util.ClassUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {LockAutoConfiguration.class,
        IdempotentTestController.class,
        EmbeddedRedisConfig.class,
        IdempotentBaseTest.TestApp.class
})
@DisplayName("MolanIdempotent注解测试")
@TestPropertySource(properties = {
        "molandev.lock.type=redisson",
        "molandev.autoconfig.json.enabled=true",
//        "molandev.lock.type=redis",
        "spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
public  class IdempotentBaseTest {

    @SpringBootApplication
    static class TestApp {

    }

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        resetCounter();
    }

    protected void resetCounter() throws Exception {
        mockMvc.perform(delete("/test/idempotent/counter"))
                .andReturn();
    }

    @Nested
    @DisplayName("基本幂等性测试")
    class BasicIdempotencyTest {

        @BeforeEach
        void setUp() throws Exception {
            resetCounter();
        }

        @Test
        @DisplayName("测试相同请求被拦截")
        public void testSameRequestBlocked() throws Exception {
            // 第一次请求在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/basic-same-request"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            // 第二次相同请求在另一个线程中执行，应该被拦截
            AtomicReference<String> result2Ref = new AtomicReference<>();
            Thread thread2 = new Thread(() -> {
                try {
                    result2Ref.set(mockMvc.perform(post("/test/idempotent/basic-same-request"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread2.start();
            thread2.join();

            JsonResult<?> jsonResult2 = JSONUtils.toObject(result2Ref.get(), JsonResult.class);
            assertNotEquals("0000", jsonResult2.getCode(), "第二次相同请求应该被拦截");

            // 验证计数器值仍然是1
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(1, counterJsonResult.getData(), "计数器值应该是1");
        }

        @Test
        @DisplayName("测试不同请求可以执行")
        public void testDifferentRequestsAllowed() throws Exception {
            // 执行不同的操作，都在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/different-request-1"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            AtomicReference<String> result3Ref = new AtomicReference<>();
            Thread thread3 = new Thread(() -> {
                try {
                    result3Ref.set(mockMvc.perform(post("/test/idempotent/different-request-2"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread3.start();
            thread3.join();

            JsonResult<?> jsonResult3 = JSONUtils.toObject(result3Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult3.getCode(), "第二次不同请求应该成功");

            // 验证计数器值是2
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(2, counterJsonResult.getData(), "计数器值应该是2");
        }
    }

    @Nested
    @DisplayName("基于参数的幂等性测试")
    class ParameterBasedIdempotencyTest {

        @BeforeEach
        void setUp() throws Exception {
            resetCounter();
        }

        @Test
        @DisplayName("测试相同参数的请求被拦截")
        public void testSameParameterRequestBlocked() throws Exception {
            String testKey = "parameter_test_key";

            // 第一次请求在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/parameter-based")
                                    .param("requestKey", testKey))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            // 第二次相同参数的请求在另一个线程中执行，应该被拦截
            AtomicReference<String> result2Ref = new AtomicReference<>();
            Thread thread2 = new Thread(() -> {
                try {
                    result2Ref.set(mockMvc.perform(post("/test/idempotent/parameter-based")
                                    .param("requestKey", testKey))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread2.start();
            thread2.join();

            JsonResult<?> jsonResult2 = JSONUtils.toObject(result2Ref.get(), JsonResult.class);
            assertNotEquals("0000", jsonResult2.getCode(), "第二次相同参数请求应该被拦截");

            // 验证计数器值仍然是1
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(1, counterJsonResult.getData(), "计数器值应该是1");
        }

        @Test
        @DisplayName("测试不同参数的请求可以执行")
        public void testDifferentParameterRequestsAllowed() throws Exception {
            String testKey1 = "parameter_test_key_1";
            String testKey2 = "parameter_test_key_2";

            // 使用不同参数的请求，都在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/parameter-based")
                                    .param("requestKey", testKey1))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            AtomicReference<String> result2Ref = new AtomicReference<>();
            Thread thread2 = new Thread(() -> {
                try {
                    result2Ref.set(mockMvc.perform(post("/test/idempotent/parameter-based")
                                    .param("requestKey", testKey2))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread2.start();
            thread2.join();

            JsonResult<?> jsonResult2 = JSONUtils.toObject(result2Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult2.getCode(), "第二次不同参数请求应该成功");

            // 验证计数器值是2
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(2, counterJsonResult.getData(), "计数器值应该是2");
        }
    }

    @Nested
    @DisplayName("过期时间测试")
    class ExpirationTest {

        @BeforeEach
        void setUp() throws Exception {
            resetCounter();
        }

        @Test
        @DisplayName("测试请求过期后可以再次执行")
        public void testRequestAfterExpiration() throws Exception {
            // 第一次请求在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/short-expire"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            // 等待超过过期时间
            Thread.sleep(3500);

            // 相同请求在另一个线程中执行，应该可以再次执行
            AtomicReference<String> result2Ref = new AtomicReference<>();
            Thread thread2 = new Thread(() -> {
                try {
                    result2Ref.set(mockMvc.perform(post("/test/idempotent/short-expire"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread2.start();
            thread2.join();

            JsonResult<?> jsonResult2 = JSONUtils.toObject(result2Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult2.getCode(), "过期后相同请求应该可以再次执行");

            // 验证计数器值是2
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(2, counterJsonResult.getData(), "计数器值应该是2");
        }
    }

    @Nested
    @DisplayName("默认key测试")
    class DefaultKeyTest {

        @BeforeEach
        void setUp() throws Exception {
            resetCounter();
        }

        @Test
        @DisplayName("测试默认key的幂等性")
        public void testDefaultKeyIdempotency() throws Exception {
            // 第一次请求在单独线程中执行
            AtomicReference<String> result1Ref = new AtomicReference<>();
            Thread thread1 = new Thread(() -> {
                try {
                    result1Ref.set(mockMvc.perform(post("/test/idempotent/default-key"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread1.start();
            thread1.join();

            JsonResult<?> jsonResult1 = JSONUtils.toObject(result1Ref.get(), JsonResult.class);
            assertEquals("0000", jsonResult1.getCode(), "第一次请求应该成功");

            // 第二次相同请求在另一个线程中执行，应该被拦截
            AtomicReference<String> result2Ref = new AtomicReference<>();
            Thread thread2 = new Thread(() -> {
                try {
                    result2Ref.set(mockMvc.perform(post("/test/idempotent/default-key"))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread2.start();
            thread2.join();

            JsonResult<?> jsonResult2 = JSONUtils.toObject(result2Ref.get(), JsonResult.class);
            assertNotEquals("0000", jsonResult2.getCode(), "第二次相同请求应该被拦截");

            // 验证计数器值仍然是1
            String counterResult = mockMvc.perform(get("/test/idempotent/counter"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonResult<Integer> counterJsonResult = JSONUtils.toObject(counterResult,
                    ClassUtils.getType(JsonResult.class, Integer.class));
            assertEquals("0000", counterJsonResult.getCode(), "获取计数器结果应该成功");
            assertEquals(1, counterJsonResult.getData(), "计数器值应该是1");
        }
    }
}