package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 推荐结果VO
 *
 * @author 韩东升
 */
@Data
public class RecommendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐景点列表
     */
    private List<AttractionVO> attractions;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 匹配度
     */
    private Double matchScore;
}
