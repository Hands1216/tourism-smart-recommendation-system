package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 *
 * @author 韩东升
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户偏好标签
     */
    private String preferences;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
