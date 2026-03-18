package com.molandev.framework.rpc.feign;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * 自定义返回值处理器，用于将 Feign 接口实现类的返回值自动按 @ResponseBody 方式处理
 * 即使实现类上没有 @RestController 或 @ResponseBody 注解
 */
public class FeignReturnValueHandler implements HandlerMethodReturnValueHandler {

    private static final String FEIGN_CLIENT_CLASS = "org.springframework.cloud.openfeign.FeignClient";
    
    private final RequestResponseBodyMethodProcessor delegate;

    public FeignReturnValueHandler(RequestResponseBodyMethodProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        // 如果返回类型是 ResponseEntity，由 Spring MVC 默认的 HttpEntityMethodProcessor 处理
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 检查方法所在的类是否实现了带有 @FeignClient 注解的接口，或者类本身就是该接口
        Class<?> clazz = returnType.getContainingClass();
        
        // 如果类本身就有 @FeignClient 注解（当注册的是接口方法时）
        if (MergedAnnotations.from(clazz).isPresent(FEIGN_CLIENT_CLASS)) {
            return true;
        }
        
        // 遍历实现的所有接口（当注册的是实现类方法时）
        for (Class<?> iface : clazz.getInterfaces()) {
            if (MergedAnnotations.from(iface).isPresent(FEIGN_CLIENT_CLASS)) {
                return true;
            }
        }
        
        // 如果方法本身有 @ResponseBody 或类上有相关注解，也支持
        return returnType.hasMethodAnnotation(ResponseBody.class) 
            || AnnotatedElementUtils.hasAnnotation(clazz, ResponseBody.class);
    }

    @Override
    public void handleReturnValue(Object returnValue, @NonNull MethodParameter returnType,
                                  @NonNull ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest) throws Exception {
        delegate.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }
}
