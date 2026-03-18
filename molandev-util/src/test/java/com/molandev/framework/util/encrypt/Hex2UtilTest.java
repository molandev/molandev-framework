package com.molandev.framework.util.encrypt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("十六进制工具类测试")
class Hex2UtilTest {

    @Nested
    @DisplayName("字节转十六进制测试")
    class ParseByte2HexStrTest {

        @Test
        @DisplayName("测试正常字节转十六进制")
        void testParseByte2HexStr() {
            byte[] bytes = {(byte) 0xFF, (byte) 0x00, (byte) 0x7F, (byte) 0x80};
            String hexStr = Hex2Util.parseByte2HexStr(bytes);
            assertEquals("FF007F80", hexStr, "字节转十六进制结果应正确");
        }

        @Test
        @DisplayName("测试空字节数组转十六进制")
        void testParseEmptyByte2HexStr() {
            byte[] bytes = new byte[0];
            String hexStr = Hex2Util.parseByte2HexStr(bytes);
            assertEquals("", hexStr, "空字节数组转十六进制应为空字符串");
        }

        @Test
        @DisplayName("测试单字节转十六进制")
        void testParseSingleByte2HexStr() {
            byte[] bytes = {(byte) 0x0A};
            String hexStr = Hex2Util.parseByte2HexStr(bytes);
            assertEquals("0A", hexStr, "单字节转十六进制应正确");
        }
    }

    @Nested
    @DisplayName("十六进制转字节测试")
    class ParseHexStr2ByteTest {

        @Test
        @DisplayName("测试正常十六进制转字节")
        void testParseHexStr2Byte() {
            String hexStr = "FF007F80";
            byte[] bytes = Hex2Util.parseHexStr2Byte(hexStr);
            assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x7F, (byte) 0x80}, bytes, "十六进制转字节结果应正确");
        }

        @Test
        @DisplayName("测试空十六进制字符串转字节")
        void testParseEmptyHexStr2Byte() {
            String hexStr = "";
            byte[] bytes = Hex2Util.parseHexStr2Byte(hexStr);
            assertArrayEquals(new byte[0], bytes, "空十六进制字符串转字节应得到空字节数组");
        }

        @Test
        @DisplayName("测试null十六进制字符串转字节")
        void testParseNullHexStr2Byte() {
            String hexStr = null;
            byte[] bytes = Hex2Util.parseHexStr2Byte(hexStr);
            assertArrayEquals(new byte[0], bytes, "null十六进制字符串转字节应得到空字节数组");
        }

        @Test
        @DisplayName("测试小写十六进制转字节")
        void testParseLowerCaseHexStr2Byte() {
            String hexStr = "ff007f80";
            byte[] bytes = Hex2Util.parseHexStr2Byte(hexStr);
            assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x7F, (byte) 0x80}, bytes, "小写十六进制转字节结果应正确");
        }
    }

    @Nested
    @DisplayName("转换一致性测试")
    class ConversionConsistencyTest {

        @Test
        @DisplayName("测试字节与十六进制互转一致性")
        void testByteHexConversionConsistency() {
            byte[] originalBytes = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
            String hexStr = Hex2Util.parseByte2HexStr(originalBytes);
            byte[] convertedBytes = Hex2Util.parseHexStr2Byte(hexStr);
            assertArrayEquals(originalBytes, convertedBytes, "字节与十六进制互转应保持一致性");
        }
    }
}