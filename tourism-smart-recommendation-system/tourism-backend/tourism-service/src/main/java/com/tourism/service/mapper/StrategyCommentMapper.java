package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.model.entity.StrategyComment;
import com.tourism.model.vo.CommentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 攻略评论Mapper
 *
 */
public interface StrategyCommentMapper extends BaseMapper<StrategyComment> {

    IPage<CommentVO> selectCommentPage(Page<?> page, @Param("strategyId") Long strategyId);

    List<CommentVO> selectReplies(@Param("parentId") Long parentId);
}
