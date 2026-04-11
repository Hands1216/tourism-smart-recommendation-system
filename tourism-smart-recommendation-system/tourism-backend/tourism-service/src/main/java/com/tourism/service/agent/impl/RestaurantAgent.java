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
 * 美食推荐智能体
 * 专门处理餐厅、美食相关的查询和推荐
 *
 * @author 韩东升
 */
@Slf4j
@Component
public class RestaurantAgent extends BaseTravelAgent {

    @Autowired
    private PoiSearchService poiSearchService;

    private static final List<String> KEYWORDS = Arrays.asList(
            "美食", "餐厅", "饭店", "吃饭", "小吃", "特色菜",
            "好吃", "推荐餐厅", "哪里吃", "吃什么", "美食街",
            "早餐", "午餐", "晚餐", "夜宵", "火锅", "烧烤"
    );

    private static final String SYSTEM_PROMPT = """
            你是专业的美食推荐专家，熟悉各地特色美食和餐厅。

            【你的专长】
            - 熟悉中国各地特色美食和名菜
            - 了解各城市的老字号餐厅和网红店
            - 掌握餐厅的人均消费、营业时间、招牌菜
            - 能根据口味偏好推荐合适的餐厅

            【回答原则】
            1. 推荐真实存在的餐厅，提供具体名称和地址
            2. 说明餐厅特色和招牌菜
            3. 提供人均消费参考
            4. 根据用户的口味、预算进行个性化推荐
            5. 回答简洁实用

            【回答格式】
            - 先推荐2-3家具体餐厅
            - 说明每家的特色和招牌菜
            - 提供人均消费和用餐建议
            """;

    @Override
    public String getName() {
        return "美食推荐专家";
    }

    @Override
    public String getDescription() {
        return "专门处理餐厅推荐、美食查询、特色小吃推荐";
    }

    @Override
    public String getDomain() {
        return "restaurant";
    }

    @Override
    public AgentResponse process(String query, Map<String, Object> context) {
        log.info("[美食推荐专家] 处理查询: {}", query);

        StringBuilder enhancedPrompt = new StringBuilder(query);

        if (context != null) {
            if (context.containsKey("destination")) {
                String city = (String) context.get("destination");
                enhancedPrompt.append("\n所在城市：").append(city);

                // 接入高德POI获取实时餐厅信息
                try {
                    String keyword = query.replaceAll("(有什么|哪些|推荐|好吃的|吃什么|请问|帮我|我想)", "").trim();
                    if (!keyword.isBlank()) {
                        List<PoiSearchService.PoiInfo> pois = poiSearchService.searchRestaurants(keyword, city);
                        if (!pois.isEmpty()) {
                            enhancedPrompt.append("\n\n【实时餐厅数据（高德地图）】\n");
                            for (PoiSearchService.PoiInfo poi : pois) {
                                enhancedPrompt.append("- ").append(poi.getName());
                                if (!poi.getAddress().isBlank()) {
                                    enhancedPrompt.append(" | 地址：").append(poi.getAddress());
                                }
                                if (!poi.getRating().isBlank()) {
                                    enhancedPrompt.append(" | 评分：").append(poi.getRating());
                                }
                                if (!poi.getCost().isBlank()) {
                                    enhancedPrompt.append(" | 人均：").append(poi.getCost()).append("元");
                                }
                                if (!poi.getOpenTime().isBlank()) {
                                    enhancedPrompt.append(" | 营业时间：").append(poi.getOpenTime());
                                }
                                enhancedPrompt.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取餐厅POI信息失败: {}", e.getMessage());
                }
            }
            if (context.containsKey("cuisine")) {
                enhancedPrompt.append("\n口味偏好：").append(context.get("cuisine"));
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
                    .suggestedActions(Arrays.asList("查看餐厅详情", "导航到餐厅", "查看周边景点"))
                    .build();
        }

        return AgentResponse.failure("美食推荐服务暂时不可用");
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
