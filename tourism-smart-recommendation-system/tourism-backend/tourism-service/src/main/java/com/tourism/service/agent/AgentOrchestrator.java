package com.tourism.service.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能体协调器
 * 负责分析用户意图，选择合适的智能体处理请求
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final List<TravelAgent> agents;

    /**
     * 处理用户查询
     * 自动选择最合适的智能体
     *
     * @param query   用户查询
     * @param context 上下文信息
     * @return 智能体响应
     */
    public AgentResponse process(String query, Map<String, Object> context) {
        log.info("协调器收到查询: {}", query);

        // 计算每个智能体的置信度
        List<AgentScore> scores = agents.stream()
                .map(agent -> new AgentScore(agent, agent.canHandle(query)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .collect(Collectors.toList());

        // 打印各智能体得分
        log.info("智能体置信度评估:");
        for (AgentScore score : scores) {
            log.info("  - {}: {}", score.agent.getName(), String.format("%.2f", score.score));
        }

        // 选择置信度最高的智能体
        if (!scores.isEmpty() && scores.get(0).score > 0.1) {
            TravelAgent selectedAgent = scores.get(0).agent;
            log.info("选择智能体: {} (置信度: {})", selectedAgent.getName(), scores.get(0).score);

            AgentResponse response = selectedAgent.process(query, context);
            response.setAgentName(selectedAgent.getName());
            return response;
        }

        // 没有合适的智能体，使用通用响应
        log.info("没有找到合适的专业智能体，使用通用处理");
        return processGeneral(query, context);
    }

    /**
     * 并行处理 - 同时调用多个智能体
     * 用于需要综合多方面信息的复杂查询
     *
     * @param query   用户查询
     * @param context 上下文信息
     * @param domains 指定的领域列表
     * @return 合并后的响应
     */
    public AgentResponse processParallel(String query, Map<String, Object> context, List<String> domains) {
        log.info("并行处理查询，指定领域: {}", domains);

        List<AgentResponse> responses = new ArrayList<>();

        for (TravelAgent agent : agents) {
            if (domains.contains(agent.getDomain())) {
                try {
                    AgentResponse response = agent.process(query, context);
                    if (response.isSuccess()) {
                        responses.add(response);
                    }
                } catch (Exception e) {
                    log.error("智能体 {} 处理失败: {}", agent.getName(), e.getMessage());
                }
            }
        }

        // 合并响应
        return mergeResponses(responses);
    }

    /**
     * 链式处理 - 按顺序调用多个智能体
     * 前一个智能体的输出作为后一个的输入
     *
     * @param query   用户查询
     * @param context 上下文信息
     * @param domains 按顺序执行的领域列表
     * @return 最终响应
     */
    public AgentResponse processChain(String query, Map<String, Object> context, List<String> domains) {
        log.info("链式处理查询，执行顺序: {}", domains);

        String currentQuery = query;
        Map<String, Object> currentContext = new HashMap<>(context != null ? context : new HashMap<>());
        AgentResponse lastResponse = null;

        for (String domain : domains) {
            TravelAgent agent = findAgentByDomain(domain);
            if (agent != null) {
                lastResponse = agent.process(currentQuery, currentContext);
                if (lastResponse.isSuccess()) {
                    // 将当前响应添加到上下文
                    currentContext.put(domain + "_result", lastResponse.getContent());
                    currentQuery = currentQuery + "\n\n参考信息：" + lastResponse.getContent();
                }
            }
        }

        return lastResponse != null ? lastResponse : AgentResponse.failure("链式处理失败");
    }

    /**
     * 获取所有可用的智能体信息
     */
    public List<Map<String, String>> getAvailableAgents() {
        return agents.stream()
                .map(agent -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("name", agent.getName());
                    info.put("description", agent.getDescription());
                    info.put("domain", agent.getDomain());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据领域查找智能体
     */
    private TravelAgent findAgentByDomain(String domain) {
        return agents.stream()
                .filter(agent -> agent.getDomain().equals(domain))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通用处理（当没有专业智能体匹配时）
     */
    private AgentResponse processGeneral(String query, Map<String, Object> context) {
        // 尝试找到任意一个可用的智能体来处理
        for (TravelAgent agent : agents) {
            try {
                AgentResponse response = agent.process(query, context);
                if (response.isSuccess()) {
                    return response;
                }
            } catch (Exception e) {
                log.debug("智能体 {} 处理失败: {}", agent.getName(), e.getMessage());
            }
        }

        return AgentResponse.builder()
                .success(true)
                .content("抱歉，我暂时无法理解您的问题。您可以尝试询问：\n" +
                        "- 景点推荐（如：北京有什么好玩的景点？）\n" +
                        "- 美食推荐（如：西安有什么特色小吃？）\n" +
                        "- 住宿推荐（如：杭州西湖附近有什么酒店？）\n" +
                        "- 行程规划（如：成都3天怎么玩？）")
                .type("text")
                .agentName("通用助手")
                .confidence(0.5)
                .build();
    }

    /**
     * 合并多个智能体的响应
     */
    private AgentResponse mergeResponses(List<AgentResponse> responses) {
        if (responses.isEmpty()) {
            return AgentResponse.failure("没有获取到有效响应");
        }

        if (responses.size() == 1) {
            return responses.get(0);
        }

        StringBuilder mergedContent = new StringBuilder();
        List<String> agentNames = new ArrayList<>();
        List<AgentResponse.RecommendationItem> allRecommendations = new ArrayList<>();

        for (AgentResponse response : responses) {
            if (response.getAgentName() != null) {
                agentNames.add(response.getAgentName());
                mergedContent.append("【").append(response.getAgentName()).append("】\n");
            }
            mergedContent.append(response.getContent()).append("\n\n");

            if (response.getRecommendations() != null) {
                allRecommendations.addAll(response.getRecommendations());
            }
        }

        return AgentResponse.builder()
                .success(true)
                .content(mergedContent.toString().trim())
                .type("merged")
                .agentName(String.join(" + ", agentNames))
                .recommendations(allRecommendations.isEmpty() ? null : allRecommendations)
                .confidence(0.9)
                .build();
    }

    /**
     * 智能体评分记录
     */
    private static class AgentScore {
        TravelAgent agent;
        double score;

        AgentScore(TravelAgent agent, double score) {
            this.agent = agent;
            this.score = score;
        }
    }
}
