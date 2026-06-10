package com.tourism.service.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信登录配置
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {

    /**
     * 微信应用ID
     */
    private String appId;

    /**
     * 微信应用密钥
     */
    private String appSecret;

    /**
     * 微信授权URL
     */
    private String authorizeUrl = "https://open.weixin.qq.com/connect/qrconnect";

    /**
     * 微信获取access_token URL
     */
    private String accessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";

    /**
     * 微信获取用户信息URL
     */
    private String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo";
}
