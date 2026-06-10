package com.tourism.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 聊天请求DTO
 *
 */
@Data
public class ChatRequest {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 历史消息
     */
    private List<ChatMessage> history;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    @Data
    public static class ChatMessage {
        /**
         * 角色: user/assistant/system
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
