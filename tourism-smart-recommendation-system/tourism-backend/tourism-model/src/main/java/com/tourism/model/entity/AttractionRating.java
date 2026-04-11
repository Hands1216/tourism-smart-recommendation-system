package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点评分实体
 *
 * @author 韩东升
 */
@Data
@TableName("attraction_rating")
public class AttractionRating implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分ID
     */
    @TableId(type = IdType.AUTO)
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
     * 综合评分（自动计算平均值）
     */
    private BigDecimal overallScore;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评分时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
