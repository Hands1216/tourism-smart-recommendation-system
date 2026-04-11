package com.tourism.service.agent.impl;

import com.tourism.service.agent.AgentResponse;
import com.tourism.service.agent.BaseTravelAgent;
import com.tourism.service.external.PoiSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 景点推荐智能体
 * 专门处理景点相关的查询和推荐
 *
 * @author 韩东升
 */
@Slf4j
@Component
public class AttractionAgent extends BaseTravelAgent {

    @Autowired
    private PoiSearchService poiSearchService;

    private static final List<String> KEYWORDS = Arrays.asList(
            "景点", "景区", "旅游", "游玩", "参观", "游览", "打卡",
            "名胜", "古迹", "风景", "公园", "博物馆", "寺庙", "古镇",
            "哪里好玩", "去哪玩", "推荐景点", "必去", "值得去"
    );

    private static final String SYSTEM_PROMPT = """
            你是专业的景点推荐专家，拥有丰富的旅游知识。

            【你的专长】
            - 熟悉中国各地著名景点、小众景点
            - 了解景点的历史文化背景
            - 掌握景点的开放时间、门票价格、最佳游览季节
            - 能根据用户偏好推荐合适的景点

            【回答原则】
            1. 推荐真实存在的景点，提供具体名称
            2. 说明景点特色和亮点
            3. 提供实用的游览建议（最佳时间、注意事项）
            4. 根据用户的时间、预算、兴趣进行个性化推荐
            5. 回答简洁明了，重点突出

            【回答格式】
            - 先直接回答用户问题
            - 然后提供2-3个具体推荐
            - 最后给出简短的游览建议
            """;

    @Override
    public String getName() {
        return "景点推荐专家";
    }

    @Override
    public String getDescription() {
        return "专门处理景点查询、景点推荐、景点信息咨询";
    }

    @Override
    public String getDomain() {
        return "attraction";
    }

    @Override
    public AgentResponse process(String query, Map<String, Object> context) {
        log.info("[景点推荐专家] 处理查询: {}", query);

        // 构建增强提示词
        StringBuilder enhancedPrompt = new StringBuilder(query);

        // 添加上下文信息
        if (context != null) {
            if (context.containsKey("destination")) {
                String city = (String) context.get("destination");
                enhancedPrompt.append("\n目的地：").append(city);

                // 接入高德POI获取实时景点信息
                try {
                    String keyword = extractKeyword(query);
                    if (!keyword.isBlank()) {
                        List<PoiSearchService.PoiInfo> pois = poiSearchService.searchAttractions(keyword, city);
                        if (!pois.isEmpty()) {
                            enhancedPrompt.append("\n\n【实时景点数据（高德地图）】\n");
                            for (PoiSearchService.PoiInfo poi : pois) {
                                enhancedPrompt.append("- ").append(poi.getName());
                                if (!poi.getAddress().isBlank()) {
                                    enhancedPrompt.append(" | 地址：").append(poi.getAddress());
                                }
                                if (!poi.getRating().isBlank()) {
                                    enhancedPrompt.append(" | 评分：").append(poi.getRating());
                                }
                                if (!poi.getOpenTime().isBlank()) {
                                    enhancedPrompt.append(" | 营业时间：").append(poi.getOpenTime());
                                }
                                enhancedPrompt.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取POI信息失败: {}", e.getMessage());
                }
            }
            if (context.containsKey("preferences")) {
                enhancedPrompt.append("\n用户偏好：").append(context.get("preferences"));
            }
            if (context.containsKey("budget")) {
                enhancedPrompt.append("\n预算：").append(context.get("budget"));
            }
        }

        String response = callLLM(enhancedPrompt.toString(), SYSTEM_PROMPT);

        if (response != null && !response.isEmpty()) {
            return AgentResponse.builder()
                    .success(true)
                    .content(response)
                    .type("text")
                    .agentName(getName())
                    .confidence(0.9)
                    .suggestedActions(Arrays.asList("查看景点详情", "添加到行程", "查看周边美食"))
                    .build();
        }

        return AgentResponse.failure("景点推荐服务暂时不可用");
    }

    @Override
    public double canHandle(String query) {
        return calculateConfidence(query, KEYWORDS);
    }

    @Override
    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    /**
     * 从查询中提取关键词（去除常见疑问词）
     */
    private String extractKeyword(String query) {
        if (query == null) {
            return "";
        }
        return query.replaceAll("(有什么|哪些|推荐|好玩的|值得去的|怎么样|在哪|有哪些|请问|帮我|我想)", "").trim();
    }
}
