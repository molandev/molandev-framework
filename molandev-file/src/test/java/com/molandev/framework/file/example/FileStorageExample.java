package com.molandev.framework.file.example;

import com.molandev.framework.file.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * FileStorage 使用示例
 * 展示各种文件上传场景的最佳实践
 *
 * @author molandev
 */
@Service
public class FileStorageExample {

    @Autowired
    private FileStorage fileStorage;

    /**
     * 示例1：上传小文件（文本、配置文件等）
     * 适用场景：文件内容已在内存中，文件较小（< 10MB）
     */
    public String uploadSmallFile(String content, String path) {
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return fileStorage.upload(inputStream, path);
    }

    /**
     * 示例2：上传 MultipartFile（最常见的Web上传场景）
     * 适用场景：用户通过表单上传文件
     */
    public String uploadFromMultipartFile(MultipartFile file, String targetPath) throws IOException {
        // 方式1：未知大小的流式上传（适用于所有场景）
        return fileStorage.upload(file.getInputStream(), targetPath);
    }

    /**
     * 示例3：上传大文件（已知大小）- 推荐方式
     * 适用场景：上传大文件（视频、压缩包等），已知文件大小
     * 优势：SDK可以优化上传策略，避免内存溢出
     */
    public String uploadLargeFileWithSize(MultipartFile file, String targetPath) throws IOException {
        long fileSize = file.getSize();
        return fileStorage.upload(file.getInputStream(), "default", targetPath, fileSize);
    }

    /**
     * 示例4：上传本地文件
     * 适用场景：服务器本地文件需要上传到对象存储
     */
    public String uploadLocalFile(Path localFilePath, String targetPath) throws IOException {
        long fileSize = Files.size(localFilePath);
        try (InputStream inputStream = Files.newInputStream(localFilePath)) {
            return fileStorage.upload(inputStream, "default", targetPath, fileSize);
        }
    }

    /**
     * 示例5：批量上传文件
     * 适用场景：批量导入、迁移等
     */
    public List<String> uploadBatch(List<MultipartFile> files, String directory) {
        return files.stream()
                .map(file -> {
                    try {
                        String path = directory + "/" + file.getOriginalFilename();
                        return uploadLargeFileWithSize(file, path);
                    } catch (IOException e) {
                        throw new RuntimeException("Upload failed: " + file.getOriginalFilename(), e);
                    }
                })
                .toList();
    }

    /**
     * 示例6：多租户场景 - 不同租户使用不同bucket
     * 适用场景：SaaS系统，数据隔离
     */
    public String uploadForTenant(String tenantId, MultipartFile file) throws IOException {
        String bucket = "tenant-" + tenantId;
        String path = "uploads/" + file.getOriginalFilename();
        long fileSize = file.getSize();
        return fileStorage.upload(file.getInputStream(), bucket, path, fileSize);
    }

    /**
     * 示例7：上传并生成缩略图（业务逻辑示例）
     * 适用场景：图片上传需要处理
     */
    public String uploadImageWithThumbnail(MultipartFile image, String basePath) throws IOException {
        // 上传原图
        String originalPath = basePath + "/original/" + image.getOriginalFilename();
        fileStorage.upload(image.getInputStream(), originalPath);

        // 这里可以生成缩略图（示例省略）
        // byte[] thumbnail = generateThumbnail(image);
        // String thumbnailPath = basePath + "/thumbnail/" + image.getOriginalFilename();
        // fileStorage.upload(new ByteArrayInputStream(thumbnail), thumbnailPath);

        return originalPath;
    }

    /**
     * 示例8：流式下载大文件（避免内存溢出）
     * 适用场景：下载大文件到客户端
     */
    public void downloadLargeFile(String path, OutputStream outputStream) throws IOException {
        try (InputStream inputStream = fileStorage.download(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 示例9：检查文件是否存在（避免重复上传）
     */
    public String uploadIfNotExists(MultipartFile file, String path) throws IOException {
        if (fileStorage.exists(path)) {
            return path; // 返回已存在的文件路径
        }
        return uploadLargeFileWithSize(file, path);
    }

    /**
     * 示例10：列出目录下所有文件
     * 适用场景：文件管理、清理等
     */
    public List<String> listUserFiles(String userId) {
        String prefix = "users/" + userId + "/";
        return fileStorage.list(prefix);
    }

    /**
     * 示例11：删除过期文件
     * 适用场景：定时清理任务
     */
    public void deleteExpiredFiles(List<String> expiredPaths) {
        expiredPaths.forEach(path -> {
            if (fileStorage.exists(path)) {
                fileStorage.delete(path);
            }
        });
    }

    /**
     * 示例12：上传临时文件（使用完后删除）
     */
    public void processTemporaryFile(InputStream content, String tempPath) {
        try {
            // 上传
            fileStorage.upload(content, tempPath);
            
            // 处理文件...
            
        } finally {
            // 清理
            fileStorage.delete(tempPath);
        }
    }
}
