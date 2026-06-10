package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourism.model.entity.UserFootprint;
import com.tourism.model.vo.UserFootprintVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户足迹Mapper
 *
 */
public interface UserFootprintMapper extends BaseMapper<UserFootprint> {

    /**
     * 获取用户足迹列表（带景点信息）
     */
    List<UserFootprintVO> selectUserFootprints(@Param("userId") Long userId);
}
