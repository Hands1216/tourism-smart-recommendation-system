<template>
  <div class="callback-container">
    <el-card class="callback-card">
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>正在登录中...</p>
      </div>
      <div v-else-if="error" class="error">
        <el-icon><CircleCloseFilled /></el-icon>
        <p>{{ error }}</p>
        <el-button type="primary" @click="goLogin">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Loading, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref('')

onMounted(async () => {
  const code = route.query.code as string
  if (!code) {
    error.value = '授权失败，未获取到授权码'
    loading.value = false
    return
  }

  try {
    await authStore.loginByWechat(code)
    ElMessage.success('登录成功')
    router.push('/index')
  } catch (e: any) {
    error.value = e.message || '微信登录失败'
  } finally {
    loading.value = false
  }
})

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.callback-card {
  width: 300px;
  text-align: center;
  padding: 40px 20px;
}

.loading, .error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading .el-icon {
  font-size: 48px;
  color: #667eea;
}

.error .el-icon {
  font-size: 48px;
  color: #f56c6c;
}
</style>
