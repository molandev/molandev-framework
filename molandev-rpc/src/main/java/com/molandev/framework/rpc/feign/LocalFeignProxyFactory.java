package com.molandev.framework.rpc.feign;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 本服务内，将api转换为本地实现而不是FeignClient
 * 进入到此处的，是FeignClientBeanDefinitionPostProcessor中转换过来的
 *
 */
public class LocalFeignProxyFactory extends FeignClientFactoryBean implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() {
        if (getObjectType() == null) {
            return null;
        }
        return Proxy.newProxyInstance(getObjectType().getClassLoader(), new Class[]{getObjectType()}, new RpcInvocationHandler<>(applicationContext, getObjectType()));
    }

    @RequiredArgsConstructor
    public static class RpcInvocationHandler<T> implements InvocationHandler {

        private final ApplicationContext applicationContext;

        private final Class<T> type;

        private volatile T target;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            T t = getLocalBean(proxy);
            if (t != null) {
                return method.invoke(t, args);
            }
            return null;
        }

        /**
         * 获取本地的bean，极限并发条件可能会调用两次，但是无所谓
         */
        @SuppressWarnings("unchecked")
        private T getLocalBean(Object proxy) {
            if (target != null) {
                return target;
            }
            MergedAnnotation<?> ann = MergedAnnotations.from(type).get(FeignClient.class);
            Class<?> fallback = null, fallbackFactory = null;
            if (ann.isPresent()) {
                // 过滤掉 Fallback 类，防止重复注册映射
                fallback = ann.getClass("fallback");
                fallbackFactory = ann.getClass("fallbackFactory");
            }


            Map<String, ?> beans = applicationContext.getBeansOfType(type);
            for (Object bean : beans.values()) {
                // 查找非 FeignClientFactoryBean 代理的本地实现类
                if (bean == proxy) {
                    continue;
                }
                if (bean instanceof FeignClientFactoryBean) {
                    continue;
                }
                if (fallback != null && fallback.isAssignableFrom(bean.getClass())) {
                    continue;
                }
                if (fallbackFactory != null && fallbackFactory.isAssignableFrom(bean.getClass())) {
                    continue;
                }
                target = (T) bean;
                break;
            }
            if (target == null) {
                throw new IllegalStateException("未找到 Feign 接口的本地实现:" + type.getName());
            }
            return target;
        }
    }
}
