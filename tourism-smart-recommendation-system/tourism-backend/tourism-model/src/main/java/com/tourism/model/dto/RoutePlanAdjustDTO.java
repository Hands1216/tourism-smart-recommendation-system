package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 行程调整请求DTO
 *
 */
@Data
public class RoutePlanAdjustDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行程ID
     */
    private Long planId;

    /**
     * 调整类型：add/remove/modify/reorder
     */
    private String adjustType;

    /**
     * 目标天数（从1开始）
     */
    private Integer dayIndex;

    /**
     * 目标活动索引（从0开始）
     */
    private Integer activityIndex;

    /**
     * 新活动数据（add/modify时使用）
     */
    private ActivityData newActivity;

    /**
     * 新位置（reorder时使用）
     */
    private Integer newDayIndex;
    private Integer newActivityIndex;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 活动数据
     */
    @Data
    public static class ActivityData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String type;        // attraction/restaurant/hotel/transport
        private String title;
        private String description;
        private String time;
        private String duration;
        private String distance;
        private String transport;
        private String cost;
        private String tips;
    }
}
