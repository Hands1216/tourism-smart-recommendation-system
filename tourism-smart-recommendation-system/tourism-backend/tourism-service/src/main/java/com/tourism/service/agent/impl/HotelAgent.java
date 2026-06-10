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
 * 住宿推荐智能体
 * 专门处理酒店、民宿相关的查询和推荐
 *
 */
@Slf4j
@Component
public class HotelAgent extends BaseTravelAgent {

    @Autowired
    private PoiSearchService poiSearchService;

    private static final List<String> KEYWORDS = Arrays.asList(
            "酒店", "住宿", "宾馆", "民宿", "客栈", "旅馆",
            "住哪", "订房", "住在哪", "推荐酒店", "便宜酒店",
            "五星级", "经济型", "青年旅舍", "度假村"
    );

    private static final String SYSTEM_PROMPT = """
            你是专业的住宿推荐专家，熟悉各类酒店和民宿。

            【你的专长】
            - 熟悉各城市的酒店分布和特点
            - 了解不同档次酒店的价格区间
            - 掌握酒店的设施、服务、位置优势
            - 能根据需求推荐合适的住宿

            【回答原则】
            1. 推荐真实存在的酒店，提供具体名称
            2. 说明酒店位置优势（靠近景点/交通便利）
            3. 提供价格参考
            4. 根据用户的预算、出行目的进行推荐
            5. 回答简洁实用

            【回答格式】
            - 推荐2-3家不同档次的酒店
            - 说明每家的位置和特点
            - 提供价格区间参考
            """;

    @Override
    public String getName() {
        return "住宿推荐专家";
    }

    @Override
    public String getDescription() {
        return "专门处理酒店推荐、民宿查询、住宿预订建议";
    }

    @Override
    public String getDomain() {
        return "hotel";
    }

    @Override
    public AgentResponse process(String query, Map<String, Object> context) {
        log.info("[住宿推荐专家] 处理查询: {}", query);

        StringBuilder enhancedPrompt = new StringBuilder(query);

        if (context != null) {
            if (context.containsKey("destination")) {
                String city = (String) context.get("destination");
                enhancedPrompt.append("\n目的地：").append(city);

                // 接入高德POI获取实时酒店信息
                try {
                    String keyword = query.replaceAll("(有什么|哪些|推荐|住哪|住在哪|请问|帮我|我想)", "").trim();
                    if (!keyword.isBlank()) {
                        List<PoiSearchService.PoiInfo> pois = poiSearchService.searchHotels(keyword, city);
                        if (!pois.isEmpty()) {
                            enhancedPrompt.append("\n\n【实时酒店数据（高德地图）】\n");
                            for (PoiSearchService.PoiInfo poi : pois) {
                                enhancedPrompt.append("- ").append(poi.getName());
                                if (!poi.getAddress().isBlank()) {
                                    enhancedPrompt.append(" | 地址：").append(poi.getAddress());
                                }
                                if (!poi.getRating().isBlank()) {
                                    enhancedPrompt.append(" | 评分：").append(poi.getRating());
                                }
                                if (!poi.getCost().isBlank()) {
                                    enhancedPrompt.append(" | 参考价格：").append(poi.getCost()).append("元");
                                }
                                enhancedPrompt.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取酒店POI信息失败: {}", e.getMessage());
                }
            }
            if (context.containsKey("checkIn")) {
                enhancedPrompt.append("\n入住日期：").append(context.get("checkIn"));
            }
            if (context.containsKey("nights")) {
                enhancedPrompt.append("\n入住天数：").append(context.get("nights"));
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
                    .suggestedActions(Arrays.asList("查看酒店详情", "查看周边景点", "规划行程"))
                    .build();
        }

        return AgentResponse.failure("住宿推荐服务暂时不可用");
    }

    @Override
    public double canHandle(String query) {
        return calculateConfidence(query, KEYWORDS);
    }

    @Override
    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}
