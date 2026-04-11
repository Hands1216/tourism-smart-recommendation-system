package com.tourism.ai.service;

import com.tourism.ai.dto.ChatRequest;
import com.tourism.ai.dto.ChatResponse;

/**
 * 大语言模型服务接口
 *
 * @author 韩东升
 */
public interface LLMService {

    /**
     * 发送聊天消息
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 分析用户意图
     *
     * @param userInput 用户输入
     * @return 意图分析结果
     */
    String analyzeIntent(String userInput);

    /**
     * 生成旅游攻略
     *
     * @param destination 目的地
     * @param days        天数
     * @param budget      预算
     * @param interests   兴趣标签
     * @return 攻略内容
     */
    String generateStrategy(String destination, Integer days, Double budget, String interests);

    /**
     * 生成路线规划
     *
     * @param destination 目的地
     * @param days        天数
     * @param preferences 偏好
     * @return 路线规划JSON
     */
    String generateRoutePlan(String destination, Integer days, String preferences);
}
