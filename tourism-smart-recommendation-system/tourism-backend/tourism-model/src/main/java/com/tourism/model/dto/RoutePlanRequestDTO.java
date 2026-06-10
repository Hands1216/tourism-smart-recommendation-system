package com.tourism.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 路线规划请求DTO
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出发省份
     */
    private String departureProvince;

    /**
     * 出发城市
     */
    private String departureCity;

    /**
     * 目的地列表
     */
    private List<String> destinations;

    /**
     * 出行天数
     */
    private Integer days;

    /**
     * 出行月份（可选）
     */
    private Integer month;

    /**
     * 出发日期 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 返回日期 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 同行伙伴
     */
    private String companion;

    /**
     * 风格偏好
     */
    private List<String> stylePreferences;

    /**
     * 行程节奏
     */
    private String pace;

    /**
     * 备注说明
     */
    private String remark;
}
