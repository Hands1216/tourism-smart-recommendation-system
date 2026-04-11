package com.tourism.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论创建DTO
 *
 * @author 韩东升
 */
@Data
public class CommentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父评论ID（0为一级评论）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;
}
