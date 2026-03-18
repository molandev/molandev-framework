package com.molandev.framework.encrypt.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import com.molandev.framework.util.encrypt.AesUtil;
import com.molandev.framework.util.encrypt.RsaUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 双层加密过滤器
 * 全局拦截请求进行双层解密和响应加密，支持白名单配置
 */
public class HybridEncryptFilter implements Filter {

    private final EncryptProperties encryptProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 当前请求使用的AES密钥（用于响应加密）
     */
    private static final ThreadLocal<String> CURRENT_AES_KEY = new ThreadLocal<>();

    public HybridEncryptFilter(EncryptProperties encryptProperties, ObjectMapper objectMapper) {
        this.encryptProperties = encryptProperties;
        this.objectMapper = objectMapper;
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

        try {
            // 包装请求，处理解密
            HybridEncryptRequestWrapper requestWrapper = new HybridEncryptRequestWrapper(
                    httpRequest, encryptProperties, objectMapper);

            // 保存AES密钥到ThreadLocal，用于响应加密
            if (requestWrapper.getAesKey() != null) {
                CURRENT_AES_KEY.set(requestWrapper.getAesKey());
            }

            // 包装响应，处理加密
            HybridEncryptResponseWrapper responseWrapper = new HybridEncryptResponseWrapper(httpResponse);

            // 继续处理请求
            chain.doFilter(requestWrapper, responseWrapper);

            // 加密响应数据
            encryptResponse(responseWrapper, httpResponse);

        }  finally {
            // 清除ThreadLocal中的AES密钥
            CURRENT_AES_KEY.remove();
        }
    }

    /**
     * 检查请求URI是否在白名单中
     */
    private boolean isWhitelisted(String requestUri) {
        List<String> whitelist = encryptProperties.getHybrid().getWhitelist();
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
     * 加密响应数据
     */
    private void encryptResponse(HybridEncryptResponseWrapper responseWrapper,
                                  HttpServletResponse originalResponse) throws IOException {

        byte[] responseData = responseWrapper.getResponseData();
        if (responseData == null || responseData.length == 0) {
            return;
        }

        try {
            String responseBody = new String(responseData, StandardCharsets.UTF_8);

            // 获取请求中传入的AES密钥
            String aesKey = CURRENT_AES_KEY.get();
            if (StringUtils.isEmpty(aesKey)) {
                throw new HybridEncryptException("未找到请求中的AES密钥，无法加密响应");
            }

            // 使用相同的AES密钥加密响应数据
            String encryptedData = AesUtil.encrypt(responseBody, aesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 直接返回加密后的数据字符串，前端使用自己的密钥解密即可
            originalResponse.setContentType("application/json;charset=UTF-8");
            originalResponse.setContentLength(encryptedData.getBytes(StandardCharsets.UTF_8).length);
            originalResponse.getWriter().write(encryptedData);

        } catch (Exception e) {
            throw new HybridEncryptException("响应加密失败: " + e.getMessage(), e);
        }
    }

}
