package com.molandev.framework.encrypt.sign;

import com.molandev.framework.util.encrypt.Md5Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 签名工具类
 */
public class SignUtils {

    /**
     * 生成签名
     *
     * @param params 请求参数
     * @param secret 密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, String> params, String secret) {
        // 1. 过滤空值和签名字段
        Map<String, String> filteredParams = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .filter(entry -> !"sign".equals(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 2. 按参数名排序
        List<String> keys = new ArrayList<>(filteredParams.keySet());
        Collections.sort(keys);

        // 3. 拼接参数
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append("=").append(filteredParams.get(key)).append("&");
        }
        sb.append("secret=").append(secret);

        // 4. MD5加密并转大写
        return Md5Utils.md5(sb.toString()).toUpperCase();
    }

    /**
     * 验证签名
     *
     * @param params 请求参数
     * @param sign   签名
     * @param secret 密钥
     * @return 是否验证通过
     */
    public static boolean verifySign(Map<String, String> params, String sign, String secret) {
        String generatedSign = generateSign(params, secret);
        return generatedSign.equals(sign);
    }

    /**
     * 验证时间戳是否在有效期内
     *
     * @param timestamp  时间戳（毫秒）
     * @param expireTime 过期时间（秒）
     * @return 是否有效
     */
    public static boolean verifyTimestamp(long timestamp, long expireTime) {
        long currentTime = System.currentTimeMillis();
        return Math.abs(currentTime - timestamp) <= expireTime * 1000;
    }

}
