package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景点攻略关联实体
 *
 * @author 韩东升
 */
@Data
@TableName("attraction_strategy")
public class AttractionStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 攻略ID
     */
    private Long strategyId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
