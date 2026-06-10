package com.tourism.service.wechat;

import com.tourism.model.vo.LoginVO;

/**
 * 微信登录服务接口
 *
 */
public interface WechatService {

    /**
     * 获取微信授权URL
     *
     * @param redirectUri 回调地址
     * @param state 状态参数（防止CSRF攻击）
     * @return 授权URL
     */
    String getAuthorizeUrl(String redirectUri, String state);

    /**
     * 微信登录
     *
     * @param code 微信授权码
     * @return 登录结果
     */
    LoginVO loginByWechat(String code);

    /**
     * 获取微信用户信息
     *
     * @param code 微信授权码
     * @return 微信用户信息
     */
    WechatUserInfo getWechatUserInfo(String code);
}
