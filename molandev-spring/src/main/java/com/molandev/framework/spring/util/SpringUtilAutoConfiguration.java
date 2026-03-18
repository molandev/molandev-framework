package com.molandev.framework.spring.util;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

/**
 * 初始化SpringUtils 之所以放到listener中，是方便在beanRegister时就能拿到SpringUtils
 */
@Order(Integer.MIN_VALUE)
public class SpringUtilAutoConfiguration implements ApplicationListener<ApplicationPreparedEvent> {

    /**
     * 自定义key
     */
    public static final String CUSTOM_PROPERTY_SOURCE_KEY = "customPropertySource";

    /**
     * 应用程序加载之后，增加默认配置，此时增加的配置，是在最后的。
     *
     * @param applicationEvent 应用程序事件
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationPreparedEvent applicationEvent) {
        ConfigurableApplicationContext applicationContext = applicationEvent.getApplicationContext();
        SpringUtils.setApplicationContext(applicationContext);
        MapPropertySource mapPropertySource = new MapPropertySource(CUSTOM_PROPERTY_SOURCE_KEY,
                SpringUtils.getCustomProperty());
        applicationContext.getEnvironment().getPropertySources().addLast(mapPropertySource);

    }

}
