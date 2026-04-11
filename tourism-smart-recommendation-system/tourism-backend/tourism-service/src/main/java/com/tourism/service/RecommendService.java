package com.tourism.service;

import com.tourism.model.dto.RecommendRequestDTO;
import com.tourism.model.dto.RoutePlanAdjustDTO;
import com.tourism.model.dto.RoutePlanRequestDTO;
import com.tourism.model.entity.RoutePlan;
import com.tourism.model.vo.AttractionVO;
import com.tourism.model.vo.RecommendVO;
import com.tourism.model.vo.RoutePlanVO;

import java.util.List;

/**
 * 推荐服务接口
 *
 * @author 韩东升
 */
public interface RecommendService {

    /**
     * 获取个性化推荐
     */
    RecommendVO getPersonalizedRecommend(Long userId, RecommendRequestDTO request);

    /**
     * 生成路线规划 - 支持AI生成
     */
    RoutePlanVO generateRoutePlan(Long userId, RoutePlanRequestDTO request);

    /**
     * 保存路线规划
     */
    Long saveRoutePlan(Long userId, RoutePlanVO routePlanVO);

    /**
     * 获取用户的路线规划列表
     */
    List<RoutePlan> getUserRoutePlans(Long userId);

    /**
     * 根据ID获取路线规划（带用户权限验证）
     */
    RoutePlan getRoutePlanById(Long id, Long userId);

    /**
     * 删除路线规划（带用户权限验证）
     */
    boolean deleteRoutePlan(Long id, Long userId);

    /**
     * 根据偏好推荐景点
     */
    List<AttractionVO> recommendByPreferences(Long userId, String city, List<String> interests, Integer limit);

    /**
     * 动态调整行程
     *
     * @param userId 用户ID
     * @param adjustDTO 调整请求
     * @return 调整后的行程
     */
    RoutePlanVO adjustRoutePlan(Long userId, RoutePlanAdjustDTO adjustDTO);

    /**
     * 基于用户反馈优化后续行程
     *
     * @param userId 用户ID
     * @param planId 行程ID
     * @param feedback 用户反馈
     * @return 优化后的行程
     */
    RoutePlanVO optimizeRoutePlan(Long userId, Long planId, String feedback);

    /**
     * 确认行程
     *
     * @param userId 用户ID
     * @param planId 行程ID
     * @return 是否成功
     */
    boolean confirmRoutePlan(Long userId, Long planId);

    /**
     * 更新行程状态
     *
     * @param userId 用户ID
     * @param planId 行程ID
     * @param status 状态：0-草稿，1-已确认，2-进行中，3-已完成
     * @return 是否成功
     */
    boolean updateRoutePlanStatus(Long userId, Long planId, Integer status);
}
