package com.molandev.framework.spring.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * JacksonConfig 配置时间转换规则
 */
@Configuration
@ConditionalOnProperty(prefix = "molandev.autoconfig.json", name = "enabled", havingValue = "true")
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class JsonAutoConfiguration implements WebMvcConfigurer {

    /**
     * 注册自定义的 ObjectMapper Bean，提供全局统一的 JSON 序列化配置
     * <p>
     * 此 ObjectMapper 会被 Web MVC 和 Feign 共同使用，确保日期格式等配置完全一致
     * </p>
     */
    @Bean
    public ObjectMapper objectMapper() {
        return JSONUtils.getJsonMapper();
    }

    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        int index = -1;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                index = i;
                break;
            }
        }
        converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        MappingJackson2HttpMessageConverter primaryObjectMapper = new MappingJackson2HttpMessageConverter(JSONUtils.getJsonMapper());
        primaryObjectMapper.setDefaultCharset(StandardCharsets.UTF_8);
        if (index >= 0) {
            converters.add(index, primaryObjectMapper);
        } else {
            converters.add(primaryObjectMapper);
        }
    }

    /**
     * 增加GET请求参数中时间类型转换
     * <ul>
     * <li>HH:mm:ss -> LocalTime</li>
     * <li>yyyy-MM-dd -> LocalDate</li>
     * <li>yyyy-MM-dd HH:mm:ss -> LocalDateTime</li>
     * </ul>
     */
    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setTimeFormatter(DateTimeFormatter.ofPattern(JsonJavaTimeModule.NORM_TIME_PATTERN));
        registrar.setDateFormatter(DateTimeFormatter.ofPattern(JsonJavaTimeModule.NORM_DATE_PATTERN));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern(JsonJavaTimeModule.NORM_DATETIME_PATTERN));
        registrar.registerFormatters(registry);
    }

    /**
     * 避免form 提交 context-type 不规范中文乱码
     *
     * @return Filter
     */
    @Bean
    public OrderedCharacterEncodingFilter characterEncodingFilter() {
        OrderedCharacterEncodingFilter filter = new OrderedCharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceEncoding(true);
        filter.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filter;
    }
}
