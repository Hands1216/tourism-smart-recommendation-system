package com.tourism.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐结果
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 推荐列表
     */
    private List<RecommendationItem> items;

    /**
     * 推荐原因
     */
    private String reason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        /**
         * 项目ID（景点ID）
         */
        private String itemId;

        /**
         * 项目名称
         */
        private String itemName;

        /**
         * 预测评分
         */
        private Double score;

        /**
         * 推荐理由
         */
        private String reason;
    }
}
