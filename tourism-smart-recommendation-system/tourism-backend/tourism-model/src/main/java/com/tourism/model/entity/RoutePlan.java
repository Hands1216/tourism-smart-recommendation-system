package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 路线规划实体
 *
 */
@Data
@TableName("route_plan")
public class RoutePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路线ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 路线标题
     */
    private String title;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 预算
     */
    private BigDecimal budget;

    /**
     * 详细行程数据（JSON格式）
     */
    private String planData;

    /**
     * 是否AI生成
     */
    private Integer isAiGenerated;

    /**
     * 版本号（用于动态调整）
     */
    private Integer version;

    /**
     * 调整历史（JSON格式，记录每次调整）
     */
    private String adjustmentHistory;

    /**
     * 行程状态：0-草稿，1-已确认，2-进行中，3-已完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
