package com.tourism.model.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户足迹DTO
 *
 * @author 韩东升
 */
@Data
public class FootprintDTO {

    /**
     * 景点ID
     */
    private Long attractionId;

    /**
     * 游览日期
     */
    private LocalDate visitDate;

    /**
     * 备注
     */
    private String note;
}
