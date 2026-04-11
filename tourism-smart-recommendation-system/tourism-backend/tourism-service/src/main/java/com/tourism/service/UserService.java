package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tourism.model.dto.UserLoginDTO;
import com.tourism.model.dto.UserRegisterDTO;
import com.tourism.model.dto.ResetPasswordDTO;
import com.tourism.model.entity.User;
import com.tourism.model.vo.LoginVO;
import com.tourism.model.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author 韩东升
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO loginDTO);

    /**
     * 管理员登录
     */
    LoginVO adminLogin(UserLoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(UserRegisterDTO registerDTO);

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String phone);

    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    /**
     * 验证重置密码前的手机号与验证码
     */
    void verifyResetPassword(String phone, String code);

    /**
     * 验证短信验证码（不消耗，仅校验）
     */
    boolean verifySmsCode(String phone, String code);

    /**
     * 修改密码（已登录用户）
     */
    void changePassword(Long userId, String newPassword);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(UserVO userVO);

    /**
     * 记录用户行为
     */
    void recordBehavior(Long userId, String itemType, Long itemId, String behaviorType, Double weight);

    /**
     * 记录操作日志
     */
    void recordOperateLog(Long userId, String username, String userRole, String operationType, String module, String description, String requestMethod, String requestParams, String ipAddress, Integer status, Long executeTime);
}
