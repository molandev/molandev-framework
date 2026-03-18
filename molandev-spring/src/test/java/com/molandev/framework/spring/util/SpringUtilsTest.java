package com.molandev.framework.spring.util;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Spring工具类测试")
class SpringUtilsTest {

    private ConfigurableApplicationContext originalContext;

    @BeforeEach
    void setUp() {
        // 保存原始的应用上下文
        originalContext = SpringUtils.getApplicationContext();

        // 创建模拟的应用上下文用于测试
        GenericApplicationContext mockContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.application.name", "test-application");
        mockEnvironment.setProperty("test.property", "test-value");
        mockContext.setEnvironment(mockEnvironment);

        // 注册一些测试Bean
        mockContext.registerBean("testBean", TestBean.class, () -> new TestBean("testName", 18));
        mockContext.registerBean("anotherBean", AnotherBean.class, () -> new AnotherBean());

        mockContext.refresh();

        // 设置SpringUtils的应用上下文
        SpringUtils.setApplicationContext(mockContext);
    }

    @AfterEach
    void tearDown() {
        // 恢复原始的应用上下文
        SpringUtils.setApplicationContext(originalContext);

        // 清理自定义属性
        SpringUtils.getCustomProperty().clear();
        System.clearProperty("custom.test.property");
    }

    @Nested
    @DisplayName("Bean获取测试")
    class BeanGetTest {

        @Test
        @DisplayName("getBean通过Bean ID获取测试")
        void getBean_byBeanId_shouldReturnBeanInstance() {
            // 测试通过bean ID获取bean实例
            Object bean = SpringUtils.getBean("testBean");
            assertNotNull(bean);
            assertTrue(bean instanceof TestBean);
            assertEquals("testName", ((TestBean) bean).getName());
        }

        @Test
        @DisplayName("getBean通过Bean类型获取测试")
        void getBean_byBeanClass_shouldReturnBeanInstance() {
            // 测试通过bean类型获取bean实例
            TestBean bean = SpringUtils.getBean(TestBean.class);
            assertNotNull(bean);
            assertEquals("testName", bean.getName());
            assertEquals(18, bean.getAge());
        }
    }

    @Nested
    @DisplayName("Bean存在性检查测试")
    class BeanExistCheckTest {

        @Test
        @DisplayName("containsBean存在Bean测试")
        void containsBean_shouldReturnTrueWhenBeanExists() {
            // 测试当bean存在时返回true
            boolean result = SpringUtils.containsBean(TestBean.class);
            assertTrue(result);
        }

        @Test
        @DisplayName("containsBean不存在Bean测试")
        void containsBean_shouldReturnFalseWhenBeanNotExists() {
            // 测试当bean不存在时返回false
            boolean result = SpringUtils.containsBean(NonExistentBean.class);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Bean列表获取测试")
    class BeanListGetTest {

        @Test
        @DisplayName("getBeansByType获取指定类型Bean列表测试")
        void getBeansByType_shouldReturnAllBeansOfType() {
            // 测试获取指定类型的所有bean
            List<TestBean> beans = SpringUtils.getBeansByType(TestBean.class);
            assertNotNull(beans);
            assertEquals(1, beans.size());
            assertEquals("testName", beans.get(0).getName());
        }

        @Test
        @DisplayName("getBeansByType获取不存在类型Bean列表测试")
        void getBeansByType_shouldReturnEmptyListWhenNoBeansOfType() {
            // 测试当没有指定类型的bean时返回空列表
            List<NonExistentBean> beans = SpringUtils.getBeansByType(NonExistentBean.class);
            assertNotNull(beans);
            assertTrue(beans.isEmpty());
        }
    }

    @Nested
    @DisplayName("属性获取测试")
    class PropertyGetTest {

        @Test
        @DisplayName("getProperty获取属性值测试")
        void getProperty_shouldReturnPropertyValue() {
            // 测试获取配置属性值
            String value = SpringUtils.getProperty("test.property");
            assertEquals("test-value", value);
        }

        @Test
        @DisplayName("getProperty带默认值获取属性值测试")
        void getProperty_withDefaultValue_shouldReturnPropertyValue() {
            // 测试获取配置属性值（带默认值）
            String value = SpringUtils.getProperty("test.property", "default-value");
            assertEquals("test-value", value);
        }

        @Test
        @DisplayName("getProperty属性不存在时返回默认值测试")
        void getProperty_withDefaultValue_shouldReturnDefaultValueWhenPropertyNotExists() {
            // 测试当属性不存在时返回默认值
            String value = SpringUtils.getProperty("non.existent.property", "default-value");
            assertEquals("default-value", value);
        }

        @Test
        @DisplayName("hasProperty属性存在测试")
        void hasProperty_shouldReturnTrueWhenPropertyExists() {
            // 测试当属性存在时返回true
            boolean result = SpringUtils.hasProperty("test.property");
            assertTrue(result);
        }

        @Test
        @DisplayName("hasProperty属性不存在测试")
        void hasProperty_shouldReturnFalseWhenPropertyNotExists() {
            // 测试当属性不存在时返回false
            boolean result = SpringUtils.hasProperty("non.existent.property");
            assertFalse(result);
        }

        @Test
        @DisplayName("getProperties获取所有属性测试")
        void getProperties_shouldReturnAllProperties() {
            // 测试获取所有配置属性
            Properties properties = SpringUtils.getProperties();
            assertNotNull(properties);
            assertTrue(properties.containsKey("test.property"));
            assertEquals("test-value", properties.get("test.property"));
        }

        @Test
        @DisplayName("getApplicatioName获取应用名称测试")
        void getApplicatioName_shouldReturnApplicationName() {
            // 测试获取应用名称
            String appName = SpringUtils.getApplicationName();
            assertEquals("test-application", appName);
        }
    }

    @Nested
    @DisplayName("属性设置测试")
    class PropertySetTest {

        @Test
        @DisplayName("putProperty添加系统属性测试")
        void putProperty_shouldAddSystemProperty() {
            // 测试添加系统属性
            SpringUtils.putProperty("custom.test.property", "custom-value");
            assertEquals("custom-value", System.getProperty("custom.test.property"));
        }

        @Test
        @DisplayName("putProperties添加多个系统属性测试")
        void putProperties_shouldAddMultipleSystemProperties() {
            // 测试添加多个系统属性
            Map<String, String> props = new HashMap<>();
            props.put("prop1", "value1");
            props.put("prop2", "value2");

            SpringUtils.putProperties(props);

            assertEquals("value1", System.getProperty("prop1"));
            assertEquals("value2", System.getProperty("prop2"));
        }

        @Test
        @DisplayName("addProperty添加自定义属性测试")
        void addProperty_shouldAddCustomProperty() {
            // 测试添加自定义属性
            SpringUtils.addProperty("custom.property", "custom-value");
            assertEquals("custom-value", SpringUtils.getCustomProperty().get("custom.property"));
        }
    }

    @Nested
    @DisplayName("类处理测试")
    class ClassHandleTest {

        @Test
        @DisplayName("getOriginClass获取原始类测试")
        void getOriginClass_shouldReturnSameClassWhenNotProxy() {
            // 测试获取非代理类的原始类
            Class<?> originClass = SpringUtils.getOriginClass(TestBean.class);
            assertEquals(TestBean.class, originClass);
        }
    }

    // 测试用的简单Bean类
    @Setter
    @Getter
    static class TestBean {
        private String name;
        private int age;

        public TestBean() {
        }

        public TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

    }

    // 另一个测试用的Bean类
    static class AnotherBean {
        public AnotherBean() {
        }
    }

    // 不存在的Bean类
    static class NonExistentBean {
        public NonExistentBean() {
        }
    }
}