package com.molandev.framework.util.encrypt;

import java.util.Base64;

/**
 * Base64工具类
 */
public final class Base64Utils {

    /**
     * 编码 Encodes hex octects into Base64
     */
    public static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 解码
     */
    public static byte[] decode(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

}
