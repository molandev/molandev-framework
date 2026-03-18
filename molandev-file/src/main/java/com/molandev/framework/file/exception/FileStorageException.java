package com.molandev.framework.file.exception;

/**
 * 文件存储异常
 *
 * @author molandev
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
