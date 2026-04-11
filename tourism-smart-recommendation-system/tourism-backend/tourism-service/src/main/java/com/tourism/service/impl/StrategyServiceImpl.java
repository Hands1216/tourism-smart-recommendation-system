package com.tourism.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.ai.service.LLMService;
import com.tourism.common.enums.AuditStatus;
import com.tourism.model.dto.StrategyCreateDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.AttractionStrategy;
import com.tourism.model.entity.Favorite;
import com.tourism.model.entity.Strategy;
import com.tourism.model.vo.StrategyVO;
import com.tourism.service.FavoriteService;
import com.tourism.service.StrategyService;
import com.tourism.service.SensitiveWordService;
import com.tourism.service.mapper.AttractionMapper;
import com.tourism.service.mapper.AttractionStrategyMapper;
import com.tourism.service.mapper.FavoriteMapper;
import com.tourism.service.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 旅游攻略服务实现
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    private final StrategyMapper strategyMapper;
    private final FavoriteService favoriteService;
    private final LLMService llmService;
    private final StringRedisTemplate redisTemplate;
    private final SensitiveWordService sensitiveWordService;
    private final AttractionMapper attractionMapper;
    private final AttractionStrategyMapper attractionStrategyMapper;

    private static final String HOT_KEYWORDS_KEY = "strategy:hot_keywords";

    @Override
    public IPage<StrategyVO> getStrategyPage(Page<?> page, String destination, String keyword,
                                             String tag, String orderBy, Long userId,
                                             BigDecimal minBudget, BigDecimal maxBudget,
                                             Integer minDays, Integer maxDays, String season) {
        // 记录搜索词
        if (keyword != null && !keyword.trim().isEmpty()) {
            recordKeyword(keyword.trim());
        }
        return strategyMapper.selectStrategyPage(page, destination, keyword, tag, orderBy, userId,
                minBudget, maxBudget, minDays, maxDays, season);
    }

    @Override
    public StrategyVO getStrategyDetail(Long id, Long userId) {
        // 增加浏览量
        Strategy strategy = getById(id);
        if (strategy != null) {
            strategy.setViewCount((strategy.getViewCount() == null ? 0 : strategy.getViewCount()) + 1);
            updateById(strategy);
        }
        return strategyMapper.selectStrategyDetail(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStrategy(Long userId, StrategyCreateDTO dto) {
        // 敏感词检查
        checkSensitiveContent(dto);

        Strategy strategy = new Strategy();
        strategy.setUserId(userId);
        strategy.setTitle(dto.getTitle());
        strategy.setCoverImage(dto.getCoverImage());
        strategy.setDestination(dto.getDestination());
        strategy.setDays(dto.getDays());
        strategy.setContent(dto.getContent());
        strategy.setBudget(dto.getBudget());
        strategy.setSeason(dto.getSeason());
        strategy.setSummary(dto.getSummary());
        strategy.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : 1);

        // 转换列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            strategy.setImages(JSON.toJSONString(dto.getImages()));
        }
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            strategy.setTags(JSON.toJSONString(dto.getTags()));
        }

        strategy.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        strategy.setViewCount(0);
        strategy.setLikeCount(0);
        strategy.setFavoriteCount(0);
        strategy.setCommentCount(0);
        strategy.setIsAiGenerated(0);
        strategy.setAuditStatus(AuditStatus.PENDING.getCode());

        save(strategy);
        if (strategy.getRootStrategyId() == null) {
            strategy.setRootStrategyId(strategy.getId());
            updateById(strategy);
        }
        return strategy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStrategy(Long userId, Long id, StrategyCreateDTO dto) {
        Strategy strategy = getById(id);
        if (strategy == null) {
            throw new RuntimeException("攻略不存在");
        }
        if (!strategy.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此攻略");
        }

        // 敏感词检查
        checkSensitiveContent(dto);

        if (strategy.getRootStrategyId() == null) {
            strategy.setRootStrategyId(strategy.getId());
            updateById(strategy);
        }

        // 判断是否需要重新提审：已发布或已驳回的攻略点击"发布"时，创建新记录
        boolean needResubmit = dto.getStatus() != null && dto.getStatus() == 1
                && (AuditStatus.APPROVED.getCode().equals(strategy.getAuditStatus())
                    || AuditStatus.REJECTED.getCode().equals(strategy.getAuditStatus()));

        if (needResubmit) {
            // 已发布的攻略：将原记录下架
            if (AuditStatus.APPROVED.getCode().equals(strategy.getAuditStatus())) {
                strategy.setStatus(0);
                updateById(strategy);
            }
            // 已驳回的攻略：原记录保持不变

            // 创建一条新的待审核记录
            Strategy newStrategy = new Strategy();
            newStrategy.setUserId(userId);
            newStrategy.setTitle(dto.getTitle());
            newStrategy.setCoverImage(dto.getCoverImage());
            newStrategy.setDestination(dto.getDestination());
            newStrategy.setDays(dto.getDays());
            newStrategy.setContent(dto.getContent());
            newStrategy.setBudget(dto.getBudget());
            newStrategy.setSeason(dto.getSeason());
            newStrategy.setSummary(dto.getSummary());
            newStrategy.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : strategy.getVisibility());
            newStrategy.setImages(dto.getImages() != null ? JSON.toJSONString(dto.getImages()) : null);
            newStrategy.setTags(dto.getTags() != null ? JSON.toJSONString(dto.getTags()) : null);
            newStrategy.setStatus(1);
            newStrategy.setViewCount(0);
            newStrategy.setLikeCount(0);
            newStrategy.setFavoriteCount(0);
            newStrategy.setCommentCount(0);
            newStrategy.setIsAiGenerated(0);
            newStrategy.setAuditStatus(AuditStatus.PENDING.getCode());
            newStrategy.setAuditReason(null);
            newStrategy.setRootStrategyId(strategy.getRootStrategyId() != null ? strategy.getRootStrategyId() : strategy.getId());
            save(newStrategy);
        } else {
            // 普通编辑（草稿、待审核等）：直接更新原记录
            strategy.setTitle(dto.getTitle());
            strategy.setCoverImage(dto.getCoverImage());
            strategy.setDestination(dto.getDestination());
            strategy.setDays(dto.getDays());
            strategy.setContent(dto.getContent());
            strategy.setBudget(dto.getBudget());
            strategy.setSeason(dto.getSeason());
            strategy.setSummary(dto.getSummary());
            if (dto.getVisibility() != null) {
                strategy.setVisibility(dto.getVisibility());
            }

            if (dto.getImages() != null) {
                strategy.setImages(JSON.toJSONString(dto.getImages()));
            }
            if (dto.getTags() != null) {
                strategy.setTags(JSON.toJSONString(dto.getTags()));
            }
            if (dto.getStatus() != null) {
                strategy.setStatus(dto.getStatus());
            }

            updateById(strategy);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStrategy(Long userId, Long id) {
        Strategy strategy = getById(id);
        if (strategy == null) {
            throw new RuntimeException("攻略不存在");
        }
        if (!strategy.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此攻略");
        }
        removeById(id);
    }

    @Override
    public String generateAiStrategy(String destination, Integer days, Double budget, String interests) {
        try {
            return llmService.generateStrategy(destination, days, budget, interests);
        } catch (Exception e) {
            log.error("AI生成攻略失败", e);
            // 降级方案：返回模板
            return generateTemplateStrategy(destination, days);
        }
    }

    private String generateTemplateStrategy(String destination, Integer days) {
        StringBuilder strategy = new StringBuilder();
        strategy.append("<h1>").append(destination).append(days).append("日游攻略</h1>");
        strategy.append("<h2>目的地简介</h2>");
        strategy.append("<p>").append(destination).append("是一个风景秀丽、文化底蕴深厚的旅游胜地。</p>");
        strategy.append("<h2>推荐行程</h2>");

        String[] attractions = {"著名景点A", "著名景点B", "著名景点C", "著名景点D"};
        for (int i = 0; i < days; i++) {
            strategy.append("<h3>第").append(i + 1).append("天</h3>");
            strategy.append("<ul>");
            strategy.append("<li>上午：游览").append(attractions[i % attractions.length]).append("</li>");
            strategy.append("<li>下午：继续探索").append(attractions[(i + 1) % attractions.length]).append("</li>");
            strategy.append("<li>晚上：品尝当地美食，休息放松</li>");
            strategy.append("</ul>");
        }

        strategy.append("<h2>美食推荐</h2>");
        strategy.append("<ul><li>特色菜1：当地必尝美食</li><li>特色菜2：不可错过的美味</li></ul>");
        strategy.append("<h2>住宿建议</h2>");
        strategy.append("<p>建议住在市中心或景区附近，交通便利</p>");
        strategy.append("<h2>注意事项</h2>");
        strategy.append("<ul><li>提前预订门票和住宿</li><li>注意天气变化，携带合适衣物</li><li>尊重当地文化习俗</li></ul>");

        return strategy.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeStrategy(Long userId, Long strategyId) {
        FavoriteMapper favoriteMapper = (FavoriteMapper) favoriteService.getBaseMapper();
        Favorite existing = favoriteMapper.selectByUserAndItem(userId, "strategy_like", strategyId);

        if (existing != null && existing.getDeleted() == 0) {
            // 已点赞 → 取消点赞（逻辑删除）
            favoriteService.removeById(existing.getId());
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setLikeCount(Math.max(0, strategy.getLikeCount() - 1));
                updateById(strategy);
            }
        } else if (existing != null && existing.getDeleted() == 1) {
            // 之前点赞后取消过 → 恢复记录
            favoriteMapper.restoreFavorite(existing.getId(), LocalDateTime.now());
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setLikeCount((strategy.getLikeCount() == null ? 0 : strategy.getLikeCount()) + 1);
                updateById(strategy);
            }
        } else {
            // 从未点赞过 → 新增
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemType("strategy_like");
            favorite.setItemId(strategyId);
            favoriteService.save(favorite);
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setLikeCount((strategy.getLikeCount() == null ? 0 : strategy.getLikeCount()) + 1);
                updateById(strategy);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoriteStrategy(Long userId, Long strategyId) {
        FavoriteMapper favoriteMapper = (FavoriteMapper) favoriteService.getBaseMapper();
        Favorite existing = favoriteMapper.selectByUserAndItem(userId, "strategy", strategyId);

        if (existing != null && existing.getDeleted() == 0) {
            // 已收藏 → 取消收藏（逻辑删除）
            favoriteService.removeById(existing.getId());
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setFavoriteCount(Math.max(0, strategy.getFavoriteCount() - 1));
                updateById(strategy);
            }
        } else if (existing != null && existing.getDeleted() == 1) {
            // 之前收藏后取消过 → 恢复记录
            favoriteMapper.restoreFavorite(existing.getId(), LocalDateTime.now());
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setFavoriteCount((strategy.getFavoriteCount() == null ? 0 : strategy.getFavoriteCount()) + 1);
                updateById(strategy);
            }
        } else {
            // 从未收藏过 → 新增
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemType("strategy");
            favorite.setItemId(strategyId);
            favoriteService.save(favorite);
            Strategy strategy = getById(strategyId);
            if (strategy != null) {
                strategy.setFavoriteCount((strategy.getFavoriteCount() == null ? 0 : strategy.getFavoriteCount()) + 1);
                updateById(strategy);
            }
        }
    }

    @Override
    public IPage<StrategyVO> getAdminStrategyPage(Page<?> page, String title, Integer auditStatus, String destination) {
        return strategyMapper.selectAdminStrategyPage(page, title, auditStatus, destination);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditStrategy(Long id, Integer auditStatus, String auditReason) {
        Strategy strategy = getById(id);
        if (strategy == null) {
            throw new RuntimeException("攻略不存在");
        }
        strategy.setAuditStatus(auditStatus);
        strategy.setAuditReason(auditReason);
        updateById(strategy);

        // 审核通过时自动建立景点-攻略关联
        if (AuditStatus.APPROVED.getCode().equals(auditStatus)) {
            buildAttractionStrategyRelation(strategy);
        }
    }

    /**
     * 根据攻略的destination和title自动关联匹配的景点
     */
    private void buildAttractionStrategyRelation(Strategy strategy) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
        if (strategy.getDestination() != null && !strategy.getDestination().isEmpty()) {
            wrapper.eq(Attraction::getCity, strategy.getDestination())
                   .or()
                   .like(Attraction::getName, strategy.getTitle());
        } else {
            return;
        }

        List<Attraction> matchedAttractions = attractionMapper.selectList(wrapper);
        if (matchedAttractions.isEmpty()) {
            return;
        }

        List<AttractionStrategy> relations = new ArrayList<>();
        // 查询已有关联避免重复
        List<Long> existingAttractionIds = attractionStrategyMapper.selectList(
                new LambdaQueryWrapper<AttractionStrategy>()
                        .eq(AttractionStrategy::getStrategyId, strategy.getId())
        ).stream().map(AttractionStrategy::getAttractionId).toList();

        for (Attraction attraction : matchedAttractions) {
            if (!existingAttractionIds.contains(attraction.getId())) {
                AttractionStrategy as = new AttractionStrategy();
                as.setAttractionId(attraction.getId());
                as.setStrategyId(strategy.getId());
                relations.add(as);
            }
        }

        if (!relations.isEmpty()) {
            attractionStrategyMapper.batchInsert(relations);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStrategyStatus(Long id, Integer status) {
        Strategy strategy = getById(id);
        if (strategy == null) {
            throw new RuntimeException("攻略不存在");
        }
        strategy.setStatus(status);
        updateById(strategy);
    }

    @Override
    public IPage<StrategyVO> getUserStrategyPage(Page<?> page, Long userId) {
        return strategyMapper.selectUserStrategyPage(page, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoSaveStrategy(Long userId, StrategyCreateDTO dto) {
        Strategy strategy;
        if (dto.getStatus() == null) {
            dto.setStatus(0); // 草稿
        }

        // 查找用户最近的草稿（如果没有指定id）
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getUserId, userId)
                .eq(Strategy::getStatus, 0)
                .orderByDesc(Strategy::getUpdateTime)
                .last("LIMIT 1");
        strategy = getOne(wrapper);

        if (strategy != null && dto.getTitle() != null && dto.getTitle().equals(strategy.getTitle())) {
            // 更新已有草稿
            strategy.setContent(dto.getContent());
            strategy.setCoverImage(dto.getCoverImage());
            strategy.setDestination(dto.getDestination());
            strategy.setDays(dto.getDays());
            strategy.setBudget(dto.getBudget());
            strategy.setSeason(dto.getSeason());
            strategy.setSummary(dto.getSummary());
            strategy.setVisibility(dto.getVisibility());
            if (dto.getImages() != null) {
                strategy.setImages(JSON.toJSONString(dto.getImages()));
            }
            if (dto.getTags() != null) {
                strategy.setTags(JSON.toJSONString(dto.getTags()));
            }
            updateById(strategy);
            return strategy.getId();
        } else {
            // 新建草稿
            return createStrategy(userId, dto);
        }
    }

    @Override
    public IPage<StrategyVO> getDraftPage(Page<?> page, Long userId) {
        return strategyMapper.selectDraftPage(page, userId);
    }

    @Override
    public List<StrategyVO> getRelatedStrategies(Long id, Integer limit) {
        Strategy current = getById(id);
        if (current == null) {
            return Collections.emptyList();
        }
        return strategyMapper.selectRelatedStrategies(id, current.getDestination(), current.getTags(), limit);
    }

    @Override
    public List<String> getHotKeywords() {
        try {
            Set<String> keywords = redisTemplate.opsForZSet().reverseRange(HOT_KEYWORDS_KEY, 0, 9);
            return keywords != null ? new ArrayList<>(keywords) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("获取热门搜索词失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void recordKeyword(String keyword) {
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, 1);
            redisTemplate.expire(HOT_KEYWORDS_KEY, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("记录搜索词失败", e);
        }
    }

    /**
     * 检查内容是否包含敏感词
     */
    private void checkSensitiveContent(StrategyCreateDTO dto) {
        StringBuilder textToCheck = new StringBuilder();
        if (dto.getTitle() != null) textToCheck.append(dto.getTitle()).append(" ");
        if (dto.getContent() != null) textToCheck.append(dto.getContent()).append(" ");
        if (dto.getSummary() != null) textToCheck.append(dto.getSummary());

        Set<String> sensitiveWords = sensitiveWordService.findSensitiveWords(textToCheck.toString());
        if (!sensitiveWords.isEmpty()) {
            throw new RuntimeException("内容包含敏感词：" + String.join("、", sensitiveWords));
        }
    }
}
