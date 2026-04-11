package com.tourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tourism.model.dto.CommentCreateDTO;
import com.tourism.model.entity.StrategyComment;
import com.tourism.model.vo.CommentVO;

/**
 * 攻略评论服务接口
 *
 * @author 韩东升
 */
public interface StrategyCommentService extends IService<StrategyComment> {

    /**
     * 分页查询评论列表
     */
    IPage<CommentVO> getCommentPage(Page<?> page, Long strategyId);

    /**
     * 添加评论
     */
    Long addComment(Long userId, Long strategyId, CommentCreateDTO dto);

    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 点赞评论
     */
    void likeComment(Long commentId);
}
