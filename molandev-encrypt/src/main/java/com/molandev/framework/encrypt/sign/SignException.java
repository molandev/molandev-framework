package com.molandev.framework.encrypt.sign;

/**
 * 签名异常
 */
public class SignException extends RuntimeException {

    public SignException(String message) {
        super(message);
    }

    public SignException(String message, Throwable cause) {
        super(message, cause);
    }

}
