package com.tourism.service.impl;

import com.tourism.ai.dto.ChatRequest;
import com.tourism.ai.dto.ChatResponse;
import com.tourism.ai.service.LLMService;
import com.tourism.model.dto.ChatMessageDTO;
import com.tourism.model.vo.ChatResponseVO;
import com.tourism.service.ChatService;
import com.tourism.service.agent.AgentOrchestrator;
import com.tourism.service.agent.AgentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI聊天服务实现
 * 集成 DeepSeek 大语言模型 + 多智能体架构
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final LLMService llmService;
    private final AgentOrchestrator agentOrchestrator;

    /**
     * 是否启用多智能体模式
     */
    @Value("${chat.multi-agent.enabled:true}")
    private boolean multiAgentEnabled;

    @Override
    public ChatResponseVO sendMessage(Long userId, String sessionId, String message, List<ChatMessageDTO> history) {
        ChatResponseVO response = new ChatResponseVO();
        response.setSessionId(sessionId != null ? sessionId : UUID.randomUUID().toString());

        try {
            // 分析意图
            String intent = analyzeIntent(message);
            response.setIntent(intent);

            // 根据配置选择处理方式
            if (multiAgentEnabled) {
                // 使用多智能体架构处理
                response = processWithAgents(userId, message, history, response);
            } else {
                // 使用传统单一LLM处理
                response = processWithLLM(userId, message, history, response);
            }

            log.info("用户 {} 发送消息成功，会话ID: {}，使用模式: {}",
                    userId, response.getSessionId(), multiAgentEnabled ? "多智能体" : "单一LLM");

        } catch (Exception e) {
            log.error("AI聊天服务调用失败", e);
            response.setContent("抱歉，我遇到了一些问题，请稍后再试。");
        }

        return response;
    }

    /**
     * 使用多智能体架构处理消息
     */
    private ChatResponseVO processWithAgents(Long userId, String message,
                                              List<ChatMessageDTO> history, ChatResponseVO response) {
        // 构建上下文
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        // 从历史消息中提取上下文信息
        if (history != null && !history.isEmpty()) {
            extractContextFromHistory(history, context);
        }

        // 调用智能体协调器
        AgentResponse agentResponse = agentOrchestrator.process(message, context);

        if (agentResponse.isSuccess()) {
            response.setContent(agentResponse.getContent());
            response.setAgentName(agentResponse.getAgentName());

            // 使用智能体返回的建议操作
            if (agentResponse.getSuggestedActions() != null) {
                response.setSuggestions(agentResponse.getSuggestedActions());
            } else {
                response.setSuggestions(generateSuggestions(response.getIntent()));
            }
        } else {
            // 智能体处理失败，降级到传统LLM
            log.warn("智能体处理失败，降级到传统LLM: {}", agentResponse.getErrorMessage());
            return processWithLLM(userId, message, history, response);
        }

        return response;
    }

    /**
     * 使用传统单一LLM处理消息
     */
    private ChatResponseVO processWithLLM(Long userId, String message,
                                           List<ChatMessageDTO> history, ChatResponseVO response) {
        // 构建 AI 请求
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setSessionId(response.getSessionId());
        chatRequest.setMessage(message);

        // 转换历史消息格式
        if (history != null && !history.isEmpty()) {
            List<ChatRequest.ChatMessage> historyMessages = history.stream()
                    .map(dto -> {
                        ChatRequest.ChatMessage msg = new ChatRequest.ChatMessage();
                        msg.setRole(dto.getRole());
                        msg.setContent(dto.getContent());
                        return msg;
                    })
                    .collect(Collectors.toList());
            chatRequest.setHistory(historyMessages);
        }

        // 调用 AI 服务
        ChatResponse aiResponse = llmService.chat(chatRequest);

        response.setContent(aiResponse.getContent());
        response.setSuggestions(generateSuggestions(response.getIntent()));

        return response;
    }

    /**
     * 从历史消息中提取上下文信息
     */
    private void extractContextFromHistory(List<ChatMessageDTO> history, Map<String, Object> context) {
        for (ChatMessageDTO msg : history) {
            String content = msg.getContent();
            if (content == null) continue;

            // 提取目的地信息
            if (content.contains("去") || content.contains("到")) {
                // 简单的目的地提取逻辑
                String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "西安", "南京", "苏州", "厦门"};
                for (String city : cities) {
                    if (content.contains(city)) {
                        context.put("destination", city);
                        break;
                    }
                }
            }

            // 提取天数信息
            if (content.matches(".*\\d+天.*") || content.matches(".*\\d+日.*")) {
                String days = content.replaceAll(".*?(\\d+)[天日].*", "$1");
                try {
                    context.put("days", Integer.parseInt(days));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public String analyzeIntent(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return "unknown";
        }

        String lowerInput = userInput.toLowerCase();

        // 景点推荐意图
        if (lowerInput.contains("推荐") || lowerInput.contains("景点") || lowerInput.contains("去哪玩") || lowerInput.contains("好玩")) {
            return "recommend_attraction";
        }

        // 路线规划意图
        if (lowerInput.contains("路线") || lowerInput.contains("行程") || lowerInput.contains("规划") || lowerInput.contains("安排")) {
            return "route_plan";
        }

        // 美食推荐意图
        if (lowerInput.contains("吃") || lowerInput.contains("美食") || lowerInput.contains("餐厅") || lowerInput.contains("小吃")) {
            return "recommend_food";
        }

        // 住宿推荐意图
        if (lowerInput.contains("住") || lowerInput.contains("酒店") || lowerInput.contains("住宿") || lowerInput.contains("民宿")) {
            return "recommend_hotel";
        }

        // 交通查询意图
        if (lowerInput.contains("怎么去") || lowerInput.contains("交通") || lowerInput.contains("地铁") || lowerInput.contains("公交")) {
            return "query_traffic";
        }

        // 天气查询意图
        if (lowerInput.contains("天气") || lowerInput.contains("气温") || lowerInput.contains("穿什么")) {
            return "query_weather";
        }

        return "general_chat";
    }

    @Override
    public String generateChatTitle(String firstMessage) {
        if (firstMessage == null || firstMessage.isEmpty()) {
            return "新对话";
        }

        // 简单截取前15个字符作为标题
        String title = firstMessage.length() > 15 ? firstMessage.substring(0, 15) + "..." : firstMessage;
        return title;
    }

    /**
     * 根据意图生成建议操作
     */
    private List<String> generateSuggestions(String intent) {
        List<String> suggestions = new ArrayList<>();

        switch (intent) {
            case "recommend_attraction":
                suggestions.add("查看热门景点");
                suggestions.add("按城市筛选");
                suggestions.add("查看景点详情");
                break;
            case "route_plan":
                suggestions.add("生成行程规划");
                suggestions.add("查看我的行程");
                suggestions.add("修改行程偏好");
                break;
            case "recommend_food":
                suggestions.add("查看美食推荐");
                suggestions.add("按口味筛选");
                break;
            case "recommend_hotel":
                suggestions.add("查看酒店推荐");
                suggestions.add("按价格筛选");
                break;
            default:
                suggestions.add("景点推荐");
                suggestions.add("行程规划");
                suggestions.add("美食推荐");
                break;
        }

        return suggestions;
    }
}
