package com.molandev.framework.rpc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.rpc.condition.ConditionalOnCloudMode;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Feign JSON 配置，确保与 Web MVC 使用相同的日期格式
 * <p>
 * 此配置会注入 Spring 容器中的 ObjectMapper Bean：
 * <ul>
 *   <li>如果引入了 molandev-spring 并启用 JsonAutoConfiguration，使用定制的 ObjectMapper（LocalDateTime 格式：yyyy-MM-dd HH:mm:ss）</li>
 *   <li>如果没有引入 molandev-spring，使用 Spring Boot 默认的 ObjectMapper（ISO-8601 格式）</li>
 * </ul>
 * </p>
 */
@Slf4j
@Configuration
@ConditionalOnCloudMode
public class FeignJsonConfiguration {

    /**
     * 日期时间格式常量（与 molandev-spring 的 JsonJavaTimeModule 保持一致）
     */
    private static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String NORM_DATE_PATTERN = "yyyy-MM-dd";
    private static final String NORM_TIME_PATTERN = "HH:mm:ss";

    /**
     * 配置 Feign 的 Encoder，使用与 Web 相同的 ObjectMapper
     */
    @Bean
    public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        log.info("配置 Feign Encoder 以使用自定义 ObjectMapper");
        return new SpringEncoder(messageConverters);
    }

    /**
     * 配置 Feign 的 Decoder，使用与 Web 相同的 ObjectMapper
     */
    @Bean
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        log.info("配置 Feign Decoder 以使用自定义 ObjectMapper");
        return new ResponseEntityDecoder(new org.springframework.cloud.openfeign.support.SpringDecoder(messageConverters));
    }

    /**
     * 配置 Feign 专用的 HttpMessageConverters
     * <p>
     * <b>为什么需要这个配置？</b><br>
     * Feign 有独立的编解码体系(SpringEncoder/SpringDecoder)，不会自动使用 Spring MVC 的配置。<br>
     * 此 Bean 的作用是将 Spring 容器中的 ObjectMapper 显式桥接给 Feign 使用，确保 Web MVC 和 Feign 使用相同的 JSON 序列化配置。
     * </p>
     * <p>
     * <b>ObjectMapper 来源：</b>
     * <ul>
     *   <li>如果启用了 molandev-spring 的 JsonAutoConfiguration，使用其注册的自定义 ObjectMapper（LocalDateTime 格式：yyyy-MM-dd HH:mm:ss）</li>
     *   <li>否则使用 Spring Boot 默认的 ObjectMapper（ISO-8601 格式）</li>
     * </ul>
     * </p>
     */
    @Bean
    public HttpMessageConverters feignHttpMessageConverters(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        log.info("配置 Feign HttpMessageConverters，使用 Spring 容器中的 ObjectMapper Bean");
        return new HttpMessageConverters(converter);
    }

    /**
     * 配置 Feign 的请求参数格式化器（用于 @RequestParam 等查询参数）
     * <p>
     * <b>为什么需要这个配置？</b><br>
     * Feign 在将 @RequestParam 参数转换为 URL 查询字符串时，需要将 LocalDateTime 等类型转换为字符串。<br>
     * 此配置确保 Feign 使用与 Spring MVC 相同的日期格式，避免服务端解析失败。
     * </p>
     * <p>
     * <b>格式说明：</b>
     * <ul>
     *   <li>LocalDateTime: yyyy-MM-dd HH:mm:ss</li>
     *   <li>LocalDate: yyyy-MM-dd</li>
     *   <li>LocalTime: HH:mm:ss</li>
     * </ul>
     * </p>
     * <p>
     * <b>生效条件：</b><br>
     * 只有当启用了 molandev.autoconfig.json.enabled=true 时才注册此配置，<br>
     * 确保与 Spring MVC 的 JsonAutoConfiguration 保持一致。
     * </p>
     */
    @Bean
    @ConditionalOnProperty(prefix = "molandev.autoconfig.json", name = "enabled", havingValue = "true")
    public FeignFormatterRegistrar feignFormatterRegistrar() {
        return new FeignFormatterRegistrar() {
            @Override
            public void registerFormatters(@NonNull FormatterRegistry registry) {
                DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
                registrar.setTimeFormatter(DateTimeFormatter.ofPattern(NORM_TIME_PATTERN));
                registrar.setDateFormatter(DateTimeFormatter.ofPattern(NORM_DATE_PATTERN));
                registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN));
                registrar.registerFormatters(registry);
                log.info("配置 Feign 请求参数格式化器：LocalDateTime 格式为 {}", NORM_DATETIME_PATTERN);
            }
        };
    }
}
