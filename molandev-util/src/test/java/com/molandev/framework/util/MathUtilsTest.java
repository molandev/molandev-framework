package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("数学工具类测试")
class MathUtilsTest {

    @Nested
    @DisplayName("最值计算测试")
    class MinMaxTest {

        @Test
        @DisplayName("min方法测试")
        void min() {
            // 测试正常情况
            assertEquals(1, MathUtils.min(1, 2, 3));
            assertEquals(-5, MathUtils.min(10, -5, 0, 3));
            assertEquals(0, MathUtils.min(0));

            // 测试相同数值
            assertEquals(5, MathUtils.min(5, 5, 5));
        }

        @Test
        @DisplayName("max方法测试")
        void max() {
            // 测试正常情况
            assertEquals(3, MathUtils.max(1, 2, 3));
            assertEquals(10, MathUtils.max(10, -5, 0, 3));
            assertEquals(0, MathUtils.max(0));

            // 测试相同数值
            assertEquals(5, MathUtils.max(5, 5, 5));
        }
    }

    @Nested
    @DisplayName("基本运算测试")
    class BasicOperationTest {

        @Test
        @DisplayName("add方法测试")
        void add() {
            // 测试正常加法
            assertEquals(5.0, MathUtils.add(2.0, 3.0));
            assertEquals(-1.0, MathUtils.add(2.0, -3.0));
            assertEquals(0.0, MathUtils.add(-2.0, 2.0));

            // 测试浮点数精度问题
            assertEquals(0.3, MathUtils.add(0.1, 0.2), 0.0001);
        }

        @Test
        @DisplayName("sub方法测试")
        void sub() {
            // 测试正常减法
            assertEquals(1.0, MathUtils.sub(3.0, 2.0));
            assertEquals(5.0, MathUtils.sub(2.0, -3.0));
            assertEquals(-4.0, MathUtils.sub(-2.0, 2.0));

            // 测试浮点数精度问题
            assertEquals(0.1, MathUtils.sub(0.3, 0.2), 0.0001);
        }

        @Test
        @DisplayName("mul方法测试")
        void mul() {
            // 测试正常乘法
            assertEquals(6.0, MathUtils.mul(2.0, 3.0));
            assertEquals(-6.0, MathUtils.mul(2.0, -3.0));
            assertEquals(0.0, MathUtils.mul(0.0, 2.0));
            assertEquals(4.0, MathUtils.mul(-2.0, -2.0));

            // 测试浮点数精度问题
            assertEquals(0.02, MathUtils.mul(0.1, 0.2), 0.0001);
        }
    }

    @Nested
    @DisplayName("除法运算测试")
    class DivisionTest {

        @Test
        @DisplayName("div默认精度方法测试")
        void div() {
            // 测试正常除法
            assertEquals(2.0, MathUtils.div(6.0, 3.0));
            assertEquals(-2.0, MathUtils.div(6.0, -3.0));
            assertEquals(0.0, MathUtils.div(0.0, 2.0));
            assertEquals(1.0, MathUtils.div(-2.0, -2.0));

            // 测试默认精度
            assertEquals(0.3333333333, MathUtils.div(1.0, 3.0), 0.0000000001);
        }

        @Test
        @DisplayName("div指定精度方法测试")
        void divWithScale() {
            // 测试指定精度的除法
            assertEquals(0.33, MathUtils.div(1.0, 3.0, 2), 0.01);
            assertEquals(0.333, MathUtils.div(1.0, 3.0, 3), 0.001);
            assertEquals(2.0, MathUtils.div(7.0, 3.0, 0), 0.5);

            // 测试负精度异常
            assertThrows(IllegalArgumentException.class, () -> MathUtils.div(1.0, 3.0, -1));
        }
    }

    @Nested
    @DisplayName("四舍五入测试")
    class RoundTest {

        @Test
        @DisplayName("round方法测试")
        void round() {
            // 测试四舍五入
            assertEquals(1.23, MathUtils.round(1.234, 2), 0.01);
            assertEquals(1.24, MathUtils.round(1.235, 2), 0.01);
            assertEquals(1.0, MathUtils.round(1.235, 0), 0.5);
            assertEquals(0.0, MathUtils.round(0.1, 0), 0.5);

            // 测试负精度异常
            assertThrows(IllegalArgumentException.class, () -> MathUtils.round(1.234, -1));
        }
    }

    @Nested
    @DisplayName("相等判断测试")
    class EqualsTest {

        @Test
        @DisplayName("equals指定精度方法测试")
        void equalsWithScale() {
            // 测试带精度的相等判断
            assertTrue(MathUtils.equals(1.0, 1.0, 0.001));
            assertTrue(MathUtils.equals(1.0, 1.0001, 0.001));
            assertFalse(MathUtils.equals(1.0, 1.001, 0.0001));
            assertTrue(MathUtils.equals(0.1 + 0.2, 0.3, 0.0000001));
        }

        @Test
        @DisplayName("equals默认精度方法测试")
        void equals() {
            // 测试默认精度的相等判断
            assertTrue(MathUtils.equals(1.0, 1.0));
            assertTrue(MathUtils.equals(0.1 + 0.2, 0.3)); // 测试浮点数精度问题
            assertFalse(MathUtils.equals(1.0, 1.1));
        }
    }
}