package com.tourism.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 高德地图API服务
 * 提供路线规划、实时交通、地理编码等功能
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapApiService {

    private final MapApiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    /**
     * 路线规划结果
     */
    @Data
    public static class RouteResult {
        private String origin;
        private String destination;
        private String distance;      // 距离（米）
        private String duration;      // 预计时间（秒）
        private String trafficStatus; // 交通状况
        private List<String> steps;   // 路线步骤
        private String strategy;      // 策略
    }

    /**
     * 实时交通状况
     */
    @Data
    public static class TrafficStatus {
        private String status;        // 畅通/缓行/拥堵
        private String description;
        private String expedite;      // 畅通路段比例
        private String congested;     // 拥堵路段比例
    }

    /**
     * 地理编码结果
     */
    @Data
    public static class GeoLocation {
        private String address;
        private String province;
        private String city;
        private String district;
        private String location;      // 经纬度 "lng,lat"
        private String adcode;        // 区域编码
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
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        return config.isEnabled() && config.getKey() != null && !config.getKey().isEmpty();
    }

    /**
     * 驾车路线规划
     *
     * @param origin      起点（经纬度或地址）
     * @param destination 终点（经纬度或地址）
     * @param strategy    策略：0-速度优先，1-费用优先，2-距离优先，4-躲避拥堵
     * @return 路线规划结果
     */
    public RouteResult getDrivingRoute(String origin, String destination, int strategy) {
        if (!isAvailable()) {
            log.warn("高德地图API未启用或未配置");
            return createFallbackRoute(origin, destination);
        }

        try {
            // 如果是地址，先进行地理编码
            String originLocation = origin.contains(",") ? origin : geocode(origin);
            String destLocation = destination.contains(",") ? destination : geocode(destination);

            if (originLocation == null || destLocation == null) {
                return createFallbackRoute(origin, destination);
            }

            String url = String.format("%s/direction/driving?key=%s&origin=%s&destination=%s&strategy=%d&extensions=all",
                    config.getBaseUrl(), config.getKey(), originLocation, destLocation, strategy);

            log.info("调用高德驾车路线API: {}", url.replace(config.getKey(), "***"));

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseDrivingRoute(response.getBody(), origin, destination);
            }
        } catch (Exception e) {
            log.error("调用高德驾车路线API失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    /**
     * 公交/地铁路线规划
     *
     * @param origin      起点
     * @param destination 终点
     * @param city        城市
     * @return 路线规划结果
     */
    public RouteResult getTransitRoute(String origin, String destination, String city) {
        if (!isAvailable()) {
            log.warn("高德地图API未启用或未配置");
            return createFallbackRoute(origin, destination);
        }

        try {
            String originLocation = origin.contains(",") ? origin : geocode(origin);
            String destLocation = destination.contains(",") ? destination : geocode(destination);

            if (originLocation == null || destLocation == null) {
                return createFallbackRoute(origin, destination);
            }

            String url = String.format("%s/direction/transit/integrated?key=%s&origin=%s&destination=%s&city=%s&strategy=0",
                    config.getBaseUrl(), config.getKey(), originLocation, destLocation, city);

            log.info("调用高德公交路线API: {}", url.replace(config.getKey(), "***"));

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTransitRoute(response.getBody(), origin, destination);
            }
        } catch (Exception e) {
            log.error("调用高德公交路线API失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    /**
     * 步行路线规划
     *
     * @param origin      起点
     * @param destination 终点
     * @return 路线规划结果
     */
    public RouteResult getWalkingRoute(String origin, String destination) {
        if (!isAvailable()) {
            return createFallbackRoute(origin, destination);
        }

        try {
            String originLocation = origin.contains(",") ? origin : geocode(origin);
            String destLocation = destination.contains(",") ? destination : geocode(destination);

            if (originLocation == null || destLocation == null) {
                return createFallbackRoute(origin, destination);
            }

            String url = String.format("%s/direction/walking?key=%s&origin=%s&destination=%s",
                    config.getBaseUrl(), config.getKey(), originLocation, destLocation);

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseWalkingRoute(response.getBody(), origin, destination);
            }
        } catch (Exception e) {
            log.error("调用高德步行路线API失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    /**
     * 获取实时交通状况
     *
     * @param rectangle 矩形区域 "左下角经度,左下角纬度;右上角经度,右上角纬度"
     * @return 交通状况
     */
    public TrafficStatus getTrafficStatus(String rectangle) {
        if (!isAvailable()) {
            return createFallbackTraffic();
        }

        try {
            String url = String.format("%s/traffic/status/rectangle?key=%s&rectangle=%s&level=6",
                    config.getBaseUrl(), config.getKey(), rectangle);

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTrafficStatus(response.getBody());
            }
        } catch (Exception e) {
            log.error("调用高德交通状况API失败: {}", e.getMessage());
        }

        return createFallbackTraffic();
    }

    /**
     * 地理编码（地址转经纬度）
     *
     * @param address 地址
     * @return 经纬度 "lng,lat"
     */
    public String geocode(String address) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String url = String.format("%s/geocode/geo?key=%s&address=%s",
                    config.getBaseUrl(), config.getKey(), address);

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if ("1".equals(root.path("status").asText())) {
                    JsonNode geocodes = root.path("geocodes");
                    if (geocodes.isArray() && geocodes.size() > 0) {
                        return geocodes.get(0).path("location").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("地理编码失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 逆地理编码（经纬度转地址）
     *
     * @param location 经纬度 "lng,lat"
     * @return 地理位置信息
     */
    public GeoLocation reverseGeocode(String location) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String url = String.format("%s/geocode/regeo?key=%s&location=%s",
                    config.getBaseUrl(), config.getKey(), location);

            ResponseEntity<String> response = getRestTemplate().getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if ("1".equals(root.path("status").asText())) {
                    JsonNode regeocode = root.path("regeocode");
                    GeoLocation geo = new GeoLocation();
                    geo.setAddress(regeocode.path("formatted_address").asText());
                    JsonNode addressComponent = regeocode.path("addressComponent");
                    geo.setProvince(addressComponent.path("province").asText());
                    geo.setCity(addressComponent.path("city").asText());
                    geo.setDistrict(addressComponent.path("district").asText());
                    geo.setAdcode(addressComponent.path("adcode").asText());
                    geo.setLocation(location);
                    return geo;
                }
            }
        } catch (Exception e) {
            log.error("逆地理编码失败: {}", e.getMessage());
        }

        return null;
    }

    // ========== 私有方法 ==========

    private RouteResult parseDrivingRoute(String json, String origin, String destination) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"1".equals(root.path("status").asText())) {
                return createFallbackRoute(origin, destination);
            }

            JsonNode route = root.path("route");
            JsonNode paths = route.path("paths");

            if (paths.isArray() && paths.size() > 0) {
                JsonNode path = paths.get(0);
                RouteResult result = new RouteResult();
                result.setOrigin(origin);
                result.setDestination(destination);
                result.setDistance(path.path("distance").asText());
                result.setDuration(path.path("duration").asText());
                result.setStrategy(path.path("strategy").asText());

                // 解析交通状况
                JsonNode steps = path.path("steps");
                List<String> stepList = new ArrayList<>();
                int totalTrafficLights = 0;
                for (JsonNode step : steps) {
                    stepList.add(step.path("instruction").asText());
                    totalTrafficLights += step.path("traffic_lights").asInt(0);
                }
                result.setSteps(stepList);
                result.setTrafficStatus("途经" + totalTrafficLights + "个红绿灯");

                return result;
            }
        } catch (Exception e) {
            log.error("解析驾车路线失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    private RouteResult parseTransitRoute(String json, String origin, String destination) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"1".equals(root.path("status").asText())) {
                return createFallbackRoute(origin, destination);
            }

            JsonNode route = root.path("route");
            JsonNode transits = route.path("transits");

            if (transits.isArray() && transits.size() > 0) {
                JsonNode transit = transits.get(0);
                RouteResult result = new RouteResult();
                result.setOrigin(origin);
                result.setDestination(destination);
                result.setDistance(transit.path("distance").asText());
                result.setDuration(transit.path("duration").asText());
                result.setStrategy("公交/地铁");

                // 解析换乘步骤
                JsonNode segments = transit.path("segments");
                List<String> stepList = new ArrayList<>();
                for (JsonNode segment : segments) {
                    JsonNode bus = segment.path("bus");
                    if (bus.has("buslines")) {
                        JsonNode buslines = bus.path("buslines");
                        if (buslines.isArray() && buslines.size() > 0) {
                            String lineName = buslines.get(0).path("name").asText();
                            stepList.add("乘坐 " + lineName);
                        }
                    }
                    JsonNode walking = segment.path("walking");
                    if (walking.has("distance")) {
                        int walkDist = walking.path("distance").asInt(0);
                        if (walkDist > 0) {
                            stepList.add("步行 " + walkDist + "米");
                        }
                    }
                }
                result.setSteps(stepList);
                result.setTrafficStatus("公共交通");

                return result;
            }
        } catch (Exception e) {
            log.error("解析公交路线失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    private RouteResult parseWalkingRoute(String json, String origin, String destination) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"1".equals(root.path("status").asText())) {
                return createFallbackRoute(origin, destination);
            }

            JsonNode route = root.path("route");
            JsonNode paths = route.path("paths");

            if (paths.isArray() && paths.size() > 0) {
                JsonNode path = paths.get(0);
                RouteResult result = new RouteResult();
                result.setOrigin(origin);
                result.setDestination(destination);
                result.setDistance(path.path("distance").asText());
                result.setDuration(path.path("duration").asText());
                result.setStrategy("步行");
                result.setTrafficStatus("步行");

                JsonNode steps = path.path("steps");
                List<String> stepList = new ArrayList<>();
                for (JsonNode step : steps) {
                    stepList.add(step.path("instruction").asText());
                }
                result.setSteps(stepList);

                return result;
            }
        } catch (Exception e) {
            log.error("解析步行路线失败: {}", e.getMessage());
        }

        return createFallbackRoute(origin, destination);
    }

    private TrafficStatus parseTrafficStatus(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"1".equals(root.path("status").asText())) {
                return createFallbackTraffic();
            }

            JsonNode trafficInfo = root.path("trafficinfo");
            TrafficStatus status = new TrafficStatus();
            status.setDescription(trafficInfo.path("description").asText());
            status.setExpedite(trafficInfo.path("evaluation").path("expedite").asText());
            status.setCongested(trafficInfo.path("evaluation").path("congested").asText());

            // 根据拥堵比例判断状态
            double congestedRatio = Double.parseDouble(status.getCongested().replace("%", "")) / 100;
            if (congestedRatio < 0.1) {
                status.setStatus("畅通");
            } else if (congestedRatio < 0.3) {
                status.setStatus("缓行");
            } else {
                status.setStatus("拥堵");
            }

            return status;
        } catch (Exception e) {
            log.error("解析交通状况失败: {}", e.getMessage());
        }

        return createFallbackTraffic();
    }

    private RouteResult createFallbackRoute(String origin, String destination) {
        RouteResult result = new RouteResult();
        result.setOrigin(origin);
        result.setDestination(destination);
        result.setDistance("未知");
        result.setDuration("未知");
        result.setTrafficStatus("未知");
        result.setSteps(List.of("建议使用导航软件获取详细路线"));
        result.setStrategy("默认");
        return result;
    }

    private TrafficStatus createFallbackTraffic() {
        TrafficStatus status = new TrafficStatus();
        status.setStatus("未知");
        status.setDescription("无法获取实时交通信息");
        status.setExpedite("--");
        status.setCongested("--");
        return status;
    }

    /**
     * 格式化距离显示
     */
    public String formatDistance(String distanceInMeters) {
        try {
            int meters = Integer.parseInt(distanceInMeters);
            if (meters >= 1000) {
                return String.format("%.1f公里", meters / 1000.0);
            } else {
                return meters + "米";
            }
        } catch (Exception e) {
            return distanceInMeters;
        }
    }

    /**
     * 格式化时间显示
     */
    public String formatDuration(String durationInSeconds) {
        try {
            int seconds = Integer.parseInt(durationInSeconds);
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;

            if (hours > 0) {
                return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
            } else {
                return minutes + "分钟";
            }
        } catch (Exception e) {
            return durationInSeconds;
        }
    }
}
