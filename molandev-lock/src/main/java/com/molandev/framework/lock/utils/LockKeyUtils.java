package com.molandev.framework.lock.utils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Key 解析工具类
 */
public class LockKeyUtils {

    /**
     * 参数名称发现器
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * SpEL 表达式解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private LockKeyUtils() {
        // 工具类不允许实例化
    }

    /**
     * 解析 key，获取锁的名字
     *
     * @param joinPoint 连接点
     * @param key       key表达式
     * @return 解析后的key
     */
    public static String resolveKey(JoinPoint joinPoint, String key) {
        // key为空，默认获取类名加方法名
        if (!StringUtils.hasText(key)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            return signature.getDeclaringTypeName() + "#" + signature.getMethod().getName();
        }

        // 如果key不包含#号，则认为是普通字符串，直接返回
        if (!key.contains("#")) {
            return key;
        }

        // 解析 SpEL 表达式
        Method method = getMethod(joinPoint);
        String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new MethodBasedEvaluationContext(new Object(), method, args, NAME_DISCOVERER);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        Object value = PARSER.parseExpression(key).getValue(context);
        return value != null ? value.toString() : "";
    }

    /**
     * 获取方法
     *
     * @param joinPoint 连接点
     * @return 方法对象
     */
    static Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(
                        signature.getName(),
                        method.getParameterTypes()
                );
            } catch (Exception e) {
                // 保持原方法
            }
        }
        return method;
    }

}
