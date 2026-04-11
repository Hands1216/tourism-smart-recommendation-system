package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 推荐请求DTO
 *
 * @author 韩东升
 */
@Data
public class RecommendRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目的地城市
     */
    private String city;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 预算
     */
    private Double budget;

    /**
     * 兴趣标签
     */
    private List<String> interests;

    /**
     * 同行人员类型
     */
    private String companions;

    /**
     * 用户偏好描述
     */
    private String preferences;
}
