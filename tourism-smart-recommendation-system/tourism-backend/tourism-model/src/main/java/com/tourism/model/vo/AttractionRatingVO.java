package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点评分VO
 *
 * @author 韩东升
 */
@Data
public class AttractionRatingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分ID
     */
    private Long id;

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 景色评分（1.0-5.0）
     */
    private BigDecimal sceneryScore;

    /**
     * 趣味性评分（1.0-5.0）
     */
    private BigDecimal funScore;

    /**
     * 性价比评分（1.0-5.0）
     */
    private BigDecimal valueScore;

    /**
     * 综合评分
     */
    private BigDecimal overallScore;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评分时间
     */
    private LocalDateTime createTime;
}
