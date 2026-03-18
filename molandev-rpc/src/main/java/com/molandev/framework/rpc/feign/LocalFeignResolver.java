package com.molandev.framework.rpc.feign;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Feign 客户端 Bean 定义后置处理器
 * 判断FeignClient的定义是否与本地服务是同一个服务，如果是，走ProxyFeignClientFactoryBean，否则，走正常的feign代理
 */
@Slf4j
public class LocalFeignResolver implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private static final String FEIGN_CLIENT_FACTORY_BEAN_CLASS = "org.springframework.cloud.openfeign.FeignClientFactoryBean";

    private Environment environment;

    private String appName;

    @Getter
    private final Map<String,String> cachedLocalFeignBeanDefinitions= new LinkedHashMap<>();

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.appName = environment.getProperty("spring.application.name");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (!registry.containsBeanDefinition(beanName)) continue;
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            String beanClassName = bd.getBeanClassName();
            if (FEIGN_CLIENT_FACTORY_BEAN_CLASS.equals(beanClassName)) {
                String feignClientName = getFeignClientName(bd);
                if (feignClientName.equalsIgnoreCase(appName)) {
                    bd.setBeanClassName(LocalFeignProxyFactory.class.getName());
                    cachedLocalFeignBeanDefinitions.put(beanName,getPropertyValue(bd,"type"));
                }
            }
        }

    }


    private String getFeignClientName(BeanDefinition bd) {
        // 从属性中获取 name, value 或 contextId
        String name = getPropertyValue(bd, "name");
        if (name == null) {
            name = getPropertyValue(bd, "value");
        }
        if (name == null) {
            name = getPropertyValue(bd, "contextId");
        }

        // 解析可能存在的占位符
        return environment.resolvePlaceholders(name != null ? name : "");
    }

    private String getPropertyValue(BeanDefinition bd, String propertyName) {
        PropertyValue pv = bd.getPropertyValues().getPropertyValue(propertyName);
        if (pv != null && pv.getValue() != null) {
            return pv.getValue().toString();
        }
        return null;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op
    }
}
