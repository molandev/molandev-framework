package com.molandev.framework.rpc.util;

import com.molandev.framework.util.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MultipartFile 构造器工具类
 * 用于在非 HTTP 请求场景下构造 MultipartFile 对象，方便 Feign 文件上传
 * 
 * <p><b>内存安全警告：</b></p>
 * <ul>
 *   <li>fromBytes/fromString：适用于小文件（< 10MB），直接加载到内存</li>
 *   <li>fromFile/fromInputStream（带 size 参数）：适用于大文件，流式传输，避免内存溢出</li>
 * </ul>
 */
public class MultipartFileBuilder {

    /**
     * 从字节数组构造 MultipartFile（小文件适用）
     * @param bytes 文件字节数组
     * @param originalFilename 原始文件名
     * @param contentType MIME 类型，如 "text/plain", "image/jpeg"
     */
    public static MultipartFile fromBytes(byte[] bytes, String originalFilename, String contentType) {
        return new ByteArrayMultipartFile(bytes, originalFilename, contentType);
    }

    /**
     * 从字节数组构造 MultipartFile（小文件适用，自动推断 MIME 类型）
     * @param bytes 文件字节数组
     * @param originalFilename 原始文件名，根据文件扩展名自动推断 MIME 类型
     */
    public static MultipartFile fromBytes(byte[] bytes, String originalFilename) {
        String contentType = FileUtils.getContentType(originalFilename);
        return new ByteArrayMultipartFile(bytes, originalFilename, contentType);
    }

    /**
     * 从字符串构造 MultipartFile（小文件适用）
     * @param content 文件内容
     * @param originalFilename 原始文件名
     */
    public static MultipartFile fromString(String content, String originalFilename) {
        return fromBytes(content.getBytes(), originalFilename, "text/plain");
    }

    /**
     * 从本地文件构造 MultipartFile（流式传输，大文件安全）
     * @param file 本地文件对象
     * @throws IOException 文件不存在或读取失败
     */
    public static MultipartFile fromFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("不是一个有效的文件: " + file);
        }
        return fromFile(file.toPath());
    }

    /**
     * 从本地文件构造 MultipartFile（流式传输，大文件安全）
     * @param filePath 本地文件路径
     * @throws IOException 文件不存在或读取失败
     */
    public static MultipartFile fromFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }
        long fileSize = Files.size(filePath);
        String fileName = filePath.getFileName().toString();
        String contentType = FileUtils.getContentType(fileName);
        
        // 使用流式传输，避免大文件内存溢出
        InputStream inputStream = Files.newInputStream(filePath);
        return new InputStreamMultipartFile(inputStream, fileName, contentType, fileSize);
    }

    /**
     * 从 InputStream 构造 MultipartFile（流式传输，大文件安全）
     * @param inputStream 输入流
     * @param originalFilename 原始文件名
     * @param contentType MIME 类型
     * @param size 文件大小（字节数），用于流式传输
     */
    public static MultipartFile fromInputStream(InputStream inputStream, String originalFilename, 
                                                String contentType, long size) {
        return new InputStreamMultipartFile(inputStream, originalFilename, contentType, size);
    }

    /**
     * 从 InputStream 构造 MultipartFile（不推荐：会加载到内存）
     * <p><b>警告：</b>此方法会将整个流读入内存，大文件可能导致 OOM。</p>
     * <p>推荐使用 {@link #fromInputStream(InputStream, String, String, long)} 并提供文件大小。</p>
     * 
     * @param inputStream 输入流
     * @param originalFilename 原始文件名
     * @param contentType MIME 类型
     * @deprecated 使用 {@link #fromInputStream(InputStream, String, String, long)} 替代
     */
    @Deprecated
    public static MultipartFile fromInputStreamUnsafe(InputStream inputStream, String originalFilename, 
                                                      String contentType) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return fromBytes(bytes, originalFilename, contentType);
    }

    /**
     * ByteArrayMultipartFile 实现类（基于内存）
     * 适用于小文件
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String originalFilename;
        private final String contentType;

        public ByteArrayMultipartFile(byte[] content, String originalFilename, String contentType) {
            this.content = content;
            this.name = "file"; // 默认参数名，对应 @RequestPart("file")
            this.originalFilename = originalFilename;
            this.contentType = contentType != null ? contentType : "application/octet-stream";
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }

    /**
     * InputStreamMultipartFile 实现类（基于流）
     * 适用于大文件，避免内存溢出
     * 
     * <p><b>重要：</b>InputStream 只能读取一次，调用后会关闭。</p>
     */
    private static class InputStreamMultipartFile implements MultipartFile {
        private final InputStream inputStream;
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final long size;
        private boolean consumed = false;

        public InputStreamMultipartFile(InputStream inputStream, String originalFilename, 
                                       String contentType, long size) {
            this.inputStream = inputStream;
            this.name = "file";
            this.originalFilename = originalFilename;
            this.contentType = contentType != null ? contentType : "application/octet-stream";
            this.size = size;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return size == 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() throws IOException {
            // 警告：此方法会将流加载到内存，仅在必要时调用
            if (consumed) {
                throw new IllegalStateException("输入流已被消费，不能重复读取");
            }
            try {
                byte[] bytes = inputStream.readAllBytes();
                consumed = true;
                return bytes;
            } finally {
                inputStream.close();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (consumed) {
                throw new IllegalStateException("输入流已被消费，不能重复读取");
            }
            consumed = true;
            return inputStream;
        }

        @Override
        public void transferTo(File dest) throws IOException {
            if (consumed) {
                throw new IllegalStateException("输入流已被消费，不能重复读取");
            }
            try (OutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                consumed = true;
            } finally {
                inputStream.close();
            }
        }
    }
}
