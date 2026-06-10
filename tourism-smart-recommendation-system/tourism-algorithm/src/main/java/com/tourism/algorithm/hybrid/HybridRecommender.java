package com.tourism.algorithm.hybrid;

import com.tourism.algorithm.collaborative.UserBasedCF;
import com.tourism.algorithm.content.ContentBasedRecommender;
import com.tourism.algorithm.model.RecommendationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合推荐算法
 * 结合协同过滤和基于内容的推荐
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridRecommender {

    private final UserBasedCF userBasedCF;
    private final ContentBasedRecommender contentBasedRecommender;

    /**
     * 协同过滤权重
     */
    private static final double CF_WEIGHT = 0.6;

    /**
     * 内容推荐权重
     */
    private static final double CB_WEIGHT = 0.4;

    /**
     * 为用户生成混合推荐
     *
     * @param userId 用户ID
     * @param topN   推荐数量
     * @return 推荐结果
     */
    public RecommendationResult recommend(String userId, int topN) {
        // 获取协同过滤推荐
        RecommendationResult cfResult = userBasedCF.recommend(userId, topN * 2, true);

        // 获取基于内容的推荐
        RecommendationResult cbResult = contentBasedRecommender.recommend(userId, topN * 2);

        // 合并结果
        Map<String, HybridScore> scoreMap = new HashMap<>();

        // 处理协同过滤结果
        for (RecommendationResult.RecommendationItem item : cfResult.getItems()) {
            scoreMap.put(item.getItemId(), new HybridScore(item.getScore() * CF_WEIGHT, 0.0));
        }

        // 处理内容推荐结果
        for (RecommendationResult.RecommendationItem item : cbResult.getItems()) {
            scoreMap.computeIfAbsent(item.getItemId(), k -> new HybridScore(0.0, 0.0))
                    .setContentScore(item.getScore() * CB_WEIGHT);
        }

        // 计算综合得分并排序
        List<RecommendationResult.RecommendationItem> recommendations = scoreMap.entrySet().stream()
                .map(entry -> {
                    String itemId = entry.getKey();
                    HybridScore score = entry.getValue();
                    double finalScore = score.getCfScore() + score.getContentScore();

                    String reason = buildRecommendationReason(score);

                    return new RecommendationResult.RecommendationItem(
                            itemId,
                            itemId,
                            finalScore,
                            reason
                    );
                })
                .filter(item -> item.getScore() > 0)
                .sorted((i1, i2) -> Double.compare(i2.getScore(), i1.getScore()))
                .limit(topN)
                .collect(Collectors.toList());

        return new RecommendationResult(userId, recommendations, "混合推荐（协同过滤 + 内容推荐）");
    }

    /**
     * 构建推荐理由
     */
    private String buildRecommendationReason(HybridScore score) {
        List<String> reasons = new ArrayList<>();

        if (score.getCfScore() > 0) {
            reasons.add("相似用户喜欢");
        }

        if (score.getContentScore() > 0) {
            reasons.add("符合您的兴趣偏好");
        }

        return String.join("，", reasons);
    }

    /**
     * 混合评分
     */
    private static class HybridScore {
        private final double cfScore;
        private double contentScore;

        public HybridScore(double cfScore, double contentScore) {
            this.cfScore = cfScore;
            this.contentScore = contentScore;
        }

        public double getCfScore() {
            return cfScore;
        }

        public double getContentScore() {
            return contentScore;
        }

        public void setContentScore(double contentScore) {
            this.contentScore = contentScore;
        }
    }
}
