package com.tourism.service.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 天气API配置（和风天气）
 * 支持API Key和JWT两种认证方式
 *
 * @author 韩东升
 */
@Data
@Component
@ConfigurationProperties(prefix = "external.weather")
public class WeatherApiConfig {

    /**
     * 和风天气API Key（API Key认证方式使用）
     */
    private String key = "";

    /**
     * 项目ID（JWT认证方式使用）
     */
    private String projectId = "";

    /**
     * 私钥ID/凭据ID（JWT认证方式使用）
     */
    private String keyId = "";

    /**
     * 私钥内容（JWT认证方式使用，Ed25519私钥）
     */
    private String privateKey = "";

    /**
     * 认证方式：key 或 jwt
     */
    private String authType = "key";

    /**
     * API基础URL
     */
    private String baseUrl = "https://devapi.qweather.com/v7";

    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 10000;

    /**
     * 是否使用JWT认证
     */
    public boolean isJwtAuth() {
        return "jwt".equalsIgnoreCase(authType);
    }
}
