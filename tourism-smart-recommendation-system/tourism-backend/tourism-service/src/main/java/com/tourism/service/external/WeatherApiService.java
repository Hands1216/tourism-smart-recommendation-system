package com.tourism.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 天气API服务（和风天气）
 * 提供实时天气、天气预报等功能
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiService {

    private final WeatherApiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    /**
     * 实时天气信息
     */
    @Data
    public static class WeatherNow {
        private String obsTime;       // 观测时间
        private String temp;          // 温度（摄氏度）
        private String feelsLike;     // 体感温度
        private String icon;          // 天气图标代码
        private String text;          // 天气状况文字描述
        private String wind360;       // 风向角度
        private String windDir;       // 风向
        private String windScale;     // 风力等级
        private String windSpeed;     // 风速（km/h）
        private String humidity;      // 相对湿度（%）
        private String precip;        // 降水量（mm）
        private String pressure;      // 大气压强（hPa）
        private String vis;           // 能见度（km）
        private String cloud;         // 云量（%）
        private String dew;           // 露点温度
    }

    /**
     * 天气预报（单日）
     */
    @Data
    public static class WeatherDaily {
        private String fxDate;        // 预报日期
        private String sunrise;       // 日出时间
        private String sunset;        // 日落时间
        private String moonrise;      // 月升时间
        private String moonset;       // 月落时间
        private String moonPhase;     // 月相
        private String tempMax;       // 最高温度
        private String tempMin;       // 最低温度
        private String iconDay;       // 白天天气图标
        private String textDay;       // 白天天气描述
        private String iconNight;     // 夜间天气图标
        private String textNight;     // 夜间天气描述
        private String wind360Day;    // 白天风向角度
        private String windDirDay;    // 白天风向
        private String windScaleDay;  // 白天风力等级
        private String humidity;      // 相对湿度
        private String precip;        // 降水量
        private String uvIndex;       // 紫外线指数
    }

    /**
     * 生活指数
     */
    @Data
    public static class LifeIndex {
        private String date;          // 日期
        private String type;          // 指数类型
        private String name;          // 指数名称
        private String level;         // 等级
        private String category;      // 类别
        private String text;          // 详细描述
    }

    /**
     * 天气预警
     */
    @Data
    public static class WeatherWarning {
        private String id;            // 预警ID
        private String sender;        // 发布单位
        private String pubTime;       // 发布时间
        private String title;         // 预警标题
        private String startTime;     // 开始时间
        private String endTime;       // 结束时间
        private String status;        // 状态
        private String level;         // 预警等级
        private String type;          // 预警类型
        private String typeName;      // 预警类型名称
        private String text;          // 预警详情
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
     * 创建请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        // 简单的User-Agent
        headers.set("User-Agent", "QWeather-Java-SDK");

        // JWT认证方式：在Header中添加Authorization
        if (config.isJwtAuth()) {
            String token = generateJwtToken();
            if (token != null) {
                headers.set("Authorization", "Bearer " + token);
                log.debug("添加JWT Authorization头");
            }
        }

        return headers;
    }

    /**
     * 生成JWT Token（和风天气JWT认证 - 使用Ed25519/EdDSA）
     * 手动构建JWT，不依赖jjwt库
     */
    private String generateJwtToken() {
        try {
            // 1. 构建Header
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "EdDSA");
            header.put("kid", config.getKeyId());

            // 2. 构建Payload
            long now = System.currentTimeMillis() / 1000;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", config.getProjectId());
            payload.put("iat", now);
            payload.put("exp", now + 3600); // 1小时有效期

            // 3. Base64Url编码
            String headerBase64 = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadBase64 = base64UrlEncode(objectMapper.writeValueAsBytes(payload));

            // 4. 签名数据
            String signingInput = headerBase64 + "." + payloadBase64;

            // 5. 使用Ed25519私钥签名
            PrivateKey privateKey = parseEd25519PrivateKey(config.getPrivateKey());
            if (privateKey == null) {
                log.error("无法解析Ed25519私钥");
                return null;
            }

            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();

            // 6. 组装JWT
            String signatureBase64 = base64UrlEncode(signatureBytes);
            String token = signingInput + "." + signatureBase64;

            log.debug("生成JWT Token成功，长度: {}", token.length());
            return token;

        } catch (Exception e) {
            log.error("生成JWT Token失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Base64Url编码（JWT标准）
     */
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * 解析Ed25519私钥
     */
    private PrivateKey parseEd25519PrivateKey(String privateKeyStr) {
        try {
            if (privateKeyStr == null || privateKeyStr.isEmpty()) {
                log.error("私钥为空");
                return null;
            }

            // 移除PEM头尾和所有空白字符
            String keyContent = privateKeyStr
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            log.debug("私钥内容长度: {}", keyContent.length());

            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            // 使用Ed25519算法
            KeyFactory kf = KeyFactory.getInstance("Ed25519");
            PrivateKey key = kf.generatePrivate(spec);
            log.debug("Ed25519私钥解析成功");
            return key;

        } catch (Exception e) {
            log.error("解析Ed25519私钥失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建API URL（根据认证方式添加key参数或不添加）
     */
    private String buildApiUrl(String endpoint, String location) {
        if (config.isJwtAuth()) {
            // JWT方式不需要key参数
            return String.format("%s/%s?location=%s", config.getBaseUrl(), endpoint, location);
        } else {
            // API Key方式
            return String.format("%s/%s?key=%s&location=%s",
                    config.getBaseUrl(), endpoint, config.getKey(), location);
        }
    }

    /**
     * 发送GET请求
     */
    private String doGet(String url) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<byte[]> response = getRestTemplate().exchange(
                    url, HttpMethod.GET, entity, byte[].class);

            byte[] body = response.getBody();
            if (body == null) {
                return null;
            }

            // 解析响应体（可能是gzip压缩）
            String responseBody;
            if (body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                responseBody = decompressGzip(body);
            } else {
                responseBody = new String(body, StandardCharsets.UTF_8);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                return responseBody;
            } else {
                log.error("HTTP请求失败，状态码: {}, 响应: {}", response.getStatusCode(), responseBody);
                return null;
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            byte[] body = e.getResponseBodyAsByteArray();
            String errorBody = "无响应体";
            if (body != null && body.length > 0) {
                if (body.length > 2 && body[0] == (byte) 0x1f && body[1] == (byte) 0x8b) {
                    errorBody = decompressGzip(body);
                } else {
                    errorBody = new String(body, StandardCharsets.UTF_8);
                }
            }
            log.error("HTTP请求失败，状态码: {}, 错误信息: {}", e.getStatusCode(), errorBody);
            return null;
        } catch (Exception e) {
            log.error("HTTP请求异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解压gzip数据
     */
    private String decompressGzip(byte[] compressed) {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.error("Gzip解压失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        if (!config.isEnabled()) {
            return false;
        }
        if (config.isJwtAuth()) {
            // JWT方式需要projectId、keyId和privateKey
            return config.getProjectId() != null && !config.getProjectId().isEmpty()
                    && config.getKeyId() != null && !config.getKeyId().isEmpty()
                    && config.getPrivateKey() != null && !config.getPrivateKey().isEmpty();
        } else {
            // API Key方式
            return config.getKey() != null && !config.getKey().isEmpty();
        }
    }

    /**
     * 获取实时天气
     *
     * @param location 位置（城市ID、经纬度或城市名称）
     * @return 实时天气信息
     */
    public WeatherNow getWeatherNow(String location) {
        if (!isAvailable()) {
            log.warn("天气API未启用或未配置");
            return createFallbackWeatherNow();
        }

        try {
            // 如果是城市名称，先获取城市ID
            String locationId = getCityId(location);
            if (locationId == null) {
                log.warn("无法获取城市ID: {}", location);
                return createFallbackWeatherNow();
            }

            String url = buildApiUrl("weather/now", locationId);
            log.info("调用和风天气实时天气API: {}", maskKey(url));

            String body = doGet(url);
            if (body != null) {
                return parseWeatherNow(body);
            }
        } catch (Exception e) {
            log.error("调用天气API失败: {}", e.getMessage());
        }

        return createFallbackWeatherNow();
    }

    /**
     * 隐藏URL中的key
     */
    private String maskKey(String url) {
        if (config.getKey() != null && !config.getKey().isEmpty()) {
            return url.replace(config.getKey(), "***");
        }
        return url;
    }

    /**
     * 获取天气预报（7天）
     *
     * @param location 位置
     * @return 天气预报列表
     */
    public List<WeatherDaily> getWeatherForecast(String location) {
        return getWeatherForecast(location, 7);
    }

    /**
     * 获取天气预报
     *
     * @param location 位置
     * @param days     天数（3/7/10/15/30）
     * @return 天气预报列表
     */
    public List<WeatherDaily> getWeatherForecast(String location, int days) {
        if (!isAvailable()) {
            log.warn("天气API未启用或未配置");
            return createFallbackForecast(days);
        }

        try {
            String locationId = getCityId(location);
            if (locationId == null) {
                log.warn("无法获取城市ID: {}", location);
                return createFallbackForecast(days);
            }

            // 根据天数选择API端点
            String endpoint = days <= 3 ? "weather/3d" : (days <= 7 ? "weather/7d" : "weather/15d");
            String url = buildApiUrl(endpoint, locationId);

            log.info("调用和风天气预报API: {}", maskKey(url));

            String body = doGet(url);
            if (body != null) {
                return parseWeatherForecast(body, days);
            }
        } catch (Exception e) {
            log.error("调用天气预报API失败: {}", e.getMessage());
        }

        return createFallbackForecast(days);
    }

    /**
     * 获取生活指数
     *
     * @param location 位置
     * @return 生活指数列表
     */
    public List<LifeIndex> getLifeIndices(String location) {
        if (!isAvailable()) {
            return createFallbackIndices();
        }

        try {
            String locationId = getCityId(location);
            if (locationId == null) {
                return createFallbackIndices();
            }

            // type=0 表示获取所有指数
            String url = buildApiUrl("indices/1d", locationId) + "&type=0";

            String body = doGet(url);
            if (body != null) {
                return parseLifeIndices(body);
            }
        } catch (Exception e) {
            log.error("调用生活指数API失败: {}", e.getMessage());
        }

        return createFallbackIndices();
    }

    /**
     * 获取天气预警
     *
     * @param location 位置
     * @return 预警列表
     */
    public List<WeatherWarning> getWeatherWarnings(String location) {
        if (!isAvailable()) {
            return new ArrayList<>();
        }

        try {
            String locationId = getCityId(location);
            if (locationId == null) {
                return new ArrayList<>();
            }

            String url = buildApiUrl("warning/now", locationId);

            String body = doGet(url);
            if (body != null) {
                return parseWeatherWarnings(body);
            }
        } catch (Exception e) {
            log.error("调用天气预警API失败: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * 根据城市名称获取城市ID
     */
    private String getCityId(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            return null;
        }

        // 如果已经是ID（纯数字）或经纬度格式，直接返回
        if (cityName.matches("\\d+") || cityName.matches("[\\d.]+,[\\d.]+")) {
            return cityName;
        }

        // 先尝试从缓存获取（优先使用缓存，避免API调用）
        String cachedId = getCommonCityId(cityName);
        if (cachedId != null) {
            log.debug("从缓存获取城市ID: {} -> {}", cityName, cachedId);
            return cachedId;
        }

        // 如果是区/县级地名，尝试提取上级城市
        String parentCity = extractParentCity(cityName);
        if (parentCity != null) {
            cachedId = getCommonCityId(parentCity);
            if (cachedId != null) {
                log.debug("从上级城市获取ID: {} -> {} -> {}", cityName, parentCity, cachedId);
                return cachedId;
            }
        }

        // GeoAPI调用（作为最后手段，因为免费版可能不支持）
        try {
            String cleanName = cityName.replace("市", "").replace("省", "")
                    .replace("区", "").replace("县", "").trim();
            String encodedName = URLEncoder.encode(cleanName, StandardCharsets.UTF_8);

            // 构建GeoAPI URL，JWT方式使用专属域名
            String geoBaseUrl = config.getBaseUrl().replace("/v7", "").replace("devapi", "geoapi");
            String url;
            if (config.isJwtAuth()) {
                // JWT方式：使用专属域名，不需要key参数
                url = String.format("%s/v2/city/lookup?location=%s&number=1", geoBaseUrl, encodedName);
            } else {
                // API Key方式
                url = String.format("https://geoapi.qweather.com/v2/city/lookup?key=%s&location=%s&number=1",
                        config.getKey(), encodedName);
            }

            log.debug("查询城市ID: {} -> URL: {}", cleanName, maskKey(url));

            String body = doGet(url);
            if (body != null) {
                JsonNode root = objectMapper.readTree(body);
                String code = root.path("code").asText();
                if ("200".equals(code)) {
                    JsonNode locations = root.path("location");
                    if (locations.isArray() && locations.size() > 0) {
                        String cityId = locations.get(0).path("id").asText();
                        log.info("获取城市ID成功: {} -> {}", cleanName, cityId);
                        return cityId;
                    }
                } else {
                    log.debug("GeoAPI返回: {}, 城市: {}", code, cleanName);
                }
            }
        } catch (Exception e) {
            log.debug("GeoAPI调用失败: {} - {}", cityName, e.getMessage());
        }

        log.warn("无法获取城市ID，将使用降级数据: {}", cityName);
        return null;
    }

    /**
     * 从区/县级地名提取上级城市
     */
    private String extractParentCity(String location) {
        if (location == null) return null;

        // 常见区名到城市的映射
        if (location.contains("江北") || location.contains("渝中") || location.contains("南岸") ||
            location.contains("沙坪坝") || location.contains("九龙坡")) {
            return "重庆";
        }
        if (location.contains("朝阳") || location.contains("海淀") || location.contains("东城") ||
            location.contains("西城") || location.contains("丰台") || location.contains("通州")) {
            return "北京";
        }
        if (location.contains("浦东") || location.contains("黄浦") || location.contains("静安") ||
            location.contains("徐汇") || location.contains("长宁") || location.contains("虹口")) {
            return "上海";
        }
        if (location.contains("天河") || location.contains("越秀") || location.contains("荔湾") ||
            location.contains("白云") || location.contains("番禺")) {
            return "广州";
        }
        if (location.contains("福田") || location.contains("罗湖") || location.contains("南山") ||
            location.contains("宝安") || location.contains("龙岗")) {
            return "深圳";
        }
        if (location.contains("西湖") || location.contains("上城") || location.contains("下城") ||
            location.contains("江干") || location.contains("拱墅") || location.contains("滨江")) {
            return "杭州";
        }
        if (location.contains("玄武") || location.contains("秦淮") || location.contains("鼓楼") ||
            location.contains("建邺") || location.contains("栖霞")) {
            return "南京";
        }
        if (location.contains("姑苏") || location.contains("虎丘") || location.contains("吴中") ||
            location.contains("相城") || location.contains("吴江")) {
            return "苏州";
        }
        if (location.contains("锦江") || location.contains("青羊") || location.contains("金牛") ||
            location.contains("武侯") || location.contains("成华")) {
            return "成都";
        }
        if (location.contains("江汉") || location.contains("武昌") || location.contains("汉阳") ||
            location.contains("洪山") || location.contains("青山")) {
            return "武汉";
        }
        if (location.contains("雁塔") || location.contains("碑林") || location.contains("莲湖") ||
            location.contains("新城") || location.contains("未央")) {
            return "西安";
        }

        return null;
    }

    /**
     * 常用城市ID映射（减少API调用）
     */
    private String getCommonCityId(String cityName) {
        // 去掉后缀
        String name = cityName.replace("市", "").replace("省", "")
                .replace("区", "").replace("县", "").trim();

        // 常用城市ID（和风天气LocationID）
        switch (name) {
            // 直辖市
            case "北京": return "101010100";
            case "上海": return "101020100";
            case "天津": return "101030100";
            case "重庆": return "101040100";
            // 省会城市
            case "广州": return "101280101";
            case "深圳": return "101280601";
            case "杭州": return "101210101";
            case "南京": return "101190101";
            case "苏州": return "101190401";
            case "成都": return "101270101";
            case "武汉": return "101200101";
            case "西安": return "101110101";
            case "长沙": return "101250101";
            case "郑州": return "101180101";
            case "青岛": return "101120201";
            case "大连": return "101070201";
            case "沈阳": return "101070101";
            case "哈尔滨": return "101050101";
            case "长春": return "101060101";
            case "济南": return "101120101";
            case "厦门": return "101230201";
            case "福州": return "101230101";
            case "昆明": return "101290101";
            case "贵阳": return "101260101";
            case "南宁": return "101300101";
            case "海口": return "101310101";
            case "三亚": return "101310201";
            case "拉萨": return "101140101";
            case "乌鲁木齐": return "101130101";
            case "兰州": return "101160101";
            case "西宁": return "101150101";
            case "银川": return "101170101";
            case "呼和浩特": return "101080101";
            case "石家庄": return "101090101";
            case "太原": return "101100101";
            case "合肥": return "101220101";
            case "南昌": return "101240101";
            // 热门旅游城市
            case "丽江": return "101291401";
            case "大理": return "101290201";
            case "桂林": return "101300501";
            case "张家界": return "101251101";
            case "黄山": return "101221001";
            case "九寨沟": return "101271901";
            case "敦煌": return "101160801";
            case "洛阳": return "101180901";
            case "开封": return "101180801";
            case "无锡": return "101190201";
            case "扬州": return "101190601";
            case "镇江": return "101190301";
            case "常州": return "101191101";
            case "嘉兴": return "101210301";
            case "绍兴": return "101210501";
            case "宁波": return "101210401";
            case "温州": return "101210701";
            case "珠海": return "101280701";
            case "佛山": return "101280800";
            case "东莞": return "101281601";
            default: return null;
        }
    }

    // ========== 解析方法 ==========

    private WeatherNow parseWeatherNow(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"200".equals(root.path("code").asText())) {
                return createFallbackWeatherNow();
            }

            JsonNode now = root.path("now");
            WeatherNow weather = new WeatherNow();
            weather.setObsTime(now.path("obsTime").asText());
            weather.setTemp(now.path("temp").asText());
            weather.setFeelsLike(now.path("feelsLike").asText());
            weather.setIcon(now.path("icon").asText());
            weather.setText(now.path("text").asText());
            weather.setWind360(now.path("wind360").asText());
            weather.setWindDir(now.path("windDir").asText());
            weather.setWindScale(now.path("windScale").asText());
            weather.setWindSpeed(now.path("windSpeed").asText());
            weather.setHumidity(now.path("humidity").asText());
            weather.setPrecip(now.path("precip").asText());
            weather.setPressure(now.path("pressure").asText());
            weather.setVis(now.path("vis").asText());
            weather.setCloud(now.path("cloud").asText());
            weather.setDew(now.path("dew").asText());

            return weather;
        } catch (Exception e) {
            log.error("解析实时天气失败: {}", e.getMessage());
        }

        return createFallbackWeatherNow();
    }

    private List<WeatherDaily> parseWeatherForecast(String json, int days) {
        List<WeatherDaily> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"200".equals(root.path("code").asText())) {
                return createFallbackForecast(days);
            }

            JsonNode daily = root.path("daily");
            if (daily.isArray()) {
                int count = 0;
                for (JsonNode day : daily) {
                    if (count >= days) break;

                    WeatherDaily weather = new WeatherDaily();
                    weather.setFxDate(day.path("fxDate").asText());
                    weather.setSunrise(day.path("sunrise").asText());
                    weather.setSunset(day.path("sunset").asText());
                    weather.setMoonrise(day.path("moonrise").asText());
                    weather.setMoonset(day.path("moonset").asText());
                    weather.setMoonPhase(day.path("moonPhase").asText());
                    weather.setTempMax(day.path("tempMax").asText());
                    weather.setTempMin(day.path("tempMin").asText());
                    weather.setIconDay(day.path("iconDay").asText());
                    weather.setTextDay(day.path("textDay").asText());
                    weather.setIconNight(day.path("iconNight").asText());
                    weather.setTextNight(day.path("textNight").asText());
                    weather.setWind360Day(day.path("wind360Day").asText());
                    weather.setWindDirDay(day.path("windDirDay").asText());
                    weather.setWindScaleDay(day.path("windScaleDay").asText());
                    weather.setHumidity(day.path("humidity").asText());
                    weather.setPrecip(day.path("precip").asText());
                    weather.setUvIndex(day.path("uvIndex").asText());

                    result.add(weather);
                    count++;
                }
            }
        } catch (Exception e) {
            log.error("解析天气预报失败: {}", e.getMessage());
            return createFallbackForecast(days);
        }

        return result;
    }

    private List<LifeIndex> parseLifeIndices(String json) {
        List<LifeIndex> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"200".equals(root.path("code").asText())) {
                return createFallbackIndices();
            }

            JsonNode daily = root.path("daily");
            if (daily.isArray()) {
                for (JsonNode item : daily) {
                    LifeIndex index = new LifeIndex();
                    index.setDate(item.path("date").asText());
                    index.setType(item.path("type").asText());
                    index.setName(item.path("name").asText());
                    index.setLevel(item.path("level").asText());
                    index.setCategory(item.path("category").asText());
                    index.setText(item.path("text").asText());
                    result.add(index);
                }
            }
        } catch (Exception e) {
            log.error("解析生活指数失败: {}", e.getMessage());
            return createFallbackIndices();
        }

        return result;
    }

    private List<WeatherWarning> parseWeatherWarnings(String json) {
        List<WeatherWarning> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            if (!"200".equals(root.path("code").asText())) {
                return result;
            }

            JsonNode warnings = root.path("warning");
            if (warnings.isArray()) {
                for (JsonNode item : warnings) {
                    WeatherWarning warning = new WeatherWarning();
                    warning.setId(item.path("id").asText());
                    warning.setSender(item.path("sender").asText());
                    warning.setPubTime(item.path("pubTime").asText());
                    warning.setTitle(item.path("title").asText());
                    warning.setStartTime(item.path("startTime").asText());
                    warning.setEndTime(item.path("endTime").asText());
                    warning.setStatus(item.path("status").asText());
                    warning.setLevel(item.path("level").asText());
                    warning.setType(item.path("type").asText());
                    warning.setTypeName(item.path("typeName").asText());
                    warning.setText(item.path("text").asText());
                    result.add(warning);
                }
            }
        } catch (Exception e) {
            log.error("解析天气预警失败: {}", e.getMessage());
        }

        return result;
    }

    // ========== 降级方法 ==========

    private WeatherNow createFallbackWeatherNow() {
        WeatherNow weather = new WeatherNow();
        weather.setTemp("--");
        weather.setFeelsLike("--");
        weather.setText("暂无数据");
        weather.setWindDir("--");
        weather.setWindScale("--");
        weather.setHumidity("--");
        return weather;
    }

    private List<WeatherDaily> createFallbackForecast(int days) {
        List<WeatherDaily> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            WeatherDaily weather = new WeatherDaily();
            weather.setFxDate("--");
            weather.setTempMax("--");
            weather.setTempMin("--");
            weather.setTextDay("暂无数据");
            weather.setTextNight("暂无数据");
            result.add(weather);
        }
        return result;
    }

    private List<LifeIndex> createFallbackIndices() {
        List<LifeIndex> result = new ArrayList<>();

        // 添加旅游相关的常用指数
        String[] names = {"穿衣指数", "紫外线指数", "旅游指数", "运动指数"};
        for (String name : names) {
            LifeIndex index = new LifeIndex();
            index.setName(name);
            index.setCategory("暂无数据");
            index.setText("暂无数据");
            result.add(index);
        }

        return result;
    }

    /**
     * 获取旅游建议（基于天气）
     *
     * @param location 位置
     * @return 旅游建议文本
     */
    public String getTravelAdvice(String location) {
        WeatherNow weather = getWeatherNow(location);
        List<LifeIndex> indices = getLifeIndices(location);
        List<WeatherWarning> warnings = getWeatherWarnings(location);

        StringBuilder advice = new StringBuilder();

        // 天气预警
        if (!warnings.isEmpty()) {
            advice.append("⚠️ 天气预警：");
            for (WeatherWarning warning : warnings) {
                advice.append(warning.getTitle()).append("；");
            }
            advice.append("\n");
        }

        // 当前天气
        if (weather.getTemp() != null && !"--".equals(weather.getTemp())) {
            advice.append("当前天气：").append(weather.getText())
                    .append("，温度").append(weather.getTemp()).append("°C")
                    .append("，体感").append(weather.getFeelsLike()).append("°C")
                    .append("，").append(weather.getWindDir()).append(weather.getWindScale()).append("级")
                    .append("\n");
        }

        // 旅游指数
        for (LifeIndex index : indices) {
            if ("旅游指数".equals(index.getName()) || "3".equals(index.getType())) {
                advice.append("旅游指数：").append(index.getCategory())
                        .append(" - ").append(index.getText()).append("\n");
                break;
            }
        }

        // 穿衣建议
        for (LifeIndex index : indices) {
            if ("穿衣指数".equals(index.getName()) || "3".equals(index.getType())) {
                advice.append("穿衣建议：").append(index.getText()).append("\n");
                break;
            }
        }

        return advice.length() > 0 ? advice.toString() : "暂无天气建议";
    }
}
