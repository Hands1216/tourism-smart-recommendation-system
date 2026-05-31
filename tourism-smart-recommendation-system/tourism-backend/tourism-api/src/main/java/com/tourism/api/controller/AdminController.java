package com.tourism.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.AttractionAdminDTO;
import com.tourism.model.dto.AttractionQueryDTO;
import com.tourism.model.dto.AuditDTO;
import com.tourism.model.dto.OperateLogRecordDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.Strategy;
import com.tourism.model.entity.User;
import com.tourism.model.vo.AttractionStatsVO;
import com.tourism.model.vo.AttractionVO;
import com.tourism.model.vo.DashboardStatsVO;
import com.tourism.model.vo.StrategyVO;
import com.tourism.service.AttractionService;
import com.tourism.service.StrategyService;
import com.tourism.service.UserService;
import com.tourism.service.OperateLogService;
import com.tourism.model.vo.OperateLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台控制器
 *
 * @author 韩东升
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StrategyService strategyService;
    private final UserService userService;
    private final OperateLogService operateLogService;
    private final AttractionService attractionService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 统计用户总数（包括所有角色，排除逻辑删除）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        stats.setUserCount(userService.count(userWrapper));

        // 统计景点总数（排除逻辑删除）
        LambdaQueryWrapper<Attraction> attractionWrapper = new LambdaQueryWrapper<>();
        attractionWrapper.eq(Attraction::getDeleted, 0);
        stats.setAttractionCount(attractionService.count(attractionWrapper));

        // 统计攻略总数（排除逻辑删除）
        LambdaQueryWrapper<Strategy> strategyWrapper = new LambdaQueryWrapper<>();
        strategyWrapper.eq(Strategy::getDeleted, 0);
        stats.setStrategyCount(strategyService.count(strategyWrapper));

        // 统计待审核攻略数
        LambdaQueryWrapper<Strategy> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Strategy::getAuditStatus, 0)
                     .eq(Strategy::getDeleted, 0);
        stats.setPendingAuditCount(strategyService.count(pendingWrapper));

        return Result.success(stats);
    }

    /**
     * 获取热门景点列表（按浏览量排序）- 仪表盘使用
     */
    @GetMapping("/dashboard/hot-attractions")
    public Result<List<AttractionStatsVO>> getDashboardHotAttractions(
            @RequestParam(defaultValue = "15") Integer limit,
            @RequestParam(defaultValue = "30") Integer days) {
        List<AttractionStatsVO> stats = attractionService.getHotAttractionStats(limit, days);
        return Result.success(stats);
    }

    /**
     * 获取热门攻略列表（按浏览量排序，替换最新攻略）
     */
    @GetMapping("/dashboard/hot-strategies")
    public Result<List<StrategyVO>> getHotStrategies(
            @RequestParam(defaultValue = "15") Integer limit) {
        // 查询浏览量最高的攻略
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getDeleted, 0)
               .eq(Strategy::getAuditStatus, 1)
               .eq(Strategy::getStatus, 1)
               .orderByDesc(Strategy::getViewCount)
               .last("LIMIT " + limit);

        List<Strategy> strategies = strategyService.list(wrapper);
        return Result.success(convertToStrategyVOList(strategies));
    }

    /**
     * 转换Strategy列表为StrategyVO列表（简化版）
     */
    private List<StrategyVO> convertToStrategyVOList(List<Strategy> strategies) {
        return strategies.stream().map(strategy -> {
            StrategyVO vo = new StrategyVO();
            vo.setId(strategy.getId());
            vo.setTitle(strategy.getTitle());
            vo.setDestination(strategy.getDestination());
            vo.setViewCount(strategy.getViewCount());
            // 设置作者名称
            if (strategy.getUserId() != null) {
                User author = userService.getById(strategy.getUserId());
                if (author != null) {
                    vo.setAuthorName(author.getNickname());
                }
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    // ==================== 攻略管理接口 ====================

    /**
     * 管理后台攻略列表（所有状态）
     */
    @GetMapping("/strategies")
    public Result<IPage<StrategyVO>> getStrategyList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<StrategyVO> pageParam = new Page<>(page, size);
        IPage<StrategyVO> result = strategyService.getAdminStrategyPage(pageParam, title, auditStatus, destination);
        return Result.success(result);
    }

    /**
     * 审核攻略
     */
    @PutMapping("/strategy/{id}/audit")
    public Result<Void> auditStrategy(@PathVariable Long id, @RequestBody AuditDTO dto) {
        // 获取当前登录用户
        User currentUser = userService.getById(UserContext.getUserId());
        // 获取攻略信息用于日志记录
        Strategy strategy = strategyService.getById(id);
        if (strategy != null && currentUser != null) {
            String statusText = dto.getAuditStatus() == 1 ? "通过" : "驳回";
            String reason = dto.getAuditReason();
            String desc = reason != null && !reason.isEmpty()
                    ? String.format("审核攻略[%d]: %s, 原因: %s", id, statusText, reason)
                    : String.format("审核攻略[%d]: %s", id, statusText);

            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "audit", "攻略管理", desc, "PUT", String.format("/admin/strategy/%d/audit", id), null, 1, null);
        }

        strategyService.auditStrategy(id, dto.getAuditStatus(), dto.getAuditReason());
        return Result.success();
    }

    /**
     * 修改攻略上下架状态
     */
    @PutMapping("/strategy/{id}/status")
    public Result<Void> updateStrategyStatus(@PathVariable Long id, @RequestParam Integer status) {
        strategyService.updateStrategyStatus(id, status);
        return Result.success();
    }

    /**
     * 删除攻略（管理员）
     */
    @DeleteMapping("/strategy/{id}")
    public Result<Void> deleteStrategy(@PathVariable Long id) {
        // 获取当前登录用户
        User currentUser = userService.getById(UserContext.getUserId());
        // 获取攻略信息用于日志记录
        Strategy strategy = strategyService.getById(id);
        if (strategy != null && currentUser != null) {
            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "delete", "攻略管理", String.format("删除攻略[%d]: %s", id, strategy.getTitle()),
                    "DELETE", String.format("/admin/strategy/%d", id), null, 1, null);
        }

        strategyService.removeById(id);
        return Result.success();
    }

    /**
     * 设置攻略加精状态
     */
    @PutMapping("/strategy/{id}/featured")
    public Result<Void> setStrategyFeatured(@PathVariable Long id, @RequestParam Integer featured) {
        User currentUser = userService.getById(UserContext.getUserId());
        Strategy strategy = strategyService.getById(id);

        if (strategy != null) {
            strategy.setFeatured(featured);
            strategyService.updateById(strategy);

            if (currentUser != null) {
                String action = featured == 1 ? "加精" : "取消加精";
                operateLogService.recordOperateLog(currentUser.getId(),
                        currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(),
                        currentUser.getRole(), "update", "攻略管理",
                        String.format("%s攻略[%d]: %s", action, id, strategy.getTitle()),
                        "PUT", String.format("/admin/strategy/%d/featured", id), null, 1, null);
            }
        }

        return Result.success();
    }

    /**
     * 设置攻略置顶状态
     */
    @PutMapping("/strategy/{id}/pinned")
    public Result<Void> setStrategyPinned(@PathVariable Long id, @RequestParam Integer pinned) {
        User currentUser = userService.getById(UserContext.getUserId());
        Strategy strategy = strategyService.getById(id);

        if (strategy != null) {
            strategy.setPinned(pinned);
            strategy.setPinnedTime(pinned == 1 ? LocalDateTime.now() : null);
            strategyService.updateById(strategy);

            if (currentUser != null) {
                String action = pinned == 1 ? "置顶" : "取消置顶";
                operateLogService.recordOperateLog(currentUser.getId(),
                        currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(),
                        currentUser.getRole(), "update", "攻略管理",
                        String.format("%s攻略[%d]: %s", action, id, strategy.getTitle()),
                        "PUT", String.format("/admin/strategy/%d/pinned", id), null, 1, null);
            }
        }

        return Result.success();
    }

    /**
     * 获取加精攻略列表
     */
    @GetMapping("/strategies/featured")
    public Result<List<StrategyVO>> getFeaturedStrategies(
            @RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getDeleted, 0)
               .eq(Strategy::getAuditStatus, 1)
               .eq(Strategy::getStatus, 1)
               .eq(Strategy::getFeatured, 1)
               .orderByDesc(Strategy::getCreateTime)
               .last("LIMIT " + limit);

        List<Strategy> strategies = strategyService.list(wrapper);
        return Result.success(convertToStrategyVOList(strategies));
    }

    /**
     * 获取置顶攻略列表
     */
    @GetMapping("/strategies/pinned")
    public Result<List<StrategyVO>> getPinnedStrategies() {
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getDeleted, 0)
               .eq(Strategy::getAuditStatus, 1)
               .eq(Strategy::getStatus, 1)
               .eq(Strategy::getPinned, 1)
               .orderByDesc(Strategy::getPinnedTime);

        List<Strategy> strategies = strategyService.list(wrapper);
        return Result.success(convertToStrategyVOList(strategies));
    }

    // ==================== 用户管理接口 ====================

    /**
     * 获取用户列表（按ID排序）
     */
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 排除逻辑删除的用户
        wrapper.eq(User::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }

        // 按ID升序排列
        wrapper.orderByAsc(User::getId);

        IPage<User> result = userService.page(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userService.getById(id);
        if (user != null) {
            user.setStatus(status);
            userService.updateById(user);
        }
        return Result.success();
    }

    /**
     * 修改用户角色（日志由前端统一记录，避免重复）
     */
    @PutMapping("/user/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userService.getById(id);
        if (user != null) {
            user.setRole(role);
            userService.updateById(user);
        }
        return Result.success();
    }

    // ==================== 景点管理接口 ====================

    /**
     * 景点列表（管理后台，按ID排序）
     */
    @GetMapping("/attractions")
    public Result<IPage<AttractionVO>> getAttractionList(AttractionQueryDTO queryDTO) {
        // 设置默认排序为ID升序
        if (queryDTO.getSortBy() == null || queryDTO.getSortBy().isEmpty()) {
            queryDTO.setSortBy("id");
            queryDTO.setSortOrder("asc");
        }

        IPage<AttractionVO> page = attractionService.getAttractionPageAdvanced(queryDTO, null);
        return Result.success(page);
    }

    /**
     * 创建景点
     */
    @PostMapping("/attraction")
    public Result<Long> createAttraction(@RequestBody AttractionAdminDTO dto) {
        // 获取当前登录用户
        User currentUser = userService.getById(UserContext.getUserId());

        Long id = attractionService.createAttraction(dto);

        // 记录日志
        if (currentUser != null) {
            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "create", "景点管理", String.format("新增景点[%d]: %s", id, dto.getName()),
                    "POST", "/admin/attraction", null, 1, null);
        }

        return Result.success(id);
    }

    /**
     * 更新景点
     */
    @PutMapping("/attraction/{id}")
    public Result<Void> updateAttraction(@PathVariable Long id, @RequestBody AttractionAdminDTO dto) {
        // 获取当前登录用户和景点信息
        User currentUser = userService.getById(UserContext.getUserId());
        Attraction attraction = attractionService.getById(id);

        dto.setId(id);
        attractionService.updateAttraction(dto);

        // 记录日志
        if (attraction != null && currentUser != null) {
            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "update", "景点管理", String.format("修改景点[%d]: %s", id, attraction.getName()),
                    "PUT", String.format("/admin/attraction/%d", id), null, 1, null);
        }

        return Result.success();
    }

    /**
     * 删除景点（逻辑删除）
     */
    @DeleteMapping("/attraction/{id}")
    public Result<Void> deleteAttraction(@PathVariable Long id) {
        // 获取当前登录用户和景点信息
        User currentUser = userService.getById(UserContext.getUserId());
        Attraction attraction = attractionService.getById(id);

        if (attraction != null && currentUser != null) {
            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "delete", "景点管理", String.format("删除景点[%d]: %s", id, attraction.getName()),
                    "DELETE", String.format("/admin/attraction/%d", id), null, 1, null);
        }

        attractionService.removeById(id);
        return Result.success();
    }

    /**
     * 设置季节性状态
     */
    @PutMapping("/attraction/{id}/seasonal")
    public Result<Void> setSeasonalStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String note) {

        // 获取当前登录用户
        User currentUser = userService.getById(UserContext.getUserId());
        Attraction attraction = attractionService.getById(id);

        // 记录旧状态用于日志
        String oldStatusText = attraction != null ? getSeasonalStatusText(attraction.getSeasonalStatus()) : "未知";

        attractionService.setSeasonalStatus(id, status, note);

        // 记录日志
        if (attraction != null && currentUser != null) {
            String newStatusText = getSeasonalStatusText(status);
            operateLogService.recordOperateLog(currentUser.getId(), currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(), currentUser.getRole(),
                    "update", "景点管理", String.format("修改景点[%d]季节性状态: %s->%s", id, oldStatusText, newStatusText),
                    "PUT", String.format("/admin/attraction/%d/seasonal", id), null, 1, null);
        }

        return Result.success();
    }

    /**
     * 季节性状态码转文字
     */
    private String getSeasonalStatusText(Integer status) {
        if (status == null) return "正常开放";
        return switch (status) {
            case 0 -> "正常开放";
            case 1 -> "季节性关闭";
            case 2 -> "临时关闭";
            default -> "未知";
        };
    }

    // ==================== 操作日志接口 ====================

    /**
     * 前端记录操作日志
     */
    @PostMapping("/logs/record")
    public Result<Void> recordLog(@RequestBody OperateLogRecordDTO dto) {
        // 使用当前登录用户信息，防止前端伪造
        Long userId = UserContext.getUserId();
        User currentUser = userId != null ? userService.getById(userId) : null;
        if (currentUser == null) {
            return Result.success();
        }

        operateLogService.recordOperateLog(
                currentUser.getId(),
                currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getPhone(),
                currentUser.getRole(),
                dto.getOperationType(),
                dto.getModule(),
                dto.getDescription(),
                dto.getRequestMethod(),
                dto.getRequestParams(),
                dto.getIpAddress(),
                dto.getStatus(),
                dto.getExecuteTime()
        );
        return Result.success();
    }

    /**
     * 获取操作日志列表
     */
    @GetMapping("/logs")
    public Result<Page<OperateLogVO>> getOperateLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<?> pageParam = new Page<>(page, size);

        // 解析时间参数
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startTime != null && !startTime.isEmpty()) {
            try {
                start = LocalDateTime.parse(startTime.replace(" ", "T"));
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        if (endTime != null && !endTime.isEmpty()) {
            try {
                end = LocalDateTime.parse(endTime.replace(" ", "T"));
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        Page<OperateLogVO> result = operateLogService.getOperateLogPage(pageParam, username, operationType, null, start, end);
        return Result.success(result);
    }

    // ==================== 数据分析接口 ====================

    /**
     * 数据分析 - 热门景点
     */
    @GetMapping("/analytics/hot-attractions")
    public Result<List<AttractionStatsVO>> getHotAttractions(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "30") Integer days) {
        List<AttractionStatsVO> list = attractionService.getHotAttractionStats(limit, days);
        return Result.success(list);
    }

    /**
     * 数据分析 - 高增长潜力景点
     */
    @GetMapping("/analytics/growth-attractions")
    public Result<List<AttractionStatsVO>> getGrowthAttractions(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "30") Integer days) {
        List<AttractionStatsVO> list = attractionService.getGrowthPotentialAttractions(limit, days);
        return Result.success(list);
    }
}
