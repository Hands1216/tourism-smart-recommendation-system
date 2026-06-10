package com.tourism.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 高德POI搜索服务
 * 封装高德地图 /place/text API，为Agent提供实时POI查询能力
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PoiSearchService {

    private final MapApiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    /** 高德POI类型代码 - 风景名胜 */
    public static final String TYPE_ATTRACTION = "110000";
    /** 高德POI类型代码 - 餐饮服务 */
    public static final String TYPE_RESTAURANT = "050000";
    /** 高德POI类型代码 - 住宿服务 */
    public static final String TYPE_HOTEL = "100000";

    /**
     * POI信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PoiInfo {
        private String name;
        private String address;
        private String tel;
        private String type;
        private String rating;
        private String cost;
        private String openTime;
        private String location;
        private String city;
        private String district;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(config.getTimeout());
            factory.setReadTimeout(config.getTimeout());
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    /**
     * POI关键词搜索
     */
    public List<PoiInfo> searchPoi(String keywords, String city, String types, int limit) {
        if (!config.isEnabled() || config.getKey() == null || config.getKey().isBlank()) {
            log.warn("高德地图API未启用或Key未配置");
            return List.of();
        }

        try {
            String url = String.format(
                    "%s/place/text?key=%s&keywords=%s&city=%s&types=%s&offset=%d&extensions=all",
                    config.getBaseUrl(),
                    config.getKey(),
                    URLEncoder.encode(keywords, StandardCharsets.UTF_8),
                    URLEncoder.encode(city, StandardCharsets.UTF_8),
                    types,
                    limit
            );
            log.debug("调用高德POI搜索: keywords={}, city={}, types={}", keywords, city, types);

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parsePoiResponse(response.getBody());
            }
        } catch (Exception e) {
            log.error("高德POI搜索失败: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * 搜索景点
     */
    public List<PoiInfo> searchAttractions(String name, String city) {
        return searchPoi(name, city, TYPE_ATTRACTION, 3);
    }

    /**
     * 搜索餐厅
     */
    public List<PoiInfo> searchRestaurants(String name, String city) {
        return searchPoi(name, city, TYPE_RESTAURANT, 3);
    }

    /**
     * 搜索酒店
     */
    public List<PoiInfo> searchHotels(String name, String city) {
        return searchPoi(name, city, TYPE_HOTEL, 3);
    }

    private List<PoiInfo> parsePoiResponse(String json) {
        List<PoiInfo> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"1".equals(root.path("status").asText())) {
                log.warn("高德POI搜索返回非成功状态: {}", root.path("info").asText());
                return result;
            }

            JsonNode pois = root.path("pois");
            if (pois.isArray()) {
                for (JsonNode poi : pois) {
                    JsonNode bizExt = poi.path("biz_ext");
                    result.add(PoiInfo.builder()
                            .name(poi.path("name").asText(""))
                            .address(poi.path("address").asText(""))
                            .tel(poi.path("tel").asText(""))
                            .type(poi.path("type").asText(""))
                            .rating(bizExt.path("rating").asText(""))
                            .cost(bizExt.path("cost").asText(""))
                            .openTime(bizExt.path("opentime").asText(""))
                            .location(poi.path("location").asText(""))
                            .city(poi.path("cityname").asText(""))
                            .district(poi.path("adname").asText(""))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("解析POI响应失败: {}", e.getMessage());
        }
        return result;
    }
}
