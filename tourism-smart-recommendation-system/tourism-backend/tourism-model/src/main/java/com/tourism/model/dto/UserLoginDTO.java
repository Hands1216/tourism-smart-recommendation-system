package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录DTO
 *
 * @author 韩东升
 */
@Data
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 验证码
     */
    private String code;

    /**
     * 密码（密码登录时使用）
     */
    private String password;

    /**
     * 微信授权码（微信登录时使用）
     */
    private String wechatCode;
}
