/**
 * 主题状态管理
 * 支持亮色/深色模式切换，自动检测系统偏好
 */

import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

export const useThemeStore = defineStore('theme', () => {
  // 当前主题模式
  const mode = ref<ThemeMode>('system')
  // 实际应用的主题（light 或 dark）
  const isDark = ref(false)

  // 系统主题媒体查询
  const systemDarkQuery = window.matchMedia('(prefers-color-scheme: dark)')

  /**
   * 初始化主题
   * 从 localStorage 读取用户偏好，或使用系统偏好
   */
  const initTheme = () => {
    const stored = localStorage.getItem('theme-mode') as ThemeMode | null
    if (stored && ['light', 'dark', 'system'].includes(stored)) {
      mode.value = stored
    } else {
      mode.value = 'system'
    }
    updateTheme()

    // 监听系统主题变化
    systemDarkQuery.addEventListener('change', handleSystemThemeChange)
  }

  /**
   * 处理系统主题变化
   */
  const handleSystemThemeChange = (e: MediaQueryListEvent) => {
    if (mode.value === 'system') {
      isDark.value = e.matches
      applyTheme()
    }
  }

  /**
   * 更新主题状态
   */
  const updateTheme = () => {
    if (mode.value === 'system') {
      isDark.value = systemDarkQuery.matches
    } else {
      isDark.value = mode.value === 'dark'
    }
    applyTheme()
  }

  /**
   * 应用主题到 DOM
   */
  const applyTheme = () => {
    const theme = isDark.value ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme)

    // 更新 meta theme-color（移动端浏览器地址栏颜色）
    const metaThemeColor = document.querySelector('meta[name="theme-color"]')
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', isDark.value ? '#0F172A' : '#FFFFFF')
    }
  }

  /**
   * 设置主题模式
   */
  const setMode = (newMode: ThemeMode) => {
    mode.value = newMode
    localStorage.setItem('theme-mode', newMode)
    updateTheme()
  }

  /**
   * 切换主题（在 light 和 dark 之间切换）
   */
  const toggleTheme = () => {
    if (mode.value === 'system') {
      // 如果当前是系统模式，切换到与当前相反的固定模式
      setMode(isDark.value ? 'light' : 'dark')
    } else {
      // 否则在 light 和 dark 之间切换
      setMode(mode.value === 'dark' ? 'light' : 'dark')
    }
  }

  /**
   * 循环切换主题模式（light -> dark -> system -> light）
   */
  const cycleMode = () => {
    const modes: ThemeMode[] = ['light', 'dark', 'system']
    const currentIndex = modes.indexOf(mode.value)
    const nextIndex = (currentIndex + 1) % modes.length
    setMode(modes[nextIndex])
  }

  // 监听 mode 变化
  watch(mode, updateTheme)

  return {
    mode,
    isDark,
    initTheme,
    setMode,
    toggleTheme,
    cycleMode
  }
})
