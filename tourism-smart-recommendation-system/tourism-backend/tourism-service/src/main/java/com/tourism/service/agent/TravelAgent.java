package com.tourism.service.agent;

import java.util.Map;

/**
 * 智能体接口
 * 定义所有专业智能体的通用行为
 *
 * @author 韩东升
 */
public interface TravelAgent {

    /**
     * 获取智能体名称
     */
    String getName();

    /**
     * 获取智能体描述
     */
    String getDescription();

    /**
     * 获取智能体专业领域
     */
    String getDomain();

    /**
     * 处理用户请求
     *
     * @param query   用户查询
     * @param context 上下文信息
     * @return 处理结果
     */
    AgentResponse process(String query, Map<String, Object> context);

    /**
     * 判断是否能处理该请求
     *
     * @param query 用户查询
     * @return 置信度 0-1
     */
    double canHandle(String query);

    /**
     * 获取系统提示词
     */
    String getSystemPrompt();
}
