<template>
  <div class="register-page">
    <!-- 左侧品牌视觉区 -->
    <div class="register-visual">
      <div class="visual-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </div>
          <span class="logo-text">旅游智慧推荐</span>
        </div>
        <h1 class="visual-title">开启您的旅程</h1>
        <p class="visual-desc">注册账号，享受个性化旅游推荐</p>

        <div class="visual-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单区 -->
    <div class="register-form-wrapper">
      <div class="register-box">
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

        <div class="register-header">
          <h2>创建账号</h2>
          <p>填写信息，开始您的旅行之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入用户名"
              size="large"
              class="custom-input"
              aria-label="用户名"
            >
              <template #prefix>
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </template>
            </el-input>
          </el-form-item>

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

          <el-form-item prop="code">
            <div class="code-input-wrapper">
              <el-input
                v-model="form.code"
                placeholder="请输入验证码"
                size="large"
                class="custom-input"
                aria-label="验证码"
              >
                <template #prefix>
                  <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                    <path d="M12 3v4M8 5h8" />
                  </svg>
                </template>
              </el-input>
              <el-button
                type="primary"
                size="large"
                class="code-btn"
                :disabled="countdown > 0"
                @click="sendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请设置密码（至少6位）"
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

          <el-form-item prop="preferences">
            <div class="preferences-section">
              <div class="preferences-label">选择您的旅游偏好</div>
              <div class="preferences-tags">
                <span
                  v-for="tag in availableTags"
                  :key="tag"
                  class="preference-tag"
                  :class="{ active: selectedTags.includes(tag) }"
                  @click="toggleTag(tag)"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              <span v-if="!loading">注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
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
import { register } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const formRef = ref()
const loading = ref(false)
const countdown = ref(0)

const availableTags = [
  '自然风光', '历史文化', '美食探店', '亲子游',
  '户外探险', '摄影打卡', '休闲度假', '城市观光',
  '温泉养生', '海岛沙滩', '古镇古村', '主题乐园'
]
const selectedTags = ref<string[]>([])

const form = reactive({
  nickname: '',
  phone: '',
  code: '',
  password: '',
  preferences: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const toggleTag = (tag: string) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
  form.preferences = JSON.stringify(selectedTags.value)
}

const sendCode = async () => {
  if (!form.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('手机号格式不正确')
    return
  }

  try {
    await authStore.sendSmsCode(form.phone)
    ElMessage.success('验证码已发送')

    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch {
    // axios拦截器已处理错误提示
  }
}

const handleRegister = async () => {
  await formRef.value.validate()

  loading.value = true
  try {
    await register({
      nickname: form.nickname,
      phone: form.phone,
      code: form.code,
      password: form.password,
      preferences: form.preferences || '[]'
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // axios拦截器已处理错误提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.register-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-secondary);
}

// ========== 左侧品牌视觉区 ==========
.register-visual {
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
}

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
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

// ========== 右侧注册表单区 ==========
.register-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $space-6;
  background: var(--bg-primary);
  overflow-y: auto;

  @media (min-width: $breakpoint-lg) {
    max-width: 560px;
  }
}

.register-box {
  width: 100%;
  max-width: 420px;
  position: relative;
}

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

.register-header {
  text-align: center;
  margin-bottom: $space-6;

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

.register-form {
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

.code-input-wrapper {
  display: flex;
  gap: $space-3;
  width: 100%;

  .custom-input {
    flex: 1;
  }
}

.code-btn {
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  padding: 0 $space-4;
  min-width: 120px;
}

// 偏好标签
.preferences-section {
  width: 100%;
}

.preferences-label {
  font-size: $text-sm;
  color: var(--text-secondary);
  margin-bottom: $space-3;
}

.preferences-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $space-2;
}

.preference-tag {
  padding: $space-1 $space-3;
  border-radius: var(--radius-full);
  font-size: $text-sm;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  user-select: none;

  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }

  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
  }
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: $text-base;
  font-weight: $font-semibold;
}

.login-link {
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
</style>
