package com.tourism.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.model.dto.AttractionQueryDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.vo.AttractionStatsVO;
import com.tourism.model.vo.AttractionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点Mapper
 *
 * @author 韩东升
 */
public interface AttractionMapper extends BaseMapper<Attraction> {

    /**
     * 分页查询景点列表（带分类名称）
     */
    IPage<AttractionVO> selectAttractionPage(Page<?> page, @Param("keyword") String keyword,
                                              @Param("categoryId") Long categoryId,
                                              @Param("province") String province,
                                              @Param("userId") Long userId);

    /**
     * 高级筛选分页查询
     */
    IPage<AttractionVO> selectAttractionPageAdvanced(Page<?> page, @Param("query") AttractionQueryDTO query, @Param("userId") Long userId);

    /**
     * 查询景点详情（带收藏状态）
     */
    AttractionVO selectAttractionDetail(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 获取相似景点（基于分类、标签、城市）
     */
    List<AttractionVO> selectSimilarAttractions(@Param("attractionId") Long attractionId,
                                                 @Param("categoryId") Long categoryId,
                                                 @Param("city") String city,
                                                 @Param("limit") Integer limit);

    /**
     * 更新浏览量
     */
    void incrementViewCount(@Param("id") Long id);

    /**
     * 更新收藏量
     */
    void updateFavoriteCount(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 获取热点景点统计
     */
    List<AttractionStatsVO> selectHotAttractionStats(@Param("limit") Integer limit, @Param("days") Integer days);

    /**
     * 获取高增长潜力景点
     */
    List<AttractionStatsVO> selectGrowthPotentialAttractions(@Param("limit") Integer limit, @Param("days") Integer days);

    /**
     * 获取所有省份列表
     */
    List<String> selectAllProvinces();

    /**
     * 获取所有景区等级列表
     */
    List<String> selectAllScenicLevels();

    /**
     * 获取所有场景分类列表
     */
    List<String> selectAllSceneTypes();

    /**
     * 获取所有建议游玩时长列表
     */
    List<String> selectAllDurations();

    /**
     * 根据省份获取城市列表
     */
    List<String> selectCitiesByProvince(@Param("province") String province);

    /**
     * 根据城市获取区县列表
     */
    List<String> selectDistrictsByCity(@Param("city") String city);

    /**
     * 获取所有省份及其对应城市的映射关系
     */
    List<java.util.Map<String, String>> selectProvinceCityMapping();
}
