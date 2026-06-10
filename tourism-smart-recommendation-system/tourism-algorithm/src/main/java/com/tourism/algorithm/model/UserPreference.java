package com.tourism.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 用户偏好模型
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 偏好标签及权重
     */
    private Map<String, Double> preferences;

    /**
     * 喜欢的景点类别
     */
    private String preferredCategories;

    /**
     * 预算范围
     */
    private Double budgetRange;

    /**
     * 活跃度
     */
    private Double activityLevel;
}
