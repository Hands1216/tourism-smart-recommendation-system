package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.model.entity.Strategy;
import com.tourism.model.vo.StrategyVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 旅游攻略Mapper
 *
 * @author 韩东升
 */
public interface StrategyMapper extends BaseMapper<Strategy> {

    /**
     * 分页查询攻略列表（前台，仅审核通过且上架）
     */
    IPage<StrategyVO> selectStrategyPage(Page<?> page, @Param("destination") String destination,
                                         @Param("keyword") String keyword, @Param("tag") String tag,
                                         @Param("orderBy") String orderBy, @Param("userId") Long userId,
                                         @Param("minBudget") BigDecimal minBudget, @Param("maxBudget") BigDecimal maxBudget,
                                         @Param("minDays") Integer minDays, @Param("maxDays") Integer maxDays,
                                         @Param("season") String season);

    /**
     * 查询攻略详情
     */
    StrategyVO selectStrategyDetail(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 管理后台分页查询（所有状态）
     */
    IPage<StrategyVO> selectAdminStrategyPage(Page<?> page, @Param("title") String title,
                                              @Param("auditStatus") Integer auditStatus,
                                              @Param("destination") String destination);

    /**
     * 用户自己的攻略列表（含草稿）
     */
    IPage<StrategyVO> selectUserStrategyPage(Page<?> page, @Param("userId") Long userId);

    /**
     * 草稿列表
     */
    IPage<StrategyVO> selectDraftPage(Page<?> page, @Param("userId") Long userId);

    /**
     * 相关推荐
     */
    List<StrategyVO> selectRelatedStrategies(@Param("id") Long id, @Param("destination") String destination,
                                             @Param("tags") String tags, @Param("limit") Integer limit);
}
