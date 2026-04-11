package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游攻略实体
 *
 * @author 韩东升
 */
@Data
@TableName("strategy")
public class Strategy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 攻略ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 攻略标题
     */
    private String title;

    /**
     * 攻略封面图
     */
    private String coverImage;

    /**
     * 攻略内容（富文本）
     */
    private String content;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 行程天数
     */
    private Integer days;

    /**
     * 人均预算
     */
    private BigDecimal budget;

    /**
     * 适合季节：spring/summer/autumn/winter/all
     */
    private String season;

    /**
     * 图片列表（JSON）
     */
    private String images;

    /**
     * 标签（JSON）
     */
    private String tags;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否AI生成
     */
    private Integer isAiGenerated;

    /**
     * 审核状态：0-待审核，1-已通过，2-已驳回
     */
    private Integer auditStatus;

    /**
     * 驳回原因
     */
    @TableField("audit_reason")
    private String auditReason;

    /**
     * 攻略链路ID（同一攻略的不同版本共用）
     */
    private Long rootStrategyId;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 可见性：0-私密，1-公开
     */
    private Integer visibility;

    /**
     * 是否加精：0-否，1-是
     */
    private Integer featured;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer pinned;

    /**
     * 置顶时间（用于排序）
     */
    private LocalDateTime pinnedTime;

    /**
     * 攻略摘要
     */
    private String summary;

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
