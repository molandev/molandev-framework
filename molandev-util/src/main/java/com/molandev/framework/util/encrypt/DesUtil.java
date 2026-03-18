package com.molandev.framework.util.encrypt;


import com.molandev.framework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * DES加密处理工具类
 */
public class DesUtil {

    private static final String DES = "DES";

    /**
     * 默认的密码算法
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "DES/ECB/PKCS5Padding";

    /**
     * 修复password16
     *
     * @param password 密码
     * @return {@link String}
     */
    private static String fixPassword8(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("DES密钥不能为空");
        }
        if (password.length() > 8) {
            throw new IllegalArgumentException("DES密钥超出8位长度");
        }
        password = password + StringUtils.getRepeatStrs("0", 8 - password.length());
        return password;
    }

    /**
     * 加密
     *
     * @param content  内容
     * @param password 密码
     * @return {@link String}
     */
    public static String encrypt(String content, String password) {
        return encrypt(content, password,DEFAULT_CIPHER_ALGORITHM);
    }

    /**
     * 加密
     *
     * @param content  内容
     * @param password 密码
     * @return {@link String}
     */
    public static String encrypt(String content, String password, String cipherAlgorithm) {
        return des(content, password, Cipher.ENCRYPT_MODE, cipherAlgorithm);
    }


    /**
     * 解密
     *
     * @param content  内容
     * @param password 密码
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String password) {
        return decrypt(content, password,  DEFAULT_CIPHER_ALGORITHM);
    }


    /**
     *
     * @param content   内容
     * @param password  密钥
     * @param algorithm 算法
     * @return 解密后的字符串
     */
    public static String decrypt(String content, String password, String algorithm) {
        return des(content, password, Cipher.DECRYPT_MODE, algorithm);
    }


    /**
     * des DES加密/解密公共方法
     *
     * @param content         字符串内容
     * @param password        密钥
     * @param type            加密：{@link Cipher#ENCRYPT_MODE}，解密：{@link Cipher#DECRYPT_MODE}
     * @param cipherAlgorithm 密码算法
     * @return {@link String}
     */
    public static String des(String content, String password, int type, String cipherAlgorithm) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(fixPassword8(password).getBytes(StandardCharsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

            // 算法名称/加密模式/填充方式
            // DES共有四种工作模式-->>ECB：电子密码本模式、CBC：加密分组链接模式、CFB：加密反馈模式、OFB：输出反馈模式
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(type, keyFactory.generateSecret(desKey), random);

            if (type == Cipher.ENCRYPT_MODE) {
                byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
                return Hex2Util.parseByte2HexStr(cipher.doFinal(byteContent));
            } else {
                byte[] byteContent = Hex2Util.parseHexStr2Byte(content);
                return new String(cipher.doFinal(byteContent));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("DES解密异常", e);
        }
    }

}
