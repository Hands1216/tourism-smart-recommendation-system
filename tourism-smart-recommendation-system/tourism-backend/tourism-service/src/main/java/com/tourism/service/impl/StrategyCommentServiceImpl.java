package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.model.dto.CommentCreateDTO;
import com.tourism.model.entity.Strategy;
import com.tourism.model.entity.StrategyComment;
import com.tourism.model.vo.CommentVO;
import com.tourism.service.StrategyCommentService;
import com.tourism.service.StrategyService;
import com.tourism.service.SensitiveWordService;
import com.tourism.service.mapper.StrategyCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 攻略评论服务实现
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyCommentServiceImpl extends ServiceImpl<StrategyCommentMapper, StrategyComment>
        implements StrategyCommentService {

    private final StrategyCommentMapper commentMapper;
    private final StrategyService strategyService;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public IPage<CommentVO> getCommentPage(Page<?> page, Long strategyId) {
        IPage<CommentVO> commentPage = commentMapper.selectCommentPage(page, strategyId);

        // 查询每个一级评论的子评论
        commentPage.getRecords().forEach(comment -> {
            comment.setReplies(commentMapper.selectReplies(comment.getId()));
        });

        return commentPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long userId, Long strategyId, CommentCreateDTO dto) {
        // 敏感词检查
        Set<String> sensitiveWords = sensitiveWordService.findSensitiveWords(dto.getContent());
        if (!sensitiveWords.isEmpty()) {
            throw new RuntimeException("评论包含敏感词：" + String.join("、", sensitiveWords));
        }

        StrategyComment comment = new StrategyComment();
        comment.setStrategyId(strategyId);
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);

        save(comment);

        // 更新攻略评论数
        Strategy strategy = strategyService.getById(strategyId);
        if (strategy != null) {
            strategy.setCommentCount((strategy.getCommentCount() == null ? 0 : strategy.getCommentCount()) + 1);
            strategyService.updateById(strategy);
        }

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        StrategyComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        // 统计要删除的评论数（本条 + 子评论）
        List<StrategyComment> children = list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StrategyComment>()
                        .eq(StrategyComment::getParentId, commentId)
        );
        int deleteCount = 1 + children.size();

        // 删除子评论
        if (!children.isEmpty()) {
            List<Long> childIds = children.stream().map(StrategyComment::getId).toList();
            removeByIds(childIds);
        }

        // 删除本条评论
        removeById(commentId);

        // 更新攻略评论数
        Strategy strategy = strategyService.getById(comment.getStrategyId());
        if (strategy != null) {
            strategy.setCommentCount(Math.max(0, (strategy.getCommentCount() == null ? 0 : strategy.getCommentCount()) - deleteCount));
            strategyService.updateById(strategy);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        StrategyComment comment = getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        comment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
        updateById(comment);
    }
}
