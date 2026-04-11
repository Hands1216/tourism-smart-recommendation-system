package com.tourism.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.algorithm.collaborative.UserBasedCF;
import com.tourism.algorithm.content.ContentBasedRecommender;
import com.tourism.algorithm.hybrid.HybridRecommender;
import com.tourism.algorithm.model.RecommendationResult;
import com.tourism.algorithm.model.UserPreference;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.User;
import com.tourism.model.entity.UserBehavior;
import com.tourism.service.mapper.AttractionMapper;
import com.tourism.service.mapper.UserBehaviorMapper;
import com.tourism.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐算法桥梁服务
 * 负责从数据库构建算法输入、初始化模型、提供推荐结果
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendAlgorithmService {

    private final UserBasedCF userBasedCF;
    private final ContentBasedRecommender contentBasedRecommender;
    private final HybridRecommender hybridRecommender;
    private final UserBehaviorMapper userBehaviorMapper;
    private final AttractionMapper attractionMapper;
    private final UserMapper userMapper;

    private volatile boolean initialized = false;

    /**
     * 定时刷新推荐模型（启动后30秒首次执行，之后每小时刷新）
     */
    @Scheduled(initialDelay = 30000, fixedRate = 3600000)
    public void refreshModels() {
        try {
            initModels();
            log.info("推荐模型定时刷新完成");
        } catch (Exception e) {
            log.error("推荐模型刷新失败: {}", e.getMessage());
        }
    }

    /**
     * 初始化推荐模型（从数据库加载数据构建矩阵）
     */
    public synchronized void initModels() {
        // A. 构建用户-项目评分矩阵（协同过滤输入）
        Map<String, Map<String, Double>> userItemMatrix = buildUserItemMatrix();

        // B. 构建景点特征向量（内容推荐输入）
        Map<String, Map<String, Double>> itemFeatures = buildItemFeatures();

        // C. 查询所有用户的显式偏好标签
        Map<String, String> userExplicitPrefs = loadUserExplicitPreferences();

        // D. 构建用户偏好（融合行为数据 + 显式偏好标签）
        Map<String, UserPreference> userPreferences = buildUserPreferences(userItemMatrix, itemFeatures, userExplicitPrefs);

        // E. 初始化模型
        userBasedCF.initModel(userItemMatrix);
        contentBasedRecommender.initModel(itemFeatures, userPreferences);
        initialized = true;

        log.info("推荐算法模型初始化完成 - 用户数: {}, 景点数: {}, 有显式偏好用户数: {}",
                userItemMatrix.size(), itemFeatures.size(), userExplicitPrefs.size());
    }

    /**
     * 获取混合推荐结果
     *
     * @param userId 用户ID
     * @param topN   推荐数量
     * @return 推荐的景点ID列表（按评分降序），冷启动用户返回空列表
     */
    public List<Long> getHybridRecommendations(Long userId, int topN) {
        if (!initialized) {
            try {
                initModels();
            } catch (Exception e) {
                log.error("推荐模型初始化失败: {}", e.getMessage());
                return List.of();
            }
        }

        try {
            RecommendationResult result = hybridRecommender.recommend(userId.toString(), topN);
            if (result == null || result.getItems() == null || result.getItems().isEmpty()) {
                return List.of();
            }

            return result.getItems().stream()
                    .map(item -> {
                        try {
                            return Long.parseLong(item.getItemId());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取推荐结果失败: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 从user_behavior表构建用户-项目评分矩阵
     */
    private Map<String, Map<String, Double>> buildUserItemMatrix() {
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getItemType, "attraction")
        );

        Map<String, Map<String, Double>> matrix = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior.getUserId() == null || behavior.getItemId() == null) {
                continue;
            }
            String uId = behavior.getUserId().toString();
            String iId = behavior.getItemId().toString();
            double weight = behavior.getWeight() != null ? behavior.getWeight() : 1.0;

            // 同一用户对同一景点取最大权重
            matrix.computeIfAbsent(uId, k -> new HashMap<>())
                    .merge(iId, weight, Math::max);
        }
        return matrix;
    }

    /**
     * 从attraction表构建景点特征向量
     */
    private Map<String, Map<String, Double>> buildItemFeatures() {
        List<Attraction> attractions = attractionMapper.selectList(
                new LambdaQueryWrapper<Attraction>()
                        .eq(Attraction::getAuditStatus, 1)
        );

        Map<String, Map<String, Double>> itemFeatures = new HashMap<>();
        for (Attraction attr : attractions) {
            Map<String, Double> features = new HashMap<>();

            // 分类特征
            if (attr.getCategoryId() != null) {
                features.put("category_" + attr.getCategoryId(), 1.0);
            }

            // 景区等级特征
            if (attr.getScenicLevel() != null) {
                features.put("level_" + attr.getScenicLevel(), parseScenicLevelWeight(attr.getScenicLevel()));
            }

            // 标签特征
            if (attr.getTags() != null && !attr.getTags().isBlank() && !"null".equals(attr.getTags())) {
                try {
                    List<String> tags = JSONUtil.toList(attr.getTags(), String.class);
                    for (String tag : tags) {
                        features.put("tag_" + tag, 1.0);
                    }
                } catch (Exception ignored) {
                    // 标签格式不规范，跳过
                }
            }

            // 场景特征
            if (attr.getSceneType() != null && !attr.getSceneType().isBlank()) {
                for (String scene : attr.getSceneType().split(",")) {
                    String trimmed = scene.trim();
                    if (!trimmed.isEmpty()) {
                        features.put("scene_" + trimmed, 1.0);
                    }
                }
            }

            // 价格特征（归一化到0-1）
            if (attr.getTicketPrice() != null) {
                double price = attr.getTicketPrice().doubleValue();
                features.put("price", Math.min(price / 500.0, 1.0));
            } else {
                features.put("price_free", 1.0);
            }

            itemFeatures.put(attr.getId().toString(), features);
        }
        return itemFeatures;
    }

    /**
     * 查询所有用户的显式偏好标签
     *
     * @return userId字符串 → preferences JSON字符串
     */
    private Map<String, String> loadUserExplicitPreferences() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .isNotNull(User::getPreferences)
                        .select(User::getId, User::getPreferences)
        );

        Map<String, String> result = new HashMap<>();
        for (User user : users) {
            String prefs = user.getPreferences();
            if (prefs != null && !prefs.isBlank() && !"[]".equals(prefs) && !"null".equals(prefs)) {
                result.put(user.getId().toString(), prefs);
            }
        }
        return result;
    }

    /**
     * 将偏好标签JSON解析为tag_xxx格式的特征键列表
     */
    private List<String> parsePreferenceTags(String preferencesJson) {
        List<String> tagKeys = new ArrayList<>();
        try {
            List<String> tags = JSONUtil.toList(preferencesJson, String.class);
            for (String tag : tags) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tagKeys.add("tag_" + trimmed);
                }
            }
        } catch (Exception e) {
            // 兼容非JSON格式（如 "自然风光,历史文化"）
            String cleaned = preferencesJson.replace("[", "").replace("]", "").replace("\"", "");
            for (String tag : cleaned.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tagKeys.add("tag_" + trimmed);
                }
            }
        }
        return tagKeys;
    }

    /**
     * 根据用户行为数据 + 显式偏好标签构建用户偏好
     */
    private Map<String, UserPreference> buildUserPreferences(
            Map<String, Map<String, Double>> userItemMatrix,
            Map<String, Map<String, Double>> itemFeatures,
            Map<String, String> userExplicitPrefs) {

        Map<String, UserPreference> preferences = new HashMap<>();

        // 收集所有需要构建偏好的用户ID（行为用户 + 有显式偏好的用户）
        Set<String> allUserIds = new HashSet<>(userItemMatrix.keySet());
        allUserIds.addAll(userExplicitPrefs.keySet());

        for (String uId : allUserIds) {
            Map<String, Double> aggregatedPrefs = new HashMap<>();
            double totalWeight = 0;

            // 1. 从行为数据聚合景点特征
            Map<String, Double> userItems = userItemMatrix.get(uId);
            if (userItems != null) {
                for (Map.Entry<String, Double> itemEntry : userItems.entrySet()) {
                    String itemId = itemEntry.getKey();
                    double weight = itemEntry.getValue();
                    Map<String, Double> features = itemFeatures.get(itemId);

                    if (features != null) {
                        for (Map.Entry<String, Double> feat : features.entrySet()) {
                            aggregatedPrefs.merge(feat.getKey(), feat.getValue() * weight, Double::sum);
                        }
                        totalWeight += weight;
                    }
                }
            }

            // 2. 融合用户显式偏好标签（权重2.0，介于浏览1.0和收藏3.0之间）
            String explicitPrefs = userExplicitPrefs.get(uId);
            if (explicitPrefs != null) {
                double explicitWeight = 2.0;
                List<String> tagKeys = parsePreferenceTags(explicitPrefs);
                for (String tagKey : tagKeys) {
                    aggregatedPrefs.merge(tagKey, explicitWeight, Double::sum);
                    totalWeight += explicitWeight;
                }
            }

            // 3. 归一化
            if (totalWeight > 0) {
                for (Map.Entry<String, Double> pref : aggregatedPrefs.entrySet()) {
                    pref.setValue(pref.getValue() / totalWeight);
                }
                preferences.put(uId, new UserPreference(uId, aggregatedPrefs, null, null, null));
            }
        }

        return preferences;
    }

    /**
     * 景区等级映射权重
     */
    private double parseScenicLevelWeight(String level) {
        if (level == null) return 1.0;
        if (level.contains("5A")) return 5.0;
        if (level.contains("4A")) return 4.0;
        if (level.contains("3A")) return 3.0;
        if (level.contains("2A")) return 2.0;
        if (level.contains("A")) return 1.0;
        if (level.contains("世界")) return 5.0;
        return 1.0;
    }
}
