package com.tourism.api.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.ChatMessageDTO;
import com.tourism.model.vo.ChatResponseVO;
import com.tourism.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天控制器
 *
 * @author 韩东升
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送聊天消息
     */
    @PostMapping("/send")
    public Result<ChatResponseVO> sendMessage(@RequestBody ChatRequest request) {
        Long userId = UserContext.getUserId();
        ChatResponseVO response = chatService.sendMessage(userId, request.getSessionId(),
                request.getMessage(), request.getHistory());
        return Result.success(response);
    }

    /**
     * 分析用户意图
     */
    @PostMapping("/analyze")
    public Result<String> analyzeIntent(@RequestBody String userInput) {
        String intent = chatService.analyzeIntent(userInput);
        return Result.success(intent);
    }

    /**
     * 聊天请求DTO
     */
    @Data
    public static class ChatRequest {
        @JsonProperty("sessionId")
        private String sessionId;

        @JsonProperty("message")
        private String message;

        @JsonProperty("history")
        private List<ChatMessageDTO> history;
    }
}
