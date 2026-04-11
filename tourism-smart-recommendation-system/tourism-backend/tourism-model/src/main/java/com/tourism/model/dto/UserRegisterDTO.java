package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 *
 * @author 韩东升
 */
@Data
public class UserRegisterDTO implements Serializable {

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
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 偏好标签（JSON数组字符串）
     */
    private String preferences;
}
