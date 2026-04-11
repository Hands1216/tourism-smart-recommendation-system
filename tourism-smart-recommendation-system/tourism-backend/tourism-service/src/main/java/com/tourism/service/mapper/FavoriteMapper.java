package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.model.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户收藏Mapper
 *
 * @author 韩东升
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 查询收藏记录（包括已删除的）- 绕过 @TableLogic
     */
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND item_type = #{itemType} AND item_id = #{itemId} LIMIT 1")
    Favorite selectByUserAndItem(@Param("userId") Long userId, @Param("itemType") String itemType, @Param("itemId") Long itemId);

    /**
     * 恢复已删除的收藏记录
     */
    @Update("UPDATE favorite SET deleted = 0, create_time = #{createTime} WHERE id = #{id}")
    int restoreFavorite(@Param("id") Long id, @Param("createTime") LocalDateTime createTime);
}
