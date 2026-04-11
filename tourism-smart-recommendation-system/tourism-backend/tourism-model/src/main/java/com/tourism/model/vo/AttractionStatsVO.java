package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 景点统计VO（用于后台数据分析）
 *
 * @author 韩东升
 */
@Data
public class AttractionStatsVO implements Serializable {

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
     * 城市
     */
    private String city;

    /**
     * 景区等级
     */
    private String scenicLevel;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 增长率（百分比）
     */
    private BigDecimal growthRate;

    /**
     * 热度分数（综合计算）
     */
    private BigDecimal hotScore;
}
