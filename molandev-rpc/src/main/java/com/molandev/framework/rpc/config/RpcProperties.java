package com.molandev.framework.rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "molandev")
public class RpcProperties {

    /**
     * 运行模式：cloud (微服务远程调用) / single (独立模式，本地调用)
     */
    private String runMode = "cloud";

    public static final String RUN_MODE_CLOUD = "cloud";
    public static final String RUN_MODE_STANDALONE = "single";
}
