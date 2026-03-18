package com.molandev.framework.file.storage;

import com.molandev.framework.file.FileStorage;
import com.molandev.framework.file.config.FileProperties;
import com.molandev.framework.file.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * S3对象存储实现
 * 支持AWS S3、阿里云OSS、腾讯云COS等兼容S3协议的对象存储
 *
 * @author molandev
 */
@Slf4j
public class S3FileStorage implements FileStorage {

    private final FileProperties properties;
    private final S3Client s3Client;

    public S3FileStorage(FileProperties properties) {
        this.properties = properties;
        this.s3Client = createS3Client();
    }

    /**
     * 创建S3客户端
     */
    private S3Client createS3Client() {
        FileProperties.S3Config s3Config = properties.getS3();
        
        if (s3Config.getAccessKey() == null || s3Config.getSecretKey() == null) {
            throw new FileStorageException("必须配置 S3 访问密钥和秘密密钥");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Config.getAccessKey(),
                s3Config.getSecretKey()
        );

        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(s3Config.getRegion()));

        // 配置自定义endpoint
        if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        // 配置路径风格访问（某些S3兼容存储需要）
        if (s3Config.isPathStyleAccess()) {
            builder.forcePathStyle(true);
        }

        S3Client client = builder.build();
        log.info("S3 客户端初始化成功");
        return client;
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        return upload(inputStream, getDefaultBucket(), path);
    }

    @Override
    public String upload(InputStream inputStream, String bucket, String path) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            // 使用流式上传，避免将整个文件读入内存
            // SDK会自动处理分块上传和流式传输
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, -1));
            
            log.debug("文件已上传到 S3: bucket={}, key={}", bucket, path);
            return path;
        } catch (Exception e) {
            throw new FileStorageException("上传文件到 S3 失败: " + path, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }

    @Override
    public String upload(InputStream inputStream, String bucket, String path, long contentLength) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .contentLength(contentLength)
                    .build();

            // 使用已知长度的流式上传，性能更优
            // SDK可以更高效地规划上传策略（如是否使用分片上传）
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            log.debug("文件已上传到 S3（已知长度）: bucket={}, key={}, size={}", 
                    bucket, path, contentLength);
            return path;
        } catch (Exception e) {
            throw new FileStorageException("上传文件到 S3 失败: " + path, e);
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
        return download(getDefaultBucket(), path);
    }

    @Override
    public InputStream download(String bucket, String path) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (NoSuchKeyException e) {
            return null;
//            throw new FileStorageException("文件在 S3 中不存在: " + path, e);
        } catch (Exception e) {
            throw new FileStorageException("从 S3 下载文件失败: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        delete(getDefaultBucket(), path);
    }

    @Override
    public void delete(String bucket, String path) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.debug("文件已从 S3 删除: bucket={}, key={}", bucket, path);
        } catch (Exception e) {
            throw new FileStorageException("从 S3 删除文件失败: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return exists(getDefaultBucket(), path);
    }

    @Override
    public boolean exists(String bucket, String path) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new FileStorageException("检查 S3 文件存在性失败: " + path, e);
        }
    }

    @Override
    public List<String> list(String prefix) {
        return list(getDefaultBucket(), prefix);
    }

    @Override
    public List<String> list(String bucket, String prefix) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucket);

            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Request listObjectsRequest = requestBuilder.build();
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);

            return response.contents().stream()
                    .map(S3Object::key)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FileStorageException("列出 S3 文件失败，前缀: " + prefix, e);
        }
    }

    /**
     * 获取默认bucket
     */
    private String getDefaultBucket() {
        String bucket = properties.getS3().getBucket();
        if (bucket == null || bucket.isEmpty()) {
            bucket = properties.getDefaultBucket();
        }
        return bucket;
    }


}
