package com.tourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tourism.model.dto.StrategyCreateDTO;
import com.tourism.model.entity.Strategy;
import com.tourism.model.vo.StrategyVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 旅游攻略服务接口
 *
 */
public interface StrategyService extends IService<Strategy> {

    /**
     * 分页查询攻略列表（前台）
     */
    IPage<StrategyVO> getStrategyPage(Page<?> page, String destination, String keyword,
                                      String tag, String orderBy, Long userId,
                                      BigDecimal minBudget, BigDecimal maxBudget,
                                      Integer minDays, Integer maxDays, String season);

    /**
     * 获取攻略详情
     */
    StrategyVO getStrategyDetail(Long id, Long userId);

    /**
     * 创建攻略
     */
    Long createStrategy(Long userId, StrategyCreateDTO dto);

    /**
     * 更新攻略
     */
    void updateStrategy(Long userId, Long id, StrategyCreateDTO dto);

    /**
     * 删除攻略（仅作者本人）
     */
    void deleteStrategy(Long userId, Long id);

    /**
     * AI生成攻略
     */
    String generateAiStrategy(String destination, Integer days, Double budget, String interests);

    /**
     * 点赞攻略
     */
    void likeStrategy(Long userId, Long strategyId);

    /**
     * 收藏攻略
     */
    void favoriteStrategy(Long userId, Long strategyId);

    /**
     * 管理后台分页查询
     */
    IPage<StrategyVO> getAdminStrategyPage(Page<?> page, String title, Integer auditStatus, String destination);

    /**
     * 审核攻略
     */
    void auditStrategy(Long id, Integer auditStatus, String auditReason);

    /**
     * 修改攻略上下架状态
     */
    void updateStrategyStatus(Long id, Integer status);

    /**
     * 用户自己的攻略列表
     */
    IPage<StrategyVO> getUserStrategyPage(Page<?> page, Long userId);

    /**
     * 自动保存草稿
     */
    Long autoSaveStrategy(Long userId, StrategyCreateDTO dto);

    /**
     * 草稿列表
     */
    IPage<StrategyVO> getDraftPage(Page<?> page, Long userId);

    /**
     * 相关推荐
     */
    List<StrategyVO> getRelatedStrategies(Long id, Integer limit);

    /**
     * 热门搜索词
     */
    List<String> getHotKeywords();

    /**
     * 记录搜索词
     */
    void recordKeyword(String keyword);
}
