<template>
  <div class="comment-section">
    <h3 class="section-title">
      评论 ({{ total }})
    </h3>

    <!-- 发表评论 -->
    <div class="comment-input">
      <el-avatar :size="36">
        {{ currentUser?.charAt(0) || '游' }}
      </el-avatar>
      <div class="input-wrapper">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
        <div class="input-actions">
          <el-button
            type="primary"
            :disabled="!newComment.trim()"
            @click="submitComment"
          >
            发表评论
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div
      v-loading="loading"
      class="comment-list"
    >
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <el-avatar
          :src="comment.authorAvatar"
          :size="36"
        >
          {{ comment.authorName?.charAt(0) }}
        </el-avatar>
        <div class="comment-body">
          <div class="comment-header">
            <span class="user-name">{{ comment.authorName }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">
            {{ comment.content }}
          </div>
          <div class="comment-actions">
            <span
              :class="['action-btn', { active: comment.isLiked }]"
              @click="handleLikeComment(comment)"
            >
              <el-icon><svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></el-icon> {{ comment.likeCount || 0 }}
            </span>
            <span
              class="action-btn"
              @click="startReply(comment)"
            >
              <el-icon><ChatDotRound /></el-icon> 回复
            </span>
            <span
              v-if="comment.userId === currentUserId"
              class="action-btn delete"
              @click="handleDelete(comment)"
            >
              <el-icon><Delete /></el-icon> 删除
            </span>
          </div>

          <!-- 回复输入框 -->
          <div
            v-if="replyingTo === comment.id"
            class="reply-input"
          >
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              :placeholder="`回复 ${comment.authorName}...`"
              maxlength="500"
            />
            <div class="reply-actions">
              <el-button
                size="small"
                @click="replyingTo = null"
              >
                取消
              </el-button>
              <el-button
                size="small"
                type="primary"
                :disabled="!replyContent.trim()"
                @click="submitReply(comment.id)"
              >
                回复
              </el-button>
            </div>
          </div>

          <!-- 子回复列表 -->
          <div
            v-if="comment.replies && comment.replies.length > 0"
            class="replies"
          >
            <div
              v-for="reply in comment.replies"
              :key="reply.id"
              class="reply-item"
            >
              <el-avatar
                :src="reply.authorAvatar"
                :size="28"
              >
                {{ reply.authorName?.charAt(0) }}
              </el-avatar>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="user-name">{{ reply.authorName }}</span>
                  <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <div class="comment-content">
                  {{ reply.content }}
                </div>
                <div class="comment-actions">
                  <span
                    :class="['action-btn', { active: reply.isLiked }]"
                    @click="handleLikeComment(reply)"
                  >
                    <el-icon><Star /></el-icon> {{ reply.likeCount || 0 }}
                  </span>
                  <span
                    v-if="reply.userId === currentUserId"
                    class="action-btn delete"
                    @click="handleDelete(reply)"
                  >
                    <el-icon><Delete /></el-icon> 删除
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty
        v-if="!loading && comments.length === 0"
        description="暂无评论，快来抢沙发吧~"
      />

      <!-- 分页 -->
      <div
        v-if="total > pageSize"
        class="pagination"
      >
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'
import { getComments, addComment, deleteComment, likeComment } from '@/api/strategy'
import type { CommentVO } from '@/api/strategy'

const props = defineProps<{
  strategyId: number
}>()

const loading = ref(false)
const comments = ref<CommentVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const newComment = ref('')
const replyingTo = ref<number | null>(null)
const replyContent = ref('')

// 从 localStorage 获取当前用户信息
const currentUser = ref(localStorage.getItem('userName') || '')
const currentUserId = computed(() => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info.id || 0
  } catch {
    return 0
  }
})

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const loadComments = async () => {
  try {
    loading.value = true
    const data = await getComments(props.strategyId, {
      page: currentPage.value,
      size: pageSize
    })
    comments.value = data.records
    total.value = data.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载评论失败')
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  try {
    await addComment(props.strategyId, { content: newComment.value.trim() })
    newComment.value = ''
    ElMessage.success('评论成功')
    currentPage.value = 1
    await loadComments()
  } catch (error: any) {
    ElMessage.error(error.message || '评论失败')
  }
}

const startReply = (comment: CommentVO) => {
  replyingTo.value = replyingTo.value === comment.id ? null : comment.id
  replyContent.value = ''
}

const submitReply = async (parentId: number) => {
  try {
    await addComment(props.strategyId, {
      content: replyContent.value.trim(),
      parentId
    })
    replyContent.value = ''
    replyingTo.value = null
    ElMessage.success('回复成功')
    await loadComments()
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败')
  }
}
const handleLikeComment = async (comment: CommentVO) => {
  try {
    await likeComment(props.strategyId, comment.id)
    comment.isLiked = !comment.isLiked
    comment.likeCount = comment.isLiked
      ? (comment.likeCount || 0) + 1
      : Math.max(0, (comment.likeCount || 0) - 1)
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (comment: CommentVO) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
    await deleteComment(props.strategyId, comment.id)
    ElMessage.success('删除成功')
    await loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 20px;
}

.section-title {
  font-size: 20px;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
}

.input-wrapper {
  flex: 1;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-list {
  min-height: 100px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.user-name {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #409eff;
}

.action-btn.active {
  color: #f56c6c;
}

.action-btn.delete:hover {
  color: #f56c6c;
}

.reply-input {
  margin-top: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.replies {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.reply-item + .reply-item {
  border-top: 1px solid #f8f8f8;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
