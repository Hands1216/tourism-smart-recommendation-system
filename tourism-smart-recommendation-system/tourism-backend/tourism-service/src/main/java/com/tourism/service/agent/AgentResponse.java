package com.tourism.service.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 智能体响应结果
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应文本
     */
    private String content;

    /**
     * 响应类型：text/json/list/recommendation
     */
    private String type;

    /**
     * 结构化数据（可选）
     */
    private Map<String, Object> data;

    /**
     * 推荐列表（可选）
     */
    private List<RecommendationItem> recommendations;

    /**
     * 置信度 0-1
     */
    private double confidence;

    /**
     * 处理该请求的智能体名称
     */
    private String agentName;

    /**
     * 后续建议操作
     */
    private List<String> suggestedActions;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 推荐项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String id;
        private String name;
        private String type;        // attraction/hotel/restaurant
        private String description;
        private String image;
        private Double rating;
        private String price;
        private String location;
        private Map<String, Object> extra;
    }

    /**
     * 创建成功响应
     */
    public static AgentResponse success(String content) {
        return AgentResponse.builder()
                .success(true)
                .content(content)
                .type("text")
                .confidence(1.0)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AgentResponse failure(String errorMessage) {
        return AgentResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .confidence(0.0)
                .build();
    }

    /**
     * 创建推荐响应
     */
    public static AgentResponse withRecommendations(String content, List<RecommendationItem> items) {
        return AgentResponse.builder()
                .success(true)
                .content(content)
                .type("recommendation")
                .recommendations(items)
                .confidence(1.0)
                .build();
    }
}
