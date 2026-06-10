package com.tourism.service.wechat;

import lombok.Data;

/**
 * 微信用户信息
 *
 */
@Data
public class WechatUserInfo {

    /**
     * 微信用户唯一标识
     */
    private String openid;

    /**
     * 微信用户昵称
     */
    private String nickname;

    /**
     * 微信用户头像
     */
    private String headimgurl;

    /**
     * 性别：1-男，2-女，0-未知
     */
    private Integer sex;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 国家
     */
    private String country;

    /**
     * 用户统一标识（同一开放平台下的应用）
     */
    private String unionid;
}
