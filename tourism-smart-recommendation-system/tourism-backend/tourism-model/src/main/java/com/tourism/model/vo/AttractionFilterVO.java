package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 景点筛选选项VO
 *
 */
@Data
public class AttractionFilterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 省份列表
     */
    private List<String> provinces;

    /**
     * 景区等级列表
     */
    private List<String> scenicLevels;

    /**
     * 场景分类列表
     */
    private List<String> sceneTypes;

    /**
     * 建议游玩时长列表
     */
    private List<String> durations;

    /**
     * 省份-城市映射（key: 省份, value: 城市列表）
     */
    private Map<String, List<String>> provinceCityMap;

    /**
     * 城市-区县映射（key: 城市, value: 区县列表）
     */
    private Map<String, List<String>> cityDistrictMap;
}
