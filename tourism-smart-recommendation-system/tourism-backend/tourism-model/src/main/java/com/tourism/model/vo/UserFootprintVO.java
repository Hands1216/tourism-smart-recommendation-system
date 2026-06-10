package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户足迹VO
 *
 */
@Data
public class UserFootprintVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 足迹ID
     */
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
     * 景点名称
     */
    private String attractionName;

    /**
     * 景点图片
     */
    private String attractionImage;

    /**
     * 城市
     */
    private String city;

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
    private LocalDateTime createTime;
}
