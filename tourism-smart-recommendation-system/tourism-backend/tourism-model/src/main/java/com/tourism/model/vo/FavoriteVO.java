package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏信息VO
 *
 */
@Data
public class FavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 收藏类型：attraction/strategy
     */
    private String itemType;

    /**
     * 收藏项目ID
     */
    private Long itemId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;

    /**
     * 景点信息（当itemType为attraction时）
     */
    private AttractionVO attraction;

    /**
     * 攻略信息（当itemType为strategy时）
     */
    private StrategyVO strategy;
}
