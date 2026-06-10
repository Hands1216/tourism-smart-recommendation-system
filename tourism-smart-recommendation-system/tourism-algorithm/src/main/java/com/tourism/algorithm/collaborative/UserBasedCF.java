package com.tourism.algorithm.collaborative;

import com.tourism.algorithm.model.RecommendationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于用户的协同过滤算法
 *
 */
@Slf4j
@Component
public class UserBasedCF {

    /**
     * 用户-项目评分矩阵
     */
    private Map<String, Map<String, Double>> userItemMatrix;

    /**
     * 相似度缓存
     */
    private Map<String, Map<String, Double>> similarityCache;

    /**
     * 初始化评分矩阵
     *
     * @param userItemMatrix 用户-项目评分矩阵
     */
    public void initModel(Map<String, Map<String, Double>> userItemMatrix) {
        this.userItemMatrix = userItemMatrix;
        this.similarityCache = new HashMap<>();
        log.info("UserBasedCF模型初始化完成，用户数: {}", userItemMatrix.size());
    }

    /**
     * 计算用户相似度（皮尔逊相关系数）
     *
     * @param user1Id 用户1 ID
     * @param user2Id 用户2 ID
     * @return 相似度 [-1, 1]
     */
    public double calculateSimilarity(String user1Id, String user2Id) {
        // 检查缓存
        if (similarityCache.containsKey(user1Id) && similarityCache.get(user1Id).containsKey(user2Id)) {
            return similarityCache.get(user1Id).get(user2Id);
        }

        Map<String, Double> user1Items = userItemMatrix.get(user1Id);
        Map<String, Double> user2Items = userItemMatrix.get(user2Id);

        if (user1Items == null || user2Items == null) {
            return 0.0;
        }

        // 找到共同评分的项目
        Set<String> commonItems = new HashSet<>(user1Items.keySet());
        commonItems.retainAll(user2Items.keySet());

        if (commonItems.size() < 2) {
            return 0.0;
        }

        // 计算平均值
        double avg1 = user1Items.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avg2 = user2Items.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 计算皮尔逊相关系数
        double numerator = 0.0;
        double denominator1 = 0.0;
        double denominator2 = 0.0;

        for (String item : commonItems) {
            double diff1 = user1Items.get(item) - avg1;
            double diff2 = user2Items.get(item) - avg2;
            numerator += diff1 * diff2;
            denominator1 += diff1 * diff1;
            denominator2 += diff2 * diff2;
        }

        double similarity = 0.0;
        if (denominator1 > 0 && denominator2 > 0) {
            similarity = numerator / Math.sqrt(denominator1 * denominator2);
        }

        // 缓存结果
        similarityCache.computeIfAbsent(user1Id, k -> new HashMap<>()).put(user2Id, similarity);
        similarityCache.computeIfAbsent(user2Id, k -> new HashMap<>()).put(user1Id, similarity);

        return similarity;
    }

    /**
     * 查找K个最相似的用户
     *
     * @param userId 用户ID
     * @param k      返回数量
     * @return 相似用户列表
     */
    public List<Map.Entry<String, Double>> findKNearestNeighbors(String userId, int k) {
        return userItemMatrix.keySet().stream()
                .filter(otherId -> !otherId.equals(userId))
                .map(otherId -> new AbstractMap.SimpleEntry<>(otherId, calculateSimilarity(userId, otherId)))
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(k)
                .collect(Collectors.toList());
    }

    /**
     * 预测用户对项目的评分
     *
     * @param userId 用户ID
     * @param itemId 项目ID
     * @return 预测评分
     */
    public double predictRating(String userId, String itemId) {
        Map<String, Double> userItems = userItemMatrix.get(userId);
        if (userItems != null && userItems.containsKey(itemId)) {
            return userItems.get(itemId);
        }

        // 找到相似用户
        List<Map.Entry<String, Double>> neighbors = findKNearestNeighbors(userId, 10);

        if (neighbors.isEmpty()) {
            return 0.0;
        }

        // 计算预测评分
        double sumSimilarity = 0.0;
        double sumRating = 0.0;

        for (Map.Entry<String, Double> neighbor : neighbors) {
            Map<String, Double> neighborItems = userItemMatrix.get(neighbor.getKey());
            if (neighborItems != null && neighborItems.containsKey(itemId)) {
                double similarity = neighbor.getValue();
                double rating = neighborItems.get(itemId);
                sumRating += similarity * rating;
                sumSimilarity += Math.abs(similarity);
            }
        }

        return sumSimilarity > 0 ? sumRating / sumSimilarity : 0.0;
    }

    /**
     * 为用户生成推荐
     *
     * @param userId         用户ID
     * @param topN           推荐数量
     * @param excludeRated   是否排除已评分项目
     * @return 推荐结果
     */
    public RecommendationResult recommend(String userId, int topN, boolean excludeRated) {
        if (!userItemMatrix.containsKey(userId)) {
            return new RecommendationResult(userId, Collections.emptyList(), "新用户，暂无推荐");
        }

        // 收集所有项目
        Set<String> allItems = new HashSet<>();
        for (Map<String, Double> items : userItemMatrix.values()) {
            allItems.addAll(items.keySet());
        }

        // 排除已评分项目
        Set<String> ratedItems = excludeRated ? userItemMatrix.get(userId).keySet() : Collections.emptySet();

        // 预测评分并排序
        List<RecommendationResult.RecommendationItem> recommendations = allItems.stream()
                .filter(item -> !ratedItems.contains(item))
                .map(item -> new RecommendationResult.RecommendationItem(
                        item,
                        item,
                        predictRating(userId, item),
                        "基于相似用户的偏好推荐"
                ))
                .filter(item -> item.getScore() > 0)
                .sorted((i1, i2) -> Double.compare(i2.getScore(), i1.getScore()))
                .limit(topN)
                .collect(Collectors.toList());

        return new RecommendationResult(userId, recommendations, "基于用户的协同过滤推荐");
    }
}
