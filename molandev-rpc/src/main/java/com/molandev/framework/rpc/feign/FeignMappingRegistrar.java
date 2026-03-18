package com.molandev.framework.rpc.feign;

import com.molandev.framework.rpc.condition.ConditionalOnCloudMode;
import com.molandev.framework.rpc.exception.RpcException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Feign 接口自动注册为 MVC 映射（Cloud模式下生效）
 */
@Slf4j
@Configuration
@ConditionalOnCloudMode
public class FeignMappingRegistrar implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Autowired
    LocalFeignResolver localFeignResolver;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 找到 fallback 到本地实现的 FeignClient
        // 这些bean需要做一个RequestMappeing的映射
        Map<String, String> cachedLocalFeignBeanDefinitions = localFeignResolver.getCachedLocalFeignBeanDefinitions();
        if (cachedLocalFeignBeanDefinitions.isEmpty()) {
            return;
        }

        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);

        // 配置自定义返回值处理器，使 Feign 接口实现类可以自动按 @ResponseBody 方式处理返回值
        configureReturnValueHandlers();

        for (Map.Entry<String, String> entry : cachedLocalFeignBeanDefinitions.entrySet()) {
            String beanName = entry.getKey();
            String beanClassName = entry.getValue();
            try {
                Class<?> feignApiInterface = Class.forName(beanClassName);
                Map<String, ?> beansOfType = applicationContext.getBeansOfType(feignApiInterface);
                if (!beansOfType.isEmpty()) {
                    MergedAnnotation<?> ann = MergedAnnotations.from(feignApiInterface).get(FeignClient.class);
                    if (ann.isPresent()) {
                        for (Map.Entry<String, ?> stringEntry : beansOfType.entrySet()) {
                            String key = stringEntry.getKey();
                            // 这个beanName对应的bean是ProxyFeignClientFactoryBean创建的代理，过滤掉
                            if(key.equals(beanName)){
                                continue;
                            }
                            Object bean = stringEntry.getValue();
                            // 过滤掉 Fallback 类，防止重复注册映射
                            Class<?> fallback = ann.getClass("fallback");
                            Class<?> fallbackFactory = ann.getClass("fallbackFactory");
                            if (bean.getClass().equals(fallback) || bean.getClass().equals(fallbackFactory)) {
                                continue;
                            }
                            registerFeignInterface(handlerMapping, feignApiInterface, bean);
                        }


                    }
                }

            } catch (Exception e) {
                // 正常情况不会走到这里
                log.debug("跳过 bean {}: {}", beanName, e.getMessage());
            }
        }
    }

    private void registerFeignInterface(RequestMappingHandlerMapping handlerMapping, Class<?> iface, Object handler) {
        log.info("正在将 Feign 接口注册为 MVC 端点: {} -> 实现类 {}", iface.getSimpleName(), handler.getClass().getSimpleName());

        // 获取 @FeignClient 的 path 属性
        MergedAnnotation<FeignClient> feignClientAnnotation = MergedAnnotations.from(iface).get(FeignClient.class);
        String feignClientPath = "";
        if (feignClientAnnotation.isPresent()) {
            feignClientPath = feignClientAnnotation.getString("path");
            if (feignClientPath != null && !feignClientPath.isEmpty()) {
                log.debug("检测到 @FeignClient path: {}", feignClientPath);
            }
        }

        // 获取类级别的 RequestMapping
        RequestMapping classRequestMapping = AnnotatedElementUtils.findMergedAnnotation(iface, RequestMapping.class);
        String[] classPaths = classRequestMapping != null ? (classRequestMapping.value().length > 0 ? classRequestMapping.value() : classRequestMapping.path()) : new String[]{""};
        
        // 将 FeignClient 的 path 与类级别的 RequestMapping 路径合并
        if (feignClientPath != null && !feignClientPath.isEmpty()) {
            String[] newClassPaths = new String[classPaths.length];
            for (int i = 0; i < classPaths.length; i++) {
                newClassPaths[i] = join(feignClientPath, classPaths[i]);
            }
            classPaths = newClassPaths;
        }
        
        // 声明为 final 以便在 lambda 中引用
        final String[] finalClassPaths = classPaths;

        ReflectionUtils.doWithMethods(iface, method -> {
            RequestMapping methodRequestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (methodRequestMapping != null) {
                String[] methodPaths = methodRequestMapping.value().length > 0 ? methodRequestMapping.value() : methodRequestMapping.path();

                // 构建 RequestMappingInfo
                RequestMappingInfo mappingInfo = RequestMappingInfo
                        .paths(combinePaths(finalClassPaths, methodPaths))
                        .methods(methodRequestMapping.method())
                        .params(methodRequestMapping.params())
                        .headers(methodRequestMapping.headers())
                        .consumes(methodRequestMapping.consumes())
                        .produces(methodRequestMapping.produces())
                        .build();

                // 校验实现类中是否存在对应方法
                Method handlerMethod = ReflectionUtils.findMethod(handler.getClass(), method.getName(), method.getParameterTypes());
                if (handlerMethod == null) {
                    throw new RpcException("未在实现类 {} 中找到 Feign 接口对应的方法 {}" + handler.getClass().getName() + " for Feign method " + method.getName());
                }

                try {
                    // 使用接口方法（method）进行注册，而不是实现类方法（handlerMethod）
                    // 这样 Spring MVC 才能正确读取接口上的参数注解（如 @RequestParam, @RequestPart, @PathVariable）
                    handlerMapping.registerMapping(mappingInfo, handler, method);
                    log.info("已映射 Feign 方法: {}#{} 到 {}", iface.getSimpleName(), method.getName(), mappingInfo);
                } catch (IllegalStateException e) {
                    // 可能是冲突
                    log.error("注册Feign mapping 失败：  {}: {}", method, e.getMessage());
                    throw new RpcException("注册Feign mapping 失败： " + method + ": " + e.getMessage(), e);
                }
            }
        }, method -> !method.isDefault() && !ReflectionUtils.isObjectMethod(method));
    }

    private String[] combinePaths(String[] classPaths, String[] methodPaths) {
        if (classPaths == null || classPaths.length == 0 || (classPaths.length == 1 && classPaths[0].isEmpty())) {
            return methodPaths;
        }
        if (methodPaths == null || methodPaths.length == 0 || (methodPaths.length == 1 && methodPaths[0].isEmpty())) {
            return classPaths;
        }

        String[] result = new String[classPaths.length * methodPaths.length];
        int i = 0;
        for (String cp : classPaths) {
            for (String mp : methodPaths) {
                result[i++] = join(cp, mp);
            }
        }
        return result;
    }

    private String join(String p1, String p2) {
        if (p1 == null) p1 = "";
        if (p2 == null) p2 = "";

        if (!p1.startsWith("/") && !p1.isEmpty()) p1 = "/" + p1;
        if (p1.endsWith("/")) p1 = p1.substring(0, p1.length() - 1);
        if (!p2.startsWith("/") && !p2.isEmpty()) p2 = "/" + p2;

        return p1 + p2;
    }

    /**
     * 配置自定义返回值处理器，使得 Feign 接口的实现类无需 @RestController 也能自动返回 JSON
     */
    private void configureReturnValueHandlers() {
        RequestMappingHandlerAdapter handlerAdapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
        List<HttpMessageConverter<?>> messageConverters = handlerAdapter.getMessageConverters();

        // 创建 RequestResponseBodyMethodProcessor 作为委托处理器
        RequestResponseBodyMethodProcessor processor = new RequestResponseBodyMethodProcessor(messageConverters);

        // 创建自定义的 Feign 返回值处理器
        FeignReturnValueHandler feignHandler =
                new FeignReturnValueHandler(processor);

        // 获取现有的返回值处理器列表
        List<HandlerMethodReturnValueHandler> originalHandlers = handlerAdapter.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();

        // 将自定义处理器放在最前面，优先处理 Feign 接口
        newHandlers.add(feignHandler);
        if (originalHandlers != null) {
            newHandlers.addAll(originalHandlers);
        }

        handlerAdapter.setReturnValueHandlers(newHandlers);
        log.info("已配置 Feign 返回值处理器以支持自动 @ResponseBody 行为");
    }
}
