package com.tourism.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体
 *
 * @author 韩东升
 */
@Data
@TableName("attraction")
public class Attraction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 景点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 景点名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 景点描述
     */
    private String description;

    /**
     * 图片URL列表（JSON格式）
     */
    private String images;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 城市
     */
    private String city;

    /**
     * 省份
     */
    private String province;

    /**
     * 区县
     */
    private String district;

    /**
     * 景区等级：5A/4A/3A/世界文化遗产/国家级自然保护区等
     */
    private String scenicLevel;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 收费类型：0-免费，1-收费
     */
    private Integer chargeType;

    /**
     * 建议游玩时长：如2-3小时、1天等
     */
    private String suggestedDuration;

    /**
     * 最佳游玩月份：如3-5月,9-11月
     */
    private String bestMonths;

    /**
     * 场景分类：独自出行/情侣出行/朋友出行/家庭出行，多个用逗号分隔
     */
    private String sceneType;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 标签列表（JSON格式）
     */
    private String tags;

    /**
     * 特色标签（用于推荐算法，JSON格式）
     */
    private String features;

    /**
     * 官方咨询电话
     */
    private String contactPhone;

    /**
     * 官方网站链接
     */
    private String officialWebsite;

    /**
     * 避坑提示/注意事项（JSON数组格式）
     */
    private String tips;

    /**
     * 审核状态：0-待审核，1-已通过，2-已驳回
     */
    private Integer auditStatus;

    /**
     * 季节性状态：0-正常开放，1-季节性关闭，2-临时关闭
     */
    private Integer seasonalStatus;

    /**
     * 季节性说明：如冬季暂停开放
     */
    private String seasonalNote;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
