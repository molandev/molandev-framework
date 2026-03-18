package com.molandev.framework.util.encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA 加密工具类，支持 SHA-1、SHA-256、SHA-512
 */
public class ShaUtil {

    // 加密算法常量
    public static final String SHA_1 = "SHA-1";
    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";

    /**
     * 对字符串进行 SHA 加密
     *
     * @param input    待加密的字符串
     * @param algorithm 加密算法（SHA-1/SHA-256/SHA-512）
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String input, String algorithm) {
        if (input == null) {
            throw new IllegalArgumentException("输入字符串不能为null");
        }
        try {
            // 获取消息摘要实例
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            // 计算哈希值（使用UTF-8编码）
            byte[] hashBytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 转换为十六进制字符串
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的加密算法: " + algorithm, e);
        }
    }

    /**
     * 对文件进行 SHA 加密
     *
     * @param file     待加密的文件
     * @param algorithm 加密算法（SHA-1/SHA-256/SHA-512）
     * @return 加密后的十六进制字符串
     */
    public static String encryptFile(File file, String algorithm) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件");
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                // 分块读取文件并更新哈希
                while ((bytesRead = fis.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hashBytes = messageDigest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的加密算法: " + algorithm, e);
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            // 转换为无符号整数（0-255）
            int unsignedByte = b & 0xFF;
            // 小于16的数补前导0（如1→01）
            if (unsignedByte < 16) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(unsignedByte));
        }
        return hexString.toString();
    }

    /**
     * SHA-1 加密
     *
     * @param input 待加密字符串
     * @return 加密结果
     */
    public static String sha1(String input) {
        return encrypt(input, SHA_1);
    }

    /**
     * SHA-256 加密
     *
     * @param input 待加密字符串
     * @return 加密结果
     */
    public static String sha256(String input) {
        return encrypt(input, SHA_256);
    }

    /**
     * SHA-512 加密
     *
     * @param input 待加密字符串
     * @return 加密结果
     */
    public static String sha512(String input) {
        return encrypt(input, SHA_512);
    }

}