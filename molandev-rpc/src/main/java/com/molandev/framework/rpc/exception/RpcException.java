package com.molandev.framework.rpc.exception;

import lombok.Getter;

/**
 * 框架层异常：Feign接口映射冲突、本地实现类缺失、模式配置错误、Feign代理生成失败等
 */
@Getter
public class RpcException extends RuntimeException {

    private final Integer code;

    public RpcException(String message) {
        super(message);
        this.code = 5000;
    }

    public RpcException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
        this.code = 5000;
    }

    public RpcException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
