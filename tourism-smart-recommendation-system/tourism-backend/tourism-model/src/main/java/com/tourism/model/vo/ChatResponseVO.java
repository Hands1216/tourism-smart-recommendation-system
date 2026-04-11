package com.tourism.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聊天响应VO
 *
 * @author 韩东升
 */
@Data
public class ChatResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应内容
     */
    private String content;

    /**
     * 意图分析结果
     */
    private String intent;

    /**
     * 建议操作
     */
    private List<String> suggestions;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 处理该请求的智能体名称（多智能体模式）
     */
    private String agentName;
}
