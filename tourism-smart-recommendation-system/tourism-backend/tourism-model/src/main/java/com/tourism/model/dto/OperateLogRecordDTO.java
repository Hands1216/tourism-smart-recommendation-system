package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志记录DTO
 *
 */
@Data
public class OperateLogRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型：login-登录，create-新增，update-修改，delete-删除，audit-审核
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 执行状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;
}
