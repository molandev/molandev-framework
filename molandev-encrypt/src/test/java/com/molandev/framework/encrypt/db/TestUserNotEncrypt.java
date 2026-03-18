package com.molandev.framework.encrypt.db;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 测试用户实体类
 */
@Table("test_user")
@Getter
@Setter
public class TestUserNotEncrypt {
    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Generator, value = "uuid")
    private String id;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 手机号（加密存储）
     */
    private String phone;

    /**
     * 身份证号（加密存储）
     */
    private String idCard;

    /**
     * 邮箱（加密存储）
     */
    private String email;
}