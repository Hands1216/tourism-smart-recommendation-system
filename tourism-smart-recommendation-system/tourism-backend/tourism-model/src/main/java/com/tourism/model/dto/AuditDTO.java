package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 审核DTO
 *
 * @author 韩东升
 */
@Data
public class AuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer auditStatus;
    private String auditReason;
}
