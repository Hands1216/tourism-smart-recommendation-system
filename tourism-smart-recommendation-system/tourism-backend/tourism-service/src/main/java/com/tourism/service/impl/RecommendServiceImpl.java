package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.model.dto.RecommendRequestDTO;
import com.tourism.model.dto.RoutePlanRequestDTO;
import com.tourism.model.entity.Attraction;
import com.tourism.model.entity.RoutePlan;
import com.tourism.model.vo.AttractionVO;
import com.tourism.model.vo.RecommendVO;
import com.tourism.model.vo.RoutePlanVO;
import com.tourism.service.RecommendService;
import com.tourism.service.external.BookingUrlService;
import com.tourism.service.external.MapApiService;
import com.tourism.service.external.WeatherApiService;
import com.tourism.service.mapper.AttractionMapper;
import com.tourism.service.mapper.RoutePlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现 - 集成DeepSeek AI
 *
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private RoutePlanMapper routePlanMapper;

    @Autowired
    private MapApiService mapApiService;

    @Autowired
    private WeatherApiService weatherApiService;

    @Autowired
    private BookingUrlService bookingUrlService;

    @Autowired
    private RecommendAlgorithmService recommendAlgorithmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:deepseek-chat}")
    private String model;

    @Value("${llm.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private RestTemplate restTemplate;

    // 超时时间（毫秒）- 复杂行程需要更长时间，设置为5分钟
    private static final int TIMEOUT_MS = 300000;

    /**
     * 路线规划系统提示词 - 强制要求真实名称和标准JSON格式
     */
    private static final String ROUTE_PLAN_SYSTEM_PROMPT =
        "你是专业的旅游规划助手。请根据用户需求生成JSON格式的详细旅行计划。" +
        "\n\n【CRITICAL JSON格式要求】必须严格遵守，否则输出无效：" +
        "\n1. 所有字符串必须使用双引号(\")，绝不能使用单引号(')" +
        "\n2. 返回的JSON必须以 { 开始，以 } 结束" +
        "\n3. 不要使用markdown代码块（不要用 ```json 或 ``` 包裹）" +
        "\n4. 直接返回JSON对象，不要添加任何额外说明文字" +
        "\n\n【绝对禁止 - 会导致回答不合格】：" +
        "\n- 禁止使用单引号(')作为JSON字符串分隔符" +
        "\n- 禁止使用'著名景点1/2/3'、'当地特色餐厅'、'市区舒适酒店'等通用模板" +
        "\n- 禁止使用'某市'、'某餐厅'、'某酒店'等模糊表述" +
        "\n- 禁止使用'待探索'、'待品尝'等空泛描述" +
        "\n- 禁止使用'```json'等markdown标记" +
        "\n\n【必须使用真实名称】：" +
        "\n- 景点：\"故宫博物院\"、\"秦始皇兵马俑博物馆\"、\"苏州拙政园\"、\"杭州西湖\"" +
        "\n- 餐厅：\"全聚德烤鸭前门店\"、\"西安老米家大雨泡馍\"、\"苏州得月楼\"" +
        "\n- 酒店：\"北京饭店\"、\"西安君悦酒店\"、\"如家快捷酒店\"" +
        "\n\n【JSON格式示例（必须严格遵循）】：" +
        "\n{" +
        "\n  \"overview\": {" +
        "\n    \"totalAttractions\": 8," +
        "\n    \"totalDistance\": \"约300公里\"," +
        "\n    \"pace\": \"适中\"" +
        "\n  }," +
        "\n  \"days\": [" +
        "\n    {" +
        "\n      \"title\": \"第一天：北京历史文化探索\"," +
        "\n      \"date\": \"第1天\"," +
        "\n      \"activities\": [" +
        "\n        {" +
        "\n          \"type\": \"attraction\"," +
        "\n          \"title\": \"故宫博物院\"," +
        "\n          \"description\": \"明清两代皇宫，世界文化遗产\"," +
        "\n          \"time\": \"09:00\"," +
        "\n          \"duration\": \"3小时\"," +
        "\n          \"cost\": \"门票60元\"," +
        "\n          \"bookingUrl\": \"https://gugong.ktmtech.cn/\"" +
        "\n        }" +
        "\n      ]" +
        "\n    }" +
        "\n  ]" +
        "\n}" +
        "\n\ntype可选值: attraction, restaurant, hotel, transport" +
        "\n\n【交通(transport)类型要求】：" +
        "\n- title格式必须为\"出发城市→到达城市 高铁\"，例如\"北京→西安 高铁\"、\"西安→成都 高铁\"" +
        "\n- 禁止写具体车次号（如G87、D2001等），只写\"高铁\"或\"动车\"" +
        "\n- description简要说明行程时间，如\"约4.5小时，二等座约515元\"" +
        "\n\n【预订链接要求】所有activity的bookingUrl字段统一填空字符串\"\"，系统会自动生成正确的预订链接。" +
        "\n\n注意：必须使用双引号，不能使用单引号！";

    // 常用目的地景点/餐厅数据库（用于备用方案）
    private static final Map<String, List<String>> ATTRACTION_DB = new HashMap<>();
    private static final Map<String, List<String>> RESTAURANT_DB = new HashMap<>();
    private static final Map<String, List<String>> HOTEL_DB = new HashMap<>();

    static {
        // 北京
        ATTRACTION_DB.put("北京", Arrays.asList("故宫博物院", "天安门广场", "颐和园", "天坛公园", "八达岭长城", "圆明园", "北海公园", "恭王府", "南锣鼓巷", "鸟巢水立方"));
        RESTAURANT_DB.put("北京", Arrays.asList("全聚德烤鸭", "东来顺涮羊肉", "护国寺小吃", "庆丰包子铺", "海碗居炸酱面", "四季民福烤鸭", "聚宝源涮肉", "簋街胡大饭馆"));
        HOTEL_DB.put("北京", Arrays.asList("北京饭店", "王府半岛酒店", "如家快捷酒店", "汉庭酒店", "希尔顿酒店", "洲际酒店"));

        // 西安
        ATTRACTION_DB.put("西安", Arrays.asList("秦始皇兵马俑博物馆", "西安城墙", "大雁塔", "陕西历史博物馆", "华清宫", "钟鼓楼", "回民街", "大唐芙蓉园", "大明宫国家遗址公园", "碑林博物馆"));
        RESTAURANT_DB.put("西安", Arrays.asList("老米家大雨泡馍", "秦豫肉夹馍", "子午路张记肉夹馍", "西安饭庄", "德发长饺子", "春发生葫芦头", "同盛祥牛羊肉泡馍"));
        HOTEL_DB.put("西安", Arrays.asList("西安钟楼饭店", "西安君悦酒店", "西安威斯汀酒店", "如家快捷酒店", "汉庭酒店", "希尔顿酒店"));

        // 上海
        ATTRACTION_DB.put("上海", Arrays.asList("外滩", "东方明珠", "豫园", "南京路步行街", "上海博物馆", "田子坊", "新天地", "陆家嘴", "朱家角古镇", "上海迪士尼"));
        RESTAURANT_DB.put("上海", Arrays.asList("南翔馒头店", "老正兴菜馆", "德兴馆", "王宝和酒家", "绿波廊", "小杨生煎", "鲜得来排骨年糕"));
        HOTEL_DB.put("上海", Arrays.asList("和平饭店", "浦东香格里拉", "如家快捷酒店", "汉庭酒店", "希尔顿酒店", "万豪酒店"));

        // 成都
        ATTRACTION_DB.put("成都", Arrays.asList("宽窄巷子", "锦里古街", "大熊猫繁育研究基地", "武侯祠", "杜甫草堂", "青城山", "都江堰", "春熙路", "文殊院", "金沙遗址博物馆"));
        RESTAURANT_DB.put("成都", Arrays.asList("小龙坎火锅", "蜀大侠火锅", "陈麻婆豆腐", "钟水饺", "龙抄手", "夫妻肺片", "担担面", "串串香"));
        HOTEL_DB.put("成都", Arrays.asList("锦江宾馆", "成都博舍", "如家快捷酒店", "汉庭酒店", "希尔顿酒店", "洲际酒店"));

        // 杭州
        ATTRACTION_DB.put("杭州", Arrays.asList("西湖", "灵隐寺", "千岛湖", "宋城", "西溪湿地", "雷峰塔", "断桥残雪", "苏堤春晓", "河坊街", "岳王庙"));
        RESTAURANT_DB.put("杭州", Arrays.asList("楼外楼", "知味观", "外婆家", "绿茶餐厅", "张生记", "奎元馆", "新白鹿餐厅"));
        HOTEL_DB.put("杭州", Arrays.asList("西湖国宾馆", "杭州香格里拉", "如家快捷酒店", "汉庭酒店", "希尔顿酒店", "万豪酒店"));

        // 南京
        ATTRACTION_DB.put("南京", Arrays.asList("中山陵", "夫子庙", "秦淮河", "总统府", "南京博物院", "明孝陵", "鸡鸣寺", "玄武湖", "侵华日军南京大屠杀遇难同胞纪念馆", "老门东"));
        RESTAURANT_DB.put("南京", Arrays.asList("南京大牌档", "狮子桥美食街", "尹氏汤包", "回味鸭血粉丝汤", "刘长兴", "绿柳居", "奇芳阁"));
        HOTEL_DB.put("南京", Arrays.asList("金陵饭店", "南京香格里拉", "如家快捷酒店", "汉庭酒店", "希尔顿酒店", "洲际酒店"));

        // 默认
        ATTRACTION_DB.put("默认", Arrays.asList("当地著名景点", "历史文化街区", "城市地标建筑", "特色博物馆", "自然风光区"));
        RESTAURANT_DB.put("默认", Arrays.asList("当地老字号餐厅", "特色小吃街", "网红美食店", "地方菜名店"));
        HOTEL_DB.put("默认", Arrays.asList("当地连锁酒店", "快捷酒店", "精品民宿"));
    }

    public RecommendServiceImpl() {
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        return new RestTemplate(factory);
    }

    @Override
    public RecommendVO getPersonalizedRecommend(Long userId, RecommendRequestDTO request) {
        RecommendVO vo = new RecommendVO();

        // 尝试使用混合推荐算法
        List<Long> recommendedIds = recommendAlgorithmService.getHybridRecommendations(userId, 8);

        if (!recommendedIds.isEmpty()) {
            List<Attraction> attractions = attractionMapper.selectBatchIds(recommendedIds);
            // 按推荐顺序排序
            Map<Long, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < recommendedIds.size(); i++) {
                orderMap.put(recommendedIds.get(i), i);
            }
            attractions.sort(Comparator.comparingInt(a -> orderMap.getOrDefault(a.getId(), 999)));

            vo.setAttractions(attractions.stream().map(this::convertToVO).collect(Collectors.toList()));
            vo.setRecommendType("智能推荐");
            vo.setReason("基于您的浏览历史和相似用户偏好");
            vo.setMatchScore(0.85);
        } else {
            // 冷启动降级：按评分+浏览量推荐热门景点
            List<Attraction> hotAttractions = attractionMapper.selectList(
                    new LambdaQueryWrapper<Attraction>()
                            .eq(Attraction::getAuditStatus, 1)
                            .orderByDesc(Attraction::getRating)
                            .orderByDesc(Attraction::getViewCount)
                            .last("LIMIT 8")
            );
            vo.setAttractions(hotAttractions.stream().map(this::convertToVO).collect(Collectors.toList()));
            vo.setRecommendType("热门推荐");
            vo.setReason("为您推荐热门景点");
            vo.setMatchScore(0.6);
        }
        return vo;
    }

    private AttractionVO convertToVO(Attraction attraction) {
        AttractionVO vo = new AttractionVO();
        vo.setId(attraction.getId());
        vo.setName(attraction.getName());
        vo.setCity(attraction.getCity());
        vo.setProvince(attraction.getProvince());
        vo.setAddress(attraction.getAddress());
        vo.setDescription(attraction.getDescription());
        vo.setRating(attraction.getRating());
        vo.setScenicLevel(attraction.getScenicLevel());
        vo.setTicketPrice(attraction.getTicketPrice());
        vo.setViewCount(attraction.getViewCount());
        vo.setImages(new ArrayList<>());
        vo.setTags(new ArrayList<>());
        return vo;
    }

    @Override
    public RoutePlanVO generateRoutePlan(Long userId, RoutePlanRequestDTO request) {
        String userPrompt = buildRoutePlanPrompt(request);
        RoutePlanVO result = null;

        // 获取目的地天气信息（用于增强提示词）
        String weatherInfo = getWeatherInfoForPrompt(request.getDestinations());

        // 尝试调用AI，失败时记录原因
        try {
            log.info("开始调用DeepSeek API生成行程，目的地：{}，天数：{}",
                request.getDestinations(), request.getDays());
            String aiResponse = callDeepSeekAPI(userPrompt + "\n\n" + weatherInfo);

            if (aiResponse != null && !aiResponse.isEmpty()) {
                try {
                    result = parseAIResponse(aiResponse, request);
                    // 验证结果是否包含降级内容
                    if (result != null && result.getDays() != null
                            && !result.getDays().isEmpty()
                            && isValidRoutePlan(result)) {
                        log.info("AI生成行程成功，包含 {} 天", result.getDays().size());
                        // 增强行程：添加实时交通和天气信息
                        enhanceRoutePlanWithRealTimeData(result, request);
                        return result;
                    } else {
                        log.warn("AI返回的行程包含通用模板，使用备用方案");
                    }
                } catch (Exception parseEx) {
                    log.warn("AI响应解析失败: {} - {}", parseEx.getClass().getSimpleName(), parseEx.getMessage());
                }
            }
        } catch (ResourceAccessException e) {
            log.error("DeepSeek API调用超时: {}", e.getMessage());
        } catch (Exception e) {
            log.error("调用AI生成行程失败: {}", e.getMessage());
        }

        // 使用优化的备用方案
        log.info("使用备用方案生成行程");
        RoutePlanVO fallbackPlan = generateEnhancedFallbackRoutePlan(request);
        // 增强备用方案
        enhanceRoutePlanWithRealTimeData(fallbackPlan, request);
        return fallbackPlan;
    }

    /**
     * 获取天气信息用于提示词
     */
    private String getWeatherInfoForPrompt(List<String> destinations) {
        if (destinations == null || destinations.isEmpty()) {
            return "";
        }

        StringBuilder weatherInfo = new StringBuilder();
        weatherInfo.append("【目的地天气参考】\n");

        for (String dest : destinations) {
            try {
                WeatherApiService.WeatherNow weather = weatherApiService.getWeatherNow(dest);
                if (weather != null && weather.getTemp() != null && !"--".equals(weather.getTemp())) {
                    weatherInfo.append(dest).append("：")
                            .append(weather.getText()).append("，")
                            .append(weather.getTemp()).append("°C，")
                            .append(weather.getWindDir()).append(weather.getWindScale()).append("级\n");
                }
            } catch (Exception e) {
                log.debug("获取{}天气失败: {}", dest, e.getMessage());
            }
        }

        return weatherInfo.toString();
    }

    /**
     * 增强行程：添加实时交通和天气信息
     */
    private void enhanceRoutePlanWithRealTimeData(RoutePlanVO plan, RoutePlanRequestDTO request) {
        if (plan == null || plan.getDays() == null) {
            return;
        }

        // 获取目的地天气预报
        String mainDest = (request.getDestinations() != null && !request.getDestinations().isEmpty())
                ? request.getDestinations().get(0) : null;

        List<WeatherApiService.WeatherDaily> forecast = null;
        if (mainDest != null) {
            try {
                forecast = weatherApiService.getWeatherForecast(mainDest, plan.getDays().size());
            } catch (Exception e) {
                log.debug("获取天气预报失败: {}", e.getMessage());
            }
        }

        // 为每天添加天气信息
        for (int i = 0; i < plan.getDays().size(); i++) {
            RoutePlanVO.DayPlanVO day = plan.getDays().get(i);

            // 添加天气信息到标题
            if (forecast != null && i < forecast.size()) {
                WeatherApiService.WeatherDaily dayWeather = forecast.get(i);
                if (dayWeather.getTextDay() != null && !"暂无数据".equals(dayWeather.getTextDay())) {
                    String weatherSuffix = String.format(" [%s %s~%s°C]",
                            dayWeather.getTextDay(),
                            dayWeather.getTempMin(),
                            dayWeather.getTempMax());
                    day.setTitle(day.getTitle() + weatherSuffix);
                }
            }

            // 为活动添加交通信息
            enhanceDayActivitiesWithTraffic(day, mainDest);
        }

        // 设置天气建议
        if (mainDest != null) {
            String travelAdvice = weatherApiService.getTravelAdvice(mainDest);
            if (travelAdvice != null && !travelAdvice.isEmpty()) {
                plan.setWeatherAdvice(travelAdvice);
            }
        }

        // 增强预订链接
        enhanceBookingUrls(plan, request);
    }

    /**
     * 为行程中的活动增强预订/购票链接
     */
    private void enhanceBookingUrls(RoutePlanVO plan, RoutePlanRequestDTO request) {
        if (plan == null || plan.getDays() == null || bookingUrlService == null) {
            return;
        }

        String departureCity = request.getDepartureCity();
        List<String> destinations = request.getDestinations();
        String startDate = request.getStartDate();

        // 预先计算每天所在城市：通过transport活动追踪城市切换，结合day title匹配
        List<String> dayCities = resolveDayCities(plan.getDays(), destinations);

        for (int dayIndex = 0; dayIndex < plan.getDays().size(); dayIndex++) {
            RoutePlanVO.DayPlanVO day = plan.getDays().get(dayIndex);
            if (day.getActivities() == null) {
                continue;
            }

            String currentCity = dayCities.get(dayIndex);

            // 计算当天的真实日期（yyyy-MM-dd）
            String dayRealDate = null;
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                    dayRealDate = start.plusDays(dayIndex).toString();
                } catch (Exception e) {
                    log.debug("解析开始日期失败: {}", startDate);
                }
            }

            for (RoutePlanVO.ActivityVO activity : day.getActivities()) {
                String aiUrl = activity.getBookingUrl();
                String type = activity.getType();
                if (type == null) {
                    continue;
                }

                try {
                    switch (type) {
                        case "transport" -> {
                            String title = activity.getTitle() != null ? activity.getTitle() : "";
                            String desc = activity.getDescription() != null ? activity.getDescription() : "";
                            if (title.contains("高铁") || title.contains("火车") || title.contains("动车")
                                    || desc.contains("高铁") || desc.contains("火车") || desc.contains("动车")) {
                                // 智能推断高铁出发地和目的地
                                String[] trainCities = inferTrainCities(
                                        dayIndex, departureCity, destinations, title, desc);
                                // 使用携程火车票链接
                                activity.setBookingUrl(
                                        bookingUrlService.generateCtripTrainUrl(trainCities[0], trainCities[1], dayRealDate)
                                );
                            }
                        }
                        case "attraction" -> {
                            // 统一使用携程景点搜索链接
                            String resolved = bookingUrlService.generateCtripAttractionUrl(activity.getTitle(), currentCity);
                            if (resolved != null) {
                                activity.setBookingUrl(resolved);
                            }
                        }
                        case "hotel" -> {
                            // 计算次日日期作为退房日期
                            String checkoutDate = null;
                            if (dayRealDate != null) {
                                try {
                                    checkoutDate = java.time.LocalDate.parse(dayRealDate).plusDays(1).toString();
                                } catch (Exception ignored) {}
                            }
                            String resolved = bookingUrlService.resolveHotelUrl(
                                    activity.getTitle(), currentCity, dayRealDate, checkoutDate);
                            if (resolved != null) {
                                activity.setBookingUrl(resolved);
                            }
                        }
                        case "restaurant" -> {
                            // 统一使用携程美食搜索链接
                            String resolved = bookingUrlService.generateCtripRestaurantUrl(
                                    activity.getTitle(), currentCity);
                            if (resolved != null) {
                                activity.setBookingUrl(resolved);
                            }
                        }
                        default -> { }
                    }
                } catch (Exception e) {
                    log.debug("增强预订链接失败: activity={}, error={}", activity.getTitle(), e.getMessage());
                }
            }
        }
    }

    /**
     * 预先计算每天所在城市
     * 策略：
     * 1. 检测transport活动中的"A→B"模式，提取到达城市作为当天及后续天的城市
     * 2. 检测day title中是否包含目的地城市名
     * 3. 以上都未匹配时，沿用上一天的城市（粘滞机制）
     * 4. 初始城市为目的地列表的第一个
     */
    private List<String> resolveDayCities(List<RoutePlanVO.DayPlanVO> days, List<String> destinations) {
        List<String> result = new ArrayList<>();
        if (destinations == null || destinations.isEmpty()) {
            for (int i = 0; i < days.size(); i++) {
                result.add("");
            }
            return result;
        }

        String lastKnownCity = destinations.get(0);
        String[] arrowPatterns = {"→", "->", "—", "至", "到"};

        for (RoutePlanVO.DayPlanVO day : days) {
            String detectedCity = null;

            // 1. 从transport活动的"A→B"标题中提取到达城市（最可靠）
            if (day.getActivities() != null) {
                for (RoutePlanVO.ActivityVO act : day.getActivities()) {
                    if ("transport".equals(act.getType()) && act.getTitle() != null) {
                        for (String arrow : arrowPatterns) {
                            if (act.getTitle().contains(arrow)) {
                                String afterArrow = act.getTitle().split(arrow, 2)[1]
                                        .replaceAll("[\\s高铁动车火车]", "").trim();
                                for (String dest : destinations) {
                                    if (afterArrow.contains(dest) || dest.contains(afterArrow)) {
                                        detectedCity = dest;
                                        break;
                                    }
                                }
                                if (detectedCity != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (detectedCity != null) {
                        break;
                    }
                }
            }

            // 2. 从day title中匹配目的地城市名
            if (detectedCity == null && day.getTitle() != null) {
                for (String dest : destinations) {
                    if (dest != null && !dest.isEmpty() && day.getTitle().contains(dest)) {
                        detectedCity = dest;
                        break;
                    }
                }
            }

            // 检测到新城市则更新，否则沿用上一天的城市
            if (detectedCity != null) {
                lastKnownCity = detectedCity;
            }

            result.add(lastKnownCity);
        }

        return result;
    }

    /**
     * 智能推断高铁出发地和目的地
     * 优先从标题中提取"A→B"格式，失败则根据行程天数位置推断
     */
    private String[] inferTrainCities(int dayIndex, String departureCity,
                                       List<String> destinations, String title, String desc) {
        // 优先从标题中提取"A→B"、"A->B"、"A—B"、"A至B"格式
        String[] arrowPatterns = {"→", "->", "—", "至", "到"};
        for (String arrow : arrowPatterns) {
            if (title.contains(arrow)) {
                String[] parts = title.split(arrow, 2);
                if (parts.length == 2) {
                    // 清理城市名（去掉"高铁""动车""火车"等后缀）
                    String from = parts[0].replaceAll("[\\s高铁动车火车]", "").trim();
                    String to = parts[1].replaceAll("[\\s高铁动车火车]", "").trim();
                    if (!from.isEmpty() && !to.isEmpty()) {
                        return new String[]{from, to};
                    }
                }
            }
        }

        // 兜底：根据行程天数位置推断
        String combined = title + " " + desc;
        boolean isReturn = combined.contains("返回") || combined.contains("回程");

        if (isReturn) {
            String lastDest = (destinations != null && !destinations.isEmpty())
                    ? destinations.get(destinations.size() - 1) : departureCity;
            return new String[]{lastDest, departureCity};
        }

        // 第一天：出发城市 → 第一个目的地
        if (dayIndex == 0) {
            String toCity = (destinations != null && !destinations.isEmpty()) ? destinations.get(0) : "";
            return new String[]{departureCity, toCity};
        }

        // 中间天数：前一个目的地 → 当天目的地
        if (destinations != null && !destinations.isEmpty()) {
            String fromCity = destinations.get(Math.min(dayIndex - 1, destinations.size() - 1));
            String toCity = destinations.get(Math.min(dayIndex, destinations.size() - 1));
            // 如果from和to相同，说明是最后一天返程
            if (fromCity.equals(toCity)) {
                return new String[]{fromCity, departureCity};
            }
            return new String[]{fromCity, toCity};
        }

        return new String[]{departureCity, ""};
    }

    /**
     * 为每日活动添加交通信息
     */
    private void enhanceDayActivitiesWithTraffic(RoutePlanVO.DayPlanVO day, String city) {
        if (day.getActivities() == null || day.getActivities().size() < 2) {
            return;
        }

        List<RoutePlanVO.ActivityVO> activities = day.getActivities();

        for (int i = 0; i < activities.size() - 1; i++) {
            RoutePlanVO.ActivityVO current = activities.get(i);
            RoutePlanVO.ActivityVO next = activities.get(i + 1);

            // 只为景点和餐厅之间计算交通
            if (("attraction".equals(current.getType()) || "restaurant".equals(current.getType()))
                    && ("attraction".equals(next.getType()) || "restaurant".equals(next.getType()))) {

                try {
                    // 使用高德地图API获取路线
                    MapApiService.RouteResult route = mapApiService.getDrivingRoute(
                            current.getTitle() + " " + city,
                            next.getTitle() + " " + city,
                            4  // 躲避拥堵
                    );

                    if (route != null && !"未知".equals(route.getDistance())) {
                        // 更新距离和交通信息
                        String formattedDistance = mapApiService.formatDistance(route.getDistance());
                        String formattedDuration = mapApiService.formatDuration(route.getDuration());

                        next.setDistance(formattedDistance);
                        next.setTransport(String.format("驾车约%s，%s", formattedDuration, route.getTrafficStatus()));
                    }
                } catch (Exception e) {
                    log.debug("获取交通信息失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 验证行程是否有效（不包含通用模板）
     */
    private boolean isValidRoutePlan(RoutePlanVO plan) {
        if (plan == null || plan.getDays() == null || plan.getDays().isEmpty()) {
            return false;
        }

        // 检查是否包含降级关键词
        List<String> invalidKeywords = Arrays.asList(
            "著名景点1", "著名景点2", "著名景点3",
            "当地特色餐厅", "当地餐厅",
            "市区舒适酒店", "市中心酒店",
            "某市", "某餐厅", "某酒店"
        );

        for (RoutePlanVO.DayPlanVO day : plan.getDays()) {
            if (day.getActivities() != null) {
                for (RoutePlanVO.ActivityVO activity : day.getActivities()) {
                    String title = activity.getTitle();
                    if (title != null) {
                        for (String keyword : invalidKeywords) {
                            if (title.contains(keyword)) {
                                log.warn("检测到降级关键词: {} in {}", keyword, title);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 构建路线规划提示词
     */
    private String buildRoutePlanPrompt(RoutePlanRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请规划一次").append(request.getDays()).append("天旅行：\n\n");

        if (request.getDepartureProvince() != null) {
            prompt.append("【出发地】").append(request.getDepartureProvince())
                .append(" ").append(request.getDepartureCity()).append("\n");
        }

        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            prompt.append("【目的地】").append(String.join("、", request.getDestinations())).append("\n");
        }

        if (request.getMonth() != null) {
            String monthName = java.time.Month.of(request.getMonth())
                .getDisplayName(TextStyle.FULL, Locale.CHINESE);
            prompt.append("【出行时间】").append(monthName).append("\n");
        }

        if (request.getCompanion() != null && !request.getCompanion().isEmpty()) {
            prompt.append("【同行人员】").append(request.getCompanion()).append("\n");
        }

        if (request.getStylePreferences() != null && !request.getStylePreferences().isEmpty()) {
            prompt.append("【旅行偏好】").append(String.join("、", request.getStylePreferences())).append("\n");
        }

        if (request.getPace() != null && !request.getPace().isEmpty()) {
            prompt.append("【行程节奏】").append(request.getPace()).append("\n");
        }

        if (request.getRemark() != null && !request.getRemark().isEmpty()) {
            prompt.append("【特殊要求】").append(request.getRemark()).append("\n");
        }

        prompt.append("\n【重要】请确保：\n");
        prompt.append("1. 每个景点都有具体的真实名称（不能是'著名景点1'）\n");
        prompt.append("2. 每个餐厅都有具体的真实名称（不能是'当地餐厅'）\n");
        prompt.append("3. 每个酒店都有具体的真实名称（不能是'市区酒店'）\n");
        prompt.append("4. 如果确实有不确定的信息，请标注'待补充'，但不要用通用模板\n");

        return prompt.toString();
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String userPrompt) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", ROUTE_PLAN_SYSTEM_PROMPT);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4096);
        requestBody.put("temperature", 0.3); // 降低温度以获得更确定性的结果
        requestBody.put("top_p", 0.9);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        log.info("调用DeepSeek API，URL: {}, 超时设置: {}ms", baseUrl, TIMEOUT_MS);
        log.debug("用户提示词: {}", userPrompt);

        String url = baseUrl + "/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(
            url, org.springframework.http.HttpMethod.POST, entity, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String responseBody = response.getBody();
            log.info("DeepSeek API响应成功，原始响应长度: {} 字符", responseBody.length());

            // 打印原始响应的前1000字符用于调试
            String debugPreview = responseBody.length() > 1000 ? responseBody.substring(0, 1000) : responseBody;
            log.debug("DeepSeek API响应预览: {}", debugPreview);

            // 提取content
            String content = objectMapper.readTree(responseBody)
                    .path("choices").path(0).path("message").path("content").asText();

            log.info("提取的content长度: {} 字符", content.length());
            log.debug("Content预览（前500字符）: {}", content.length() > 500 ? content.substring(0, 500) : content);

            // 检查是否包含单引号
            if (content.contains("'")) {
                log.warn("警告：AI响应包含单引号，可能导致JSON解析失败");
            }

            return extractJsonFromResponse(content);
        } else {
            log.warn("DeepSeek API返回非200状态: {}", response.getStatusCode());
            return null;
        }
    }

    /**
     * 从AI响应中提取JSON内容
     * 增强处理AI输出的单引号JSON和markdown代码块
     */
    private String extractJsonFromResponse(String response) {
        log.info("开始提取JSON，原始响应长度: {} 字符", response.length());
        log.debug("原始响应内容（前500字符）: {}", response.length() > 500 ? response.substring(0, 500) : response);

        // 首先尝试提取markdown代码块中的JSON
        if (response.contains("```json")) {
            int codeStart = response.indexOf("```json") + 7;
            int codeEnd = response.indexOf("```", codeStart);
            if (codeEnd > codeStart) {
                String codeContent = response.substring(codeStart, codeEnd).trim();
                log.info("从markdown代码块中提取JSON，长度: {} 字符", codeContent.length());
                return normalizeQuotesIfNeeded(codeContent);
            }
        } else if (response.contains("```")) {
            // 尝试普通的代码块
            int codeStart = response.indexOf("```") + 3;
            int codeEnd = response.indexOf("```", codeStart);
            if (codeEnd > codeStart) {
                String codeContent = response.substring(codeStart, codeEnd).trim();
                log.info("从代码块中提取JSON，长度: {} 字符", codeContent.length());
                return normalizeQuotesIfNeeded(codeContent);
            }
        }

        // 查找第一个 {
        int start = response.indexOf("{");
        if (start < 0) {
            log.warn("未找到JSON起始符号 {{");
            return response;
        }

        // 从start开始，匹配大括号找到完整的JSON结束位置
        int braceCount = 0;
        int end = -1;
        for (int i = start; i < response.length(); i++) {
            char c = response.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    end = i;
                    break;
                }
            }
        }

        if (end >= start) {
            String json = response.substring(start, end + 1);
            log.info("提取到JSON，长度: {} 字符", json.length());

            // 尝试规范化引号
            return normalizeQuotesIfNeeded(json);
        }

        log.warn("无法提取完整JSON，返回原始响应");
        return response;
    }

    /**
     * 检查并规范化JSON中的引号（如果需要）
     * 先尝试直接解析，失败则进行转换
     */
    private String normalizeQuotesIfNeeded(String json) {
        // 先尝试直接解析，如果成功则直接返回
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);
            log.info("JSON格式正确，无需规范化");
            return json;
        } catch (Exception e) {
            log.info("JSON解析失败，尝试规范化引号: {}", e.getMessage());
            return convertSingleQuoteJson(json);
        }
    }

    /**
     * 智能地将单引号JSON转换为双引号JSON
     * 使用状态机精确处理引号替换
     */
    private String convertSingleQuoteJson(String json) {
        // 尝试简单的替换（适用于大多数情况）
        String simpleReplaced = json.replace("'", "\"");

        // 验证替换后的JSON是否有效
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(simpleReplaced);
            log.info("简单替换成功，JSON已规范化");
            return simpleReplaced;
        } catch (Exception e) {
            log.warn("简单替换失败，尝试智能转换: {}", e.getMessage());
        }

        // 简单替换失败，返回原始内容
        // 在实际场景中，复杂的单引号JSON处理需要更复杂的解析器
        log.error("JSON规范化失败，返回原始内容");
        return json;
    }

    /**
     * 解析AI响应为RoutePlanVO
     * 增强错误处理，支持缺失字段和空值
     */
    private RoutePlanVO parseAIResponse(String jsonContent, RoutePlanRequestDTO request) {
        try {
            // 配置 ObjectMapper 以更宽松的方式解析
            ObjectMapper lenientMapper = new ObjectMapper();
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

            JsonNode root = lenientMapper.readTree(jsonContent);
            RoutePlanVO vo = new RoutePlanVO();

            // 解析总览信息
            JsonNode overview = root.path("overview");
            if (overview != null && !overview.isMissingNode() && overview.isObject()) {
                JsonNode attractionsNode = overview.path("totalAttractions");
                if (attractionsNode != null && !attractionsNode.isMissingNode()) {
                    vo.setTotalAttractions(attractionsNode.asInt(0));
                }
                JsonNode distanceNode = overview.path("totalDistance");
                if (distanceNode != null && !distanceNode.isMissingNode() && distanceNode.isTextual()) {
                    vo.setTotalDistance(distanceNode.asText());
                } else {
                    vo.setTotalDistance("约" + (request.getDays() * 150) + "公里");
                }
            } else {
                vo.setTotalAttractions(request.getDays() * 2);
                vo.setTotalDistance("约" + (request.getDays() * 150) + "公里");
            }

            // 解析每日行程
            List<RoutePlanVO.DayPlanVO> days = new ArrayList<>();
            JsonNode daysNode = root.path("days");

            if (daysNode != null && daysNode.isArray()) {
                for (JsonNode dayNode : daysNode) {
                    try {
                        RoutePlanVO.DayPlanVO day = new RoutePlanVO.DayPlanVO();
                        day.setTitle(getTextValue(dayNode, "title", ""));
                        day.setDate(getTextValue(dayNode, "date", ""));

                        List<RoutePlanVO.ActivityVO> activities = new ArrayList<>();
                        JsonNode activitiesNode = dayNode.path("activities");

                        if (activitiesNode != null && activitiesNode.isArray()) {
                            for (JsonNode activityNode : activitiesNode) {
                                try {
                                    RoutePlanVO.ActivityVO activity = new RoutePlanVO.ActivityVO();
                                    activity.setType(getTextValue(activityNode, "type", ""));
                                    activity.setTitle(getTextValue(activityNode, "title", ""));
                                    activity.setDescription(getTextValue(activityNode, "description", ""));
                                    activity.setTime(getTextValue(activityNode, "time", ""));
                                    activity.setDuration(getTextValue(activityNode, "duration", ""));
                                    activity.setDistance(getTextValue(activityNode, "distance", ""));
                                    activity.setTransport(getTextValue(activityNode, "transport", ""));
                                    activity.setCost(getTextValue(activityNode, "cost", ""));
                                    activity.setTips(getTextValue(activityNode, "tips", ""));
                                    activity.setImage(getNullableTextValue(activityNode, "image"));
                                    activity.setBookingUrl(getNullableTextValue(activityNode, "bookingUrl"));

                                    // 跳过无效活动（没有title和type）
                                    if (activity.getType() != null && !activity.getType().isEmpty()
                                        && activity.getTitle() != null && !activity.getTitle().isEmpty()) {
                                        activities.add(activity);
                                    }
                                } catch (Exception e) {
                                    log.warn("解析activity失败: {}", e.getMessage());
                                }
                            }
                        }

                        day.setActivities(activities);
                        // 只添加有有效活动的天数
                        if (!activities.isEmpty()) {
                            days.add(day);
                        }
                    } catch (Exception e) {
                        log.warn("解析day失败: {}", e.getMessage());
                    }
                }
            }

            vo.setDays(days);
            vo.setTitle(buildTitle(request));
            return vo;

        } catch (Exception e) {
            log.error("解析AI响应JSON失败: {}", e.getMessage());
            throw new RuntimeException("解析AI响应失败", e);
        }
    }

    /**
     * 生成增强型备用路线规划（带具体景点名称）
     */
    private RoutePlanVO generateEnhancedFallbackRoutePlan(RoutePlanRequestDTO request) {
        RoutePlanVO vo = new RoutePlanVO();
        vo.setTitle(buildTitle(request));
        vo.setTotalAttractions(request.getDays() * 2);
        vo.setTotalDistance("约" + (request.getDays() * 150) + "公里");

        List<RoutePlanVO.DayPlanVO> days = new ArrayList<>();
        String monthName = request.getMonth() != null
            ? java.time.Month.of(request.getMonth()).getDisplayName(TextStyle.FULL, Locale.CHINESE)
            : "当前";

        List<String> destinations = request.getDestinations() != null && !request.getDestinations().isEmpty()
            ? request.getDestinations()
            : List.of("目的地");

        // 为每一天生成行程，循环使用目的地
        for (int i = 1; i <= request.getDays(); i++) {
            String destination = destinations.get((i - 1) % destinations.size());
            RoutePlanVO.DayPlanVO day = new RoutePlanVO.DayPlanVO();
            day.setTitle("第" + i + "天：" + destination + "探索之旅");
            day.setDate(monthName + "行程");
            day.setActivities(generateEnhancedDayActivities(i, destination));
            days.add(day);
        }

        vo.setDays(days);
        return vo;
    }

    /**
     * 生成增强型每日活动（使用数据库中的真实名称）
     */
    private List<RoutePlanVO.ActivityVO> generateEnhancedDayActivities(int dayNum, String destination) {
        List<RoutePlanVO.ActivityVO> activities = new ArrayList<>();

        // 从数据库获取该目的地的景点、餐厅、酒店
        List<String> attractions = ATTRACTION_DB.getOrDefault(destination, ATTRACTION_DB.get("默认"));
        List<String> restaurants = RESTAURANT_DB.getOrDefault(destination, RESTAURANT_DB.get("默认"));
        List<String> hotels = HOTEL_DB.getOrDefault(destination, HOTEL_DB.get("默认"));

        // 循环使用景点和餐厅
        String attraction1 = attractions.get((dayNum - 1) % attractions.size());
        String attraction2 = attractions.get(dayNum % attractions.size());
        String restaurant1 = restaurants.get((dayNum - 1) % restaurants.size());
        String restaurant2 = restaurants.get(dayNum % restaurants.size());
        String hotel = hotels.get((dayNum - 1) % hotels.size());

        // 上午景点
        activities.add(createActivity("attraction",
            attraction1,
            "游览" + destination + "著名景点" + attraction1 + "，感受当地独特的历史文化和风土人情。建议提前了解景点背景，以便更好地欣赏。",
            "09:00", "2.5小时", "约2公里", "地铁或打车", "门票约80元", "建议提前网上购票，避开高峰时段"));

        // 午餐
        activities.add(createActivity("restaurant",
            restaurant1,
            "在" + destination + "知名餐厅" + restaurant1 + "品尝地道美食，体验当地饮食文化。",
            "12:00", "1.5小时", "约1公里", "步行", "人均约80元", "建议提前预订座位"));

        // 下午景点
        activities.add(createActivity("attraction",
            attraction2,
            "参观" + destination + "知名景点" + attraction2 + "，深入了解当地历史人文。建议预留充足时间细细品味。",
            "14:00", "2.5小时", "约3公里", "地铁或打车", "门票约60元", "建议携带身份证件"));

        // 晚餐
        activities.add(createActivity("restaurant",
            restaurant2,
            "品尝" + destination + "特色美食，在" + restaurant2 + "享受愉快的晚餐时光。",
            "18:00", "1.5小时", "约2公里", "步行或打车", "人均约70元", "可尝试当地特色菜品"));

        // 住宿
        activities.add(createActivity("hotel",
            hotel,
            "入住" + hotel + "，休息调整，为明天的行程做好准备。",
            "20:00", "10小时", "约0公里", "步行", "约300元/晚", "建议提前确认入住信息"));

        return activities;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveRoutePlan(Long userId, RoutePlanVO routePlanVO) {
        RoutePlan routePlan = new RoutePlan();
        routePlan.setUserId(userId);
        routePlan.setTitle(routePlanVO.getTitle());

        // 提取目的地
        StringBuilder destBuilder = new StringBuilder();
        if (routePlanVO.getDays() != null && !routePlanVO.getDays().isEmpty()) {
            for (RoutePlanVO.DayPlanVO day : routePlanVO.getDays()) {
                for (RoutePlanVO.ActivityVO activity : day.getActivities()) {
                    if ("attraction".equals(activity.getType())) {
                        if (destBuilder.length() > 0) {
                            destBuilder.append(",");
                        }
                        destBuilder.append(activity.getTitle());
                        break;
                    }
                }
            }
        }
        routePlan.setDestination(destBuilder.toString());
        routePlan.setDays(routePlanVO.getDays() != null ? routePlanVO.getDays().size() : 0);
        routePlan.setIsAiGenerated(1);

        // 转换VO为JSON
        try {
            String json = objectMapper.writeValueAsString(routePlanVO);
            routePlan.setPlanData(json);
        } catch (Exception e) {
            log.error("转换路线规划数据失败", e);
        }

        routePlanMapper.insert(routePlan);
        return routePlan.getId();
    }

    @Override
    public List<RoutePlan> getUserRoutePlans(Long userId) {
        LambdaQueryWrapper<RoutePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoutePlan::getUserId, userId)
                .orderByDesc(RoutePlan::getCreateTime);
        return routePlanMapper.selectList(wrapper);
    }

    @Override
    public RoutePlan getRoutePlanById(Long id, Long userId) {
        RoutePlan routePlan = routePlanMapper.selectById(id);
        if (routePlan == null) {
            throw new RuntimeException("行程不存在");
        }
        // 验证权限：只能查看自己的行程
        if (!routePlan.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该行程");
        }
        return routePlan;
    }

    @Override
    public boolean deleteRoutePlan(Long id, Long userId) {
        RoutePlan routePlan = routePlanMapper.selectById(id);
        if (routePlan == null) {
            throw new RuntimeException("行程不存在");
        }
        // 验证权限：只能删除自己的行程
        if (!routePlan.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该行程");
        }
        return routePlanMapper.deleteById(id) > 0;
    }

    @Override
    public List<AttractionVO> recommendByPreferences(Long userId, String city, List<String> interests, Integer limit) {
        LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attraction::getCity, city);

        List<Attraction> attractions = attractionMapper.selectList(wrapper);

        List<AttractionVO> result = new ArrayList<>();
        for (Attraction attraction : attractions) {
            AttractionVO vo = new AttractionVO();
            vo.setId(attraction.getId());
            vo.setName(attraction.getName());
            vo.setCity(attraction.getCity());
            vo.setAddress(attraction.getAddress());
            vo.setDescription(attraction.getDescription());
            vo.setRating(attraction.getRating());
            vo.setImages(new ArrayList<>());
            vo.setTags(new ArrayList<>());

            if (matchInterests(attraction, interests)) {
                result.add(vo);
            }

            if (result.size() >= limit) {
                break;
            }
        }

        return result;
    }

    private String buildTitle(RoutePlanRequestDTO request) {
        StringBuilder title = new StringBuilder();

        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            title.append(String.join("、", request.getDestinations()));
        }

        if (request.getDays() != null) {
            title.append(request.getDays()).append("日游");
        }

        return title.length() > 0 ? title.toString() : "我的旅行计划";
    }

    /**
     * 安全获取JsonNode中的文本字段值
     */
    private String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode != null && !fieldNode.isMissingNode() && !fieldNode.isNull()) {
            if (fieldNode.isTextual()) {
                String value = fieldNode.asText();
                return value != null && !value.isEmpty() ? value : defaultValue;
            } else if (fieldNode.isValueNode()) {
                // 处理null值节点（用于image字段）
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 安全获取JsonNode中的可空文本字段值（用于image等可为null的字段）
     */
    private String getNullableTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode != null && !fieldNode.isMissingNode() && !fieldNode.isNull()) {
            if (fieldNode.isTextual()) {
                String value = fieldNode.asText();
                return "null".equals(value) || value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    /**
     * 创建活动
     */
    private RoutePlanVO.ActivityVO createActivity(String type, String title, String description,
            String time, String duration, String distance, String transport, String cost, String tips) {
        RoutePlanVO.ActivityVO activity = new RoutePlanVO.ActivityVO();
        activity.setType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setTime(time);
        activity.setDuration(duration);
        activity.setDistance(distance);
        activity.setTransport(transport);
        activity.setCost(cost);
        activity.setTips(tips);
        return activity;
    }

    private boolean matchInterests(Attraction attraction, List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return true;
        }

        String tags = attraction.getTags();
        if (tags == null) {
            return true;
        }

        for (String interest : interests) {
            if (tags.contains(interest)) {
                return true;
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoutePlanVO adjustRoutePlan(Long userId, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        // 获取并验证行程
        RoutePlan routePlan = getRoutePlanById(adjustDTO.getPlanId(), userId);

        try {
            // 解析现有行程数据
            RoutePlanVO planVO = objectMapper.readValue(routePlan.getPlanData(), RoutePlanVO.class);

            // 根据调整类型执行操作
            switch (adjustDTO.getAdjustType()) {
                case "add":
                    addActivity(planVO, adjustDTO);
                    break;
                case "remove":
                    removeActivity(planVO, adjustDTO);
                    break;
                case "modify":
                    modifyActivity(planVO, adjustDTO);
                    break;
                case "reorder":
                    reorderActivity(planVO, adjustDTO);
                    break;
                default:
                    throw new RuntimeException("不支持的调整类型: " + adjustDTO.getAdjustType());
            }

            // 记录调整历史
            recordAdjustmentHistory(routePlan, adjustDTO);

            // 更新版本号
            routePlan.setVersion((routePlan.getVersion() != null ? routePlan.getVersion() : 0) + 1);

            // 保存更新后的行程
            routePlan.setPlanData(objectMapper.writeValueAsString(planVO));
            routePlanMapper.updateById(routePlan);

            log.info("行程调整成功，ID: {}, 类型: {}, 版本: {}",
                    routePlan.getId(), adjustDTO.getAdjustType(), routePlan.getVersion());

            return planVO;

        } catch (Exception e) {
            log.error("行程调整失败: {}", e.getMessage());
            throw new RuntimeException("行程调整失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoutePlanVO optimizeRoutePlan(Long userId, Long planId, String feedback) {
        // 获取并验证行程
        RoutePlan routePlan = getRoutePlanById(planId, userId);

        try {
            RoutePlanVO planVO = objectMapper.readValue(routePlan.getPlanData(), RoutePlanVO.class);

            // 构建优化提示词
            String optimizePrompt = buildOptimizePrompt(planVO, feedback);

            // 调用AI进行优化
            String aiResponse = callDeepSeekAPI(optimizePrompt);

            if (aiResponse != null && !aiResponse.isEmpty()) {
                // 尝试解析AI返回的优化建议
                RoutePlanVO optimizedPlan = parseOptimizedPlan(aiResponse, planVO);

                if (optimizedPlan != null) {
                    // 记录优化历史
                    com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO = new com.tourism.model.dto.RoutePlanAdjustDTO();
                    adjustDTO.setAdjustType("optimize");
                    adjustDTO.setReason(feedback);
                    recordAdjustmentHistory(routePlan, adjustDTO);

                    // 更新版本号
                    routePlan.setVersion((routePlan.getVersion() != null ? routePlan.getVersion() : 0) + 1);

                    // 保存优化后的行程
                    routePlan.setPlanData(objectMapper.writeValueAsString(optimizedPlan));
                    routePlanMapper.updateById(routePlan);

                    log.info("行程优化成功，ID: {}, 版本: {}", routePlan.getId(), routePlan.getVersion());
                    return optimizedPlan;
                }
            }

            // AI优化失败，返回原行程
            log.warn("AI优化失败，返回原行程");
            return planVO;

        } catch (Exception e) {
            log.error("行程优化失败: {}", e.getMessage());
            throw new RuntimeException("行程优化失败: " + e.getMessage());
        }
    }

    /**
     * 添加活动
     */
    private void addActivity(RoutePlanVO planVO, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        int dayIndex = adjustDTO.getDayIndex() - 1;
        if (dayIndex < 0 || dayIndex >= planVO.getDays().size()) {
            throw new RuntimeException("无效的天数索引");
        }

        RoutePlanVO.DayPlanVO day = planVO.getDays().get(dayIndex);
        RoutePlanVO.ActivityVO newActivity = convertToActivityVO(adjustDTO.getNewActivity());

        int activityIndex = adjustDTO.getActivityIndex();
        if (activityIndex < 0 || activityIndex > day.getActivities().size()) {
            day.getActivities().add(newActivity);
        } else {
            day.getActivities().add(activityIndex, newActivity);
        }
    }

    /**
     * 删除活动
     */
    private void removeActivity(RoutePlanVO planVO, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        int dayIndex = adjustDTO.getDayIndex() - 1;
        if (dayIndex < 0 || dayIndex >= planVO.getDays().size()) {
            throw new RuntimeException("无效的天数索引");
        }

        RoutePlanVO.DayPlanVO day = planVO.getDays().get(dayIndex);
        int activityIndex = adjustDTO.getActivityIndex();

        if (activityIndex < 0 || activityIndex >= day.getActivities().size()) {
            throw new RuntimeException("无效的活动索引");
        }

        day.getActivities().remove(activityIndex);
    }

    /**
     * 修改活动
     */
    private void modifyActivity(RoutePlanVO planVO, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        int dayIndex = adjustDTO.getDayIndex() - 1;
        if (dayIndex < 0 || dayIndex >= planVO.getDays().size()) {
            throw new RuntimeException("无效的天数索引");
        }

        RoutePlanVO.DayPlanVO day = planVO.getDays().get(dayIndex);
        int activityIndex = adjustDTO.getActivityIndex();

        if (activityIndex < 0 || activityIndex >= day.getActivities().size()) {
            throw new RuntimeException("无效的活动索引");
        }

        RoutePlanVO.ActivityVO newActivity = convertToActivityVO(adjustDTO.getNewActivity());
        day.getActivities().set(activityIndex, newActivity);
    }

    /**
     * 重新排序活动
     */
    private void reorderActivity(RoutePlanVO planVO, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        int fromDayIndex = adjustDTO.getDayIndex() - 1;
        int toDayIndex = adjustDTO.getNewDayIndex() - 1;

        if (fromDayIndex < 0 || fromDayIndex >= planVO.getDays().size() ||
            toDayIndex < 0 || toDayIndex >= planVO.getDays().size()) {
            throw new RuntimeException("无效的天数索引");
        }

        RoutePlanVO.DayPlanVO fromDay = planVO.getDays().get(fromDayIndex);
        int fromActivityIndex = adjustDTO.getActivityIndex();

        if (fromActivityIndex < 0 || fromActivityIndex >= fromDay.getActivities().size()) {
            throw new RuntimeException("无效的活动索引");
        }

        // 移除原位置的活动
        RoutePlanVO.ActivityVO activity = fromDay.getActivities().remove(fromActivityIndex);

        // 添加到新位置
        RoutePlanVO.DayPlanVO toDay = planVO.getDays().get(toDayIndex);
        int toActivityIndex = adjustDTO.getNewActivityIndex();

        if (toActivityIndex < 0 || toActivityIndex > toDay.getActivities().size()) {
            toDay.getActivities().add(activity);
        } else {
            toDay.getActivities().add(toActivityIndex, activity);
        }
    }

    /**
     * 转换活动数据
     */
    private RoutePlanVO.ActivityVO convertToActivityVO(com.tourism.model.dto.RoutePlanAdjustDTO.ActivityData data) {
        RoutePlanVO.ActivityVO activity = new RoutePlanVO.ActivityVO();
        activity.setType(data.getType());
        activity.setTitle(data.getTitle());
        activity.setDescription(data.getDescription());
        activity.setTime(data.getTime());
        activity.setDuration(data.getDuration());
        activity.setDistance(data.getDistance());
        activity.setTransport(data.getTransport());
        activity.setCost(data.getCost());
        activity.setTips(data.getTips());
        return activity;
    }

    /**
     * 记录调整历史
     */
    private void recordAdjustmentHistory(RoutePlan routePlan, com.tourism.model.dto.RoutePlanAdjustDTO adjustDTO) {
        try {
            List<Map<String, Object>> history;
            if (routePlan.getAdjustmentHistory() != null && !routePlan.getAdjustmentHistory().isEmpty()) {
                history = objectMapper.readValue(routePlan.getAdjustmentHistory(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            } else {
                history = new ArrayList<>();
            }

            Map<String, Object> record = new HashMap<>();
            record.put("type", adjustDTO.getAdjustType());
            record.put("reason", adjustDTO.getReason());
            record.put("timestamp", System.currentTimeMillis());
            record.put("dayIndex", adjustDTO.getDayIndex());
            record.put("activityIndex", adjustDTO.getActivityIndex());

            history.add(record);

            routePlan.setAdjustmentHistory(objectMapper.writeValueAsString(history));
        } catch (Exception e) {
            log.warn("记录调整历史失败: {}", e.getMessage());
        }
    }

    /**
     * 构建优化提示词
     */
    private String buildOptimizePrompt(RoutePlanVO planVO, String feedback) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据用户反馈优化以下旅行行程：\n\n");
        prompt.append("【用户反馈】\n").append(feedback).append("\n\n");
        prompt.append("【当前行程】\n");

        for (int i = 0; i < planVO.getDays().size(); i++) {
            RoutePlanVO.DayPlanVO day = planVO.getDays().get(i);
            prompt.append("第").append(i + 1).append("天：").append(day.getTitle()).append("\n");
            for (RoutePlanVO.ActivityVO activity : day.getActivities()) {
                prompt.append("  - ").append(activity.getTime()).append(" ")
                      .append(activity.getTitle()).append("\n");
            }
        }

        prompt.append("\n请根据反馈优化行程，返回JSON格式的优化后行程。");
        return prompt.toString();
    }

    /**
     * 解析优化后的行程
     */
    private RoutePlanVO parseOptimizedPlan(String aiResponse, RoutePlanVO originalPlan) {
        try {
            String jsonContent = extractJsonFromResponse(aiResponse);
            return objectMapper.readValue(jsonContent, RoutePlanVO.class);
        } catch (Exception e) {
            log.warn("解析优化行程失败，返回原行程: {}", e.getMessage());
            return originalPlan;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRoutePlan(Long userId, Long planId) {
        RoutePlan routePlan = getRoutePlanById(planId, userId);
        routePlan.setStatus(1); // 1-已确认
        return routePlanMapper.updateById(routePlan) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoutePlanStatus(Long userId, Long planId, Integer status) {
        if (status < 0 || status > 3) {
            throw new RuntimeException("无效的状态值，有效值：0-草稿，1-已确认，2-进行中，3-已完成");
        }
        RoutePlan routePlan = getRoutePlanById(planId, userId);
        routePlan.setStatus(status);
        return routePlanMapper.updateById(routePlan) > 0;
    }
}
