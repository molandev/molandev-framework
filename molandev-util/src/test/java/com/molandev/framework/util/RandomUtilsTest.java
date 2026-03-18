package com.molandev.framework.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("随机工具类测试")
public class RandomUtilsTest {

    @Test
    @DisplayName("测试随机长整型数生成")
    public void testRandomLong() {
        long min = 100L;
        long max = 200L;
        for (int i = 0; i < 100; i++) {
            long result = RandomUtils.randomLong(min, max);
            assertTrue(result >= min && result <= max, "生成的长整型数应在范围内 [" + min + ", " + max + "]");
        }
        
        assertThrows(IllegalArgumentException.class, () -> {
            RandomUtils.randomLong(10, 5); // min > max
        }, "当最小值大于最大值时应抛出IllegalArgumentException异常");
    }

    @Test
    @DisplayName("测试随机布尔值生成")
    public void testRandomBoolean() {
        boolean trueFound = false;
        boolean falseFound = false;
        
        for (int i = 0; i < 100; i++) {
            boolean result = RandomUtils.randomBoolean();
            if (result) {
                trueFound = true;
            } else {
                falseFound = true;
            }
        }
        
        assertTrue(trueFound, "应该至少生成一个true值");
        assertTrue(falseFound, "应该至少生成一个false值");
    }

    @Test
    @DisplayName("测试从数组中随机选择元素")
    public void testRandomElementFromArray() {
        String[] array = {"苹果", "香蕉", "樱桃", "葡萄"};
        String result = RandomUtils.randomElement(array);
        assertTrue(Arrays.asList(array).contains(result), "结果应该是数组中的一个元素");
        
        assertNull(RandomUtils.randomElement((String[]) null), "对于null数组应返回null");
        assertNull(RandomUtils.randomElement(new String[0]), "对于空数组应返回null");
    }

    @Test
    @DisplayName("测试从列表中随机选择元素")
    public void testRandomElementFromList() {
        List<String> list = Arrays.asList("红色", "绿色", "蓝色", "黄色");
        String result = RandomUtils.randomElement(list);
        assertTrue(list.contains(result), "结果应该是列表中的一个元素");
        
        assertNull(RandomUtils.randomElement((List<String>) null), "对于null列表应返回null");
        assertNull(RandomUtils.randomElement(Arrays.asList()), "对于空列表应返回null");
    }

    @Test
    @DisplayName("测试随机浮点数生成")
    public void testRandomDouble() {
        float min = 1.0f;
        float max = 10.0f;
        for (int i = 0; i < 100; i++) {
            double result = RandomUtils.randomDouble(min, max);
            assertTrue(result >= min && result < max, "生成的浮点数应在范围内 [" + min + ", " + max + ")");
        }

        assertThrows(IllegalArgumentException.class, () -> {
            RandomUtils.randomDouble(10.0d, 5.0d); // min > max
        }, "当最小值大于最大值时应抛出IllegalArgumentException异常");
    }

    @Test
    @DisplayName("测试随机中文字符串生成")
    public void testRandomChineseString() {
        String result = RandomUtils.randomChineseString(5);
        assertEquals(5, result.length(), "中文字符串应具有指定长度");
        
        // Check if characters are in Chinese range
        for (char c : result.toCharArray()) {
            assertTrue((c >= 0x4e00 && c <= 0x9fff), "字符应在中文Unicode范围内: " + c);
        }
    }

    @Test
    @DisplayName("测试随机手机号生成")
    public void testRandomMobileNumber() {
        String mobile = RandomUtils.randomMobileNumber();
        assertEquals(11, mobile.length(), "手机号应为11位");
        assertTrue(mobile.matches("^1[3-9]\\d{9}$"), "手机号应符合中国手机号格式: " + mobile);
    }

    @Test
    @DisplayName("测试随机邮箱地址生成")
    public void testRandomEmail() {
        String email = RandomUtils.randomEmail();
        assertTrue(email.contains("@"), "邮箱地址应包含@符号");
        assertTrue(email.length() > 5, "邮箱地址应具有合理长度");
        
        // Simple validation - should contain @ and a domain suffix
        String[] parts = email.split("@");
        assertEquals(2, parts.length, "邮箱地址应恰好包含一个@符号");
        
        // Check if email ends with one of the valid suffixes
        boolean hasValidSuffix = false;
        String[] validSuffixes = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "qq.com", 
            "163.com", "126.com", "sina.com", "sohu.com", "aliyun.com"
        };
        for (String suffix : validSuffixes) {
            if (parts[1].equals(suffix)) {
                hasValidSuffix = true;
                break;
            }
        }
        assertTrue(hasValidSuffix, "邮箱应具有有效后缀: " + email);
    }
    
    @Test
    @DisplayName("测试随机小写字符串生成")
    public void testRandomLowerString() {
        String result = RandomUtils.randomLowerString(10);
        assertEquals(10, result.length(), "小写字符串应具有指定长度");
        
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLowerCase(c), "字符应为小写: " + c);
        }
    }
    
    @Test
    @DisplayName("测试随机大写字符串生成")
    public void testRandomUpperString() {
        String result = RandomUtils.randomUpperString(10);
        assertEquals(10, result.length(), "大写字符串应具有指定长度");
        
        for (char c : result.toCharArray()) {
            assertTrue(Character.isUpperCase(c), "字符应为大写: " + c);
        }
    }
}