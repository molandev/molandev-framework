package com.molandev.framework.spring.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.molandev.framework.util.ClassUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * jsonutils
 */
public class JSONUtils {

    /**
     * 默认时区
     */
    public static final String ASIA_SHANGHAI = "GMT+8";

    /**
     * 对象映射器
     */
    @Getter
    @Setter
    static JsonMapper jsonMapper;

    @Getter
    static JsonMapper jsonMapperWithType;

    static {

        jsonMapper = JsonMapper.builder()
                .addModule(new JsonJavaTimeModule())
                .defaultTimeZone(TimeZone.getTimeZone(ASIA_SHANGHAI))
                .defaultLocale(Locale.CHINA)
                .defaultDateFormat(new SimpleDateFormat(JsonJavaTimeModule.NORM_DATETIME_PATTERN))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        jsonMapperWithType = JsonMapper.builder()
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .addModule(new JsonJavaTimeModule())
                .defaultTimeZone(TimeZone.getTimeZone(ASIA_SHANGHAI))
                .defaultLocale(Locale.CHINA)
                .defaultDateFormat(new SimpleDateFormat(JsonJavaTimeModule.NORM_DATETIME_PATTERN))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /**
     * 对象转json
     */
    public static String toJsonString(Object object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * str转对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String str) {
        try {
            return jsonMapper.readValue(str, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * json转list对象，并指定lsit内部的类型
     *
     * @param str  json字符串
     * @param type list内部的类型
     * @param <T>  泛型
     */
    public static <T> List<T> toList(String str, Type type) {
        try {
            return jsonMapper.readValue(str, new TypeReference<>() {
                @Override
                public Type getType() {
                    return ClassUtils.getType(ArrayList.class, type);
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * str转对象
     */
    public static <T> T toObject(String str, Type type) {
        try {
            return jsonMapper.readValue(str, new TypeReference<>() {
                @Override
                public Type getType() {
                    return type;
                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转java对象
     *
     * @param str   json字符串
     * @param clazz 要转换的对象类型
     * @return clazz实例
     */
    public static <T> T toObject(String str, Class<T> clazz) {
        try {
            return jsonMapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
