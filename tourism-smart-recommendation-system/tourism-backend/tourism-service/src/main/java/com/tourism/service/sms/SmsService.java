package com.tourism.service.sms;

/**
 * 短信服务接口
 * 使用阿里云号码认证服务（DYPNS）
 *
 * @author 韩东升
 */
public interface SmsService {

    /**
     * 发送短信验证码
     * 使用阿里云DYPNS服务发送验证码，验证码由阿里云生成和管理
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone);

    /**
     * 校验短信验证码
     * 使用阿里云DYPNS服务校验验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 是否校验成功
     */
    boolean checkVerificationCode(String phone, String code);
}
