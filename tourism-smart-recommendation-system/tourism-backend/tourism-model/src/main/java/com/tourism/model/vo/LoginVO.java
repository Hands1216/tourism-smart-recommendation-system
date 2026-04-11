package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * @author 韩东升
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO userInfo;
}
