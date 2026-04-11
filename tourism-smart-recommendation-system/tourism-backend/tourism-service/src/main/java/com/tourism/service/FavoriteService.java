package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tourism.model.entity.Favorite;
import com.tourism.model.vo.FavoriteVO;

import java.util.List;

/**
 * 收藏服务接口
 *
 * @author 韩东升
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @param itemType 收藏类型（可选）：attraction/strategy，为空则返回全部
     * @return 收藏列表
     */
    List<FavoriteVO> getUserFavorites(Long userId, String itemType);

    /**
     * 检查是否已收藏
     *
     * @param userId 用户ID
     * @param itemType 收藏类型
     * @param itemId 项目ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, String itemType, Long itemId);

    /**
     * 添加收藏
     *
     * @param userId 用户ID
     * @param itemType 收藏类型
     * @param itemId 项目ID
     */
    void addFavorite(Long userId, String itemType, Long itemId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param itemType 收藏类型
     * @param itemId 项目ID
     */
    void removeFavorite(Long userId, String itemType, Long itemId);
}
