import { post } from './index'

export interface LoginByPhoneParams {
  phone: string
  password: string
}

export interface SendSmsParams {
  phone: string
}

export interface RegisterParams {
  phone: string
  code: string
  password: string
  nickname: string
  preferences: string
}

export interface VerifyResetPasswordParams {
  phone: string
  code: string
}

export interface ResetPasswordParams {
  phone: string
  code: string
  newPassword: string
}

// 手机号+密码登录
export function loginByPhone(params: LoginByPhoneParams) {
  return post('/auth/login/phone', params)
}

// 管理员登录
export function adminLogin(params: LoginByPhoneParams) {
  return post('/auth/login/admin', params)
}

// 发送短信验证码
export function sendSms(params: SendSmsParams) {
  return post('/auth/sms/send', params)
}

// 用户注册
export function register(params: RegisterParams) {
  return post('/auth/register', params)
}

// 验证重置密码前的手机号和验证码
export function verifyResetPassword(params: VerifyResetPasswordParams) {
  return post('/auth/reset-password/verify', params)
}

// 重置密码
export function resetPassword(params: ResetPasswordParams) {
  return post('/auth/reset-password', params)
}

// 退出登录
export function logout() {
  return post('/auth/logout')
}
