package com.molandev.framework.encrypt.sign;

import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 签名校验过滤器
 * 全局拦截所有请求进行签名验证，支持白名单配置
 */
public class SignFilter implements Filter {

    private final EncryptProperties encryptProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 防重放攻击 - nonce缓存
     * 使用ConcurrentHashMap存储已使用的nonce，简单实现
     * 生产环境建议使用Redis存储
     */
    private final Map<String, Long> nonceCache = new ConcurrentHashMap<>();

    public SignFilter(EncryptProperties encryptProperties) {
        this.encryptProperties = encryptProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 检查是否在白名单中
        if (isWhitelisted(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }


        // 获取所有参数
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, httpRequest.getParameter(paramName));
        }

        // 获取签名配置
        EncryptProperties.SignProperties signConfig = encryptProperties.getSign();

        // 获取签名
        String sign = params.get(signConfig.getSignName());
        if (StringUtils.isEmpty(sign)) {
            throw new SignException("签名参数[" + signConfig.getSignName() + "]不能为空");
        }

        // 获取时间戳
        String timestampStr = params.get(signConfig.getTimestampName());
        if (StringUtils.isEmpty(timestampStr)) {
            throw new SignException("时间戳参数[" + signConfig.getTimestampName() + "]不能为空");
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SignException("时间戳格式错误");
        }

        // 验证时间戳
        if (!SignUtils.verifyTimestamp(timestamp, signConfig.getExpireTime())) {
            throw new SignException("请求已过期");
        }

        // 获取随机数
        String nonce = params.get(signConfig.getNonceName());
        if (StringUtils.isEmpty(nonce)) {
            throw new SignException("随机数参数[" + signConfig.getNonceName() + "]不能为空");
        }

        // 验证随机数（防重放攻击）
        if (nonceCache.containsKey(nonce)) {
            throw new SignException("请求重复，nonce已使用");
        }

        // 验证签名
        if (!SignUtils.verifySign(params, sign, signConfig.getSecret())) {
            throw new SignException("签名验证失败");
        }

        // 记录nonce
        nonceCache.put(nonce, timestamp);

        // 清理过期的nonce（简单实现，生产环境建议使用定时任务）
        cleanExpiredNonce(signConfig.getExpireTime());

        // 签名验证通过，继续处理请求
        chain.doFilter(request, response);


    }

    /**
     * 检查请求URI是否在白名单中
     */
    private boolean isWhitelisted(String requestUri) {
        List<String> whitelist = encryptProperties.getSign().getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }

        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理过期的nonce
     */
    private void cleanExpiredNonce(long expireTime) {
        long currentTime = System.currentTimeMillis();
        nonceCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > expireTime * 1000
        );
    }

}
