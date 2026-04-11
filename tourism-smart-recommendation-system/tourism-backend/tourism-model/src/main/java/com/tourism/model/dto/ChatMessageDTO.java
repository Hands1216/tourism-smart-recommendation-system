package com.tourism.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息DTO
 *
 * @author 韩东升
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色：user/assistant/system
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
