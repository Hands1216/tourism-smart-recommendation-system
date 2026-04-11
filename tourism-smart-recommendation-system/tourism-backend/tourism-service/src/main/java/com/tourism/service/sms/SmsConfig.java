package com.tourism.service.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云号码认证服务（DYPNS）短信配置
 *
 * @author 韩东升
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsConfig {

    /**
     * 是否启用真实短信发送（false则使用模拟模式）
     */
    private boolean enabled = false;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板Code
     */
    private String templateCode;

    /**
     * 验证码有效期（分钟）
     */
    private Integer codeExpireMinutes = 5;

    /**
     * 验证码长度
     */
    private Integer codeLength = 6;
}
