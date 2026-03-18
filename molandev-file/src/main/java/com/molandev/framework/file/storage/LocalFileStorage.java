package com.molandev.framework.file.storage;

import com.molandev.framework.file.FileStorage;
import com.molandev.framework.file.config.FileProperties;
import com.molandev.framework.file.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 本地文件存储实现
 *
 * @author molandev
 */
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final FileProperties properties;
    private final Path basePath;

    public LocalFileStorage(FileProperties properties) {
        this.properties = properties;
        this.basePath = Paths.get(properties.getLocal().getBasePath()).toAbsolutePath();
        initBasePath();
    }

    /**
     * 初始化基础路径
     */
    private void initBasePath() {
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("已创建基础目录: {}", basePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("创建基础目录失败: " + basePath, e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        return upload(inputStream, properties.getDefaultBucket(), path);
    }

    @Override
    public String upload(InputStream inputStream, String bucket, String path) {
        Path targetPath = resolveFilePath(bucket, path);

        try {
            // 创建父目录
            Files.createDirectories(targetPath.getParent());

            // 写入文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("文件上传成功: {}", targetPath);
            return path;
        } catch (IOException e) {
            throw new FileStorageException("上传文件失败: " + path, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }

    @Override
    public InputStream download(String path) {
        return download(properties.getDefaultBucket(), path);
    }

    @Override
    public InputStream download(String bucket, String path) {
        Path filePath = resolveFilePath(bucket, path);

        if (!Files.exists(filePath)) {
//            return null;
            throw new FileStorageException("文件不存在: " + path);
        }

        try {
            return new BufferedInputStream(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new FileStorageException("下载文件失败: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        delete(properties.getDefaultBucket(), path);
    }

    @Override
    public void delete(String bucket, String path) {
        Path filePath = resolveFilePath(bucket, path);

        try {
            Files.deleteIfExists(filePath);
            log.debug("文件已删除: {}", filePath);
        } catch (IOException e) {
            throw new FileStorageException("删除文件失败: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return exists(properties.getDefaultBucket(), path);
    }

    @Override
    public boolean exists(String bucket, String path) {
        Path filePath = resolveFilePath(bucket, path);
        return Files.exists(filePath);
    }

    @Override
    public List<String> list(String prefix) {
        return list(properties.getDefaultBucket(), prefix);
    }

    @Override
    public List<String> list(String bucket, String prefix) {
        Path dirPath = resolveFilePath(bucket, prefix != null ? prefix : "");

        if (!Files.exists(dirPath)) {
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            Path bucketPath = resolveBucketPath(bucket);
            return paths
                    .filter(Files::isRegularFile)
                    .map(bucketPath::relativize)
                    .map(Path::toString)
                    .map(p -> p.replace("\\", "/"))  // Windows路径转换
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileStorageException("列出文件失败，前缀: " + prefix, e);
        }
    }

    /**
     * 解析文件完整路径
     */
    private Path resolveFilePath(String bucket, String path) {
        Path bucketPath = resolveBucketPath(bucket);
        return bucketPath.resolve(path).normalize();
    }

    /**
     * 解析bucket路径
     */
    private Path resolveBucketPath(String bucket) {
        if (bucket == null || bucket.isEmpty()) {
            bucket = properties.getDefaultBucket();
        }
        return basePath.resolve(bucket);
    }
}
