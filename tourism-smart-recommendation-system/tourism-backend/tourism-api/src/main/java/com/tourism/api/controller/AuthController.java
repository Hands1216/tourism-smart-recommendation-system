package com.tourism.api.controller;

import com.tourism.common.result.Result;
import com.tourism.common.utils.UserContext;
import com.tourism.model.dto.UserLoginDTO;
import com.tourism.model.dto.UserRegisterDTO;
import com.tourism.model.dto.ResetPasswordDTO;
import com.tourism.model.vo.LoginVO;
import com.tourism.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author 韩东升
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 手机号+密码登录
     */
    @PostMapping("/login/phone")
    public Result<LoginVO> loginByPhone(@RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login/admin")
    public Result<LoginVO> adminLogin(@RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.adminLogin(loginDTO);

        // 记录管理员登录日志
        if (loginVO != null && loginVO.getUserInfo() != null) {
            UserContext.setUserId(loginVO.getUserInfo().getId());
            UserContext.setUsername(loginVO.getUserInfo().getNickname() != null ? loginVO.getUserInfo().getNickname() : loginVO.getUserInfo().getPhone());
            UserContext.setUserRole(loginVO.getUserInfo().getRole());
        }

        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<Void> sendSms(@RequestBody UserLoginDTO dto) {
        userService.sendSmsCode(dto.getPhone());
        return Result.success();
    }

    /**
     * 验证重置密码前的手机号与验证码
     */
    @PostMapping("/reset-password/verify")
    public Result<Void> verifyResetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        userService.verifyResetPassword(resetPasswordDTO.getPhone(), resetPasswordDTO.getCode());
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        userService.resetPassword(resetPasswordDTO);
        return Result.success();
    }
}
