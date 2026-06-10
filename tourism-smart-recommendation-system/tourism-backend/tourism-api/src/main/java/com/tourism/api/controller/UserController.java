package com.tourism.api.controller;

import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.entity.RoutePlan;
import com.tourism.model.vo.FavoriteVO;
import com.tourism.model.vo.UserVO;
import com.tourism.service.FavoriteService;
import com.tourism.service.RecommendService;
import com.tourism.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户中心控制器
 *
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RecommendService recommendService;
    private final FavoriteService favoriteService;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserVO userVO) {
        Long userId = UserContext.getUserId();
        userVO.setId(userId);
        userService.updateUser(userVO);
        return Result.success();
    }

    /**
     * 获取我的收藏
     *
     * @param type 收藏类型（可选）：attraction/strategy，为空则返回全部
     */
    @GetMapping("/favorites")
    public Result<List<FavoriteVO>> getFavorites(
            @RequestParam(required = false) String type) {
        Long userId = UserContext.getUserId();
        List<FavoriteVO> favorites = favoriteService.getUserFavorites(userId, type);
        return Result.success(favorites);
    }

    /**
     * 验证短信验证码（修改密码前验证）
     */
    @PostMapping("/verify-code")
    public Result<Void> verifyCode(@RequestBody java.util.Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        if (!userService.verifySmsCode(phone, code)) {
            throw new RuntimeException("验证码错误");
        }
        return Result.success();
    }

    /**
     * 修改密码（已登录用户，验证码验证通过后调用）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody java.util.Map<String, String> params) {
        Long userId = UserContext.getUserId();
        String newPassword = params.get("newPassword");
        userService.changePassword(userId, newPassword);
        return Result.success();
    }

    /**
     * 获取行程记录
     */
    @GetMapping("/history")
    public Result<List<RoutePlan>> getHistory() {
        Long userId = UserContext.getUserId();
        List<RoutePlan> routePlans = recommendService.getUserRoutePlans(userId);
        return Result.success(routePlans);
    }
}
