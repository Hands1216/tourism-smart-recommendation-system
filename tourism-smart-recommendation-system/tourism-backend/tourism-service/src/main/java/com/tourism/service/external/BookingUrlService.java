package com.tourism.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 预订链接生成与校验服务
 * 维护12306城市电报码、景点官方订票链接、酒店品牌官网映射表
 *
 * @author 韩东升
 */
@Slf4j
@Service
public class BookingUrlService {

    // 12306城市电报码映射
    private static final Map<String, String> CITY_STATION_CODE = Map.ofEntries(
            Map.entry("北京", "BJP"),
            Map.entry("上海", "SHH"),
            Map.entry("广州", "GZQ"),
            Map.entry("深圳", "SZQ"),
            Map.entry("成都", "CDW"),
            Map.entry("西安", "XAY"),
            Map.entry("杭州", "HZH"),
            Map.entry("南京", "NJH"),
            Map.entry("武汉", "WHN"),
            Map.entry("长沙", "CSQ"),
            Map.entry("重庆", "CQW"),
            Map.entry("天津", "TJP"),
            Map.entry("青岛", "TK"),
            Map.entry("大连", "DLT"),
            Map.entry("厦门", "XMS"),
            Map.entry("昆明", "KMM"),
            Map.entry("贵阳", "GIW"),
            Map.entry("桂林", "GLZ"),
            Map.entry("丽江", "LJL"),
            Map.entry("拉萨", "LSO"),
            Map.entry("哈尔滨", "HBB"),
            Map.entry("沈阳", "SYT"),
            Map.entry("济南", "JNK"),
            Map.entry("郑州", "ZZF"),
            Map.entry("合肥", "HFH"),
            Map.entry("福州", "FZS"),
            Map.entry("南昌", "NCG"),
            Map.entry("太原", "TYV"),
            Map.entry("兰州", "LZJ"),
            Map.entry("南宁", "NNZ"),
            Map.entry("海口", "VUQ"),
            Map.entry("三亚", "SEQ"),
            Map.entry("苏州", "SZH"),
            Map.entry("无锡", "WXH"),
            Map.entry("洛阳", "LYF"),
            Map.entry("黄山", "HKH")
    );

    // 景点官方订票链接映射
    private static final Map<String, String> ATTRACTION_BOOKING_URL = Map.ofEntries(
            Map.entry("故宫博物院", "https://gugong.ktmtech.cn/"),
            Map.entry("故宫", "https://gugong.ktmtech.cn/"),
            Map.entry("秦始皇兵马俑博物馆", "https://www.bmy.com.cn/"),
            Map.entry("兵马俑", "https://www.bmy.com.cn/"),
            Map.entry("颐和园", "https://www.summerpalace-china.com/"),
            Map.entry("八达岭长城", "https://www.badaling.cn/"),
            Map.entry("天坛公园", "https://www.tiantanpark.com/"),
            Map.entry("圆明园", "https://www.yuanmingyuanpark.cn/"),
            Map.entry("西湖", "https://www.hzwestlake.gov.cn/"),
            Map.entry("灵隐寺", "https://wwlingyinsi.org/"),
            Map.entry("上海迪士尼", "https://www.shanghaidisneyresort.com/"),
            Map.entry("上海迪士尼乐园", "https://www.shanghaidisneyresort.com/"),
            Map.entry("大熊猫繁育研究基地", "https://www.panda.org.cn/"),
            Map.entry("中山陵", "https://www.zschina.org.cn/"),
            Map.entry("陕西历史博物馆", "https://www.sxhm.com/"),
            Map.entry("南京博物院", "https://www.njmuseum.com/"),
            Map.entry("上海博物馆", "https://www.shanghaimuseum.net/"),
            Map.entry("大唐芙蓉园", "https://www.tangparadise.cn/"),
            Map.entry("华清宫", "https://www.hqc.cn/"),
            Map.entry("鼓浪屿", "https://www.gly.cn/")
    );

    // 携程酒店频道城市slug映射（用于拼接 hotels.ctrip.com/hotel/{slug} 链接）
    // 数据来源：hotels.ctrip.com 官网实际URL验证
    private static final Map<String, String> CTRIP_HOTEL_SLUG = Map.ofEntries(
            Map.entry("北京", "beijing1"), Map.entry("上海", "shanghai2"),
            Map.entry("天津", "tianjin3"), Map.entry("重庆", "chongqing4"),
            Map.entry("哈尔滨", "harbin5"), Map.entry("大连", "dalian6"),
            Map.entry("青岛", "qingdao7"), Map.entry("西安", "xi-an10"),
            Map.entry("南京", "nanjing12"), Map.entry("无锡", "wuxi13"),
            Map.entry("苏州", "suzhou14"), Map.entry("杭州", "hangzhou17"),
            Map.entry("黄山", "huangshan23"), Map.entry("厦门", "xiamen25"),
            Map.entry("成都", "chengdu28"), Map.entry("深圳", "shenzhen30"),
            Map.entry("广州", "guangzhou32"), Map.entry("桂林", "guilin33"),
            Map.entry("昆明", "kunming34"), Map.entry("丽江", "lijiang37"),
            Map.entry("贵阳", "guiyang38"), Map.entry("拉萨", "lhasa41"),
            Map.entry("海口", "haikou42"), Map.entry("三亚", "sanya61"),
            Map.entry("兰州", "lanzhou100"), Map.entry("太原", "taiyuan105"),
            Map.entry("济南", "jinan144"), Map.entry("长沙", "changsha148"),
            Map.entry("郑州", "zhengzhou150"), Map.entry("福州", "fuzhou258"),
            Map.entry("合肥", "hefei278"), Map.entry("洛阳", "luoyang350"),
            Map.entry("南昌", "nanchang376"), Map.entry("南宁", "nanning380"),
            Map.entry("沈阳", "shenyang451"), Map.entry("武汉", "wuhan477")
    );

    // 携程攻略频道城市拼音+ID映射（用于拼接 you.ctrip.com 景点/美食页面URL，注意与酒店频道ID不同）
    private static final Map<String, String> CTRIP_CITY_PINYIN = Map.ofEntries(
            Map.entry("北京", "beijing1"), Map.entry("上海", "shanghai2"),
            Map.entry("广州", "guangzhou152"), Map.entry("深圳", "shenzhen26"),
            Map.entry("成都", "chengdu104"), Map.entry("西安", "xian7"),
            Map.entry("杭州", "hangzhou14"), Map.entry("南京", "nanjing9"),
            Map.entry("武汉", "wuhan477"), Map.entry("长沙", "changsha148"),
            Map.entry("重庆", "chongqing158"), Map.entry("天津", "tianjin154"),
            Map.entry("青岛", "qingdao5"), Map.entry("大连", "dalian4"),
            Map.entry("厦门", "xiamen21"), Map.entry("昆明", "kunming100"),
            Map.entry("贵阳", "guiyang113"), Map.entry("桂林", "guilin28"),
            Map.entry("丽江", "lijiang197"), Map.entry("拉萨", "lasa36"),
            Map.entry("哈尔滨", "haerbin151"), Map.entry("沈阳", "shenyang58"),
            Map.entry("济南", "jinan128"), Map.entry("郑州", "zhengzhou293"),
            Map.entry("合肥", "hefei172"), Map.entry("福州", "fuzhou38"),
            Map.entry("南昌", "nanchang175"), Map.entry("太原", "taiyuan176"),
            Map.entry("兰州", "lanzhou332"), Map.entry("南宁", "nanning92"),
            Map.entry("海口", "haikou37"), Map.entry("三亚", "sanya61"),
            Map.entry("苏州", "suzhou11"), Map.entry("无锡", "wuxi10"),
            Map.entry("洛阳", "luoyang191"), Map.entry("黄山", "huangshan233")
    );

    // 直辖市区名 -> 市名映射（解决高德等API返回区名导致12306查找失败的问题）
    private static final Map<String, String> DISTRICT_TO_CITY = Map.ofEntries(
            // 北京
            Map.entry("东城区", "北京"), Map.entry("西城区", "北京"),
            Map.entry("朝阳区", "北京"), Map.entry("海淀区", "北京"),
            Map.entry("丰台区", "北京"), Map.entry("石景山区", "北京"),
            Map.entry("通州区", "北京"), Map.entry("顺义区", "北京"),
            Map.entry("房山区", "北京"), Map.entry("大兴区", "北京"),
            Map.entry("昌平区", "北京"), Map.entry("怀柔区", "北京"),
            Map.entry("平谷区", "北京"), Map.entry("密云区", "北京"),
            Map.entry("延庆区", "北京"), Map.entry("门头沟区", "北京"),
            // 上海
            Map.entry("黄浦区", "上海"), Map.entry("徐汇区", "上海"),
            Map.entry("长宁区", "上海"), Map.entry("静安区", "上海"),
            Map.entry("普陀区", "上海"), Map.entry("虹口区", "上海"),
            Map.entry("杨浦区", "上海"), Map.entry("浦东新区", "上海"),
            Map.entry("闵行区", "上海"), Map.entry("宝山区", "上海"),
            Map.entry("嘉定区", "上海"), Map.entry("金山区", "上海"),
            Map.entry("松江区", "上海"), Map.entry("青浦区", "上海"),
            Map.entry("奉贤区", "上海"), Map.entry("崇明区", "上海"),
            // 天津
            Map.entry("和平区", "天津"), Map.entry("河东区", "天津"),
            Map.entry("河西区", "天津"), Map.entry("南开区", "天津"),
            Map.entry("河北区", "天津"), Map.entry("红桥区", "天津"),
            Map.entry("滨海新区", "天津"), Map.entry("武清区", "天津"),
            Map.entry("宝坻区", "天津"), Map.entry("蓟州区", "天津"),
            // 重庆
            Map.entry("渝中区", "重庆"), Map.entry("江北区", "重庆"),
            Map.entry("南岸区", "重庆"), Map.entry("沙坪坝区", "重庆"),
            Map.entry("九龙坡区", "重庆"), Map.entry("渝北区", "重庆"),
            Map.entry("巴南区", "重庆"), Map.entry("北碚区", "重庆"),
            Map.entry("万州区", "重庆"), Map.entry("涪陵区", "重庆")
    );

    /**
     * 生成12306购票链接
     */
    public String generate12306Url(String fromCity, String toCity, String date) {
        String cleanFrom = cleanCityName(fromCity);
        String cleanTo = cleanCityName(toCity);
        String fromCode = CITY_STATION_CODE.get(cleanFrom);
        String toCode = CITY_STATION_CODE.get(cleanTo);

        if (fromCode == null || toCode == null) {
            log.debug("城市电报码未找到: from={}, to={}", cleanFrom, cleanTo);
            return "https://kyfw.12306.cn/otn/leftTicket/init";
        }

        String encodedFrom = URLEncoder.encode(cleanFrom, StandardCharsets.UTF_8);
        String encodedTo = URLEncoder.encode(cleanTo, StandardCharsets.UTF_8);
        String dateParam = (date != null && !date.isEmpty()) ? date : "";

        return String.format(
                "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc&fs=%s,%s&ts=%s,%s&date=%s",
                encodedFrom, fromCode, encodedTo, toCode, dateParam
        );
    }

    /**
     * 生成携程火车票购票链接
     */
    public String generateCtripTrainUrl(String fromCity, String toCity, String date) {
        String cleanFrom = cleanCityName(fromCity);
        String cleanTo = cleanCityName(toCity);

        StringBuilder url = new StringBuilder("https://trains.ctrip.com/webapp/train/list");
        url.append("?ticketType=0");
        url.append("&dStation=").append(URLEncoder.encode(cleanFrom, StandardCharsets.UTF_8));
        url.append("&aStation=").append(URLEncoder.encode(cleanTo, StandardCharsets.UTF_8));

        if (date != null && !date.isEmpty()) {
            url.append("&dDate=").append(date);
        }

        return url.toString();
    }

    /**
     * 生成携程景点页面链接
     * 优先跳转城市景点列表页（you.ctrip.com/sight/城市拼音ID.html）
     * 城市未匹配时兜底跳转携程搜索
     */
    public String generateCtripAttractionUrl(String attractionName, String city) {
        if (attractionName == null || attractionName.isBlank()) {
            return null;
        }

        String cleanCity = (city != null) ? cleanCityName(city) : "";
        String cityPinyin = CTRIP_CITY_PINYIN.get(cleanCity);

        if (cityPinyin != null) {
            // 携程城市景点列表页（已验证有效）
            return "https://you.ctrip.com/sight/" + cityPinyin + ".html";
        }

        // 兜底：携程攻略社区入口
        return "https://you.ctrip.com/sight/0.html";
    }

    /**
     * 生成携程美食页面链接
     * 优先跳转城市美食列表页（you.ctrip.com/fooditem/城市拼音ID.html）
     */
    public String generateCtripRestaurantUrl(String restaurantName, String city) {
        if (restaurantName == null || restaurantName.isBlank()) {
            return null;
        }

        String cleanCity = (city != null) ? cleanCityName(city) : "";
        String cityPinyin = CTRIP_CITY_PINYIN.get(cleanCity);

        if (cityPinyin != null) {
            return "https://you.ctrip.com/fooditem/" + cityPinyin + ".html";
        }

        return "https://you.ctrip.com/fooditem/0.html";
    }

    /**
     * 解析景点预订链接（映射表优先，大模型返回兜底）
     */
    public String resolveAttractionUrl(String attractionName, String aiReturnedUrl) {
        if (attractionName == null || attractionName.isBlank()) {
            return null;
        }

        // 匹配
        String url = ATTRACTION_BOOKING_URL.get(attractionName);
        if (url != null) {
            return url;
        }

        // 2. 模糊匹配（包含关系）
        for (Map.Entry<String, String> entry : ATTRACTION_BOOKING_URL.entrySet()) {
            if (attractionName.contains(entry.getKey()) || entry.getKey().contains(attractionName)) {
                return entry.getValue();
            }
        }

        // 3. 校验大模型返回的URL
        if (isValidUrl(aiReturnedUrl)) {
            return aiReturnedUrl;
        }

        return null;
    }

    /**
     * 生成携程酒店页面链接（PC端）
     * 跳转到携程PC端城市酒店列表页，支持入住/离店日期参数
     * URL格式：https://hotels.ctrip.com/hotel/{citySlug}?checkIn=xxx&checkOut=xxx
     */
    public String resolveHotelUrl(String hotelName, String city, String checkinDate, String checkoutDate) {
        if (hotelName == null || hotelName.isBlank()) {
            return null;
        }

        String cleanCity = (city != null) ? cleanCityName(city) : "";
        String hotelSlug = CTRIP_HOTEL_SLUG.get(cleanCity);

        if (hotelSlug != null) {
            StringBuilder url = new StringBuilder("https://hotels.ctrip.com/hotel/");
            url.append(hotelSlug);
            // 拼接入住/离店日期查询参数
            boolean hasParam = false;
            if (checkinDate != null && !checkinDate.isEmpty()) {
                url.append("?checkIn=").append(checkinDate);
                hasParam = true;
            }
            if (checkoutDate != null && !checkoutDate.isEmpty()) {
                url.append(hasParam ? "&" : "?").append("checkOut=").append(checkoutDate);
            }
            return url.toString();
        }

        // 兜底：携程酒店首页
        return "https://hotels.ctrip.com/";
    }

    /**
     * 解析餐厅链接（大模型URL优先，兜底大众点评搜索）
     */
    public String resolveRestaurantUrl(String restaurantName, String city, String aiReturnedUrl) {
        if (isValidUrl(aiReturnedUrl)) {
            return aiReturnedUrl;
        }

        if (restaurantName == null || restaurantName.isBlank()) {
            return null;
        }

        // 兜底：生成大众点评搜索链接
        String keyword = restaurantName + (city != null ? " " + city : "");
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return "https://www.dianping.com/search/keyword/0/" + encoded;
    }

    private String cleanCityName(String city) {
        if (city == null) {
            return "";
        }
        String cleaned = city.replace("市", "").replace("省", "").trim();

        // 直辖市区名映射回市名（如"黄浦区" -> "上海"）
        String mapped = DISTRICT_TO_CITY.get(cleaned);
        if (mapped != null) {
            return mapped;
        }
        // 也尝试带"区"后缀匹配（输入可能不带"区"）
        mapped = DISTRICT_TO_CITY.get(cleaned + "区");
        if (mapped != null) {
            return mapped;
        }

        return cleaned;
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
