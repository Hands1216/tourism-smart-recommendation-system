import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginByPhone, adminLogin as adminLoginApi, sendSms, logout as logoutApi } from '@/api/auth'
import { getUserInfo as fetchUserInfoApi, updateProfile as updateProfileApi } from '@/api/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const userInfo = ref<any>(null)
  const userRole = ref<string>('')

  // 初始化 - 从localStorage恢复状态
  function init() {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      token.value = savedToken
    }

    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        const info = JSON.parse(savedUserInfo)
        userInfo.value = info
        userRole.value = info.role || ''
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  }

  // 初始化时立即执行
  init()

  // 是否已登录
  const isLogin = computed(() => !!token.value)

  // 用户信息别名
  const user = computed(() => userInfo.value)

  // 头像完整URL：相对路径补/api前缀以通过Vite代理访问后端静态资源
  const avatarUrl = computed(() => {
    const raw = userInfo.value?.avatar
    if (!raw) return ''
    if (raw.startsWith('http')) return raw
    return '/api' + raw
  })

  // 设置Token
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 获取Token
  function getToken() {
    return token.value
  }

  // 设置用户信息
  function setUserInfo(info: any) {
    userInfo.value = info
    userRole.value = info.role || ''
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 获取用户信息
  function getUserInfo() {
    return userInfo.value
  }

  // 清除登录信息
  function clearAuth() {
    token.value = ''
    userInfo.value = null
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 发送短信验证码
  async function sendSmsCode(phone: string) {
    await sendSms({ phone })
  }

  // 登录（手机号+密码）
  async function login(phone: string, password: string) {
    const res = await loginByPhone({ phone, password })
    setToken(res.token)
    setUserInfo(res.userInfo)
  }

  // 管理员登录（手机号+密码）
  async function adminLogin(phone: string, password: string) {
    const res = await adminLoginApi({ phone, password })
    setToken(res.token)
    setUserInfo(res.userInfo)
  }

  // 退出登录
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.error('退出登录API调用失败', e)
    } finally {
      clearAuth()
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await fetchUserInfoApi()
    setUserInfo(res)
    return res
  }

  // 更新用户信息
  async function updateProfile(data: { nickname?: string; avatar?: string; preferences?: string }) {
    await updateProfileApi(data)
    if (userInfo.value) {
      setUserInfo({ ...userInfo.value, ...data })
    }
  }

  return {
    token,
    userInfo,
    userRole,
    isLogin,
    user,
    avatarUrl,
    setToken,
    getToken,
    setUserInfo,
    getUserInfo,
    clearAuth,
    init,
    sendSmsCode,
    login,
    adminLogin,
    logout,
    fetchUserInfo,
    updateProfile
  }
})
