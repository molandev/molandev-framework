package com.molandev.framework.lock.exception;

/**
 * 幂等异常
 */
public class IdempotentException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param message 消息
     */
    public IdempotentException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 消息
     * @param cause   原因
     */
    public IdempotentException(String message, Throwable cause) {
        super(message, cause);
    }

}
