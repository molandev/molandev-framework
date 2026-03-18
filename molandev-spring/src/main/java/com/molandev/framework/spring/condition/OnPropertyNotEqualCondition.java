package com.molandev.framework.spring.condition;

import com.molandev.framework.util.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 *
 */
public class OnPropertyNotEqualCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();

        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(ConditionalOnPropertyNotEqual.class.getName());
        if (annotationAttributes == null) {
            return ConditionOutcome.noMatch("");
        }
        String name = (String) annotationAttributes.get("name");
        String havingValue = (String) annotationAttributes.get("havingValue");
        if (StringUtils.isEmpty(name)) {
            return ConditionOutcome.noMatch("");
        }
        if (!(environment instanceof ConfigurableEnvironment)) {
            return ConditionOutcome.noMatch("");
        }

        String propertyValue = context.getEnvironment().getProperty(name);
        boolean notEqual = !havingValue.equals(propertyValue);
        if (notEqual) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch("");
    }

}