package com.tourism.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点高级查询DTO
 *
 */
@Data
public class AttractionQueryDTO {

    /**
     * 关键词（景点名称）
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 景区等级
     */
    private String scenicLevel;

    /**
     * 场景分类
     */
    private String sceneType;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 建议游玩时长
     */
    private String suggestedDuration;

    /**
     * 推荐游玩月份（1-12）（已弃用，使用bestSeason）
     */
    private Integer bestMonth;

    /**
     * 最佳季节（春季/夏季/秋季/冬季）
     */
    private String bestSeason;

    /**
     * 排序字段：hot(热门)/rating(评分)/distance(距离)/price(价格)
     */
    private String sortBy;

    /**
     * 排序方向：asc/desc
     */
    private String sortOrder;

    /**
     * 用户纬度（距离排序用）
     */
    private BigDecimal userLat;

    /**
     * 用户经度（距离排序用）
     */
    private BigDecimal userLng;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;
}
