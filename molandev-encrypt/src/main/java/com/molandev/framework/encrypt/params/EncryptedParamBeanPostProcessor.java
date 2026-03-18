package com.molandev.framework.encrypt.params;

import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 重建argumentresolver的顺序，把解密的提前处理
 */
public class EncryptedParamBeanPostProcessor implements org.springframework.beans.factory.config.BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
            RequestMappingHandlerAdapter requestMappingHandlerAdapter = (RequestMappingHandlerAdapter) bean;
            List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
            if (argumentResolvers == null) {
                return bean;
            }
            List<HandlerMethodArgumentResolver> list = new ArrayList<>(argumentResolvers);
            HandlerMethodArgumentResolver r = null;
            for (HandlerMethodArgumentResolver argumentResolver : argumentResolvers) {
                if (argumentResolver instanceof EncryptedParamHandlerMethodArgumentResolver) {
                    r = argumentResolver;
                }
            }
            if (r != null) {
                list.remove(r);
                list.add(0, r);
            }
            requestMappingHandlerAdapter.setArgumentResolvers(list);
        }
        return bean;
    }

}
