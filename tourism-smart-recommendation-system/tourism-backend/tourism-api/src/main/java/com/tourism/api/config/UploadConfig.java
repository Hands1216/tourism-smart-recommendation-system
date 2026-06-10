package com.tourism.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {

    /**
     * 上传文件存储路径
     */
    private String path = "C:/Users/HDS/Desktop/毕设/tourism-smart-recommendation-system/images/";

    /**
     * 访问URL前缀
     */
    private String urlPrefix = "/uploads/";

    /**
     * 最大文件大小（字节）
     */
    private Long maxSize = 10485760L; // 10MB

    /**
     * 允许的文件类型
     */
    private String allowedTypes = "jpg,jpeg,png,gif,webp";

    /**
     * 视频最大文件大小（字节）
     */
    private Long videoMaxSize = 104857600L; // 100MB

    /**
     * 允许的视频类型
     */
    private String videoAllowedTypes = "mp4,avi,mov,wmv,flv,mkv,webm";
}
