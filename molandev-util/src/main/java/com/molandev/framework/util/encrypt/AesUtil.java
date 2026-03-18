package com.molandev.framework.util.encrypt;

import com.molandev.framework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * AES加密处理工具类
 */
public class AesUtil {

    private static final String AES = "AES";

    /**
     * key长度
     */
    private static final int KEY_LENGTH = 16;

    /**
     * 密码算法
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param content  内容
     * @param password 密码
     * @return {@link String}
     * @see AesUtil#encrypt(String, String, String)
     */
    public static String encrypt(String content, String password) {
        return encrypt(content, password, CIPHER_ALGORITHM);
    }

    /**
     * 解密
     *
     * @param content  内容
     * @param password 密码
     * @return {@link String}
     * @see AesUtil#decrypt(String, String, String)
     */
    public static String decrypt(String content, String password) {
        return decrypt(content, password, CIPHER_ALGORITHM);
    }

    /**
     * 加密
     *
     * @param content            内容
     * @param password           注解中传入的key 可为null或空字符
     * @param aesCipherAlgorithm aes密码算法
     * @return String
     */
    public static String encrypt(String content, String password, String aesCipherAlgorithm) {
        try {
            Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
            Key k = getKey(password);
            byte[] inputData = content.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, k);
            byte[] bytes = cipher.doFinal(inputData);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                 | BadPaddingException e) {
            throw new IllegalArgumentException("AES加密异常",e);
        }
    }

    /**
     * 解密
     *
     * @param content  内容
     * @param password 注解中传入的key 可为null或空字符
     * @return String
     */
    public static String decrypt(String content, String password, String aesCipherAlgorithm) {
        try {
            byte[] decoded = Base64.getDecoder().decode(content);
            Key k = getKey(password);
            Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, k);
            byte[] bytes = cipher.doFinal(decoded);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                |  BadPaddingException  | IllegalArgumentException e) {
            throw new IllegalArgumentException("AES解密异常",e);
        }
    }

    /**
     * 获取key
     *
     * @param password 密码
     */
    static SecretKeySpec getKey(String password) {
        password = fixPassword16(password);
        return new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), AES);
    }

    /**
     * 修复password16
     *
     * @param password 密码
     * @return {@link String}
     */
    static String fixPassword16(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("密钥不能为空");
        }
        if (password.length() > KEY_LENGTH) {
            throw new IllegalArgumentException("密钥超出长度");
        }
        password = password + StringUtils.getRepeatStrs("0", 16 - password.length());
        return password;
    }

}
