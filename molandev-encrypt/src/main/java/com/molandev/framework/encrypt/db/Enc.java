package com.molandev.framework.encrypt.db;

import java.lang.annotation.*;

/**
 * 加解密、标记 @Enc 在实体类的字段上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Enc {

}
