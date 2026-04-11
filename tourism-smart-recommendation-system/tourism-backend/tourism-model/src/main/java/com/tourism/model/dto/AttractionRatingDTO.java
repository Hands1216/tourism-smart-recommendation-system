package com.tourism.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点评分DTO
 *
 * @author 韩东升
 */
@Data
public class AttractionRatingDTO {

    /**
     * 景点ID
     */
    private Long attractionId;

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
     * 评价内容
     */
    private String comment;
}
