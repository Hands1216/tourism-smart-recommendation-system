package com.tourism.ai.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tourism.ai.config.LLMConfig;
import com.tourism.ai.dto.ChatRequest;
import com.tourism.ai.dto.ChatResponse;
import com.tourism.ai.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大语言模型服务实现
 * 支持 DeepSeek API
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMServiceImpl implements LLMService {

    private final LLMConfig config;
    private WebClient webClient;

    @Override
    public ChatResponse chat(ChatRequest request) {
        if (webClient == null) {
            initWebClient();
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            // 添加系统提示词
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", request.getSystemPrompt());
                messages.add(systemMsg);
            } else {
                // 默认系统提示词
                messages.add(createSystemMessage());
            }

            // 添加历史消息
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                for (ChatRequest.ChatMessage msg : request.getHistory()) {
                    Map<String, String> historyMsg = new HashMap<>();
                    historyMsg.put("role", msg.getRole());
                    historyMsg.put("content", msg.getContent());
                    messages.add(historyMsg);
                }
            }

            // 添加当前消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", request.getTemperature() != null ? request.getTemperature() : config.getTemperature());
            requestBody.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : config.getMaxTokens());

            log.debug("发送请求到 DeepSeek API: {}", JSON.toJSONString(requestBody));

            // 发送请求到 DeepSeek API (OpenAI 兼容格式)
            // 超时设置为 120 秒，以适应较长的 AI 响应
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            log.debug("DeepSeek API 响应: {}", response);

            // 解析响应
            return parseResponse(response);

        } catch (Exception e) {
            log.error("调用大模型失败", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setContent("抱歉，我遇到了一些问题，请稍后再试。");
            return errorResponse;
        }
    }

    @Override
    public String analyzeIntent(String userInput) {
        ChatRequest request = new ChatRequest();
        request.setMessage(userInput);
        request.setSystemPrompt(getIntentAnalysisPrompt());

        ChatResponse response = chat(request);
        return response.getIntent();
    }

    @Override
    public String generateStrategy(String destination, Integer days, Double budget, String interests) {
        String prompt = buildStrategyPrompt(destination, days, budget, interests);

        ChatRequest request = new ChatRequest();
        request.setMessage(prompt);
        request.setSystemPrompt(getStrategySystemPrompt());

        ChatResponse response = chat(request);
        return response.getContent();
    }

    @Override
    public String generateRoutePlan(String destination, Integer days, String preferences) {
        String prompt = buildRoutePlanPrompt(destination, days, preferences);

        ChatRequest request = new ChatRequest();
        request.setMessage(prompt);
        request.setSystemPrompt(getRoutePlanSystemPrompt());

        ChatResponse response = chat(request);
        return response.getContent();
    }

    /**
     * 初始化WebClient
     */
    private void initWebClient() {
        webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }

    /**
     * 解析响应 - 支持 DeepSeek/OpenAI 兼容格式
     */
    private ChatResponse parseResponse(String response) {
        try {
            JSONObject resultJson = JSON.parseObject(response);
            ChatResponse chatResponse = new ChatResponse();

            // DeepSeek/OpenAI 兼容格式解析
            // 响应格式: { "choices": [{ "message": { "content": "..." } }], "usage": {...} }
            if (resultJson.containsKey("choices")) {
                var choices = resultJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    var firstChoice = choices.getJSONObject(0);
                    var message = firstChoice.getJSONObject("message");
                    if (message != null) {
                        chatResponse.setContent(message.getString("content"));
                    }
                }
                // 解析 token 使用量
                var usage = resultJson.getJSONObject("usage");
                if (usage != null) {
                    chatResponse.setUsage(usage.getInteger("total_tokens"));
                }
            }
            // 兼容通义千问格式
            else if (resultJson.containsKey("output")) {
                var output = resultJson.getJSONObject("output");
                if (output != null) {
                    chatResponse.setContent(output.getString("text"));
                }
            }

            return chatResponse;
        } catch (Exception e) {
            log.error("解析响应失败: {}", response, e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setContent("解析响应失败");
            return errorResponse;
        }
    }

    /**
     * 创建系统消息
     */
    private Map<String, String> createSystemMessage() {
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", getTourismSystemPrompt());
        return systemMsg;
    }

    /**
     * 旅游助手系统提示词
     */
    private String getTourismSystemPrompt() {
        return """
                你是一个专业的旅游助手，名叫"小游"。
                你的任务是帮助用户规划旅行、推荐景点、提供旅游建议。

                请遵循以下原则：
                1. 友好热情，以专业且平易近人的方式回答
                2. 提供准确、实用的旅游信息
                3. 根据用户需求提供个性化推荐
                4. 当信息不足时，主动询问细节
                5. 回答简洁明了，避免过长

                当用户询问景点推荐时，请考虑：
                - 用户的目的地
                - 停留天数
                - 预算范围
                - 兴趣偏好
                - 同行人员类型
                """;
    }

    /**
     * 意图分析提示词
     */
    private String getIntentAnalysisPrompt() {
        return """
                分析用户输入的旅游意图，提取以下信息：
                1. 意图类型：推荐景点/规划行程/询问信息/其他
                2. 目的地
                3. 天数
                4. 预算
                5. 兴趣标签
                6. 同行人员

                请以JSON格式返回分析结果。
                """;
    }

    /**
     * 攻略生成系统提示词
     */
    private String getStrategySystemPrompt() {
        return """
                你是一个专业的旅游攻略撰写专家。
                请根据用户提供的信息，生成一份详细、实用的旅游攻略。

                攻略应包含：
                1. 目的地简介
                2. 最佳旅行时间
                3. 推荐景点及详细介绍
                4. 美食推荐
                5. 住宿建议
                6. 交通指南
                7. 注意事项

                请使用生动有趣的语言，让攻略既有信息量又具可读性。
                """;
    }

    /**
     * 路线规划系统提示词
     */
    private String getRoutePlanSystemPrompt() {
        return """
                你是一个专业的旅游路线规划师。
                请根据用户需求，生成一份详细的每日行程规划。

                行程规划要求：
                1. 考虑景点地理位置，合理安排路线
                2. 每日行程不宜过满，预留休息时间
                3. 包含用餐时间安排
                4. 注明交通方式和时间
                5. 提供备选方案

                请以JSON格式返回路线规划结果。
                """;
    }

    /**
     * 构建攻略生成提示
     */
    private String buildStrategyPrompt(String destination, Integer days, Double budget, String interests) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为").append(destination).append("生成一份").append(days).append("天的旅游攻略。\n");
        if (budget != null) {
            prompt.append("预算约为：").append(budget).append("元。\n");
        }
        if (interests != null && !interests.isEmpty()) {
            prompt.append("用户兴趣：").append(interests).append("。\n");
        }
        return prompt.toString();
    }

    /**
     * 构建路线规划提示
     */
    private String buildRoutePlanPrompt(String destination, Integer days, String preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为").append(destination).append("规划一份").append(days).append("天的详细行程。\n");
        if (preferences != null && !preferences.isEmpty()) {
            prompt.append("用户偏好：").append(preferences).append("。\n");
        }
        return prompt.toString();
    }
}
