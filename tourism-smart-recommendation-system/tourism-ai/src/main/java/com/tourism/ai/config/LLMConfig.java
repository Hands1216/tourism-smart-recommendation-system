package com.tourism.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型配置类
 *
 * @author 韩东升
 */
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {

    /**
     * 提供商: qwen/chatglm/azure
     */
    private String provider = "qwen";

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model = "qwen-max";

    /**
     * API基础URL
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 默认温度参数
     */
    private Double temperature = 0.7;

    /**
     * 默认最大token数
     */
    private Integer maxTokens = 2000;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
