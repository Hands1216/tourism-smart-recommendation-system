<template>
  <div class="login-page">
    <!-- 左侧品牌视觉区 -->
    <div class="login-visual">
      <div class="visual-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </div>
          <span class="logo-text">旅游智慧推荐</span>
        </div>
        <h1 class="visual-title">探索世界的美好</h1>
        <p class="visual-desc">智能推荐 · AI助手 · 行程规划</p>

        <!-- 装饰元素 -->
        <div class="visual-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>

        <!-- 特性列表 -->
        <div class="feature-list">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
              <circle cx="12" cy="10" r="3" />
            </svg>
            <span>精选景点推荐</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5" />
              <path d="M2 12l10 5 10-5" />
            </svg>
            <span>AI智能助手</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <span>智能行程规划</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-form-wrapper">
      <div class="login-box">
        <!-- 主题切换 -->
        <button
          class="theme-toggle"
          @click="themeStore.toggleTheme"
          :aria-label="themeStore.isDark ? '切换到亮色模式' : '切换到深色模式'"
        >
          <svg v-if="themeStore.isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5" />
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
          </svg>
        </button>

        <div class="login-header">
          <h2>{{ loginType === 'user' ? '欢迎回来' : '管理员登录' }}</h2>
          <p>{{ loginType === 'user' ? '登录以继续您的旅程' : '请使用管理员账号登录' }}</p>
        </div>

        <!-- 登录类型选择 -->
        <div class="login-tabs" role="tablist">
          <button
            class="tab-item"
            :class="{ active: loginType === 'user' }"
            role="tab"
            :aria-selected="loginType === 'user'"
            @click="loginType = 'user'"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            普通用户
          </button>
          <button
            class="tab-item"
            :class="{ active: loginType === 'admin' }"
            role="tab"
            :aria-selected="loginType === 'admin'"
            @click="loginType = 'admin'"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 15a3 3 0 100-6 3 3 0 000 6z" />
              <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z" />
            </svg>
            管理员
          </button>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              class="custom-input"
              aria-label="手机号"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                  <line x1="12" y1="18" x2="12.01" y2="18" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              class="custom-input"
              show-password
              aria-label="密码"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0110 0v4" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-actions">
            <router-link v-if="loginType === 'user'" to="/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="loginType === 'user'" class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>

        <div class="skip-login">
          <button class="skip-btn" @click="handleSkip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4" />
              <polyline points="10 17 15 12 10 7" />
              <line x1="15" y1="12" x2="3" y2="12" />
            </svg>
            跳过登录，直接浏览
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const formRef = ref()
const loading = ref(false)
const loginType = ref<'user' | 'admin'>('user')

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// 登录
const handleLogin = async () => {
  await formRef.value.validate()

  loading.value = true
  try {
    if (loginType.value === 'admin') {
      await authStore.adminLogin(form.phone, form.password)
      ElMessage.success('登录成功')

      const role = authStore.userRole
      if (role === 'admin') {
        router.push('/admin/dashboard')
      } else if (role === 'content_admin') {
        router.push('/admin/content')
      } else {
        router.push('/admin/dashboard')
      }
    } else {
      await authStore.login(form.phone, form.password)
      ElMessage.success('登录成功')

      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/index')
    }
  } catch {
    // axios拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}

// 跳过登录
const handleSkip = () => {
  router.push('/index')
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-secondary);
}

// ========== 左侧品牌视觉区 ==========
.login-visual {
  flex: 1;
  display: none;
  position: relative;
  background: var(--gradient-primary);
  overflow: hidden;

  @media (min-width: $breakpoint-lg) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.visual-content {
  position: relative;
  z-index: 1;
  padding: $space-10;
  color: white;
  max-width: 480px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-bottom: $space-10;
}

.logo-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(8px);

  svg {
    width: 28px;
    height: 28px;
  }
}

.logo-text {
  font-size: $text-xl;
  font-weight: $font-bold;
}

.visual-title {
  font-size: $text-4xl;
  font-weight: $font-bold;
  line-height: $leading-tight;
  margin-bottom: $space-4;
}

.visual-desc {
  font-size: $text-lg;
  opacity: 0.9;
  margin-bottom: $space-10;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: $space-4;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-4;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(8px);
  transition: all var(--duration-fast) var(--ease-in-out);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateX(8px);
  }

  svg {
    width: 24px;
    height: 24px;
    flex-shrink: 0;
  }

  span {
    font-size: $text-base;
    font-weight: $font-medium;
  }
}

// 装饰圆圈
.visual-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);

  &.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    right: -100px;
    animation: float 6s ease-in-out infinite;
  }

  &.circle-2 {
    width: 200px;
    height: 200px;
    bottom: 10%;
    left: -50px;
    animation: float 8s ease-in-out infinite reverse;
  }

  &.circle-3 {
    width: 150px;
    height: 150px;
    bottom: 30%;
    right: 10%;
    animation: float 7s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

// ========== 右侧登录表单区 ==========
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $space-6;
  background: var(--bg-primary);

  @media (min-width: $breakpoint-lg) {
    max-width: 560px;
  }
}

.login-box {
  width: 100%;
  max-width: 400px;
  position: relative;
}

// 主题切换按钮
.theme-toggle {
  position: absolute;
  top: 0;
  right: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: var(--bg-secondary);
    color: var(--color-primary);
    transform: rotate(15deg);
  }
}

.login-header {
  text-align: center;
  margin-bottom: $space-8;

  h2 {
    font-size: $text-2xl;
    font-weight: $font-bold;
    color: var(--text-primary);
    margin-bottom: $space-2;
  }

  p {
    color: var(--text-secondary);
    font-size: $text-sm;
  }
}

// 登录类型选择
.login-tabs {
  display: flex;
  gap: $space-2;
  padding: $space-1;
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  margin-bottom: $space-6;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  padding: $space-3;
  border-radius: var(--radius-md);
  font-size: $text-sm;
  font-weight: $font-medium;
  color: var(--text-secondary);
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover:not(.active) {
    color: var(--text-primary);
  }

  &.active {
    background: var(--bg-primary);
    color: var(--color-primary);
    box-shadow: var(--shadow-sm);
  }
}

// 表单样式
.login-form {
  margin-bottom: $space-4;
}

.custom-input {
  :deep(.el-input__wrapper) {
    padding: $space-3 $space-4;
    border-radius: var(--radius-lg);
    background: var(--bg-secondary);
    box-shadow: none;
    border: 1px solid transparent;
    transition: all var(--duration-fast) var(--ease-in-out);

    &:hover {
      border-color: var(--border-color);
    }

    &.is-focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 3px rgba($brand-primary, 0.1);
      background: var(--bg-primary);
    }
  }
}

.input-icon {
  width: 20px;
  height: 20px;
  color: var(--text-tertiary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: $space-4;
}

.forgot-link {
  font-size: $text-sm;
  color: var(--color-primary);
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: $text-base;
  font-weight: $font-semibold;
}

.register-link {
  text-align: center;
  font-size: $text-sm;
  color: var(--text-secondary);

  a {
    color: var(--color-primary);
    text-decoration: none;
    font-weight: $font-medium;

    &:hover {
      text-decoration: underline;
    }
  }
}

.skip-login {
  margin-top: $space-6;
  text-align: center;
}

.skip-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  padding: $space-3 $space-6;
  font-size: $text-sm;
  color: var(--text-tertiary);
  background: transparent;
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: rgba($brand-primary, 0.04);
  }
}
</style>
