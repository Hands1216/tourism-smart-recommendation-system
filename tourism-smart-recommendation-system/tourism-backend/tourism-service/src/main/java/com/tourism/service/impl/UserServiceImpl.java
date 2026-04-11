package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.common.enums.UserRole;
import com.tourism.common.utils.JwtUtil;
import com.tourism.model.dto.UserLoginDTO;
import com.tourism.model.dto.UserRegisterDTO;
import com.tourism.model.dto.ResetPasswordDTO;
import com.tourism.model.entity.User;
import com.tourism.model.entity.UserBehavior;
import com.tourism.model.vo.LoginVO;
import com.tourism.model.vo.UserVO;
import com.tourism.service.UserBehaviorService;
import com.tourism.service.UserService;
import com.tourism.service.mapper.UserMapper;
import com.tourism.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户服务实现
 *
 * @author 韩东升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserBehaviorService userBehaviorService;
    private final JwtUtil jwtUtil;
    private final com.tourism.service.OperateLogService operateLogService;
    private final SmsService smsService;

    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDTO.getPhone());
        User user = getOne(wrapper);

        // 用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }

        // 校验密码
        if (user.getPassword() == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
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
    public LoginVO adminLogin(UserLoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDTO.getPhone());
        User user = getOne(wrapper);

        // 用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验密码
        if (user.getPassword() == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 检查是否为管理员角色
        String role = user.getRole();
        if (!UserRole.ADMIN.getCode().equals(role) && !UserRole.CONTENT_ADMIN.getCode().equals(role)) {
            throw new RuntimeException("您没有管理员权限");
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
    public void register(UserRegisterDTO registerDTO) {
        // 使用DYPNS服务校验验证码
        if (!smsService.checkVerificationCode(registerDTO.getPhone(), registerDTO.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 密码长度校验
        if (registerDTO.getPassword() == null || registerDTO.getPassword().length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        // 查询手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, registerDTO.getPhone());
        if (count(wrapper) > 0) {
            throw new RuntimeException("该账户已存在");
        }

        // 创建用户
        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setRole(UserRole.USER.getCode());
        user.setStatus(1);
        user.setPreferences(registerDTO.getPreferences() != null ? registerDTO.getPreferences() : "[]");
        save(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 调用DYPNS短信服务发送验证码（验证码由阿里云自动生成和管理）
        boolean success = smsService.sendVerificationCode(phone);
        if (!success) {
            throw new RuntimeException("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        verifyResetPassword(resetPasswordDTO.getPhone(), resetPasswordDTO.getCode());

        // 密码长度校验
        if (resetPasswordDTO.getNewPassword() == null || resetPasswordDTO.getNewPassword().length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, resetPasswordDTO.getPhone());
        User user = getOne(wrapper);

        // 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        updateById(updateUser);
    }

    @Override
    public void verifyResetPassword(String phone, String code) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("该账户不存在");
        }

        if (!smsService.checkVerificationCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        return smsService.checkVerificationCode(phone, code);
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码至少6位");
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateById(updateUser);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = getById(userId);
        return buildUserVO(user);
    }

    @Override
    public void updateUser(UserVO userVO) {
        User user = new User();
        user.setId(userVO.getId());
        user.setNickname(userVO.getNickname());
        user.setAvatar(userVO.getAvatar());
        user.setPreferences(userVO.getPreferences());
        updateById(user);
    }

    @Override
    public void recordBehavior(Long userId, String itemType, Long itemId, String behaviorType, Double weight) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setItemType(itemType);
        behavior.setItemId(itemId);
        behavior.setBehaviorType(behaviorType);
        behavior.setWeight(weight);
        userBehaviorService.save(behavior);
    }

    @Override
    public void recordOperateLog(Long userId, String username, String userRole, String operationType, String module, String description, String requestMethod, String requestParams, String ipAddress, Integer status, Long executeTime) {
        // 记录操作日志
        // 由于之前存在循环依赖问题，这里通过ApplicationContext获取OperateLogService的Bean
        // 或者使用静态工具类方式
        // 当前简化处理：只记录日志，不保存到数据库
        log.info("操作日志 - 用户ID: {}, 用户名: {}, 角色: {}, 操作: {}, 模块: {}, 描述: {}",
            userId, username, userRole, operationType, module, description);

        // 获取客户端IP地址
        if (ipAddress == null) {
            ipAddress = getClientIp();
        }
        if (requestMethod == null) {
            requestMethod = "UNKNOWN";
        }
        if (status == null) {
            status = 1;
        }

        // 直接使用注入的OperateLogService记录操作日志
        operateLogService.recordOperateLog(userId, username, userRole, operationType, module, description,
                requestMethod, requestParams, ipAddress, status, executeTime);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 处理多个IP的情况，取第一个
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
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
