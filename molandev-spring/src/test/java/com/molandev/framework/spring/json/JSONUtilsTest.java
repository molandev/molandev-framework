package com.molandev.framework.spring.json;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON工具类测试")
class JSONUtilsTest {

    @Nested
    @DisplayName("对象转JSON字符串测试")
    class ObjectToJsonStringTest {

        @Test
        @DisplayName("toJsonString正常对象转换测试")
        void toJsonString_shouldConvertObjectToJsonString() {
            // 测试正常对象转换为JSON字符串
            Map<String, Object> map = new HashMap<>();
            map.put("name", "test");
            map.put("age", 18);

            String jsonString = JSONUtils.toJsonString(map);
            assertNotNull(jsonString);
            assertTrue(jsonString.contains("\"name\":\"test\""));
            assertTrue(jsonString.contains("\"age\":18"));
        }

        @Test
        @DisplayName("toJsonString null值处理测试")
        void toJsonString_shouldHandleNullValue() {
            // 测试null值转换
            String jsonString = JSONUtils.toJsonString(null);
            assertEquals("null", jsonString);
        }

        @Test
        @DisplayName("toJsonString复杂对象处理测试")
        void toJsonString_shouldHandleComplexObject() {
            // 测试复杂对象序列化
            ComplexObject complexObject = new ComplexObject();
            complexObject.setId(1L);
            complexObject.setName("complex");
            complexObject.setTags(Arrays.asList("tag1", "tag2"));
            complexObject.setCreateTime(LocalDateTime.of(2023, 1, 1, 12, 0, 0));

            Map<String, Object> properties = new HashMap<>();
            properties.put("prop1", "value1");
            properties.put("prop2", 42);
            complexObject.setProperties(properties);

            String jsonString = JSONUtils.toJsonString(complexObject);
            assertNotNull(jsonString);
            assertTrue(jsonString.contains("\"id\":1"));
            assertTrue(jsonString.contains("\"name\":\"complex\""));
            assertTrue(jsonString.contains("\"tags\":[\"tag1\",\"tag2\"]"));
        }
    }

    @Nested
    @DisplayName("JSON字符串转Map测试")
    class JsonStringToMapTest {

        @Test
        @DisplayName("toMap正常转换测试")
        void toMap_shouldConvertJsonStringToMap() {
            // 测试JSON字符串转换为Map
            String jsonString = "{\"name\":\"test\",\"age\":18}";
            Map<String, Object> map = JSONUtils.toMap(jsonString);

            assertNotNull(map);
            assertEquals("test", map.get("name"));
            assertEquals(18, map.get("age"));
        }

        @Test
        @DisplayName("toMap无效JSON处理测试")
        void toMap_shouldHandleInvalidJsonString() {
            // 测试无效JSON字符串
            assertThrows(RuntimeException.class, () -> {
                JSONUtils.toMap("{invalid json}");
            });
        }
    }

    @Nested
    @DisplayName("JSON字符串转List测试")
    class JsonStringToListTest {

        @Test
        @DisplayName("toList正常转换测试")
        void toList_shouldConvertJsonStringToList() {
            // 测试JSON字符串转换为List
            String jsonString = "[{\"name\":\"test1\",\"age\":18},{\"name\":\"test2\",\"age\":20}]";
            Type type = new TypeReference<Map<String, Object>>() {
            }.getType();
            List<Map<String, Object>> list = JSONUtils.toList(jsonString, type);

            assertNotNull(list);
            assertEquals(2, list.size());
            assertEquals("test1", list.get(0).get("name"));
            assertEquals(18, list.get(0).get("age"));
            assertEquals("test2", list.get(1).get("name"));
            assertEquals(20, list.get(1).get("age"));
        }

        @Test
        @DisplayName("toList无效JSON处理测试")
        void toList_shouldHandleInvalidJsonString() {
            // 测试无效JSON字符串
            assertThrows(RuntimeException.class, () -> {
                Type type = new TypeReference<Map<String, Object>>() {
                }.getType();
                JSONUtils.toList("[invalid json]", type);
            });
        }
    }

    @Nested
    @DisplayName("JSON字符串转对象测试")
    class JsonStringToObjectTest {

        @Test
        @DisplayName("toObject使用Type转换测试")
        void toObject_withType_shouldConvertJsonStringToObject() {
            // 测试使用Type转换JSON字符串为对象
            String jsonString = "{\"name\":\"test\",\"age\":18}";
            Type type = new TypeReference<Map<String, Object>>() {
            }.getType();
            Map<String, Object> map = JSONUtils.toObject(jsonString, type);

            assertNotNull(map);
            assertEquals("test", map.get("name"));
            assertEquals(18, map.get("age"));
        }

        @Test
        @DisplayName("toObject使用Type处理无效JSON测试")
        void toObject_withType_shouldHandleInvalidJsonString() {
            // 测试无效JSON字符串
            assertThrows(RuntimeException.class, () -> {
                Type type = new TypeReference<Map<String, Object>>() {
                }.getType();
                JSONUtils.toObject("{invalid json}", type);
            });
        }

        @Test
        @DisplayName("toObject使用Class转换测试")
        void toObject_withClass_shouldConvertJsonStringToObject() {
            // 测试使用Class转换JSON字符串为对象
            String jsonString = "{\"name\":\"test\",\"value\":123}";
            TestObject testObject = JSONUtils.toObject(jsonString, TestObject.class);

            assertNotNull(testObject);
            assertEquals("test", testObject.name);
            assertEquals(123, testObject.value);
        }

        @Test
        @DisplayName("toObject使用Class处理无效JSON测试")
        void toObject_withClass_shouldHandleInvalidJsonString() {
            // 测试无效JSON字符串
            assertThrows(RuntimeException.class, () -> {
                JSONUtils.toObject("{invalid json}", TestObject.class);
            });
        }

        @Test
        @DisplayName("toObject复杂对象处理测试")
        void toObject_shouldHandleComplexObject() {
            // 测试复杂对象反序列化
            String jsonString = "{\"id\":1,\"name\":\"complex\",\"tags\":[\"tag1\",\"tag2\"],\"createTime\":\"2023-01-01 12:00:00\",\"properties\":{\"prop1\":\"value1\",\"prop2\":42}}";
            ComplexObject complexObject = JSONUtils.toObject(jsonString, ComplexObject.class);

            assertNotNull(complexObject);
            assertEquals(1L, complexObject.getId());
            assertEquals("complex", complexObject.getName());
            assertEquals(Arrays.asList("tag1", "tag2"), complexObject.getTags());
            assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0, 0), complexObject.getCreateTime());
            assertNotNull(complexObject.getProperties());
            assertEquals("value1", complexObject.getProperties().get("prop1"));
            assertEquals(42, complexObject.getProperties().get("prop2"));
        }
    }

    // 测试用的简单类
    @Setter
    @Getter
    static class TestObject {
        private String name;
        private int value;

    }

    // 测试用的复杂类
    @Setter
    @Getter
    static class ComplexObject {
        private Long id;
        private String name;
        private List<String> tags;
        private LocalDateTime createTime;
        private Map<String, Object> properties;

    }
}