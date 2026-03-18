package com.molandev.framework.encrypt.params;

import java.lang.annotation.*;

/**
 * Controller 上使用，标记此参数是加密后的，需要解密
 */
@Target(value = {ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptedParam {

    /**
     * 参数名称
     * 如果不指定（默认为空字符串），则自动使用方法参数名
     *
     * @return {@link String}
     */
    String value() default "";

}
