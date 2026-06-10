package com.tourism.service.wechat;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.enums.UserRole;
import com.tourism.common.utils.JwtUtil;
import com.tourism.model.entity.User;
import com.tourism.model.vo.LoginVO;
import com.tourism.model.vo.UserVO;
import com.tourism.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微信登录服务实现
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    private final WechatConfig wechatConfig;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getAuthorizeUrl(String redirectUri, String state) {
        try {
            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
            return String.format("%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
                    wechatConfig.getAuthorizeUrl(),
                    wechatConfig.getAppId(),
                    encodedRedirectUri,
                    state);
        } catch (Exception e) {
            log.error("生成微信授权URL失败", e);
            throw new RuntimeException("生成微信授权URL失败");
        }
    }

    @Override
    public LoginVO loginByWechat(String code) {
        // 获取微信用户信息
        WechatUserInfo wechatUserInfo = getWechatUserInfo(code);
        if (wechatUserInfo == null || StrUtil.isBlank(wechatUserInfo.getOpenid())) {
            throw new RuntimeException("获取微信用户信息失败");
        }

        // 查询用户是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getWechatOpenid, wechatUserInfo.getOpenid());
        User user = userMapper.selectOne(wrapper);

        // 用户不存在则自动注册
        if (user == null) {
            user = new User();
            user.setWechatOpenid(wechatUserInfo.getOpenid());
            user.setNickname(StrUtil.isNotBlank(wechatUserInfo.getNickname())
                    ? wechatUserInfo.getNickname()
                    : "微信用户" + wechatUserInfo.getOpenid().substring(0, 6));
            user.setAvatar(wechatUserInfo.getHeadimgurl());
            user.setRole(UserRole.USER.getCode());
            user.setStatus(1);
            user.setPreferences("[]");
            userMapper.insert(user);
            log.info("微信用户自动注册成功，openid：{}", wechatUserInfo.getOpenid());
        } else {
            // 更新用户信息（头像、昵称可能变化）
            if (StrUtil.isNotBlank(wechatUserInfo.getNickname())) {
                user.setNickname(wechatUserInfo.getNickname());
            }
            if (StrUtil.isNotBlank(wechatUserInfo.getHeadimgurl())) {
                user.setAvatar(wechatUserInfo.getHeadimgurl());
            }
            userMapper.updateById(user);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.createToken(user.getId(), user.getRole());

        // 构建返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(buildUserVO(user));

        return loginVO;
    }

    @Override
    public WechatUserInfo getWechatUserInfo(String code) {
        try {
            // 1. 通过code获取access_token
            String accessTokenUrl = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                    wechatConfig.getAccessTokenUrl(),
                    wechatConfig.getAppId(),
                    wechatConfig.getAppSecret(),
                    code);

            String accessTokenResponse = restTemplate.getForObject(accessTokenUrl, String.class);
            log.debug("微信access_token响应：{}", accessTokenResponse);

            JSONObject accessTokenJson = JSON.parseObject(accessTokenResponse);
            if (accessTokenJson.containsKey("errcode")) {
                log.error("获取微信access_token失败：{}", accessTokenResponse);
                throw new RuntimeException("获取微信access_token失败：" + accessTokenJson.getString("errmsg"));
            }

            String accessToken = accessTokenJson.getString("access_token");
            String openid = accessTokenJson.getString("openid");

            // 2. 通过access_token获取用户信息
            String userInfoUrl = String.format("%s?access_token=%s&openid=%s&lang=zh_CN",
                    wechatConfig.getUserInfoUrl(),
                    accessToken,
                    openid);

            String userInfoResponse = restTemplate.getForObject(userInfoUrl, String.class);
            log.debug("微信用户信息响应：{}", userInfoResponse);

            JSONObject userInfoJson = JSON.parseObject(userInfoResponse);
            if (userInfoJson.containsKey("errcode")) {
                log.error("获取微信用户信息失败：{}", userInfoResponse);
                throw new RuntimeException("获取微信用户信息失败：" + userInfoJson.getString("errmsg"));
            }

            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenid(userInfoJson.getString("openid"));
            userInfo.setNickname(userInfoJson.getString("nickname"));
            userInfo.setHeadimgurl(userInfoJson.getString("headimgurl"));
            userInfo.setSex(userInfoJson.getInteger("sex"));
            userInfo.setProvince(userInfoJson.getString("province"));
            userInfo.setCity(userInfoJson.getString("city"));
            userInfo.setCountry(userInfoJson.getString("country"));
            userInfo.setUnionid(userInfoJson.getString("unionid"));

            return userInfo;
        } catch (Exception e) {
            log.error("获取微信用户信息异常", e);
            throw new RuntimeException("获取微信用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 构建用户VO
     */
    private UserVO buildUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setPhone(user.getPhone());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());
        userVO.setPreferences(user.getPreferences());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }
}
