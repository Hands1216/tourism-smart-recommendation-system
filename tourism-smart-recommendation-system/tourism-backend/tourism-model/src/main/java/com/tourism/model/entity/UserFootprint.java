package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户足迹实体（去过标记）
 *
 */
@Data
@TableName("user_footprint")
public class UserFootprint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 足迹ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 游览日期
     */
    private LocalDate visitDate;

    /**
     * 备注
     */
    private String note;

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
}
