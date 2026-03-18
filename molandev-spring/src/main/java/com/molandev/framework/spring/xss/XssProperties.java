package com.molandev.framework.spring.xss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Xss配置类
 */
@Getter
@Setter
@ConfigurationProperties(XssProperties.PREFIX)
public class XssProperties {

    public static final String PREFIX = "molandev.autoconfig.xss";

    /**
     * 开启xss
     */
    private boolean enabled = false;

    /**
     * 拦截的路由，默认为空
     */
    private String urlPattern = "/*";

    /**
     * 放行的路由，默认为空
     */
    private List<String> pathExcludePatterns = new ArrayList<>();

    /**
     * 过滤器优先级，默认 Integer.MIN_VALUE + 100，在混合加密和签名校验之后执行
     */
    private int order = Integer.MIN_VALUE + 100;

}