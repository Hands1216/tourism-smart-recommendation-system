package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.model.entity.AttractionRating;
import com.tourism.model.vo.AttractionRatingVO;
import com.tourism.model.vo.RatingStatsVO;
import org.apache.ibatis.annotations.Param;

/**
 * 景点评分Mapper
 *
 * @author 韩东升
 */
public interface AttractionRatingMapper extends BaseMapper<AttractionRating> {

    /**
     * 获取景点评分统计
     */
    RatingStatsVO selectRatingStats(@Param("attractionId") Long attractionId);

    /**
     * 更新景点平均评分
     */
    void updateAttractionRating(@Param("attractionId") Long attractionId);

    /**
     * 分页获取景点评论列表（仅有评论内容的）
     */
    IPage<AttractionRatingVO> selectRatingPage(Page<AttractionRatingVO> page, @Param("attractionId") Long attractionId);
}
