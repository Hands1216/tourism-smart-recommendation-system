package com.tourism.common.result;

import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author 韩东升
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户不存在
     */
    USER_NOT_EXIST(1001, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXIST(1002, "用户已存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(1003, "密码错误"),

    /**
     * 验证码错误
     */
    CODE_ERROR(1004, "验证码错误"),

    /**
     * 验证码已过期
     */
    CODE_EXPIRED(1005, "验证码已过期"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(1006, "Token已过期"),

    /**
     * Token无效
     */
    TOKEN_INVALID(1007, "Token无效"),

    /**
     * 景点不存在
     */
    ATTRACTION_NOT_EXIST(2001, "景点不存在"),

    /**
     * 攻略不存在
     */
    STRATEGY_NOT_EXIST(3001, "攻略不存在"),

    /**
     * 攻略审核中
     */
    STRATEGY_AUDITING(3002, "攻略审核中"),

    /**
     * 内容包含敏感词
     */
    SENSITIVE_WORD(4001, "内容包含敏感词");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
