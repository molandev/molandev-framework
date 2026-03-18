package com.molandev.framework.lock.exception;

/**
 * 锁超时异常
 */
public class LockTimeoutException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 消息
     */
    public LockTimeoutException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 消息
     * @param cause   原因
     */
    public LockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
