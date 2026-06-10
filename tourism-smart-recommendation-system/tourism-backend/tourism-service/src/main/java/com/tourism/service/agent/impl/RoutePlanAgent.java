package com.tourism.service.agent.impl;

import com.tourism.service.agent.AgentResponse;
import com.tourism.service.agent.BaseTravelAgent;
import com.tourism.service.external.BookingUrlService;
import com.tourism.service.external.MapApiService;
import com.tourism.service.external.WeatherApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 行程规划智能体
 * 专门处理路线规划、行程安排相关的查询
 *
 */
@Slf4j
@Component
public class RoutePlanAgent extends BaseTravelAgent {

    @Autowired
    private MapApiService mapApiService;

    @Autowired
    private WeatherApiService weatherApiService;

    @Autowired
    private BookingUrlService bookingUrlService;

    private static final List<String> KEYWORDS = Arrays.asList(
            "行程", "路线", "规划", "安排", "几天", "怎么玩",
            "游玩顺序", "先去哪", "路线图", "攻略", "计划",
            "自由行", "跟团", "自驾游", "一日游", "两日游"
    );

    private static final String SYSTEM_PROMPT = """
            你是专业的行程规划专家，擅长制定合理的旅行计划。

            【你的专长】
            - 熟悉各地景点的地理位置和游览时间
            - 了解景点之间的交通方式和耗时
            - 能合理安排每日行程，避免过于紧凑或松散
            - 考虑用餐、休息等实际需求

            【回答原则】
            1. 根据天数合理分配景点
            2. 考虑景点之间的距离和交通
            3. 安排合理的用餐时间
            4. 预留适当的休息时间
            5. 提供具体的时间安排

            【回答格式】
            按天数列出行程安排：
            - 每天的主要景点（2-3个）
            - 建议的游览顺序
            - 用餐建议
            - 交通方式建议
            """;

    @Override
    public String getName() {
        return "行程规划专家";
    }

    @Override
    public String getDescription() {
        return "专门处理行程规划、路线安排、游玩顺序建议";
    }

    @Override
    public String getDomain() {
        return "route";
    }

    @Override
    public AgentResponse process(String query, Map<String, Object> context) {
        log.info("[行程规划专家] 处理查询: {}", query);

        StringBuilder enhancedPrompt = new StringBuilder(query);

        // 添加上下文信息
        if (context != null) {
            if (context.containsKey("destination")) {
                String dest = (String) context.get("destination");
                enhancedPrompt.append("\n目的地：").append(dest);

                // 获取天气信息
                try {
                    String weatherAdvice = weatherApiService.getTravelAdvice(dest);
                    if (weatherAdvice != null && !weatherAdvice.isEmpty()) {
                        enhancedPrompt.append("\n\n【天气参考】\n").append(weatherAdvice);
                    }
                } catch (Exception e) {
                    log.debug("获取天气信息失败: {}", e.getMessage());
                }
            }
            if (context.containsKey("days")) {
                enhancedPrompt.append("\n出行天数：").append(context.get("days")).append("天");
            }
            if (context.containsKey("departure") && context.containsKey("destination")) {
                // 追加12306购票链接信息
                try {
                    String from = (String) context.get("departure");
                    String to = (String) context.get("destination");
                    String trainUrl = bookingUrlService.generate12306Url(from, to, null);
                    enhancedPrompt.append("\n\n【火车票预订参考】\n")
                            .append("12306购票链接：").append(trainUrl);
                } catch (Exception e) {
                    log.debug("生成12306链接失败: {}", e.getMessage());
                }
            }
            if (context.containsKey("companion")) {
                enhancedPrompt.append("\n同行人员：").append(context.get("companion"));
            }
            if (context.containsKey("preferences")) {
                enhancedPrompt.append("\n偏好：").append(context.get("preferences"));
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
                    .suggestedActions(Arrays.asList("生成详细行程", "查看景点详情", "预订酒店"))
                    .build();
        }

        return AgentResponse.failure("行程规划服务暂时不可用");
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
