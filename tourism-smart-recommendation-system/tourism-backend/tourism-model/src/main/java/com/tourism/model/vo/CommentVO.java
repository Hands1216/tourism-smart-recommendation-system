package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 *
 * @author 韩东升
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long strategyId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    private List<CommentVO> replies;
}
