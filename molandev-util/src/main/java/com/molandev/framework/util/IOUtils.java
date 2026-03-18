package com.molandev.framework.util;


import java.io.*;

/**
 *
 */
public class IOUtils {

    /**
     * 默认字符集
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 读取为字符串
     *
     * @param in 在
     * @return {@link String}
     */
    public static String readToString(InputStream in) {
        return readToString(in, DEFAULT_CHARSET);
    }

    /**
     * 读取为字符串
     *
     * @param in      在
     * @param charset 字符集
     * @return {@link String}
     */
    public static String readToString(InputStream in, String charset) {
        try {
            return new String(readToBytes(in), charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取bytes
     *
     * @param in 在
     * @return {@link byte[]}
     */
    public static byte[] readToBytes(InputStream in) {
        byte[] bytes = new byte[0];
        byte[] data = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(data)) >= 0) {
                byte[] newArray = new byte[bytes.length + read];
                System.arraycopy(bytes, 0, newArray, 0, bytes.length);
                System.arraycopy(data, 0, newArray, bytes.length, read);
                bytes = newArray;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }


        return bytes;
    }

    /**
     * 写入流
     *
     * @param content 内容
     * @param out     出
     */
    public static void writeToStream(String content, OutputStream out) {
        writeToStream(content, out, DEFAULT_CHARSET);
    }

    /**
     * 写入字符串
     *
     * @param content 内容
     * @param out     出
     * @param charset 字符集
     */
    public static void writeToStream(String content, OutputStream out, String charset) {
        try {
            writeToStream(content.getBytes(charset), out);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写入数据
     *
     * @param bytes        字节
     * @param outputStream 输出流
     */
    public static void writeToStream(byte[] bytes, OutputStream outputStream) {

        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * 读和写并行
     *
     * @param in           在
     * @param outputStream 输出流
     */
    public static void readAndWrite(InputStream in, OutputStream outputStream) {
        byte[] data = new byte[1024];
        int read = 0;

        try {
            while ((read = in.read(data)) > 0) {
                outputStream.write(data, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }

    }

    public static InputStream toInputStream(String input, String charsetName) {

        try {
            byte[] bytes = input.getBytes(charsetName);
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }

    }

    public static String readFromClassPath(String templatePath) {
        String path = templatePath;
        if (templatePath.startsWith("classpath:")) {
            path = templatePath.substring("classpath:".length());
        }

        InputStream inputStream = IOUtils.class.getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new RuntimeException("文件未找到: " + templatePath);
        }

        return readToString(inputStream);
    }
}