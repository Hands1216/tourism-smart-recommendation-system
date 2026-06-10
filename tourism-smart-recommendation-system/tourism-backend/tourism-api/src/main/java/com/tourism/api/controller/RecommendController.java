package com.tourism.api.controller;

import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.RecommendRequestDTO;
import com.tourism.model.dto.RoutePlanAdjustDTO;
import com.tourism.model.dto.RoutePlanRequestDTO;
import com.tourism.model.entity.RoutePlan;
import com.tourism.model.vo.AttractionVO;
import com.tourism.model.vo.RecommendVO;
import com.tourism.model.vo.RoutePlanVO;
import com.tourism.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 推荐控制器
 *
 */
@RestController
@RequestMapping("/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 获取个性化推荐
     */
    @PostMapping("/personalized")
    public Result<RecommendVO> getPersonalized(@RequestBody RecommendRequestDTO request) {
        Long userId = UserContext.getUserId();
        RecommendVO result = recommendService.getPersonalizedRecommend(userId, request);
        return Result.success(result);
    }

    /**
     * 根据偏好推荐景点
     */
    @GetMapping("/attractions")
    public Result<List<AttractionVO>> recommendByPreferences(
            @RequestParam String city,
            @RequestParam(required = false) String interests) {
        Long userId = UserContext.getUserId();
        List<String> interestList = interests != null ? List.of(interests.split(",")) : List.of();
        List<AttractionVO> result = recommendService.recommendByPreferences(userId, city, interestList, 10);
        return Result.success(result);
    }

    /**
     * 生成路线规划
     */
    @PostMapping("/route-plan")
    public Result<RoutePlanVO> generateRoutePlan(@RequestBody RoutePlanRequestDTO request) {
        Long userId = UserContext.getUserId();
        RoutePlanVO result = recommendService.generateRoutePlan(userId, request);
        return Result.success(result);
    }

    /**
     * 保存路线规划
     */
    @PostMapping("/route-plan/save")
    public Result<Long> saveRoutePlan(@RequestBody RoutePlanVO routePlanVO) {
        Long userId = UserContext.getUserId();
        Long id = recommendService.saveRoutePlan(userId, routePlanVO);
        return Result.success(id);
    }

    /**
     * 获取用户的路线规划列表
     */
    @GetMapping("/route-plan/list")
    public Result<List<RoutePlan>> getRoutePlans() {
        Long userId = UserContext.getUserId();
        List<RoutePlan> result = recommendService.getUserRoutePlans(userId);
        return Result.success(result);
    }

    /**
     * 获取路线规划详情
     */
    @GetMapping("/route-plan/{id}")
    public Result<RoutePlan> getRoutePlan(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        RoutePlan result = recommendService.getRoutePlanById(id, userId);
        return Result.success(result);
    }

    /**
     * 删除路线规划
     */
    @DeleteMapping("/route-plan/{id}")
    public Result<Boolean> deleteRoutePlan(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        boolean result = recommendService.deleteRoutePlan(id, userId);
        return Result.success(result);
    }

    /**
     * 动态调整行程
     * 支持添加、删除、修改、重排序活动
     */
    @PostMapping("/route-plan/adjust")
    public Result<RoutePlanVO> adjustRoutePlan(@RequestBody RoutePlanAdjustDTO adjustDTO) {
        Long userId = UserContext.getUserId();
        RoutePlanVO result = recommendService.adjustRoutePlan(userId, adjustDTO);
        return Result.success(result);
    }

    /**
     * AI优化行程
     * 根据用户反馈智能优化行程安排
     */
    @PostMapping("/route-plan/{id}/optimize")
    public Result<RoutePlanVO> optimizeRoutePlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        String feedback = request.getOrDefault("feedback", "");
        RoutePlanVO result = recommendService.optimizeRoutePlan(userId, id, feedback);
        return Result.success(result);
    }

    /**
     * 确认行程
     * 将行程状态从草稿改为已确认
     */
    @PutMapping("/route-plan/{id}/confirm")
    public Result<Boolean> confirmRoutePlan(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        boolean result = recommendService.confirmRoutePlan(userId, id);
        return Result.success(result);
    }

    /**
     * 更新行程状态
     */
    @PutMapping("/route-plan/{id}/status")
    public Result<Boolean> updateRoutePlanStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        Long userId = UserContext.getUserId();
        boolean result = recommendService.updateRoutePlanStatus(userId, id, status);
        return Result.success(result);
    }
}
