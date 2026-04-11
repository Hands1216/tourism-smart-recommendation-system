package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 评分统计VO
 *
 * @author 韩东升
 */
@Data
public class RatingStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总评分人数
     */
    private Integer totalCount;

    /**
     * 平均景色评分
     */
    private BigDecimal avgSceneryScore;

    /**
     * 平均趣味性评分
     */
    private BigDecimal avgFunScore;

    /**
     * 平均性价比评分
     */
    private BigDecimal avgValueScore;

    /**
     * 平均综合评分
     */
    private BigDecimal avgOverallScore;

    /**
     * 5星评分人数
     */
    private Integer fiveStarCount;

    /**
     * 4星评分人数
     */
    private Integer fourStarCount;

    /**
     * 3星评分人数
     */
    private Integer threeStarCount;

    /**
     * 2星评分人数
     */
    private Integer twoStarCount;

    /**
     * 1星评分人数
     */
    private Integer oneStarCount;
}
