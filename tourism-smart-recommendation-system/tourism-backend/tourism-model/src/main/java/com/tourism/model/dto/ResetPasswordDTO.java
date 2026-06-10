package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码DTO
 *
 */
@Data
public class ResetPasswordDTO implements Serializable {

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
     * 新密码
     */
    private String newPassword;
}
