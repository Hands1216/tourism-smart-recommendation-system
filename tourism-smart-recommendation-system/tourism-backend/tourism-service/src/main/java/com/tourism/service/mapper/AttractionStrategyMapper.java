package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.model.entity.AttractionStrategy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点攻略关联Mapper
 *
 */
public interface AttractionStrategyMapper extends BaseMapper<AttractionStrategy> {

    /**
     * 获取景点关联的攻略ID列表
     */
    List<Long> selectStrategyIdsByAttractionId(@Param("attractionId") Long attractionId);

    /**
     * 批量插入景点攻略关联
     */
    void batchInsert(@Param("list") List<AttractionStrategy> list);
}
