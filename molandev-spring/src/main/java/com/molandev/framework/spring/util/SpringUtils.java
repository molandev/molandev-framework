package com.molandev.framework.spring.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Spring相关工具，获取bean，配置等
 */
@Configuration
@Order(Integer.MIN_VALUE)
public class SpringUtils {

    /**
     * cglib类后缀
     */
    public static final String CGLIB_CLASS_POSTFIX = "EnhancerBySpringCGLIB";

    /**
     * jdk类后缀
     */
    public static final String JDK_CLASS_POSTFIX = "$Proxy";
    /**
     * 自定义properties，最低优先级，配置文件中如果配置了，则会被覆盖，用于设置预设配置
     */
    @Getter
    static Map<String, Object> customProperty = new HashMap<>();
    /**
     * 应用程序上下文
     */
    @Getter
    private static ConfigurableApplicationContext applicationContext;

    /**
     * 设置应用程序上下文
     *
     * @param applicationContext 应用程序上下文
     */
    public static void setApplicationContext(ConfigurableApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取一个bean
     */
    public static Object getBean(String beanId) {
        return applicationContext.getBean(beanId);
    }

    /**
     * 根据class获取bean
     */
    public static boolean containsBean(Class<?> beanClass) {
        try {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(beanClass);
            if (beanNamesForType.length > 0) {
                return true;
            }
        } catch (BeansException e) {
            return false;
        }
        return false;
    }

    /**
     * 根据class获取bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * 根据注解获取bean
     */
    public static Map<String, Object> getBeansByAnnotation(Class<? extends Annotation> annotation) {
        return applicationContext.getBeansWithAnnotation(annotation);
    }

    /**
     * 根据类获取所有的bean
     */
    public static <T> List<T> getBeansByType(Class<T> beanClass) {
        Map<String, T> beansOfType = applicationContext.getBeansOfType(beanClass);
        return new ArrayList<T>(beansOfType.values());

    }

    /**
     * 获取配置
     */
    public static String getProperty(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 获取配置，并指定默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 判断是都存在配置
     */
    public static boolean hasProperty(String key) {
        return applicationContext.getEnvironment().containsProperty(key);
    }

    /**
     * 设置自定义的配置，最高优先级
     */
    public static void putProperties(Map<String, String> props) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 设置自定义的配置，最高优先级
     */
    public static void putProperty(String key, String value) {
        System.setProperty(key, value);
    }

    /**
     * 添加一个配置，此配置为最低优先级，会被配置文件中的值覆盖， 如果不想被覆盖，请使用putProperty
     */
    public static void addProperty(String key, String value) {
        customProperty.put(key, value);
    }

    /**
     * 获取所有配置
     */
    public static Properties getProperties() {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        Properties p = new Properties();
        for (PropertySource<?> next : propertySources) {
            if (next instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource<?>) next).getPropertyNames();
                for (String propertyName : propertyNames) {
                    if (p.get(propertyName) == null) {
                        p.put(propertyName, next.getProperty(propertyName));
                    }
                }
            }
        }
        return p;

    }

    /**
     * 获取起源类
     *
     * @param clazz clazz
     * @return {@link Class}
     */
    public static Class<?> getOriginClass(Class<?> clazz) {
        if (clazz.getName().contains(CGLIB_CLASS_POSTFIX)) {
            return getOriginClass(clazz.getSuperclass());
        }
        if (clazz.getName().contains(JDK_CLASS_POSTFIX)) {
            return getOriginClass(clazz.getSuperclass());
        }
        return clazz;
    }

    /**
     * 事件发布
     *
     * @param event 普通对象
     */
    public static void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

    /**
     * 事件发布
     *
     * @param event 自定义事件对象
     */
    public static void publishEvent(ApplicationEvent event) {
        applicationContext.publishEvent(event);
    }

    public static String getApplicationName() {
        return getProperty("spring.application.name");
    }

}
