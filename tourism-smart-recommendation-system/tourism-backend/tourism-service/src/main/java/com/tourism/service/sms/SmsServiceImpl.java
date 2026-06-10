package com.tourism.service.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现
 * 使用阿里云号码认证服务（DYPNS）发送和校验验证码
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SmsConfig smsConfig;
    private final StringRedisTemplate redisTemplate;

    private static final String SMS_CODE_KEY = "sms:code:";
    private Client client;

    @PostConstruct
    public void init() {
        if (smsConfig.isEnabled()) {
            try {
                Config config = new Config()
                        .setAccessKeyId(smsConfig.getAccessKeyId())
                        .setAccessKeySecret(smsConfig.getAccessKeySecret())
                        .setEndpoint("dypnsapi.aliyuncs.com");
                this.client = new Client(config);
                log.info("阿里云号码认证服务客户端初始化成功");
            } catch (Exception e) {
                log.error("阿里云号码认证服务客户端初始化失败", e);
            }
        }
    }

    @Override
    public boolean sendVerificationCode(String phone) {
        // 模拟模式：生成验证码存入Redis
        if (!smsConfig.isEnabled() || client == null) {
            String mockCode = generateCode();
            saveCodeToRedis(phone, mockCode);
            log.info("【模拟短信】发送验证码到手机号 {}，验证码：{}", phone, mockCode);
            System.out.println("========================================");
            System.out.println("【模拟短信】发送验证码到手机号 " + phone + "，验证码：" + mockCode);
            System.out.println("========================================");
            return true;
        }

        // 真实发送模式 - 使用阿里云DYPNS服务
        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(smsConfig.getSignName())
                    .setTemplateCode(smsConfig.getTemplateCode())
                    .setTemplateParam("{\"code\":\"##code##\",\"min\":\"" + smsConfig.getCodeExpireMinutes() + "\"}")
                    .setCodeLength((long) smsConfig.getCodeLength())
                    .setValidTime((long) (smsConfig.getCodeExpireMinutes() * 60));

            RuntimeOptions runtime = new RuntimeOptions();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);

            if (response != null && response.getBody() != null) {
                String code = response.getBody().getCode();
                if ("OK".equals(code)) {
                    log.info("短信验证码发送成功，手机号：{}", phone);
                    return true;
                } else {
                    log.error("短信验证码发送失败，手机号：{}，错误码：{}，错误信息：{}",
                            phone, code, response.getBody().getMessage());
                    return fallbackToSimulation(phone);
                }
            }
            return fallbackToSimulation(phone);
        } catch (Exception e) {
            log.error("短信验证码发送异常，手机号：{}", phone, e);
            return fallbackToSimulation(phone);
        }
    }

    @Override
    public boolean checkVerificationCode(String phone, String code) {
        // 模拟模式：从Redis校验
        if (!smsConfig.isEnabled() || client == null) {
            return checkCodeFromRedis(phone, code);
        }

        // 真实校验模式 - 使用阿里云DYPNS服务
        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(code);

            RuntimeOptions runtime = new RuntimeOptions();
            CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCodeWithOptions(request, runtime);

            if (response != null && response.getBody() != null) {
                String resultCode = response.getBody().getCode();
                if ("OK".equals(resultCode)) {
                    String verifyResult = response.getBody().getModel().getVerifyResult();
                    if ("PASS".equals(verifyResult)) {
                        log.info("验证码校验成功，手机号：{}", phone);
                        return true;
                    } else {
                        log.warn("验证码校验失败，手机号：{}，结果：{}", phone, verifyResult);
                        // 阿里云校验失败，回退到Redis校验（支持模拟发送的验证码）
                        return checkCodeFromRedis(phone, code);
                    }
                } else {
                    log.error("验证码校验请求失败，手机号：{}，错误码：{}，错误信息：{}",
                            phone, resultCode, response.getBody().getMessage());
                    // 阿里云API请求失败，回退到Redis校验
                    return checkCodeFromRedis(phone, code);
                }
            }
            // 响应为空，回退到Redis校验
            return checkCodeFromRedis(phone, code);
        } catch (Exception e) {
            log.error("验证码校验异常，手机号：{}，回退到Redis校验", phone, e);
            // 异常时回退到Redis校验
            return checkCodeFromRedis(phone, code);
        }
    }

    /**
     * 生成验证码
     */
    private String generateCode() {
        int length = smsConfig.getCodeLength();
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return String.valueOf((int) (Math.random() * (max - min + 1) + min));
    }

    /**
     * 保存验证码到Redis
     */
    private void saveCodeToRedis(String phone, String code) {
        String key = SMS_CODE_KEY + phone;
        redisTemplate.opsForValue().set(key, code, smsConfig.getCodeExpireMinutes(), TimeUnit.MINUTES);
    }

    /**
     * 从Redis校验验证码
     */
    private boolean checkCodeFromRedis(String phone, String code) {
        String key = SMS_CODE_KEY + phone;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode != null && savedCode.equals(code)) {
            redisTemplate.delete(key); // 验证成功后删除
            log.info("【模拟模式】验证码校验成功，手机号：{}", phone);
            return true;
        }
        log.warn("【模拟模式】验证码校验失败，手机号：{}，输入：{}，期望：{}", phone, code, savedCode);
        return false;
    }

    /**
     * 回退到模拟模式
     */
    private boolean fallbackToSimulation(String phone) {
        String mockCode = generateCode();
        saveCodeToRedis(phone, mockCode);
        log.info("【回退模拟】发送验证码到手机号 {}，验证码：{}", phone, mockCode);
        System.out.println("========================================");
        System.out.println("【回退模拟】发送验证码到手机号 " + phone + "，验证码：" + mockCode);
        System.out.println("========================================");
        return true;
    }
}
