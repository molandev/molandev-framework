package com.molandev.framework.file.storage;

import com.molandev.framework.file.FileStorage;
import com.molandev.framework.file.config.FileProperties;
import com.molandev.framework.file.exception.FileStorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalFileStorage 单元测试
 *
 * @author molandev
 */
class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private FileStorage fileStorage;
    private FileProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileProperties();
        properties.setType(FileProperties.StorageType.LOCAL);
        properties.getLocal().setBasePath(tempDir.toString());
        properties.setDefaultBucket("test-bucket");

        fileStorage = new LocalFileStorage(properties);
    }

    @Test
    void testUploadAndDownload() throws IOException {
        // 准备测试数据
        String content = "Hello, World!";
        String path = "test/hello.txt";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // 上传文件
        String result = fileStorage.upload(inputStream, path);
        assertNotNull(result);
        assertEquals(path, result);

        // 下载文件并验证内容
        try (InputStream downloaded = fileStorage.download(path)) {
            String downloadedContent = new String(downloaded.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, downloadedContent);
        }
    }

    @Test
    void testUploadWithBucket() throws IOException {
        String content = "Test content";
        String bucket = "custom-bucket";
        String path = "file.txt";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        // 上传到自定义bucket
        String result = fileStorage.upload(inputStream, bucket, path);
        assertNotNull(result);
        assertEquals(path, result);

        // 从自定义bucket下载
        try (InputStream downloaded = fileStorage.download(bucket, path)) {
            String downloadedContent = new String(downloaded.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, downloadedContent);
        }
    }

    @Test
    void testExists() {
        String path = "test-exists.txt";
        InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

        // 文件不存在
        assertFalse(fileStorage.exists(path));

        // 上传后存在
        fileStorage.upload(inputStream, path);
        assertTrue(fileStorage.exists(path));
    }

    @Test
    void testDelete() {
        String path = "test-delete.txt";
        InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

        // 上传文件
        fileStorage.upload(inputStream, path);
        assertTrue(fileStorage.exists(path));

        // 删除文件
        fileStorage.delete(path);
        assertFalse(fileStorage.exists(path));
    }

    @Test
    void testList() {
        String prefix = "list-test/";
        
        // 上传多个文件
        fileStorage.upload(new ByteArrayInputStream("1".getBytes()), prefix + "file1.txt");
        fileStorage.upload(new ByteArrayInputStream("2".getBytes()), prefix + "file2.txt");
        fileStorage.upload(new ByteArrayInputStream("3".getBytes()), prefix + "sub/file3.txt");

        // 列出文件
        List<String> files = fileStorage.list(prefix);
        assertEquals(3, files.size());
        assertTrue(files.stream().anyMatch(f -> f.contains("file1.txt")));
        assertTrue(files.stream().anyMatch(f -> f.contains("file2.txt")));
        assertTrue(files.stream().anyMatch(f -> f.contains("file3.txt")));
    }

    @Test
    void testDownloadNonExistentFile() {
        assertThrows(FileStorageException.class, () -> {
            fileStorage.download("non-existent.txt");
        });
    }

    @Test
    void testUploadCreatesParentDirectories() throws IOException {
        String path = "deep/nested/directory/file.txt";
        String content = "test";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        fileStorage.upload(inputStream, path);
        assertTrue(fileStorage.exists(path));

        try (InputStream downloaded = fileStorage.download(path)) {
            String downloadedContent = new String(downloaded.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, downloadedContent);
        }
    }
}
