package com.tourism.model.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 路线规划结果VO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 行程标题
     */
    private String title;

    /**
     * 总景点数
     */
    private Integer totalAttractions;

    /**
     * 总里程
     */
    private String totalDistance;

    /**
     * 每日行程
     */
    private List<DayPlanVO> days;

    /**
     * 出发省份（用于保存）
     */
    private String departureProvince;

    /**
     * 出发城市（用于保存）
     */
    private String departureCity;

    /**
     * 目的地列表（用于保存）
     */
    private List<String> destinations;

    /**
     * 出行天数（用于保存）
     */
    private Integer tripDays;

    /**
     * 出行月份（用于保存）
     */
    private Integer month;

    /**
     * 同行伙伴（用于保存）
     */
    private String companion;

    /**
     * 行程节奏（用于保存）
     */
    private String pace;

    /**
     * 备注说明（用于保存）
     */
    private String remark;

    /**
     * 天气建议（实时获取）
     */
    private String weatherAdvice;

    /**
     * 交通状况概览（实时获取）
     */
    private String trafficOverview;

    /**
     * 日程VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPlanVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 当天标题
         */
        private String title;

        /**
         * 日期
         */
        private String date;

        /**
         * 活动列表
         */
        private List<ActivityVO> activities;
    }

    /**
     * 活动VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 活动类型: attraction-景点, restaurant-美食, hotel-住宿, transport-交通
         */
        private String type;

        /**
         * 活动标题
         */
        private String title;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 时间
         */
        private String time;

        /**
         * 游览时长
         */
        private String duration;

        /**
         * 距离
         */
        private String distance;

        /**
         * 交通方式
         */
        private String transport;

        /**
         * 预估费用
         */
        private String cost;

        /**
         * 提示信息
         */
        private String tips;

        /**
         * 图片URL
         */
        private String image;

        /**
         * 预订/购票链接
         */
        private String bookingUrl;
    }
}
