package com.tourism.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.model.entity.OperateLog;
import com.tourism.model.vo.OperateLogVO;
import com.tourism.service.OperateLogService;
import com.tourism.service.mapper.OperateLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 */
@Slf4j
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogService {

    @Override
    public Page<OperateLogVO> getOperateLogPage(Page<?> page, String username, String operationType, String module, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();

        // 按用户名筛选
        if (StrUtil.isNotEmpty(username)) {
            wrapper.and(w -> w.like(OperateLog::getUsername, username)
                    .or().like(OperateLog::getIpAddress, username));
        }
        // 按操作类型筛选
        if (StrUtil.isNotEmpty(operationType)) {
            wrapper.eq(OperateLog::getOperationType, operationType);
        }
        // 按模块筛选
        if (StrUtil.isNotEmpty(module)) {
            wrapper.and(w -> w.like(OperateLog::getModule, module)
                    .or().like(OperateLog::getDescription, module));
        }
        // 按时间范围筛选
        if (startTime != null) {
            wrapper.ge(OperateLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperateLog::getCreateTime, endTime);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(OperateLog::getCreateTime);

        Page<OperateLog> entityPage = page(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        Page<OperateLogVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(OperateLogVO::fromEntity).toList());
        return voPage;
    }

    @Override
    public void recordOperateLog(Long userId, String username, String userRole, String operationType, String module, String description, String requestMethod, String requestParams, String ipAddress, Integer status, Long executeTime) {
        OperateLog operateLog = new OperateLog();
        operateLog.setUserId(userId);
        operateLog.setUsername(username);
        operateLog.setUserRole(userRole);
        operateLog.setOperationType(operationType);
        operateLog.setModule(module);
        operateLog.setDescription(description);
        operateLog.setRequestMethod(requestMethod);
        operateLog.setRequestParams(requestParams);
        operateLog.setIpAddress(ipAddress);
        operateLog.setStatus(status != null ? status : 0);
        operateLog.setExecuteTime(executeTime);
        operateLog.setCreateTime(LocalDateTime.now());

        save(operateLog);
    }

    @Override
    public Long countByDays(LocalDateTime startTime, Integer days) {
        LambdaQueryWrapper<OperateLog> wrapper = new LambdaQueryWrapper<>();

        if (startTime != null) {
            wrapper.ge(OperateLog::getCreateTime, startTime);
        }

        return count(wrapper);
    }
}
