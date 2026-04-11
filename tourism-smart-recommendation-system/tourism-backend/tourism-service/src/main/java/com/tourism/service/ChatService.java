package com.tourism.service;

import com.tourism.model.dto.ChatMessageDTO;
import com.tourism.model.vo.ChatResponseVO;

import java.util.List;

/**
 * AI聊天服务接口
 *
 * @author 韩东升
 */
public interface ChatService {

    /**
     * 发送聊天消息
     */
    ChatResponseVO sendMessage(Long userId, String sessionId, String message, List<ChatMessageDTO> history);

    /**
     * 分析用户意图
     */
    String analyzeIntent(String userInput);

    /**
     * 生成聊天标题
     */
    String generateChatTitle(String firstMessage);
}
