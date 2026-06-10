package com.tourism.common.constant;

/**
 * Redis键常量
 *
 */
public class RedisKey {

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * 用户信息缓存
     */
    public static final String USER_INFO = "user:info:";

    /**
     * 短信验证码
     */
    public static final String SMS_CODE = "sms:code:";

    /**
     * 短信验证码KEY
     */
    public static final String SMS_CODE_KEY = "sms:code:";

    /**
     * 用户收藏
     */
    public static final String USER_FAVORITE = "user:favorite:";

    /**
     * 景点缓存
     */
    public static final String ATTRACTION_INFO = "attraction:info:";

    /**
     * 聊天会话
     */
    public static final String CHAT_SESSION = "chat:session:";

    /**
     * 推荐结果缓存
     */
    public static final String RECOMMEND_RESULT = "recommend:result:";

    /**
     * 敏感词列表
     */
    public static final String SENSITIVE_WORDS = "sensitive:words";

    /**
     * 在线用户
     */
    public static final String ONLINE_USER = "online:user";

    private RedisKey() {
    }
}
