package com.molandev.framework.encrypt.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


/**
 * 加密数据基础配置
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "molandev.encrypt")
public class EncryptProperties {

    /**
     * 数据库加密配置
     */
    private DbProperties db = new DbProperties();

    /**
     * 参数加密配置
     */
    private ParamsProperties params = new ParamsProperties();

    /**
     * 签名校验配置
     */
    private SignProperties sign = new SignProperties();

    /**
     * 双层加密配置
     */
    private HybridProperties hybrid = new HybridProperties();

    /**
     * 参数加密配置
     */
    @Getter
    @Setter
    public static class ParamsProperties {
        /**
         * 是否开启参数加密功能
         */
        private boolean enabled = false;

        /**
         * 加密方式
         */
        private ParamEncryptType type = ParamEncryptType.RSA;

        /**
         * 默认的加密算法, 当type为aes时生效
         */
        private String algorithm = "AES/ECB/PKCS5Padding";

        /**
         * 解密密钥，必传，如果是RSA，此处应为私钥
         */
        private String key;

    }

    /**
     * 数据库加密配置，目前仅作AES加密，DES不安全，RSA不方便，国密之类的我没做
     */
    @Getter
    @Setter
    public static class DbProperties {
        /**
         * 是否开启数据库加密功能
         * 开启后，字段上增加@Enc注解，即可实现保存数据库和查询时自动加解密
         */
        private boolean enabled = false;

        /**
         * 默认的加密算法
         */
        private String algorithm = "AES/ECB/PKCS5Padding";

        /**
         * 加密密钥，必传
         */
        private String key;
    }

    /**
     * 签名校验配置
     */
    @Getter
    @Setter
    public static class SignProperties {
        /**
         * 是否开启签名校验功能
         */
        private boolean enabled = false;

        /**
         * 签名密钥
         */
        private String secret;

        /**
         * 签名参数名称
         */
        private String signName = "sign";

        /**
         * 时间戳参数名称
         */
        private String timestampName = "timestamp";

        /**
         * 随机数参数名称
         */
        private String nonceName = "nonce";

        /**
         * 签名有效期，单位秒，默认5分钟
         */
        private long expireTime = 300;

        /**
         * 拦截的路由
         */
        private String urlPattern = "/*";

        /**
         * 过滤器优先级，默认 Integer.MIN_VALUE + 20，在混合加密解密之后执行
         */
        private int order = Integer.MIN_VALUE + 20;

        /**
         * 白名单，支持Ant风格路径匹配
         * 例如：/api/public/**、/health、/actuator/**
         */
        private List<String> whitelist = new ArrayList<>();
    }

    /**
     * 双层加密配置
     */
    @Getter
    @Setter
    public static class HybridProperties {
        /**
         * 是否开启双层加密功能
         */
        private boolean enabled = false;

        /**
         * RSA公钥（用于加密AES密钥返回给客户端）
         */
        private String publicKey;

        /**
         * RSA私钥（用于解密客户端发送的加密AES密钥）
         */
        private String privateKey;

        /**
         * AES加密算法
         */
        private String aesAlgorithm = "AES/ECB/PKCS5Padding";

        /**
         * 拦截的路由
         */
        private String urlPattern = "/*";

        /**
         * 过滤器优先级，默认 Integer.MIN_VALUE + 10，最先执行以解密请求数据
         */
        private int order = Integer.MIN_VALUE + 10;

        /**
         * 白名单，支持Ant风格路径匹配
         * 例如：/api/public/**、/health、/actuator/**
         */
        private List<String> whitelist = new ArrayList<>();
    }

    public enum ParamEncryptType {
        AES,
        RSA,
    }

}
