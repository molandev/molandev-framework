package com.molandev.framework.spring.xss;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XSS 过滤器
 */
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = XssProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class XssFilter extends OncePerRequestFilter {

    private final XssProperties xssProperties;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 注解缓存，避免每次请求都要反射读取注解
     * Key: Method 或 Class 对象
     * Value: 是否应该跳过XSS过滤
     */
    private final Map<Object, Boolean> xssIgnoreCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 检查是否应该跳过XSS过滤
        boolean shouldSkip = shouldSkipXssFilter(request);

        if (shouldSkip || !xssProperties.isEnabled()) {
            // 如果需要跳过或者XSS未启用，则直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 对于 multipart 请求，不重写 inputstream，防止大文件导致内存溢出
        // 但仍需要包装请求以处理非文件参数的XSS过滤
        if (isMultipartRequest(request)) {
            XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(request, true);
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        // 对于普通请求，使用自定义的 HttpServletRequestWrapper 包装原始请求
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(request);
        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * 判断是否应该跳过 XSS 过滤
     * <p>
     * 优化点：
     * 1. 使用 AnnotatedElementUtils 支持注解继承（父类/接口上的注解）
     * 2. 使用缓存避免重复反射读取注解
     * </p>
     *
     * @param request 当前请求
     * @return 是否跳过
     */
    private boolean shouldSkipXssFilter(HttpServletRequest request) throws ServletException {
        try {
            HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
            if (handlerExecutionChain == null) {
                return true;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();
            Method method = handlerMethod.getMethod();
            Class<?> beanType = handlerMethod.getBeanType();

            // 先检查方法级别的注解（优先级高）
            Boolean methodIgnore = xssIgnoreCache.computeIfAbsent(method, m -> 
                AnnotatedElementUtils.hasAnnotation((Method) m, XssIgnore.class)
            );
            if (methodIgnore) {
                return true;
            }

            // 再检查类级别的注解（支持继承）
            Boolean classIgnore = xssIgnoreCache.computeIfAbsent(beanType, clazz -> 
                AnnotatedElementUtils.hasAnnotation((Class<?>) clazz, XssIgnore.class)
            );
            return classIgnore;
        } catch (Exception e) {
            // 出现异常时继续执行过滤
            return false;
        }
    }

    /**
     * 判断是否为 multipart 请求（文件上传）
     *
     * @param request 当前请求
     * @return 是否为 multipart 请求
     */
    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
}