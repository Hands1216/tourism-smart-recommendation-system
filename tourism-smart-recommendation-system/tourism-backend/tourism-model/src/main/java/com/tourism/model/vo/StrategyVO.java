package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游攻略VO
 *
 * @author 韩东升
 */
@Data
public class StrategyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 攻略ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 作者昵称
     */
    private String authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;

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
     * 适合季节
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
     * 是否已点赞
     */
    private Boolean isLiked;

    /**
     * 是否已收藏
     */
    private Boolean isFavorited;

    /**
     * 是否AI生成
     */
    private Boolean isAiGenerated;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核原因
     */
    private String auditReason;

    /**
     * 攻略链路ID
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
     * 攻略摘要
     */
    private String summary;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
