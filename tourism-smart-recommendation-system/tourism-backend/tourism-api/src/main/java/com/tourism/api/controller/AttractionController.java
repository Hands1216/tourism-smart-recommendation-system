package com.tourism.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.AttractionQueryDTO;
import com.tourism.model.dto.AttractionRatingDTO;
import com.tourism.model.dto.FootprintDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.User;
import com.tourism.model.vo.*;
import com.tourism.service.AttractionService;
import com.tourism.service.UserService;
import com.tourism.service.impl.RecommendAlgorithmService;
import com.tourism.service.mapper.AttractionRatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 景点控制器
 *
 */
@RestController
@RequestMapping("/attraction")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;
    private final UserService userService;
    private final AttractionRatingMapper attractionRatingMapper;

    @Autowired
    private RecommendAlgorithmService recommendAlgorithmService;

    /**
     * 获取景点列表
     */
    @GetMapping("/list")
    public Result<IPage<AttractionVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String province,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        Page<Attraction> pageParam = new Page<>(page, size);
        IPage<AttractionVO> result = attractionService.getAttractionPage(pageParam, keyword, categoryId, province, userId);
        return Result.success(result);
    }

    /**
     * 获取景点详情
     */
    @GetMapping("/{id}")
    public Result<AttractionVO> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        AttractionVO detail = attractionService.getAttractionDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 收藏景点
     */
    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        attractionService.favoriteAttraction(userId, id);
        return Result.success();
    }

    /**
     * 取消收藏景点
     */
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        attractionService.unfavoriteAttraction(userId, id);
        return Result.success();
    }

    /**
     * 获取个性化推荐景点（已登录用户优先使用混合推荐算法，降级到偏好推荐）
     */
    @GetMapping("/recommend")
    public Result<List<AttractionVO>> getRecommend(@RequestParam(defaultValue = "8") Integer limit) {
        Long userId = UserContext.getUserId();

        // 已登录用户：优先使用混合推荐算法
        if (userId != null) {
            List<Long> recommendedIds = recommendAlgorithmService.getHybridRecommendations(userId, limit);
            if (!recommendedIds.isEmpty()) {
                List<Attraction> attractions = attractionService.listByIds(recommendedIds);
                // 按推荐顺序排序
                Map<Long, Integer> orderMap = new HashMap<>();
                for (int i = 0; i < recommendedIds.size(); i++) {
                    orderMap.put(recommendedIds.get(i), i);
                }
                attractions.sort(Comparator.comparingInt(a -> orderMap.getOrDefault(a.getId(), 999)));
                return Result.success(attractions.stream().map(this::convertAttractionToVO).collect(Collectors.toList()));
            }
        }

        // 降级：基于偏好的简单推荐（原有逻辑）
        String preferences;
        if (userId != null) {
            User user = userService.getById(userId);
            preferences = (user != null && user.getPreferences() != null)
                    ? user.getPreferences() : "自然风光,历史文化,美食";
        } else {
            preferences = "自然风光,历史文化,美食";
        }
        List<Attraction> result = attractionService.getRecommendByPreferences(preferences, limit);
        return Result.success(result.stream().map(this::convertAttractionToVO).collect(Collectors.toList()));
    }

    /**
     * 将Attraction实体转换为AttractionVO，解析JSON字符串字段为List
     */
    private AttractionVO convertAttractionToVO(Attraction attraction) {
        AttractionVO vo = new AttractionVO();
        BeanUtil.copyProperties(attraction, vo, "images", "tags", "features", "tips");
        vo.setImages(parseJsonList(attraction.getImages()));
        vo.setTags(parseJsonList(attraction.getTags()));
        vo.setFeatures(parseJsonList(attraction.getFeatures()));
        vo.setTips(parseJsonList(attraction.getTips()));
        RatingStatsVO ratingStats = attractionRatingMapper.selectRatingStats(attraction.getId());
        if (ratingStats != null && ratingStats.getTotalCount() != null && ratingStats.getTotalCount() > 0) {
            vo.setRating(ratingStats.getAvgOverallScore());
            vo.setRatingCount(ratingStats.getTotalCount());
        } else {
            vo.setRating(java.math.BigDecimal.ZERO);
            vo.setRatingCount(0);
        }
        return vo;
    }

    private List<String> parseJsonList(String json) {
        if (json != null && !json.isEmpty() && !"null".equals(json)) {
            try {
                return JSONUtil.toList(json, String.class);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    // ========== 新增接口 ==========

    /**
     * 高级筛选列表
     */
    @PostMapping("/list/advanced")
    public Result<IPage<AttractionVO>> listAdvanced(@RequestBody AttractionQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        IPage<AttractionVO> page = attractionService.getAttractionPageAdvanced(queryDTO, userId);
        return Result.success(page);
    }

    /**
     * 获取景点详情（增强版）
     */
    @GetMapping("/{id}/enhanced")
    public Result<AttractionVO> getDetailEnhanced(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        AttractionVO vo = attractionService.getAttractionDetailEnhanced(id, userId);
        return Result.success(vo);
    }

    /**
     * 获取筛选选项
     */
    @GetMapping("/filter-options")
    public Result<AttractionFilterVO> getFilterOptions() {
        AttractionFilterVO options = attractionService.getFilterOptions();
        return Result.success(options);
    }

    /**
     * 评分景点
     */
    @PostMapping("/{id}/rate")
    public Result<Void> rateAttraction(@PathVariable Long id, @RequestBody AttractionRatingDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        dto.setAttractionId(id);
        attractionService.rateAttraction(userId, dto);
        return Result.success();
    }

    /**
     * 获取用户评分
     */
    @GetMapping("/{id}/my-rating")
    public Result<AttractionRatingVO> getMyRating(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        AttractionRatingVO rating = attractionService.getUserRating(userId, id);
        return Result.success(rating);
    }

    /**
     * 获取景点评论列表（分页，仅有评论内容的）
     */
    @GetMapping("/{id}/ratings")
    public Result<IPage<AttractionRatingVO>> getRatings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        Page<AttractionRatingVO> pageParam = new Page<>(page, size);
        IPage<AttractionRatingVO> ratings = attractionService.getAttractionRatings(id, pageParam);
        return Result.success(ratings);
    }

    /**
     * 删除景点评论
     */
    @DeleteMapping("/{id}/ratings/{ratingId}")
    public Result<Void> deleteRating(@PathVariable Long id, @PathVariable Long ratingId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        attractionService.deleteAttractionRating(userId, id, ratingId);
        return Result.success();
    }

    /**
     * 添加景点评论（纯文字评论，不带评分）
     */
    @PostMapping("/{id}/comment")
    public Result<Void> addComment(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String comment = body.get("comment");
        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        attractionService.addAttractionComment(userId, id, comment.trim());
        return Result.success();
    }

    /**
     * 标记去过
     */
    @PostMapping("/{id}/visited")
    public Result<Void> markVisited(@PathVariable Long id, @RequestBody FootprintDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        attractionService.markVisited(userId, id, dto.getVisitDate(), dto.getNote());
        return Result.success();
    }

    /**
     * 取消去过标记
     */
    @DeleteMapping("/{id}/visited")
    public Result<Void> unmarkVisited(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        attractionService.unmarkVisited(userId, id);
        return Result.success();
    }

    /**
     * 获取相似景点
     */
    @GetMapping("/{id}/similar")
    public Result<List<AttractionVO>> getSimilarAttractions(@PathVariable Long id) {
        List<AttractionVO> list = attractionService.getSimilarAttractions(id, 6);
        return Result.success(list);
    }

    /**
     * 获取相关攻略
     */
    @GetMapping("/{id}/strategies")
    public Result<List<StrategyVO>> getRelatedStrategies(@PathVariable Long id) {
        List<StrategyVO> list = attractionService.getRelatedStrategies(id, 10);
        return Result.success(list);
    }

    /**
     * 获取用户足迹
     */
    @GetMapping("/footprints")
    public Result<List<UserFootprintVO>> getMyFootprints() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        List<UserFootprintVO> list = attractionService.getUserFootprints(userId);
        return Result.success(list);
    }

    /**
     * 根据省份获取城市列表
     */
    @GetMapping("/cities")
    public Result<List<String>> getCitiesByProvince(@RequestParam String province) {
        List<String> cities = attractionService.getCitiesByProvince(province);
        return Result.success(cities);
    }

    /**
     * 根据城市获取区县列表
     */
    @GetMapping("/districts")
    public Result<List<String>> getDistrictsByCity(@RequestParam String city) {
        List<String> districts = attractionService.getDistrictsByCity(city);
        return Result.success(districts);
    }
}
