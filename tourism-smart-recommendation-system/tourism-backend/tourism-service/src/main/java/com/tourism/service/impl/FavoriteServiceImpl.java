package com.tourism.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.Favorite;
import com.tourism.model.entity.Strategy;
import com.tourism.model.entity.User;
import com.tourism.model.vo.AttractionVO;
import com.tourism.model.vo.FavoriteVO;
import com.tourism.model.vo.StrategyVO;
import com.tourism.service.FavoriteService;
import com.tourism.service.mapper.AttractionMapper;
import com.tourism.service.mapper.FavoriteMapper;
import com.tourism.service.mapper.StrategyMapper;
import com.tourism.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏服务实现
 *
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final AttractionMapper attractionMapper;
    private final StrategyMapper strategyMapper;
    private final UserMapper userMapper;

    @Override
    public List<FavoriteVO> getUserFavorites(Long userId, String itemType) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        if (StrUtil.isNotBlank(itemType)) {
            wrapper.eq(Favorite::getItemType, itemType);
        }
        wrapper.orderByDesc(Favorite::getCreateTime);

        List<Favorite> favorites = list(wrapper);
        List<FavoriteVO> result = new ArrayList<>();

        for (Favorite favorite : favorites) {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(favorite.getId());
            vo.setItemType(favorite.getItemType());
            vo.setItemId(favorite.getItemId());
            vo.setCreateTime(favorite.getCreateTime());

            if ("attraction".equals(favorite.getItemType())) {
                // 获取景点信息
                Attraction attraction = attractionMapper.selectById(favorite.getItemId());
                if (attraction != null && attraction.getDeleted() == 0) {
                    vo.setAttraction(convertToAttractionVO(attraction));
                    result.add(vo);
                }
            } else if ("strategy".equals(favorite.getItemType())) {
                // 获取攻略信息
                Strategy strategy = strategyMapper.selectById(favorite.getItemId());
                if (strategy != null && strategy.getDeleted() == 0) {
                    vo.setStrategy(convertToStrategyVO(strategy));
                    result.add(vo);
                }
            }
        }

        return result;
    }

    @Override
    public boolean isFavorited(Long userId, String itemType, Long itemId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getItemType, itemType)
               .eq(Favorite::getItemId, itemId);
        return count(wrapper) > 0;
    }

    @Override
    public void addFavorite(Long userId, String itemType, Long itemId) {
        // 检查是否已收藏
        if (isFavorited(userId, itemType, itemId)) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemType(itemType);
        favorite.setItemId(itemId);
        save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, String itemType, Long itemId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getItemType, itemType)
               .eq(Favorite::getItemId, itemId);
        remove(wrapper);
    }

    /**
     * 转换为景点VO（简化版，只包含列表展示需要的字段）
     */
    private AttractionVO convertToAttractionVO(Attraction attraction) {
        AttractionVO vo = new AttractionVO();
        vo.setId(attraction.getId());
        vo.setName(attraction.getName());
        vo.setCategoryId(attraction.getCategoryId());
        vo.setDescription(attraction.getDescription());
        vo.setAddress(attraction.getAddress());
        vo.setCity(attraction.getCity());
        vo.setProvince(attraction.getProvince());
        vo.setScenicLevel(attraction.getScenicLevel());
        vo.setOpenTime(attraction.getOpenTime());
        vo.setTicketPrice(attraction.getTicketPrice());
        vo.setChargeType(attraction.getChargeType());
        vo.setRating(attraction.getRating());
        vo.setViewCount(attraction.getViewCount());
        vo.setFavoriteCount(attraction.getFavoriteCount());
        vo.setIsFavorited(true); // 既然在收藏列表中，肯定是已收藏
        vo.setCreateTime(attraction.getCreateTime());

        // 解析图片JSON
        if (StrUtil.isNotBlank(attraction.getImages())) {
            try {
                vo.setImages(com.alibaba.fastjson2.JSON.parseArray(attraction.getImages(), String.class));
            } catch (Exception e) {
                vo.setImages(List.of());
            }
        }

        // 解析标签JSON
        if (StrUtil.isNotBlank(attraction.getTags())) {
            try {
                vo.setTags(com.alibaba.fastjson2.JSON.parseArray(attraction.getTags(), String.class));
            } catch (Exception e) {
                vo.setTags(List.of());
            }
        }

        return vo;
    }

    /**
     * 转换为攻略VO（简化版）
     */
    private StrategyVO convertToStrategyVO(Strategy strategy) {
        StrategyVO vo = new StrategyVO();
        vo.setId(strategy.getId());
        vo.setUserId(strategy.getUserId());
        vo.setTitle(strategy.getTitle());
        vo.setCoverImage(strategy.getCoverImage());
        vo.setDestination(strategy.getDestination());
        vo.setDays(strategy.getDays());
        vo.setBudget(strategy.getBudget());
        vo.setSeason(strategy.getSeason());
        vo.setSummary(strategy.getSummary());
        vo.setImages(strategy.getImages());
        vo.setTags(strategy.getTags());
        vo.setViewCount(strategy.getViewCount());
        vo.setLikeCount(strategy.getLikeCount());
        vo.setFavoriteCount(strategy.getFavoriteCount());
        vo.setCommentCount(strategy.getCommentCount());
        vo.setIsFavorited(true); // 既然在收藏列表中，肯定是已收藏
        vo.setIsAiGenerated(strategy.getIsAiGenerated() == 1);
        vo.setAuditStatus(strategy.getAuditStatus());
        vo.setStatus(strategy.getStatus());
        vo.setCreateTime(strategy.getCreateTime());

        // 获取作者信息
        User author = userMapper.selectById(strategy.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }

        return vo;
    }
}
