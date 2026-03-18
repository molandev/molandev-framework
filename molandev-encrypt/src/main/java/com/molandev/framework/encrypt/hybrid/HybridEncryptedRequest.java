package com.molandev.framework.encrypt.hybrid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双层加密请求数据包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HybridEncryptedRequest {

    /**
     * 加密后的数据
     */
    private String data;

    /**
     * RSA加密的AES密钥
     */
    private String encryptedKey;

}
