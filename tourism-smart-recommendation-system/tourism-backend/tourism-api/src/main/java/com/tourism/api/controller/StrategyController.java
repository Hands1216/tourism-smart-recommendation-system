package com.tourism.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.CommentCreateDTO;
import com.tourism.model.dto.StrategyCreateDTO;
import com.tourism.model.vo.CommentVO;
import com.tourism.model.vo.StrategyVO;
import com.tourism.service.StrategyCommentService;
import com.tourism.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 攻略控制器
 *
 */
@RestController
@RequestMapping("/strategy")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final StrategyCommentService commentService;

    /**
     * 获取攻略列表
     */
    @GetMapping("/list")
    public Result<IPage<StrategyVO>> list(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) Integer minDays,
            @RequestParam(required = false) Integer maxDays,
            @RequestParam(required = false) String season,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        Page<StrategyVO> pageParam = new Page<>(page, size);
        IPage<StrategyVO> result = strategyService.getStrategyPage(pageParam, destination, keyword, tag, orderBy, userId,
                minBudget, maxBudget, minDays, maxDays, season);
        return Result.success(result);
    }

    /**
     * 获取攻略详情
     */
    @GetMapping("/{id}")
    public Result<StrategyVO> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        StrategyVO detail = strategyService.getStrategyDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 创建攻略
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody StrategyCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Long id = strategyService.createStrategy(userId, dto);
        return Result.success(id);
    }

    /**
     * 更新攻略
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody StrategyCreateDTO dto) {
        Long userId = UserContext.getUserId();
        strategyService.updateStrategy(userId, id, dto);
        return Result.success();
    }

    /**
     * 删除攻略
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        strategyService.deleteStrategy(userId, id);
        return Result.success();
    }

    /**
     * AI生成攻略
     */
    @PostMapping("/ai-generate")
    public Result<String> aiGenerate(
            @RequestParam String destination,
            @RequestParam Integer days,
            @RequestParam(required = false) Double budget,
            @RequestParam(required = false) String interests) {
        String content = strategyService.generateAiStrategy(destination, days, budget, interests);
        return Result.success(content);
    }

    /**
     * 点赞攻略
     */
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        strategyService.likeStrategy(userId, id);
        return Result.success();
    }

    /**
     * 收藏攻略
     */
    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        strategyService.favoriteStrategy(userId, id);
        return Result.success();
    }

    /**
     * 获取用户自己的攻略列表
     */
    @GetMapping("/my")
    public Result<IPage<StrategyVO>> myStrategies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        Page<StrategyVO> pageParam = new Page<>(page, size);
        IPage<StrategyVO> result = strategyService.getUserStrategyPage(pageParam, userId);
        return Result.success(result);
    }

    /**
     * 自动保存草稿
     */
    @PostMapping("/auto-save")
    public Result<Long> autoSave(@RequestBody StrategyCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Long id = strategyService.autoSaveStrategy(userId, dto);
        return Result.success(id);
    }

    /**
     * 获取草稿列表
     */
    @GetMapping("/drafts")
    public Result<IPage<StrategyVO>> drafts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        Page<StrategyVO> pageParam = new Page<>(page, size);
        IPage<StrategyVO> result = strategyService.getDraftPage(pageParam, userId);
        return Result.success(result);
    }

    /**
     * 获取相关推荐
     */
    @GetMapping("/{id}/related")
    public Result<List<StrategyVO>> related(@PathVariable Long id,
            @RequestParam(defaultValue = "6") Integer limit) {
        List<StrategyVO> list = strategyService.getRelatedStrategies(id, limit);
        return Result.success(list);
    }

    /**
     * 获取热门搜索词
     */
    @GetMapping("/hot-keywords")
    public Result<List<String>> hotKeywords() {
        List<String> keywords = strategyService.getHotKeywords();
        return Result.success(keywords);
    }

    /**
     * 获取攻略评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<IPage<CommentVO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<CommentVO> pageParam = new Page<>(page, size);
        IPage<CommentVO> result = commentService.getCommentPage(pageParam, id);
        return Result.success(result);
    }

    /**
     * 添加评论
     */
    @PostMapping("/{id}/comments")
    public Result<Long> addComment(@PathVariable Long id, @RequestBody CommentCreateDTO dto) {
        Long userId = UserContext.getUserId();
        Long commentId = commentService.addComment(userId, id, dto);
        return Result.success(commentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        commentService.deleteComment(userId, commentId);
        return Result.success();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/comments/{commentId}/like")
    public Result<Void> likeComment(@PathVariable Long id, @PathVariable Long commentId) {
        commentService.likeComment(commentId);
        return Result.success();
    }
}

