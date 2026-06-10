package com.tourism.ai.dto;

import lombok.Data;

/**
 * 聊天响应DTO
 *
 */
@Data
public class ChatResponse {

    /**
     * 响应内容
     */
    private String content;

    /**
     * 意图类型
     */
    private String intent;

    /**
     * 提取的实体信息
     */
    private EntityInfo entities;

    /**
     * 使用的token数
     */
    private Integer usage;

    @Data
    public static class EntityInfo {
        /**
         * 目的地
         */
        private String destination;

        /**
         * 天数
         */
        private Integer days;

        /**
         * 预算
         */
        private Double budget;

        /**
         * 兴趣标签
         */
        private String interests;

        /**
         * 同行人员
         */
        private String companions;
    }
}
