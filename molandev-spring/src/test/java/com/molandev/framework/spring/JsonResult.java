package com.molandev.framework.spring;

import lombok.*;

/**
 * 响应结果
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonResult<T> {

    /**
     * 返回成功
     */
    public static final String SUCCESS = "0000";

    /**
     * 失败的
     */
    public static final String FAILED = "1000";

    /**
     * 参数不合法
     */
    public static final String INVALID = "2001";


    /**
     * 未登录
     */
    public static final String NOT_LOGIN = "3001";

    /**
     * 没有权限
     */
    public static final String NO_PERMISSION = "3002";


    /**
     * 未登录
     */
    public static final String FIRST_LOGIN = "3011";


    /**
     * 被踢
     */
    public static final String KICK_OUT = "3012";

    /**
     * 密码过期
     */
    public static final String PWD_EXPIRE = "3022";

    /**
     * 验证码错误
     */
    public static final String VERIFY_ERROR = "4001";


    /**
     * 状态码
     */
    private String code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    public JsonResult(String code, T data, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功的结果
     *
     * @return {@link JsonResult <T>}
     */
    public static <T> JsonResult<T> success() {
        return new JsonResult<>(SUCCESS, (T) null, null);
    }

    /**
     * 成功的结果
     *
     * @param data 数据
     * @return {@link JsonResult <T>}
     */
    public static <T> JsonResult<T> success(T data) {
        return new JsonResult<>(SUCCESS, data, null);
    }

    /**
     * 失败的结果
     *
     * @return {@link JsonResult}
     */
    public static <T> JsonResult<T> failed(String msg) {
        return new JsonResult<>(FAILED, (T) null, msg);
    }

    /**
     * 参数错误
     *
     * @param msg 错误信息
     * @return {@link JsonResult}
     */
    public static <T> JsonResult<T> invalid(String msg) {
        return new JsonResult<>(INVALID, (T) null, msg);
    }


    /**
     * 未登录
     *
     * @return {@link JsonResult}
     */
    public static <T> JsonResult<T> notLogin() {
        return new JsonResult<>(NOT_LOGIN, (T) null, "未登录");

    }

    /**
     * 没有权限
     *
     * @return {@link JsonResult}
     */
    public static <T> JsonResult<T> noPermission() {
        return new JsonResult<>(NO_PERMISSION, (T) null, "权限不足");
    }

    /**
     * 验证码错误
     *
     * @return {@link JsonResult}
     */
    public static <T> JsonResult<T> verifyError() {
        return new JsonResult<>(VERIFY_ERROR, (T) null, "验证码错误");
    }

    /**
     * 首次登录
     */
    public static <T> JsonResult<T> kickOut(T data) {
        return new JsonResult<>(KICK_OUT, data, "您已被踢出");
    }


}
