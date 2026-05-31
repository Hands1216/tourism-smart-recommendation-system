package com.tourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tourism.model.dto.AttractionAdminDTO;
import com.tourism.model.dto.AttractionQueryDTO;
import com.tourism.model.dto.AttractionRatingDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.vo.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 景点服务接口
 *
 * @author 韩东升
 */
public interface AttractionService extends IService<Attraction> {

    /**
     * 分页查询景点列表
     */
    IPage<AttractionVO> getAttractionPage(Page<?> page, String keyword, Long categoryId, String province, Long userId);

    /**
     * 高级筛选分页查询
     */
    IPage<AttractionVO> getAttractionPageAdvanced(AttractionQueryDTO queryDTO, Long userId);

    /**
     * 获取景点详情
     */
    AttractionVO getAttractionDetail(Long id, Long userId);

    /**
     * 获取景点详情（增强版，包含评分、相似景点、相关攻略）
     */
    AttractionVO getAttractionDetailEnhanced(Long id, Long userId);

    /**
     * 获取相似景点
     */
    List<AttractionVO> getSimilarAttractions(Long attractionId, Integer limit);

    /**
     * 获取包含该景点的攻略
     */
    List<StrategyVO> getRelatedStrategies(Long attractionId, Integer limit);

    /**
     * 评分景点
     */
    void rateAttraction(Long userId, AttractionRatingDTO ratingDTO);

    /**
     * 获取用户对景点的评分
     */
    AttractionRatingVO getUserRating(Long userId, Long attractionId);

    /**
     * 获取景点评论列表（分页，仅有评论内容的）
     */
    IPage<AttractionRatingVO> getAttractionRatings(Long attractionId, Page<AttractionRatingVO> page);

    /**
     * 删除景点评论
     */
    void deleteAttractionRating(Long userId, Long attractionId, Long ratingId);

    /**
     * 添加景点评论（纯文字评论，不带评分）
     */
    void addAttractionComment(Long userId, Long attractionId, String comment);

    /**
     * 标记去过
     */
    void markVisited(Long userId, Long attractionId, LocalDate visitDate, String note);

    /**
     * 取消去过标记
     */
    void unmarkVisited(Long userId, Long attractionId);

    /**
     * 获取用户足迹列表
     */
    List<UserFootprintVO> getUserFootprints(Long userId);

    /**
     * 获取筛选选项（用于前端下拉框）
     */
    AttractionFilterVO getFilterOptions();

    /**
     * 收藏景点
     */
    void favoriteAttraction(Long userId, Long attractionId);

    /**
     * 取消收藏景点
     */
    void unfavoriteAttraction(Long userId, Long attractionId);

    /**
     * 获取用户收藏的景点
     */
    List<Attraction> getUserFavorites(Long userId);

    /**
     * 根据城市获取推荐景点
     */
    List<Attraction> getRecommendByCity(String city, Integer limit);

    /**
     * 根据用户偏好获取推荐景点
     */
    List<Attraction> getRecommendByPreferences(String preferences, Integer limit);

    /**
     * 管理员：创建景点
     */
    Long createAttraction(AttractionAdminDTO dto);

    /**
     * 管理员：更新景点
     */
    void updateAttraction(AttractionAdminDTO dto);

    /**
     * 管理员：设置季节性状态
     */
    void setSeasonalStatus(Long id, Integer status, String note);

    /**
     * 管理员：获取热点景点统计
     */
    List<AttractionStatsVO> getHotAttractionStats(Integer limit, Integer days);

    /**
     * 管理员：获取高增长潜力景点
     */
    List<AttractionStatsVO> getGrowthPotentialAttractions(Integer limit, Integer days);

    /**
     * 根据省份获取城市列表
     */
    List<String> getCitiesByProvince(String province);

    /**
     * 根据城市获取区县列表
     */
    List<String> getDistrictsByCity(String city);
}
