package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行为记录实体
 * 用于协同过滤推荐算法
 *
 */
@Data
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 项目类型：attraction/strategy
     */
    private String itemType;

    /**
     * 项目ID
     */
    private Long itemId;

    /**
     * 行为类型：view/click/favorite/share/rating
     */
    private String behaviorType;

    /**
     * 评分（如有）
     */
    private BigDecimal rating;

    /**
     * 行为权重（用于算法计算）
     */
    private Double weight;

    /**
     * 停留时长（秒）
     */
    private Integer duration;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
