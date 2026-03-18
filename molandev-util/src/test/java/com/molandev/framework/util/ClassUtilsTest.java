package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("类工具类测试")
public class ClassUtilsTest {

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTest {

        @Test
        @DisplayName("newInstance普通类测试")
        void newInstance() {
            // 测试正常类的实例化
            TestInnerClass instance = ClassUtils.newInstance(TestInnerClass.class);
            assertNotNull(instance);
            assertEquals("test", instance.getValue());
        }

        @Test
        @DisplayName("newInstance静态内部类测试")
        void testStaticInnerClass() {
            // 测试静态内部类的实例化
            TestInnerClass.StaticInner staticInner = ClassUtils.newInstance(TestInnerClass.StaticInner.class);
            assertNotNull(staticInner);
            assertEquals("static inner", staticInner.getMessage());
        }

        @Test
        @DisplayName("newInstance非静态内部类测试")
        void testNonStaticInnerClass() {
            // 测试非静态内部类的实例化
            TestInnerClass outer = new TestInnerClass();
            TestInnerClass.NonStaticInner nonStaticInner = ClassUtils.newInstance(TestInnerClass.NonStaticInner.class);
            assertNotNull(nonStaticInner);
            assertEquals("test", nonStaticInner.getOuterValue());
        }
    }

    @Nested
    @DisplayName("类型判断测试")
    class TypeCheckTest {

        @Test
        @DisplayName("isInterface方法测试")
        void isInterface() {
            // 测试接口类型
            assertTrue(ClassUtils.isInterface(List.class));

            // 测试类类型
            assertFalse(ClassUtils.isInterface(String.class));

            // 测试基本类型
            assertFalse(ClassUtils.isInterface(int.class));
        }

        @Test
        @DisplayName("isClass方法测试")
        void isClass() {
            // 测试类类型
            assertTrue(ClassUtils.isClass(String.class));

            // 测试接口类型
            assertFalse(ClassUtils.isClass(List.class));

            // 测试基本类型
            assertFalse(ClassUtils.isClass(int.class));
        }

        @Test
        @DisplayName("isPrimitive方法测试")
        void isPrimitive() {
            // 测试基本类型
            assertTrue(ClassUtils.isPrimitive(int.class));
            assertTrue(ClassUtils.isPrimitive(double.class));
            assertTrue(ClassUtils.isPrimitive(short.class));
            assertTrue(ClassUtils.isPrimitive(long.class));
            assertTrue(ClassUtils.isPrimitive(float.class));
            assertTrue(ClassUtils.isPrimitive(boolean.class));
            assertTrue(ClassUtils.isPrimitive(char.class));
            assertTrue(ClassUtils.isPrimitive(byte.class));

            // 测试非基本类型
            assertFalse(ClassUtils.isPrimitive(String.class));
            assertFalse(ClassUtils.isPrimitive(List.class));
        }

        @Test
        @DisplayName("isGenericType方法测试")
        void isGenericType() {
            // 测试泛型类型
            Type listOfStringType = ClassUtils.getType("java.util.List<java.lang.String>");
            assertTrue(ClassUtils.isGenericType(listOfStringType));

            // 测试非泛型类型
            assertFalse(ClassUtils.isGenericType(String.class));
            assertFalse(ClassUtils.isGenericType(int.class));
        }
    }

    @Nested
    @DisplayName("类型获取测试")
    class TypeGetTest {

        @Test
        @DisplayName("getType字符串方法测试")
        void getType() {
            // 测试基本类型
            assertEquals(int.class, ClassUtils.getType("int"));
            assertEquals(boolean.class, ClassUtils.getType("boolean"));
            assertEquals(byte.class, ClassUtils.getType("byte"));
            assertEquals(short.class, ClassUtils.getType("short"));
            assertEquals(long.class, ClassUtils.getType("long"));
            assertEquals(char.class, ClassUtils.getType("char"));
            assertEquals(float.class, ClassUtils.getType("float"));
            assertEquals(double.class, ClassUtils.getType("double"));

            // 测试普通类
            assertEquals(String.class, ClassUtils.getType("class java.lang.String"));

            // 测试泛型类型
            Type listOfStringType = ClassUtils.getType("java.util.List<java.lang.String>");
            assertInstanceOf(ParameterizedType.class, listOfStringType);
            assertEquals(List.class, ((ParameterizedType) listOfStringType).getRawType());

            // 测试嵌套泛型类型
            Type mapType = ClassUtils.getType("java.util.Map<java.lang.String,java.lang.Integer>");
            assertInstanceOf(ParameterizedType.class, mapType);
            assertEquals(Map.class, ((ParameterizedType) mapType).getRawType());

            // 测试无效类型
            assertThrows(IllegalArgumentException.class, () -> ClassUtils.getType("invalid.type"));
        }

        @Test
        @DisplayName("getType类和类型方法测试")
        void getTypeWithClassAndTypes() {
            // 测试无泛型参数
            Type stringType = ClassUtils.getType(String.class);
            assertEquals(String.class, stringType);

            // 测试单个泛型参数
            Type listOfStringType = ClassUtils.getType(List.class, String.class);
            assertInstanceOf(ParameterizedType.class, listOfStringType);
            assertEquals(List.class, ((ParameterizedType) listOfStringType).getRawType());

            // 测试多个泛型参数
            Type mapType = ClassUtils.getType(Map.class, String.class, Integer.class);
            assertInstanceOf(ParameterizedType.class, mapType);
            assertEquals(Map.class, ((ParameterizedType) mapType).getRawType());
        }

        @Test
        @DisplayName("getParameterizedType方法测试")
        void getParameterizedType() {
            // 测试简单泛型
            ParameterizedType listOfStringType = ClassUtils.getParameterizedType("java.util.List<java.lang.String>");
            assertEquals(List.class, listOfStringType.getRawType());

            // 测试嵌套泛型
            ParameterizedType mapType = ClassUtils.getParameterizedType("java.util.Map<java.lang.String,java.lang.Integer>");
            assertEquals(Map.class, mapType.getRawType());

            // 测试复杂嵌套泛型
            ParameterizedType complexType = ClassUtils.getParameterizedType(
                    "java.util.Map<java.util.List<java.lang.String>,java.lang.Integer>");
            assertEquals(Map.class, complexType.getRawType());
        }
    }

    @Nested
    @DisplayName("类加载测试")
    class ClassLoadTest {

        @Test
        @DisplayName("forName方法测试")
        void forName() {
            // 测试正常类加载
            assertEquals(String.class, ClassUtils.forName("java.lang.String"));
            assertEquals(List.class, ClassUtils.forName("java.util.List"));

            // 测试不存在的类
            assertThrows(RuntimeException.class, () -> ClassUtils.forName("com.nonexistent.Class"));
        }
    }

    @Nested
    @DisplayName("泛型处理测试")
    class GenericHandleTest {

        @Test
        @DisplayName("getGenericTypes方法测试")
        void getGenericTypes() {
            // 测试获取泛型类型
            Type listOfStringType = ClassUtils.getType("java.util.List<java.lang.String>");
            Class<?>[] genericTypes = ClassUtils.getGenericTypes(listOfStringType);
            assertNotNull(genericTypes);
            assertEquals(1, genericTypes.length);
            assertEquals(String.class, genericTypes[0]);

            // 测试获取多个泛型类型
            Type mapType = ClassUtils.getType("java.util.Map<java.lang.String,java.lang.Integer>");
            Class<?>[] mapGenericTypes = ClassUtils.getGenericTypes(mapType);
            assertNotNull(mapGenericTypes);
            assertEquals(2, mapGenericTypes.length);
            assertEquals(String.class, mapGenericTypes[0]);
            assertEquals(Integer.class, mapGenericTypes[1]);

            // 测试非泛型类型
            Class<?>[] nonGenericTypes = ClassUtils.getGenericTypes(String.class);
            assertNull(nonGenericTypes);

            // 测试复杂嵌套泛型（应该抛出异常）
            Type complexType = ClassUtils.getType("java.util.Map<java.util.List<java.lang.String>,java.lang.Integer>");
            assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericTypes(complexType));
        }
    }

    // 测试用的内部类
    public static class TestInnerClass {
        private String value;

        public TestInnerClass() {
            this.value = "test";
        }

        public String getValue() {
            return value;
        }

        // 静态内部类
        public static class StaticInner {
            public String getMessage() {
                return "static inner";
            }
        }

        // 非静态内部类
        public class NonStaticInner {
            public String getOuterValue() {
                return value;
            }
        }
    }

    // 带泛型的测试类
    public static class TestGenericClass<T> {
        private T value;

        public TestGenericClass(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}