package com.molandev.framework.file;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储统一接口
 * 支持本地文件系统和S3对象存储的无缝切换
 *
 * @author molandev
 */
public interface FileStorage {

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param path        文件存储路径（相对路径，如: "images/avatar.jpg"）
     * @return 文件访问路径或URL
     */
    String upload(InputStream inputStream, String path);

    /**
     * 上传文件到指定bucket
     *
     * @param inputStream 文件输入流
     * @param bucket      bucket名称（本地存储时作为子目录）
     * @param path        文件存储路径
     * @return 文件访问路径或URL
     */
    String upload(InputStream inputStream, String bucket, String path);

    /**
     * 上传文件到指定bucket（已知文件大小）
     * 用于大文件上传优化，避免SDK需要缓冲整个流来计算长度
     *
     * @param inputStream 文件输入流
     * @param bucket      bucket名称
     * @param path        文件存储路径
     * @param contentLength 文件大小（字节）
     * @return 文件访问路径或URL
     */
    default String upload(InputStream inputStream, String bucket, String path, long contentLength) {
        // 默认实现忽略长度参数，子类可以重写优化
        return upload(inputStream, bucket, path);
    }

    /**
     * 下载文件
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    InputStream download(String path);

    /**
     * 从指定bucket下载文件
     *
     * @param bucket bucket名称
     * @param path   文件路径
     * @return 文件输入流
     */
    InputStream download(String bucket, String path);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void delete(String path);

    /**
     * 删除指定bucket中的文件
     *
     * @param bucket bucket名称
     * @param path   文件路径
     */
    void delete(String bucket, String path);

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(String path);

    /**
     * 判断指定bucket中的文件是否存在
     *
     * @param bucket bucket名称
     * @param path   文件路径
     * @return 是否存在
     */
    boolean exists(String bucket, String path);

    /**
     * 列出指定前缀的所有文件
     *
     * @param prefix 文件路径前缀
     * @return 文件路径列表
     */
    List<String> list(String prefix);

    /**
     * 列出指定bucket中指定前缀的所有文件
     *
     * @param bucket bucket名称
     * @param prefix 文件路径前缀
     * @return 文件路径列表
     */
    List<String> list(String bucket, String prefix);
}
