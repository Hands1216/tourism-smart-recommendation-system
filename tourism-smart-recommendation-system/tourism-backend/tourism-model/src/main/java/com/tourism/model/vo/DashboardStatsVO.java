package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 仪表盘统计VO
 *
 * @author 韩东升
 */
@Data
public class DashboardStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户总数（包括所有角色）
     */
    private Long userCount;

    /**
     * 景点总数
     */
    private Long attractionCount;

    /**
     * 攻略总数
     */
    private Long strategyCount;

    /**
     * 待审核攻略数
     */
    private Long pendingAuditCount;
}
