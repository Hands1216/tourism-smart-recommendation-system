package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 攻略创建/更新DTO
 *
 */
@Data
public class StrategyCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 攻略标题
     */
    private String title;

    /**
     * 封面图URL
     */
    private String coverImage;

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
     * 攻略内容（富文本HTML）
     */
    private String content;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 状态：0-草稿，1-发布
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
}
