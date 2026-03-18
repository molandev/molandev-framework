package com.molandev.framework.spring.condition;

import com.molandev.framework.util.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 当有某个固定前缀的配置存在时
 */
public class OnPropertyPrefixCondition extends SpringBootCondition {

    /**
     * 有前缀
     *
     * @param env    env
     * @param prefix 前缀
     */
    public static boolean hasPrefix(ConfigurableEnvironment env, String prefix) {
        MutablePropertySources propertySources = env.getPropertySources();
        for (PropertySource<?> next : propertySources) {
            if (next instanceof EnumerablePropertySource) {
                String[] propertyNames = ((EnumerablePropertySource<?>) next).getPropertyNames();
                for (String propertyName : propertyNames) {
                    if (propertyName.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();

        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(ConditionalOnPropertyPrefix.class.getName());
        if (annotationAttributes == null) {
            return ConditionOutcome.noMatch("");
        }
        String prefix = (String) annotationAttributes.get("prefix");
        boolean matchIfMissing = (boolean) annotationAttributes.get("matchIfMissing");
        if (StringUtils.isEmpty(prefix)) {
            return ConditionOutcome.noMatch("");
        }
        if (!(environment instanceof ConfigurableEnvironment)) {
            return ConditionOutcome.noMatch("");
        }
        boolean hasPrefix = hasPrefix((ConfigurableEnvironment) environment, prefix);
        if (hasPrefix && !matchIfMissing) {
            return ConditionOutcome.match();
        } else if (!hasPrefix && matchIfMissing) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch("");
    }

}
