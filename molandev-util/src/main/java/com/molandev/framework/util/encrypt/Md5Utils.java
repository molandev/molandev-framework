package com.molandev.framework.util.encrypt;


import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5工具
 */
public class Md5Utils {

    /**
     * 默认字符集
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * md 5
     */
    public static final String MD_5 = "MD5";

    /**
     * 字符串md5 ，UTF-8
     *
     * @param content 内容
     * @return {@link String}
     */
    public static String md5(String content) {
        return md5(content, DEFAULT_CHARSET);
    }

    /**
     * 指定charset的md5
     */
    public static String md5(String content, String charset) {
        try {
            return md5(content.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件md5
     */
    public static String md5(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest m = MessageDigest.getInstance(MD_5);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                m.update(buffer, 0, length);
            }
            return toHex(m.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * byte进行md5
     *
     * @param bytes 字节
     * @return {@link String}
     */
    public static String md5(byte[] bytes) {
        try {
            MessageDigest m = MessageDigest.getInstance(MD_5);
            m.update(bytes);
            byte[] s = m.digest();
            return toHex(s);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 十六进制
     */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int var3 = bytes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte b = bytes[var4];
            String s = Integer.toHexString(255 & b);
            if (s.length() < 2) {
                sb.append("0");
            }

            sb.append(s);
        }

        return sb.toString();
    }

}
