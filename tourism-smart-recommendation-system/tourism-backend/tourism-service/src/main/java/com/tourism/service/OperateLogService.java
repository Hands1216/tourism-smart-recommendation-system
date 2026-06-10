package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.model.entity.OperateLog;
import com.tourism.model.vo.OperateLogVO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 *
 */
public interface OperateLogService extends IService<OperateLog> {

    /**
     * 分页查询操作日志
     */
    Page<OperateLogVO> getOperateLogPage(Page<?> page, String username, String operationType, String module, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录操作日志
     */
    void recordOperateLog(Long userId, String username, String userRole, String operationType, String module, String description, String requestMethod, String requestParams, String ipAddress, Integer status, Long executeTime);

    /**
     * 获取最近N天的操作日志数量
     */
    Long countByDays(LocalDateTime startTime, Integer days);
}
