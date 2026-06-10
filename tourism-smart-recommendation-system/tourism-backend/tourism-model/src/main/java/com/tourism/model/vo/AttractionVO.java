package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 景点信息VO
 *
 */
@Data
public class AttractionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 景点ID
     */
    private Long id;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 景点描述
     */
    private String description;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 城市
     */
    private String city;

    /**
     * 省份
     */
    private String province;

    /**
     * 区县
     */
    private String district;

    /**
     * 景区等级
     */
    private String scenicLevel;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 收费类型：0-免费，1-收费
     */
    private Integer chargeType;

    /**
     * 建议游玩时长
     */
    private String suggestedDuration;

    /**
     * 最佳游玩月份
     */
    private String bestMonths;

    /**
     * 场景分类
     */
    private String sceneType;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 特色标签
     */
    private List<String> features;

    /**
     * 官方咨询电话
     */
    private String contactPhone;

    /**
     * 官方网站链接
     */
    private String officialWebsite;

    /**
     * 避坑提示列表
     */
    private List<String> tips;

    /**
     * 季节性状态：0-暂停开放，1-正常开放
     */
    private Integer seasonalStatus;

    /**
     * 季节性说明
     */
    private String seasonalNote;

    /**
     * 是否已收藏
     */
    private Boolean isFavorited;

    /**
     * 是否已去过
     */
    private Boolean isVisited;

    /**
     * 用户评分信息
     */
    private AttractionRatingVO userRating;

    /**
     * 评分统计
     */
    private RatingStatsVO ratingStats;

    /**
     * 相似景点列表
     */
    private List<AttractionVO> similarAttractions;

    /**
     * 相关攻略列表
     */
    private List<StrategyVO> relatedStrategies;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
