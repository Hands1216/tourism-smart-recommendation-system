<template>
  <div class="profile-page">
    <!-- 返回按钮 -->
    <BackButton fallback="/index" class="page-back-btn" />

    <el-card>
      <template #header>
        <h2>个人信息</h2>
      </template>

      <el-form
        :model="form"
        label-width="100px"
        style="max-width: 500px"
      >
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar
              :size="80"
              :src="avatarSrc"
            >
              {{ form.nickname?.charAt(0) }}
            </el-avatar>
            <el-button
              size="small"
              :loading="avatarUploading"
              style="margin-left: 15px"
              @click="triggerAvatarUpload"
            >
              更换头像
            </el-button>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              style="display: none"
              @change="handleAvatarChange"
            >
          </div>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
            v-model="form.phone"
            disabled
          />
        </el-form-item>

        <el-form-item label="偏好标签">
          <div class="preferences-tags">
            <span
              v-for="tag in availableTags"
              :key="tag"
              class="preference-tag"
              :class="{ active: interests.includes(tag) }"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="saveProfile"
          >
            保存
          </el-button>
          <el-button @click="$router.push('/user/change-password')">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { uploadImage } from '@/api/upload'
import BackButton from '@/components/BackButton.vue'

const authStore = useAuthStore()
const loading = ref(false)
const avatarUploading = ref(false)
const interests = ref<string[]>([])
const avatarInputRef = ref<HTMLInputElement | null>(null)

const availableTags = [
  '自然风光', '历史文化', '美食探店', '亲子游',
  '户外探险', '摄影打卡', '休闲度假', '城市观光',
  '温泉养生', '海岛沙滩', '古镇古村', '主题乐园'
]

const toggleTag = (tag: string) => {
  const index = interests.value.indexOf(tag)
  if (index > -1) {
    interests.value.splice(index, 1)
  } else {
    interests.value.push(tag)
  }
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }

  avatarUploading.value = true
  try {
    form.avatar = await uploadImage(file) as unknown as string
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

const form = reactive({
  nickname: '',
  phone: '',
  avatar: ''
})

// 头像src：相对路径加/api前缀以通过Vite代理访问后端静态资源
const avatarSrc = computed(() => {
  if (!form.avatar) return ''
  if (form.avatar.startsWith('http')) return form.avatar
  return '/api' + form.avatar
})

const loadProfile = async () => {
  try {
    const user = await authStore.fetchUserInfo()
    form.nickname = user.nickname || ''
    form.phone = user.phone || ''
    form.avatar = user.avatar || ''
    // 解析偏好标签
    if (user.preferences) {
      try {
        interests.value = JSON.parse(user.preferences)
      } catch (e) {
        // 如果解析失败，尝试解析格式如 ["标签1","标签2"]
        const cleaned = user.preferences.replace(/[\[\]"]/g, '')
        interests.value = cleaned.split(',').filter((t: string) => t.trim())
      }
    }
  } catch (error) {
    console.error('加载失败', error)
  }
}

const saveProfile = async () => {
  loading.value = true
  try {
    await authStore.updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
      preferences: JSON.stringify(interests.value)
    })
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 600px;
  margin: 0 auto;
}

.page-back-btn {
  margin-bottom: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

.preferences-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.preference-tag {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  user-select: none;
}

.preference-tag:hover {
  border-color: #409eff;
  color: #409eff;
}

.preference-tag.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}
</style>
