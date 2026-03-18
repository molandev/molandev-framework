package com.molandev.framework.encrypt.password;

import com.molandev.framework.util.RandomUtils;
import com.molandev.framework.util.encrypt.DesUtil;
import com.molandev.framework.util.encrypt.ShaUtil;

/**
 * 密码编码器
 */
public class MolanPasswordEncoder {

    /**
     * 盐长度
     */
    private static final int SALT_LENGTH = 7;

    /**
     * 加密后的盐长度
     */
    private static final int SALT_ENCODED_LENGTH = 16;
    /**
     * sha256编码长度
     */
    private static final int SHA256_ENCODED_LENGTH = 64;

    private final String key;

    public MolanPasswordEncoder(String key) {
        this.key = key;
    }

    /**
     * 获取随机盐值，以保证每次加密的密码不一样，防止暴力破解
     */
    private static String getSalt() {
        return RandomUtils.randomString(SALT_LENGTH);
    }

    /**
     * 加密密码
     * 生成随机七位盐值，然后使用des加密，生成16位加密字符串
     * 使用sha256加密password+salt 生成64位加密字符串
     * 最终返回des加密的字符串+上述64位字符串，共计80位
     */
    public String encode(String password) {
        String salt = getSalt();
        String encrypt = DesUtil.encrypt(salt, key).toLowerCase();
        String encrypt1 = ShaUtil.sha256(password + salt);
        return encrypt + encrypt1;
    }

    /**
     * 校验密码是否匹配
     *
     * @param password        密码明文
     * @param encodedPassword 加密后的密码密文
     */
    public boolean notMatch(String password, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() != SALT_ENCODED_LENGTH + SHA256_ENCODED_LENGTH) {
            return true;
        }
        try {
            String encryptSalt = encodedPassword.substring(0, 16);
            String encryptContent = encodedPassword.substring(16);
            String salt = DesUtil.decrypt(encryptSalt, key);
            String encrypt = ShaUtil.sha256(password + salt);
            if (encrypt.equals(encryptContent)) {
                return false;
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

}