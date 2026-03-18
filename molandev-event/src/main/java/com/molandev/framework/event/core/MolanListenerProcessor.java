package com.molandev.framework.event.core;

import com.molandev.framework.event.annotation.MolanListener;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描带有 @MolanListener 注解的方法
 */
public class MolanListenerProcessor implements BeanPostProcessor {

    @Getter
    private final List<MolanListenerDefinition> listeners = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            MolanListener annotation = AnnotationUtils.findAnnotation(method, MolanListener.class);
            if (annotation != null) {
                // 解析事件类型：优先使用注解指定，否则从方法参数推断
                Class<?> eventType = annotation.value();
                if (eventType == Void.class) {
                    eventType = resolveEventTypeFromMethod(method, bean.getClass());
                }
                
                // 校验方法签名：必须有且仅有一个参数
                validateListenerMethod(method, eventType, bean.getClass());
                
                MolanListenerDefinition definition = new MolanListenerDefinition(
                        bean,
                        method,
                        eventType,
                        annotation.group(),
                        annotation.async()
                );
                listeners.add(definition);
            }
        });
        return bean;
    }
    
    /**
     * 从方法的第一个参数推断事件类型
     */
    private Class<?> resolveEventTypeFromMethod(Method method, Class<?> beanClass) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalStateException(
                    String.format("监听器方法必须有且仅有一个参数：%s.%s",
                            beanClass.getName(), method.getName())
            );
        }
        return parameterTypes[0];
    }
    
    /**
     * 校验监听器方法签名
     */
    private void validateListenerMethod(Method method, Class<?> eventType, Class<?> beanClass) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        
        // 1. 必须有且仅有一个参数
        if (parameterTypes.length != 1) {
            throw new IllegalStateException(
                    String.format("监听器方法必须有且仅有一个参数：%s.%s",
                            beanClass.getName(), method.getName())
            );
        }
        
        // 2. 参数类型必须与事件类型匹配
        if (!parameterTypes[0].equals(eventType)) {
            throw new IllegalStateException(
                    String.format("监听器方法参数类型 [%s] 与注解指定的事件类型 [%s] 不匹配：%s.%s",
                            parameterTypes[0].getName(), eventType.getName(),
                            beanClass.getName(), method.getName())
            );
        }
    }
}
