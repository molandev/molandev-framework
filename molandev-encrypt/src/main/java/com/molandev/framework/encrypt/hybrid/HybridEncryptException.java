package com.molandev.framework.encrypt.hybrid;

/**
 * 双层加密异常
 */
public class HybridEncryptException extends RuntimeException {

    public HybridEncryptException(String message) {
        super(message);
    }

    public HybridEncryptException(String message, Throwable cause) {
        super(message, cause);
    }

}
