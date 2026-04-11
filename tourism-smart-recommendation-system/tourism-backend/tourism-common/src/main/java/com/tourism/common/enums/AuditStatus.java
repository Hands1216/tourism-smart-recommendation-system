package com.tourism.common.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author 韩东升
 */
@Getter
public enum AuditStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回");

    private final Integer code;
    private final String desc;

    AuditStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AuditStatus getByCode(Integer code) {
        for (AuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
