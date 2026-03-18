package com.molandev.framework.encrypt.db;

import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import com.molandev.framework.util.encrypt.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据加解密service
 */
@Slf4j
public class DbEncryptService {

    @Autowired
    EncryptProperties encryptProperties;

    /**
     * 缓存记录一下需要加密的类
     */
    private final Map<Class<?>, List<Field>> needEncMap = new ConcurrentHashMap<>();

    /**
     * 获取内附字段
     *
     * @param clazz clazz
     * @return {@link List<Field>}
     */
    public List<Field> getEncFields(Class<?> clazz) {
        needEncMap.computeIfAbsent(clazz, aClass -> {
            List<Field> list = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                Enc annotation = field.getAnnotation(Enc.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    list.add(field);
                }
            });
            return list;
        });
        return needEncMap.get(clazz);
    }

    /**
     * 加密
     *
     * @param param 参数
     */
    public void encrypt(Object param) {
        if (Objects.isNull(param)) {
            return;
        }
        Class<?> parameterObjectClass = param.getClass();
        if (parameterObjectClass == String.class || parameterObjectClass == Integer.class
                || parameterObjectClass == LocalDateTime.class) {
            return;
        }
        List<Field> list = getEncFields(parameterObjectClass);
        if (list != null && !list.isEmpty()) {
            for (Field field : list) {
                Object originalVal = ReflectionUtils.getField(field, param);
                if (originalVal instanceof String && StringUtils.isNotEmpty((String) originalVal)) {
                    Object encryptVal = AesUtil.encrypt((String) originalVal, encryptProperties.getDb().getKey(), encryptProperties.getDb().getAlgorithm());
                    ReflectionUtils.setField(field, param, encryptVal);
                }
            }
        }
    }

    /**
     * 解密
     *
     * @param list 列表
     */
    public void decrypt(List<?> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Object o = list.get(0);
        if (o == null) {
            return;
        }
        Class<?> aClass = o.getClass();
        List<Field> byClass = getEncFields(aClass);
        if (byClass != null && !byClass.isEmpty()) {
            for (Object entity : list) {
                decrypt(entity);
            }
        }
    }

    /**
     * 解密
     *
     * @param entity 实体
     */
    public void decrypt(Object entity) {
        if (entity == null) {
            return;
        }
        if (entity instanceof List) {
            decrypt((List<?>) entity);
            return;
        }
        List<Field> fields = getEncFields(entity.getClass());
        if (fields == null || fields.isEmpty()) {
            return;
        }
        decrypt(entity, fields);
    }

    /**
     * 解密
     *
     * @param entity 实体
     * @param fields 通过类
     */
    public void decrypt(Object entity, List<Field> fields) {
        for (Field field : fields) {
            Object obj = ReflectionUtils.getField(field, entity);
            if (obj instanceof String && !StringUtils.isEmpty((String) obj)) {
                String decrypt = AesUtil.decrypt((String) obj, encryptProperties.getDb().getKey(), encryptProperties.getDb().getAlgorithm());
                ReflectionUtils.setField(field, entity, decrypt);
            }
        }
    }

}
