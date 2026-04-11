<template>
  <div class="change-password-page">
    <BackButton fallback="/user/profile" class="page-back-btn" />

    <el-card>
      <template #header>
        <h2>修改密码</h2>
      </template>

      <!-- 步骤1：验证验证码 -->
      <div v-if="step === 1" class="step-content">
        <el-form label-width="100px" style="max-width: 500px">
          <el-form-item label="手机号">
            <el-input v-model="phone" disabled />
          </el-form-item>

          <el-form-item label="验证码">
            <div class="code-input">
              <el-input
                v-model="code"
                placeholder="请输入验证码"
                maxlength="6"
              />
              <el-button
                :disabled="countdown > 0"
                :loading="sendingCode"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="verifying"
              @click="handleVerify"
            >
              一键验证
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：重置密码 -->
      <div v-if="step === 2" class="step-content">
        <el-form label-width="100px" style="max-width: 500px">
          <el-form-item label="新密码">
            <el-input
              v-model="newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码">
            <el-input
              v-model="confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="submitting"
              @click="handleChangePassword"
            >
              确认修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { sendSms } from '@/api/auth'
import { verifyCode, changePassword } from '@/api/user'
import BackButton from '@/components/BackButton.vue'

const router = useRouter()
const authStore = useAuthStore()

const phone = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const step = ref(1)
const countdown = ref(0)
const sendingCode = ref(false)
const verifying = ref(false)
const submitting = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  const user = await authStore.fetchUserInfo()
  phone.value = user.phone || ''
})

const handleSendCode = async () => {
  if (!phone.value) return
  sendingCode.value = true
  try {
    await sendSms({ phone: phone.value })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch {
    // axios拦截器已处理错误提示
  } finally {
    sendingCode.value = false
  }
}

const handleVerify = async () => {
  if (!code.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  verifying.value = true
  try {
    await verifyCode(phone.value, code.value)
    step.value = 2
  } catch {
    // axios拦截器已处理错误提示
  } finally {
    verifying.value = false
  }
}

const handleChangePassword = async () => {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('两次密码不一致')
    return
  }
  submitting.value = true
  try {
    await changePassword(newPassword.value)
    ElMessage.success('密码修改成功，请重新登录')
    authStore.logout()
    router.push('/login')
  } catch {
    // axios拦截器已处理错误提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.change-password-page {
  max-width: 600px;
  margin: 0 auto;
}

.page-back-btn {
  margin-bottom: 20px;
}

.code-input {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-input .el-input {
  flex: 1;
}
</style>
