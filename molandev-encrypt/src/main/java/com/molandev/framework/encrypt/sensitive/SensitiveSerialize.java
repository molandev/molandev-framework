package com.molandev.framework.encrypt.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.molandev.framework.util.encrypt.SensitiveUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Objects;

/**
 * 敏感的序列化
 */
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveSerialize extends JsonSerializer<String> implements ContextualSerializer {

    /**
     * 类型
     */
    private Sensitive sensitive;

    @Override
    public void serialize(final String origin, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {

        switch (sensitive.type()) {
            case CHINESE_NAME:
                jsonGenerator.writeString(SensitiveUtils.chineseName(origin));
                break;
            case ID_CARD:
                jsonGenerator.writeString(SensitiveUtils.idCardNum(origin));
                break;
            case FIXED_PHONE:
                jsonGenerator.writeString(SensitiveUtils.fixedPhone(origin));
                break;
            case MOBILE_PHONE:
                jsonGenerator.writeString(SensitiveUtils.mobilePhone(origin));
                break;
            case ADDRESS:
                jsonGenerator.writeString(SensitiveUtils.address(origin));
                break;
            case EMAIL:
                jsonGenerator.writeString(SensitiveUtils.email(origin));
                break;
            case BANK_CARD:
                jsonGenerator.writeString(SensitiveUtils.bankCard(origin));
                break;
            case PASSWORD:
                jsonGenerator.writeString(SensitiveUtils.password(origin));
                break;
            case KEY:
                jsonGenerator.writeString(SensitiveUtils.key(origin));
                break;
            case CUSTOMER:
                jsonGenerator.writeString(SensitiveUtils.desValue(origin, sensitive.preLength(), sensitive.postLength(), sensitive.maskStr()));
                break;
            default:
                throw new IllegalArgumentException("Unknow sensitive type enum " + sensitive.type());
        }

    }

    @Override
    public JsonSerializer<?> createContextual(final SerializerProvider serializerProvider,
                                              final BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                Sensitive sensitive = beanProperty.getAnnotation(Sensitive.class);
                if (sensitive == null) {
                    sensitive = beanProperty.getContextAnnotation(Sensitive.class);
                }
                if (sensitive != null) {
                    return new SensitiveSerialize(sensitive);
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(null);
    }

}
