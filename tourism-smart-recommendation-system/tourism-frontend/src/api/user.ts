import { get, post, put } from './index'

export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  role: string
  preferences: string
  createTime: string
}

export interface UpdateProfileParams {
  nickname?: string
  avatar?: string
  preferences?: string
}

// 获取用户信息
export function getUserInfo() {
  return get<UserInfo>('/user/profile')
}

// 更新用户信息
export function updateProfile(data: UpdateProfileParams) {
  return put('/user/profile', data)
}

// 获取用户收藏列表
export function getFavorites() {
  return get('/user/favorites')
}

// 获取用户行程历史
export function getHistory() {
  return get('/user/history')
}

// 验证短信验证码（修改密码前）
export function verifyCode(phone: string, code: string) {
  return post('/user/verify-code', { phone, code })
}

// 修改密码
export function changePassword(newPassword: string) {
  return post('/user/change-password', { newPassword })
}
