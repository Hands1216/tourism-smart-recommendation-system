package com.tourism.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.enums.AuditStatus;
import com.tourism.model.dto.AttractionAdminDTO;
import com.tourism.model.dto.AttractionQueryDTO;
import com.tourism.model.dto.AttractionRatingDTO;
import com.tourism.model.entity.*;
import com.tourism.model.vo.*;
import com.tourism.service.*;
import com.tourism.service.mapper.AttractionMapper;
import com.tourism.service.mapper.AttractionRatingMapper;
import com.tourism.service.mapper.AttractionStrategyMapper;
import com.tourism.service.mapper.UserFootprintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 景点服务实现
 *
 */
@Service
@RequiredArgsConstructor
public class AttractionServiceImpl extends ServiceImpl<AttractionMapper, Attraction> implements AttractionService {

    private final AttractionMapper attractionMapper;
    private final AttractionRatingMapper ratingMapper;
    private final UserFootprintMapper footprintMapper;
    private final AttractionStrategyMapper strategyMapper;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final StrategyService strategyService;

    @Override
    public IPage<AttractionVO> getAttractionPage(Page<?> page, String keyword, Long categoryId, String province, Long userId) {
        return attractionMapper.selectAttractionPage(page, keyword, categoryId, province, userId);
    }

    @Override
    public AttractionVO getAttractionDetail(Long id, Long userId) {
        AttractionVO vo = attractionMapper.selectAttractionDetail(id, userId);
        if (vo != null) {
            // 记录浏览行为
            userService.recordBehavior(userId, "attraction", id, "view", 1.0);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoriteAttraction(Long userId, Long attractionId) {
        // 检查是否已收藏
        if (favoriteService.isFavorited(userId, "attraction", attractionId)) {
            throw new RuntimeException("已收藏该景点");
        }

        // 新增收藏
        favoriteService.addFavorite(userId, "attraction", attractionId);

        // 更新收藏量
        attractionMapper.updateFavoriteCount(attractionId, 1);
        // 记录收藏行为
        userService.recordBehavior(userId, "attraction", attractionId, "favorite", 3.0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteAttraction(Long userId, Long attractionId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, "attraction")
                .eq(Favorite::getItemId, attractionId);
        favoriteService.remove(wrapper);

        // 更新收藏量
        attractionMapper.updateFavoriteCount(attractionId, -1);
    }

    @Override
    public IPage<AttractionVO> getAttractionPageAdvanced(AttractionQueryDTO queryDTO, Long userId) {
        Page<AttractionVO> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<AttractionVO> result = attractionMapper.selectAttractionPageAdvanced(page, queryDTO, userId);

        // 解析JSON字段
        result.getRecords().forEach(this::parseJsonFields);

        return result;
    }

    @Override
    @Transactional
    public AttractionVO getAttractionDetailEnhanced(Long id, Long userId) {
        // 查询基本信息
        AttractionVO vo = attractionMapper.selectAttractionDetail(id, userId);
        if (vo == null) {
            throw new RuntimeException("景点不存在");
        }

        // 增加浏览量
        attractionMapper.incrementViewCount(id);

        // 解析JSON字段
        Attraction attraction = getById(id);
        parseJsonFieldsFromEntity(vo, attraction);

        // 查询用户评分（只取真正打过分的最新一条）
        if (userId != null) {
            AttractionRating rating = ratingMapper.selectOne(
                new LambdaQueryWrapper<AttractionRating>()
                    .eq(AttractionRating::getAttractionId, id)
                    .eq(AttractionRating::getUserId, userId)
                    .gt(AttractionRating::getOverallScore, BigDecimal.ZERO)
                    .orderByDesc(AttractionRating::getCreateTime)
                    .last("LIMIT 1")
            );
            if (rating != null) {
                vo.setUserRating(BeanUtil.copyProperties(rating, AttractionRatingVO.class));
            }
        }

        // 查询评分统计
        RatingStatsVO stats = ratingMapper.selectRatingStats(id);
        vo.setRatingStats(stats);

        // 查询相似景点
        List<AttractionVO> similar = attractionMapper.selectSimilarAttractions(
            id, vo.getCategoryId(), vo.getCity(), 6
        );
        similar.forEach(this::parseJsonFields);
        vo.setSimilarAttractions(similar);

        // 查询相关攻略
        List<Long> strategyIds = strategyMapper.selectStrategyIdsByAttractionId(id);
        if (!strategyIds.isEmpty()) {
            List<Strategy> strategies = strategyService.listByIds(strategyIds).stream()
                    .filter(s -> s.getDeleted() == 0 && s.getStatus() == 1 && s.getAuditStatus() == 1)
                    .collect(Collectors.toList());
            vo.setRelatedStrategies(convertStrategiesToVO(strategies));
        }

        // 记录浏览行为
        if (userId != null) {
            userService.recordBehavior(userId, "attraction", id, "view", 1.0);
        }

        return vo;
    }

    @Override
    public List<AttractionVO> getSimilarAttractions(Long attractionId, Integer limit) {
        Attraction attraction = getById(attractionId);
        if (attraction == null) {
            return List.of();
        }

        List<AttractionVO> similar = attractionMapper.selectSimilarAttractions(
            attractionId, attraction.getCategoryId(), attraction.getCity(), limit
        );
        similar.forEach(this::parseJsonFields);
        return similar;
    }

    @Override
    public List<StrategyVO> getRelatedStrategies(Long attractionId, Integer limit) {
        List<Long> strategyIds = strategyMapper.selectStrategyIdsByAttractionId(attractionId);
        if (strategyIds.isEmpty()) {
            return List.of();
        }

        List<Strategy> strategies = strategyService.listByIds(strategyIds);

        // 过滤已删除、未上架、未通过审核的攻略
        strategies = strategies.stream()
                .filter(s -> s.getDeleted() == 0 && s.getStatus() == 1 && s.getAuditStatus() == 1)
                .collect(Collectors.toList());

        // 限制数量
        if (limit != null && strategies.size() > limit) {
            strategies = strategies.subList(0, limit);
        }

        return convertStrategiesToVO(strategies);
    }

    /**
     * 批量转换Strategy为StrategyVO，填充作者信息
     */
    private List<StrategyVO> convertStrategiesToVO(List<Strategy> strategies) {
        if (strategies.isEmpty()) {
            return List.of();
        }

        // 批量查询作者信息
        Set<Long> userIds = strategies.stream()
                .map(Strategy::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<StrategyVO> voList = new ArrayList<>();
        for (Strategy strategy : strategies) {
            StrategyVO vo = BeanUtil.copyProperties(strategy, StrategyVO.class);
            User author = userMap.get(strategy.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional
    public void rateAttraction(Long userId, AttractionRatingDTO dto) {
        // 计算综合评分
        BigDecimal overall = dto.getSceneryScore()
            .add(dto.getFunScore())
            .add(dto.getValueScore())
            .divide(new BigDecimal("3"), 1, RoundingMode.HALF_UP);

        // 检查是否已评分（只查真正打过分的最新一条）
        AttractionRating existing = ratingMapper.selectOne(
            new LambdaQueryWrapper<AttractionRating>()
                .eq(AttractionRating::getAttractionId, dto.getAttractionId())
                .eq(AttractionRating::getUserId, userId)
                .gt(AttractionRating::getOverallScore, BigDecimal.ZERO)
                .orderByDesc(AttractionRating::getCreateTime)
                .last("LIMIT 1")
        );

        if (existing != null) {
            // 更新评分
            existing.setSceneryScore(dto.getSceneryScore());
            existing.setFunScore(dto.getFunScore());
            existing.setValueScore(dto.getValueScore());
            existing.setOverallScore(overall);
            existing.setComment(dto.getComment());
            ratingMapper.updateById(existing);
        } else {
            // 新增评分
            AttractionRating rating = new AttractionRating();
            rating.setAttractionId(dto.getAttractionId());
            rating.setUserId(userId);
            rating.setSceneryScore(dto.getSceneryScore());
            rating.setFunScore(dto.getFunScore());
            rating.setValueScore(dto.getValueScore());
            rating.setOverallScore(overall);
            rating.setComment(dto.getComment());
            ratingMapper.insert(rating);
        }

        // 更新景点平均评分
        ratingMapper.updateAttractionRating(dto.getAttractionId());

        // 记录评分行为
        userService.recordBehavior(userId, "attraction", dto.getAttractionId(), "rate", overall.doubleValue());
    }

    @Override
    public AttractionRatingVO getUserRating(Long userId, Long attractionId) {
        AttractionRating rating = ratingMapper.selectOne(
            new LambdaQueryWrapper<AttractionRating>()
                .eq(AttractionRating::getAttractionId, attractionId)
                .eq(AttractionRating::getUserId, userId)
                .orderByDesc(AttractionRating::getCreateTime)
                .last("LIMIT 1")
        );
        return rating != null ? BeanUtil.copyProperties(rating, AttractionRatingVO.class) : null;
    }

    @Override
    public IPage<AttractionRatingVO> getAttractionRatings(Long attractionId, Page<AttractionRatingVO> page) {
        return ratingMapper.selectRatingPage(page, attractionId);
    }

    @Override
    @Transactional
    public void deleteAttractionRating(Long userId, Long attractionId, Long ratingId) {
        AttractionRating rating = ratingMapper.selectById(ratingId);
        if (rating == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!rating.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        if (!rating.getAttractionId().equals(attractionId)) {
            throw new RuntimeException("评论不属于该景点");
        }
        ratingMapper.deleteById(ratingId);
        ratingMapper.updateAttractionRating(attractionId);
    }

    @Override
    @Transactional
    public void addAttractionComment(Long userId, Long attractionId, String comment) {
        AttractionRating rating = new AttractionRating();
        rating.setAttractionId(attractionId);
        rating.setUserId(userId);
        rating.setComment(comment);
        // 纯评论不打分，给评分字段设置默认值以满足数据库非空约束
        rating.setSceneryScore(BigDecimal.ZERO);
        rating.setFunScore(BigDecimal.ZERO);
        rating.setValueScore(BigDecimal.ZERO);
        rating.setOverallScore(BigDecimal.ZERO);
        ratingMapper.insert(rating);
    }

    @Override
    @Transactional
    public void markVisited(Long userId, Long attractionId, LocalDate visitDate, String note) {
        // 检查是否已标记
        UserFootprint existing = footprintMapper.selectOne(
            new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId)
                .eq(UserFootprint::getAttractionId, attractionId)
        );

        if (existing != null) {
            // 更新
            existing.setVisitDate(visitDate);
            existing.setNote(note);
            footprintMapper.updateById(existing);
        } else {
            // 新增
            UserFootprint footprint = new UserFootprint();
            footprint.setUserId(userId);
            footprint.setAttractionId(attractionId);
            footprint.setVisitDate(visitDate);
            footprint.setNote(note);
            footprintMapper.insert(footprint);
        }

        // 记录行为
        userService.recordBehavior(userId, "attraction", attractionId, "visited", 2.0);
    }

    @Override
    @Transactional
    public void unmarkVisited(Long userId, Long attractionId) {
        footprintMapper.delete(
            new LambdaQueryWrapper<UserFootprint>()
                .eq(UserFootprint::getUserId, userId)
                .eq(UserFootprint::getAttractionId, attractionId)
        );
    }

    @Override
    public List<UserFootprintVO> getUserFootprints(Long userId) {
        return footprintMapper.selectUserFootprints(userId);
    }

    @Override
    public AttractionFilterVO getFilterOptions() {
        AttractionFilterVO vo = new AttractionFilterVO();
        vo.setProvinces(attractionMapper.selectAllProvinces());
        vo.setScenicLevels(attractionMapper.selectAllScenicLevels());

        // 解析场景分类（逗号分隔）
        List<String> sceneTypes = attractionMapper.selectAllSceneTypes();
        Set<String> sceneTypeSet = new HashSet<>();
        for (String types : sceneTypes) {
            if (types != null && !types.isEmpty()) {
                sceneTypeSet.addAll(Arrays.asList(types.split(",")));
            }
        }
        vo.setSceneTypes(new ArrayList<>(sceneTypeSet));

        vo.setDurations(attractionMapper.selectAllDurations());

        // 构建省市区映射数据
        Map<String, List<String>> provinceCityMap = new LinkedHashMap<>();
        Map<String, List<String>> cityDistrictMap = new LinkedHashMap<>();

        List<Map<String, String>> mappings = attractionMapper.selectProvinceCityMapping();
        for (Map<String, String> mapping : mappings) {
            String province = mapping.get("province");
            String city = mapping.get("city");

            // 构建省-市映射
            provinceCityMap.computeIfAbsent(province, k -> new ArrayList<>()).add(city);

            // 对每个城市获取其区县列表
            if (!cityDistrictMap.containsKey(city)) {
                List<String> districts = attractionMapper.selectDistrictsByCity(city);
                cityDistrictMap.put(city, districts);
            }
        }

        vo.setProvinceCityMap(provinceCityMap);
        vo.setCityDistrictMap(cityDistrictMap);

        return vo;
    }

    @Override
    public List<String> getCitiesByProvince(String province) {
        return attractionMapper.selectCitiesByProvince(province);
    }

    @Override
    public List<String> getDistrictsByCity(String city) {
        return attractionMapper.selectDistrictsByCity(city);
    }

    @Override
    @Transactional
    public Long createAttraction(AttractionAdminDTO dto) {
        Attraction attraction = new Attraction();
        BeanUtil.copyProperties(dto, attraction);

        // JSON字段需要手动处理：将List<String>转换为JSON字符串
        if (dto.getTags() != null) {
            attraction.setTags(JSONUtil.toJsonStr(dto.getTags()));
        } else {
            attraction.setTags(null);
        }

        if (dto.getFeatures() != null) {
            attraction.setFeatures(JSONUtil.toJsonStr(dto.getFeatures()));
        } else {
            attraction.setFeatures(null);
        }

        if (dto.getTips() != null) {
            attraction.setTips(JSONUtil.toJsonStr(dto.getTips()));
        } else {
            attraction.setTips(null);
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            attraction.setImages(JSONUtil.toJsonStr(dto.getImages()));
        } else {
            attraction.setImages(null);
        }

        // 设置默认值
        attraction.setAuditStatus(1); // 已通过
        attraction.setViewCount(0);
        attraction.setFavoriteCount(0);

        save(attraction);
        return attraction.getId();
    }

    @Override
    @Transactional
    public void updateAttraction(AttractionAdminDTO dto) {
        Attraction attraction = getById(dto.getId());
        if (attraction == null) {
            throw new RuntimeException("景点不存在");
        }

        BeanUtil.copyProperties(dto, attraction, "id", "viewCount", "favoriteCount", "rating");

        // JSON字段需要手动处理：将List<String>转换为JSON字符串
        if (dto.getTags() != null) {
            attraction.setTags(JSONUtil.toJsonStr(dto.getTags()));
        } else {
            attraction.setTags(null);
        }

        if (dto.getFeatures() != null) {
            attraction.setFeatures(JSONUtil.toJsonStr(dto.getFeatures()));
        } else {
            attraction.setFeatures(null);
        }

        if (dto.getTips() != null) {
            attraction.setTips(JSONUtil.toJsonStr(dto.getTips()));
        } else {
            attraction.setTips(null);
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            attraction.setImages(JSONUtil.toJsonStr(dto.getImages()));
        } else {
            attraction.setImages(null);
        }

        // sceneType是字符串类型（逗号分隔），直接设置
        if (dto.getSceneType() != null) {
            attraction.setSceneType(dto.getSceneType());
        }

        updateById(attraction);
    }

    @Override
    @Transactional
    public void setSeasonalStatus(Long id, Integer status, String note) {
        Attraction attraction = getById(id);
        if (attraction == null) {
            throw new RuntimeException("景点不存在");
        }

        attraction.setSeasonalStatus(status);
        attraction.setSeasonalNote(note);
        updateById(attraction);
    }

    @Override
    public List<AttractionStatsVO> getHotAttractionStats(Integer limit, Integer days) {
        return attractionMapper.selectHotAttractionStats(limit != null ? limit : 10, days != null ? days : 30);
    }

    @Override
    public List<AttractionStatsVO> getGrowthPotentialAttractions(Integer limit, Integer days) {
        return attractionMapper.selectGrowthPotentialAttractions(limit != null ? limit : 10, days != null ? days : 7);
    }

    /**
     * 解析JSON字段
     */
    private void parseJsonFields(AttractionVO vo) {
        Attraction attraction = getById(vo.getId());
        if (attraction != null) {
            parseJsonFieldsFromEntity(vo, attraction);
        }
    }

    /**
     * 从实体解析JSON字段到VO
     */
    private void parseJsonFieldsFromEntity(AttractionVO vo, Attraction attraction) {
        // 解析 images 字段
        if (attraction.getImages() != null && !attraction.getImages().isEmpty() && !"null".equals(attraction.getImages())) {
            try {
                vo.setImages(JSONUtil.toList(attraction.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        // 解析 tags 字段
        if (attraction.getTags() != null && !attraction.getTags().isEmpty() && !"null".equals(attraction.getTags())) {
            try {
                vo.setTags(JSONUtil.toList(attraction.getTags(), String.class));
            } catch (Exception e) {
                vo.setTags(new ArrayList<>());
            }
        } else {
            vo.setTags(new ArrayList<>());
        }

        // 解析 features 字段
        if (attraction.getFeatures() != null && !attraction.getFeatures().isEmpty() && !"null".equals(attraction.getFeatures())) {
            try {
                vo.setFeatures(JSONUtil.toList(attraction.getFeatures(), String.class));
            } catch (Exception e) {
                vo.setFeatures(new ArrayList<>());
            }
        } else {
            vo.setFeatures(new ArrayList<>());
        }

        // 解析 tips 字段
        if (attraction.getTips() != null && !attraction.getTips().isEmpty() && !"null".equals(attraction.getTips())) {
            try {
                vo.setTips(JSONUtil.toList(attraction.getTips(), String.class));
            } catch (Exception e) {
                vo.setTips(new ArrayList<>());
            }
        } else {
            vo.setTips(new ArrayList<>());
        }
    }

    @Override
    public List<Attraction> getUserFavorites(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemType, "attraction");

        List<Favorite> favorites = favoriteService.list(wrapper);
        List<Long> attractionIds = favorites.stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());

        if (attractionIds.isEmpty()) {
            return List.of();
        }

        return list(new LambdaQueryWrapper<Attraction>()
                .in(Attraction::getId, attractionIds)
                .eq(Attraction::getAuditStatus, AuditStatus.APPROVED.getCode()));
    }

    @Override
    public List<Attraction> getRecommendByCity(String city, Integer limit) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attraction::getCity, city)
                .eq(Attraction::getAuditStatus, AuditStatus.APPROVED.getCode())
                .orderByDesc(Attraction::getRating)
                .last("LIMIT " + (limit != null ? limit : 10));
        return list(wrapper);
    }

    @Override
    public List<Attraction> getRecommendByPreferences(String preferences, Integer limit) {
        // 解析用户偏好
        Set<String> userPrefs = new HashSet<>();
        if (preferences != null && !preferences.isEmpty()) {
            try {
                String[] prefs = preferences.replace("[", "").replace("]", "").replace("\"", "").split(",");
                for (String p : prefs) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        userPrefs.add(trimmed);
                    }
                }
            } catch (Exception e) {
                // 解析失败，使用默认偏好
                userPrefs.add("自然风光");
            }
        }

        if (userPrefs.isEmpty()) {
            userPrefs.add("自然风光");
        }

        // 查询所有景点
        List<Attraction> allAttractions = list(new LambdaQueryWrapper<Attraction>()
                .eq(Attraction::getAuditStatus, AuditStatus.APPROVED.getCode()));

        // 计算每个景点的匹配分数
        Map<Long, Double> scoreMap = new HashMap<>();
        for (Attraction attraction : allAttractions) {
            double score = calculateMatchScore(attraction, userPrefs);
            scoreMap.put(attraction.getId(), score);
        }

        // 按分数排序并返回
        return allAttractions.stream()
                .sorted((a1, a2) -> Double.compare(
                        scoreMap.getOrDefault(a2.getId(), 0.0),
                        scoreMap.getOrDefault(a1.getId(), 0.0)))
                .limit(limit != null ? limit : 8)
                .collect(Collectors.toList());
    }

    /**
     * 计算景点与用户偏好的匹配分数
     */
    private double calculateMatchScore(Attraction attraction, Set<String> userPrefs) {
        double score = 0.0;

        // 基础分：评分权重
        score += attraction.getRating() != null ? attraction.getRating().doubleValue() : 0.0;

        // 标签匹配分数
        if (attraction.getTags() != null && !attraction.getTags().isEmpty()) {
            String[] tags = attraction.getTags().split(",");
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                for (String pref : userPrefs) {
                    if (trimmedTag.contains(pref) || pref.contains(trimmedTag)) {
                        score += 1.5; // 标签匹配加分
                        break;
                    }
                }
            }
        }

        // 特征匹配分数
        if (attraction.getFeatures() != null && !attraction.getFeatures().isEmpty()) {
            // 这里简化处理，实际应解析JSON
            String features = attraction.getFeatures();
            for (String pref : userPrefs) {
                if (features.contains(pref)) {
                    score += 1.0;
                }
            }
        }

        return score;
    }
}
