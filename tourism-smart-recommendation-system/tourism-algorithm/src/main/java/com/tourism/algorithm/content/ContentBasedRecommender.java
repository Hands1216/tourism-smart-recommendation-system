package com.tourism.algorithm.content;

import com.tourism.algorithm.model.RecommendationResult;
import com.tourism.algorithm.model.UserPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于内容的推荐算法
 *
 * @author 韩东升
 */
@Slf4j
@Component
public class ContentBasedRecommender {

    /**
     * 景点特征向量
     */
    private Map<String, Map<String, Double>> itemFeatures;

    /**
     * 用户偏好
     */
    private Map<String, UserPreference> userPreferences;

    /**
     * 初始化模型
     *
     * @param itemFeatures    项目特征
     * @param userPreferences 用户偏好
     */
    public void initModel(Map<String, Map<String, Double>> itemFeatures,
                          Map<String, UserPreference> userPreferences) {
        this.itemFeatures = itemFeatures;
        this.userPreferences = userPreferences;
        log.info("基于内容的推荐模型初始化完成");
    }

    /**
     * 计算项目相似度（余弦相似度）
     *
     * @param item1Id 项目1 ID
     * @param item2Id 项目2 ID
     * @return 相似度 [0, 1]
     */
    public double calculateItemSimilarity(String item1Id, String item2Id) {
        Map<String, Double> features1 = itemFeatures.get(item1Id);
        Map<String, Double> features2 = itemFeatures.get(item2Id);

        if (features1 == null || features2 == null) {
            return 0.0;
        }

        // 计算余弦相似度
        Set<String> allFeatures = new HashSet<>();
        allFeatures.addAll(features1.keySet());
        allFeatures.addAll(features2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String feature : allFeatures) {
            double f1 = features1.getOrDefault(feature, 0.0);
            double f2 = features2.getOrDefault(feature, 0.0);
            dotProduct += f1 * f2;
            norm1 += f1 * f1;
            norm2 += f2 * f2;
        }

        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator > 0 ? dotProduct / denominator : 0.0;
    }

    /**
     * 根据用户偏好计算项目匹配度
     *
     * @param userId 用户ID
     * @param itemId 项目ID
     * @return 匹配度 [0, 1]
     */
    public double calculateUserItemMatch(String userId, String itemId) {
        UserPreference preference = userPreferences.get(userId);
        Map<String, Double> itemFeature = itemFeatures.get(itemId);

        if (preference == null || itemFeature == null || preference.getPreferences() == null) {
            return 0.0;
        }

        // 计算用户偏好与项目特征的匹配度
        double matchScore = 0.0;
        Map<String, Double> userPrefs = preference.getPreferences();

        for (Map.Entry<String, Double> entry : userPrefs.entrySet()) {
            String feature = entry.getKey();
            double weight = entry.getValue();
            double featureValue = itemFeature.getOrDefault(feature, 0.0);
            matchScore += weight * featureValue;
        }

        // 归一化
        double totalWeight = userPrefs.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalWeight > 0 ? matchScore / totalWeight : 0.0;
    }

    /**
     * 为用户生成推荐
     *
     * @param userId 用户ID
     * @param topN   推荐数量
     * @return 推荐结果
     */
    public RecommendationResult recommend(String userId, int topN) {
        if (!userPreferences.containsKey(userId)) {
            return new RecommendationResult(userId, Collections.emptyList(), "用户偏好信息不足");
        }

        // 计算所有项目的匹配度
        List<RecommendationResult.RecommendationItem> recommendations = itemFeatures.keySet().stream()
                .map(itemId -> new RecommendationResult.RecommendationItem(
                        itemId,
                        itemId,
                        calculateUserItemMatch(userId, itemId),
                        "基于您的兴趣偏好推荐"
                ))
                .filter(item -> item.getScore() > 0)
                .sorted((i1, i2) -> Double.compare(i2.getScore(), i1.getScore()))
                .limit(topN)
                .collect(Collectors.toList());

        return new RecommendationResult(userId, recommendations, "基于内容的推荐");
    }

    /**
     * 查找相似项目
     *
     * @param itemId 项目ID
     * @param topN   返回数量
     * @return 相似项目列表
     */
    public List<String> findSimilarItems(String itemId, int topN) {
        return itemFeatures.keySet().stream()
                .filter(otherId -> !otherId.equals(itemId))
                .map(otherId -> new AbstractMap.SimpleEntry<>(
                        otherId,
                        calculateItemSimilarity(itemId, otherId)
                ))
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
