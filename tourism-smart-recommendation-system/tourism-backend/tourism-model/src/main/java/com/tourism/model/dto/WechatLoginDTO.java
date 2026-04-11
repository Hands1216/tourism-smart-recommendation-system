package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录请求DTO
 *
 * @author 韩东升
 */
@Data
public class WechatLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信授权码
     */
    private String code;

    /**
     * 状态参数（可选，用于防止CSRF攻击）
     */
    private String state;
}
