package com.molandev.framework.encrypt.params;

import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import com.molandev.framework.util.encrypt.AesUtil;
import com.molandev.framework.util.encrypt.RsaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 方法参数解析器接口，这个接口是SpringMVC参数解析绑定的核心接口。 不同的参数类型绑定都是通过实现这个接口来实现。 也可以通过实现这个接口来自定义参数解析器
 */
public class EncryptedParamHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    EncryptProperties encryptProperties;

    /**
     * 该解析器是否支持parameter参数的解析
     *
     * @param parameter 拦截到的参数
     * @return 是否符合我们的拦截规则
     */
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(String.class)
                && parameter.hasParameterAnnotation(EncryptedParam.class);
    }

    /**
     * 解密参数并注入
     *
     * @param parameter     参数
     * @param mavContainer  飞行器容器
     * @param webRequest    web请求
     * @param binderFactory 粘结剂的工厂
     * @return {@link Object}
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        EncryptedParam annotation = parameter.getParameter().getAnnotation(EncryptedParam.class);
        
        // 获取参数名：优先使用注解指定，否则使用方法参数名
        String paramKey = annotation.value();
        if (StringUtils.isEmpty(paramKey)) {
            paramKey = parameter.getParameterName();
            if (paramKey == null) {
                throw new IllegalArgumentException(
                        "无法获取参数名，请显式指定 @EncryptedParam 的 value 属性或确保编译时包含参数名信息（-parameters）"
                );
            }
        }
        
        String parameterVal = webRequest.getParameter(paramKey);
        if (StringUtils.isNotEmpty(parameterVal)) {
            if (EncryptProperties.ParamEncryptType.AES.equals(encryptProperties.getParams().getType())) {
                return AesUtil.decrypt(parameterVal, encryptProperties.getParams().getKey(), encryptProperties.getParams().getAlgorithm());
            } else {
                return RsaUtil.privateDecrypt(parameterVal, encryptProperties.getParams().getKey());
            }
        }
        return parameterVal;
    }

}
