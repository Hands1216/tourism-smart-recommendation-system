package com.tourism.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 智能体基类
 * 提供通用的LLM调用能力
 *
 */
@Slf4j
public abstract class BaseTravelAgent implements TravelAgent {

    @Value("${llm.api-key:}")
    protected String apiKey;

    @Value("${llm.model:deepseek-chat}")
    protected String model;

    @Value("${llm.base-url:https://api.deepseek.com}")
    protected String baseUrl;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected RestTemplate restTemplate;

    protected static final int TIMEOUT_MS = 60000;

    protected RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(TIMEOUT_MS);
            factory.setReadTimeout(TIMEOUT_MS);
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    /**
     * 调用LLM API
     */
    protected String callLLM(String userPrompt, String systemPrompt) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody), headers);

            String url = baseUrl + "/chat/completions";
            ResponseEntity<String> response = getRestTemplate().exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return objectMapper.readTree(response.getBody())
                        .path("choices").path(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("[{}] LLM调用失败: {}", getName(), e.getMessage());
        }
        return null;
    }

    /**
     * 关键词匹配计算置信度
     */
    protected double calculateConfidence(String query, List<String> keywords) {
        if (query == null || keywords == null || keywords.isEmpty()) {
            return 0.0;
        }

        String lowerQuery = query.toLowerCase();
        int matchCount = 0;
        for (String keyword : keywords) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                matchCount++;
            }
        }

        return Math.min(1.0, matchCount * 0.3);
    }
}
