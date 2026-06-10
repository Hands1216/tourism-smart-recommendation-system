package com.tourism.service.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图API配置
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "external.amap")
public class MapApiConfig {

    /**
     * 高德地图API Key
     */
    private String key = "";

    /**
     * API基础URL
     */
    private String baseUrl = "https://restapi.amap.com/v3";

    /**
     * 是否启用
     */
    private boolean enabled = false;

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 10000;
}
