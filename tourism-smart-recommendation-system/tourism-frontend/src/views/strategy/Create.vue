<template>
  <div class="strategy-create-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button
              :icon="ArrowLeft"
              @click="handleBack"
            >
              返回
            </el-button>
            <h2>{{ isEdit ? '编辑攻略' : '创建攻略' }}</h2>
          </div>
          <span
            v-if="autoSaveTime"
            class="auto-save-tip"
          >已自动保存 {{ autoSaveTime }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="攻略标题"
          prop="title"
        >
          <el-input
            v-model="form.title"
            placeholder="请输入攻略标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item
              label="目的地"
              prop="destination"
            >
              <el-input
                v-model="form.destination"
                placeholder="请输入目的地"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="行程天数">
              <el-input-number
                v-model="form.days"
                :min="1"
                :max="30"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="人均预算">
              <el-input-number
                v-model="form.budget"
                :min="0"
                :max="999999"
                :step="100"
                :precision="0"
                placeholder="元"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="适合季节">
              <el-select
                v-model="form.season"
                placeholder="选择季节"
                clearable
                style="width: 100%"
              >
                <el-option
                  label="全年"
                  value="all"
                />
                <el-option
                  label="春季"
                  value="spring"
                />
                <el-option
                  label="夏季"
                  value="summer"
                />
                <el-option
                  label="秋季"
                  value="autumn"
                />
                <el-option
                  label="冬季"
                  value="winter"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="可见性">
              <el-radio-group v-model="form.visibility">
                <el-radio :value="1">
                  公开
                </el-radio>
                <el-radio :value="0">
                  仅自己可见
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="封面图片">
          <el-upload
            class="cover-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeUpload"
          >
            <img
              v-if="form.coverImage"
              :src="form.coverImage"
              class="cover-image"
            >
            <el-icon
              v-else
              class="cover-uploader-icon"
            >
              <Plus />
            </el-icon>
          </el-upload>
          <div class="tip">
            建议尺寸：800x450，支持jpg/png/webp格式，最大10MB
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            closable
            style="margin-right: 10px"
            @close="handleTagClose(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="tagInputVisible"
            ref="tagInputRef"
            v-model="tagInputValue"
            size="small"
            style="width: 100px"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button
            v-else
            size="small"
            @click="showTagInput"
          >
            + 添加标签
          </el-button>
        </el-form-item>

        <el-form-item label="攻略摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="简要描述攻略亮点（选填，不填则自动截取）"
          />
        </el-form-item>

        <el-form-item
          label="攻略内容"
          prop="content"
        >
          <div class="editor-container">
            <Toolbar
              :editor="editorRef"
              :default-config="toolbarConfig"
              mode="default"
              style="border-bottom: 1px solid #ccc"
            />
            <Editor
              v-model="form.content"
              :default-config="editorConfig"
              mode="default"
              style="height: 500px; overflow-y: hidden"
              @on-created="handleCreated"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handlePublish"
          >
            发布攻略
          </el-button>
          <el-button
            :loading="loading"
            @click="handleSaveDraft"
          >
            保存草稿
          </el-button>
          <el-button @click="showPreview = true">
            预览
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="showPreview"
      title="攻略预览"
      fullscreen
    >
      <div class="preview-container">
        <h1 class="preview-title">
          {{ form.title }}
        </h1>
        <div class="preview-meta">
          <span v-if="form.destination">📍 {{ form.destination }}</span>
          <span v-if="form.days">🗓 {{ form.days }}天</span>
          <span v-if="form.budget">💰 人均￥{{ form.budget }}</span>
          <span v-if="form.season">🌤 {{ seasonLabel(form.season) }}</span>
        </div>
        <div
          v-if="form.tags && form.tags.length"
          class="preview-tags"
        >
          <el-tag
            v-for="tag in form.tags"
            :key="tag"
            type="info"
            style="margin-right: 8px"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div
          v-if="form.summary"
          class="preview-summary"
        >
          {{ form.summary }}
        </div>
        <div
          class="preview-content"
          v-html="form.content"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, shallowRef, onBeforeUnmount, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { create, update, getDetail, getDrafts } from '@/api/strategy'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isEdit = ref(false)
const strategyId = ref<number>()
const loading = ref(false)
const formRef = ref()
const editorRef = shallowRef()
const showPreview = ref(false)
const autoSaveTime = ref('')
const autoSaveTimer = ref<number>()
const lastSavedContent = ref('')

const uploadUrl = '/api/upload/image'
const uploadHeaders = {
  Authorization: `Bearer ${authStore.token}`
}

const form = reactive({
  title: '',
  destination: '',
  days: 3,
  budget: undefined as number | undefined,
  season: '',
  coverImage: '',
  tags: [] as string[],
  summary: '',
  content: '',
  status: 1,
  visibility: 1
})

const rules = {
  title: [{ required: true, message: '请输入攻略标题', trigger: 'blur' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  content: [{ required: true, message: '请输入攻略内容', trigger: 'blur' }]
}

const seasonLabel = (season?: string) => {
  const map: Record<string, string> = { all: '全年', spring: '春季', summer: '夏季', autumn: '秋季', winter: '冬季' }
  return map[season || ''] || ''
}

// 标签输入
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

const showTagInput = () => {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value?.focus())
}

const handleTagClose = (tag: string) => {
  form.tags.splice(form.tags.indexOf(tag), 1)
}

const handleTagConfirm = () => {
  if (tagInputValue.value && !form.tags.includes(tagInputValue.value)) {
    form.tags.push(tagInputValue.value)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 封面上传
const handleCoverSuccess = (response: any) => {
  form.coverImage = response.data
  ElMessage.success('封面上传成功')
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt10M) { ElMessage.error('图片大小不能超过 10MB'); return false }
  return true
}

// WangEditor 配置
const toolbarConfig = {
  toolbarKeys: [
    'headerSelect', 'bold', 'italic', 'underline', 'through',
    'color', 'bgColor', '|',
    'bulletedList', 'numberedList', 'blockquote', '|',
    'uploadImage', 'insertLink', 'insertTable', '|',
    'undo', 'redo', 'fullScreen'
  ]
}

const editorConfig = {
  placeholder: '请输入攻略内容...',
  MENU_CONF: {
    uploadImage: {
      server: uploadUrl,
      fieldName: 'file',
      maxFileSize: 10 * 1024 * 1024,
      allowedFileTypes: ['image/*'],
      headers: uploadHeaders,
      customInsert(res: any, insertFn: Function) {
        insertFn(res.data, '', '')
      }
    }
  }
}

const handleCreated = (editor: any) => {
  editorRef.value = editor
}

// 自动保存到localStorage
const startAutoSave = () => {
  autoSaveTimer.value = window.setInterval(() => {
    const currentContent = JSON.stringify(form)
    if (currentContent === lastSavedContent.value) return
    if (!form.title && !form.content) return

    try {
      localStorage.setItem('strategy_draft', currentContent)
      lastSavedContent.value = currentContent
      const now = new Date()
      autoSaveTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    } catch {
      // 静默失败
    }
  }, 30000)
}

const recoverDraft = () => {
  const saved = localStorage.getItem('strategy_draft')
  if (saved && !route.params.id && !route.query.draftId) {
    ElMessageBox.confirm('检测到未保存的草稿，是否恢复？', '提示', { type: 'info' }).then(() => {
      const data = JSON.parse(saved)
      Object.assign(form, data)
    }).catch(() => {
      localStorage.removeItem('strategy_draft')
    })
  }
}

// 检查草稿数量限制
const checkDraftLimit = async (): Promise<boolean> => {
  try {
    const res = await getDrafts({ page: 1, size: 10 })
    const draftCount = res.total || 0
    // 如果是编辑已有草稿，不计入限制
    if (isEdit.value && form.status === 0) {
      return true
    }
    if (draftCount >= 3) {
      ElMessage.warning('草稿箱已满（最多3条），请先删除旧草稿')
      return false
    }
    return true
  } catch {
    return true // 出错时允许保存
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!form.title) {
    ElMessage.warning('请至少输入攻略标题')
    return
  }

  // 检查草稿数量限制（仅新建时检查）
  if (!isEdit.value) {
    const canSave = await checkDraftLimit()
    if (!canSave) return
  }

  loading.value = true
  try {
    const data = { ...form, status: 0 }
    if (isEdit.value && strategyId.value) {
      await update(strategyId.value, data)
    } else {
      await create(data)
    }
    ElMessage.success('草稿保存成功')
    localStorage.removeItem('strategy_draft')
    router.push('/strategy')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 返回按钮处理
const handleBack = async () => {
  // 检查是否有未保存的内容
  const hasContent = form.title || form.content || form.destination
  if (!hasContent) {
    localStorage.removeItem('strategy_draft')
    router.push('/strategy')
    return
  }

  try {
    await ElMessageBox.confirm(
      '是否将当前内容保存为草稿？',
      '提示',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '保存草稿',
        cancelButtonText: '不保存',
        type: 'warning'
      }
    )
    // 用户点击"保存草稿"
    await handleSaveDraft()
  } catch (action) {
    if (action === 'cancel') {
      // 用户点击"不保存"
      localStorage.removeItem('strategy_draft')
      router.push('/strategy')
    }
    // 用户点击关闭按钮，不做任何操作
  }
}

// 发布攻略
const handlePublish = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const data = { ...form, status: 1 }

    if (isEdit.value && strategyId.value) {
      await update(strategyId.value, data)
      ElMessage.success('攻略更新成功，等待审核')
    } else {
      await create(data)
      ElMessage.success('攻略发布成功，等待审核')
    }

    localStorage.removeItem('strategy_draft')
    router.push('/strategy')
  } catch (error: any) {
    if (error !== 'cancel' && error?.message) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载攻略数据（编辑模式）
const loadStrategy = async (id: number) => {
  try {
    isEdit.value = true
    strategyId.value = id
    const data = await getDetail(id)
    form.title = data.title
    form.destination = data.destination
    form.days = data.days || 3
    form.budget = data.budget
    form.season = data.season || ''
    form.coverImage = data.coverImage || ''
    form.tags = typeof data.tags === 'string' ? JSON.parse(data.tags) : (data.tags || [])
    form.summary = data.summary || ''
    form.content = data.content
    form.status = data.status || 1
    form.visibility = data.visibility ?? 1
  } catch (error: any) {
    ElMessage.error('加载攻略失败')
    router.back()
  }
}

onMounted(() => {
  const id = Number(route.params.id)
  const draftId = Number(route.query.draftId)

  if (id) {
    loadStrategy(id)
  } else if (draftId) {
    loadStrategy(draftId)
  } else {
    recoverDraft()
  }
  startAutoSave()
})

onBeforeUnmount(() => {
  if (autoSaveTimer.value) clearInterval(autoSaveTimer.value)
  editorRef.value?.destroy()
})
</script>

<style scoped>
.strategy-create-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
}

.auto-save-tip {
  font-size: 13px;
  color: #67c23a;
}

.cover-uploader {
  width: 300px;
  height: 169px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 169px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.editor-container {
  border: 1px solid #ccc;
  z-index: 100;
}

/* 预览样式 */
.preview-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.preview-title {
  font-size: 28px;
  margin: 0 0 16px 0;
}

.preview-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.preview-tags {
  margin-bottom: 16px;
}

.preview-summary {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 6px;
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.preview-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.preview-content :deep(h1),
.preview-content :deep(h2),
.preview-content :deep(h3) {
  margin: 24px 0 12px 0;
}

.preview-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.preview-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 12px 16px;
  background: #f5f7fa;
  margin: 16px 0;
  border-radius: 4px;
}
</style>
