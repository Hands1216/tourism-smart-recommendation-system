<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 顶部栏 -->
      <header class="chat-header">
        <div class="header-left">
          <div class="ai-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5" />
              <path d="M2 12l10 5 10-5" />
            </svg>
          </div>
          <div class="header-info">
            <h1 class="header-title">AI 旅行助手</h1>
            <span class="header-status">
              <span class="status-dot"></span>
              在线
            </span>
          </div>
        </div>
        <el-button
          class="clear-btn"
          @click="handleClear"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
          </svg>
          <span class="hidden-sm">清除会话</span>
        </el-button>
      </header>

      <!-- 消息列表 -->
      <div
        ref="messagesRef"
        class="chat-messages"
        role="log"
        aria-label="聊天消息"
      >
        <!-- 欢迎消息 -->
        <div
          v-if="chatStore.messages.length === 0"
          class="welcome-section"
        >
          <div class="welcome-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5" />
              <path d="M2 12l10 5 10-5" />
            </svg>
          </div>
          <h2 class="welcome-title">你好！我是你的 AI 旅行助手</h2>
          <p class="welcome-desc">我可以帮你规划旅程、推荐景点、解答旅行问题</p>

          <div class="suggestion-grid">
            <button
              v-for="suggestion in suggestions"
              :key="suggestion.text"
              class="suggestion-card"
              @click="handleSuggestion(suggestion.text)"
            >
              <div class="suggestion-icon" :class="suggestion.color">
                <component :is="suggestion.icon" />
              </div>
              <span class="suggestion-text">{{ suggestion.text }}</span>
            </button>
          </div>
        </div>

        <!-- 消息气泡 -->
        <TransitionGroup name="message">
          <div
            v-for="(msg, index) in chatStore.messages"
            :key="index"
            class="message-item"
            :class="msg.role"
          >
            <div
              v-if="msg.role === 'assistant'"
              class="message-avatar assistant-avatar"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
            <div class="message-bubble">
              <div
                class="message-content"
                v-html="formatContent(msg.content)"
              />
              <div class="message-time">
                {{ formatTime(msg.timestamp) }}
              </div>
            </div>
            <div
              v-if="msg.role === 'user'"
              class="message-avatar user-avatar"
            >
              {{ authStore.user?.nickname?.charAt(0) || '我' }}
            </div>
          </div>
        </TransitionGroup>

        <!-- 加载动画 -->
        <Transition name="fade">
          <div
            v-if="chatStore.isLoading"
            class="message-item assistant"
          >
            <div class="message-avatar assistant-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-wrapper">
          <el-input
            v-model="chatStore.currentInput"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入您的问题...（Enter 发送）"
            :disabled="chatStore.isLoading"
            resize="none"
            class="message-input"
            aria-label="消息输入框"
            @keydown="handleKeydown"
          />
          <button
            class="send-btn"
            :class="{ active: chatStore.currentInput.trim() }"
            :disabled="!chatStore.currentInput.trim() || chatStore.isLoading"
            @click="handleSend"
            aria-label="发送消息"
          >
            <svg v-if="!chatStore.isLoading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
            <div v-else class="loading-spinner"></div>
          </button>
        </div>
        <p class="input-hint">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          Shift + Enter 换行
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, h } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'

const chatStore = useChatStore()
const authStore = useAuthStore()
const messagesRef = ref<HTMLElement>()

// 建议问题
const suggestions = [
  {
    text: '推荐一个适合周末游的城市',
    color: 'primary',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z' }),
      h('circle', { cx: '12', cy: '10', r: '3' })
    ])
  },
  {
    text: '帮我规划一个3天的北京行程',
    color: 'secondary',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('rect', { x: '3', y: '4', width: '18', height: '18', rx: '2', ry: '2' }),
      h('line', { x1: '16', y1: '2', x2: '16', y2: '6' }),
      h('line', { x1: '8', y1: '2', x2: '8', y2: '6' }),
      h('line', { x1: '3', y1: '10', x2: '21', y2: '10' })
    ])
  },
  {
    text: '有什么美食推荐吗？',
    color: 'accent',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M18 8h1a4 4 0 010 8h-1' }),
      h('path', { d: 'M2 8h16v9a4 4 0 01-4 4H6a4 4 0 01-4-4V8z' }),
      h('line', { x1: '6', y1: '1', x2: '6', y2: '4' }),
      h('line', { x1: '10', y1: '1', x2: '10', y2: '4' }),
      h('line', { x1: '14', y1: '1', x2: '14', y2: '4' })
    ])
  },
  {
    text: '旅行需要准备什么？',
    color: 'success',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z' }),
      h('polyline', { points: '14 2 14 8 20 8' }),
      h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
      h('line', { x1: '16', y1: '17', x2: '8', y2: '17' }),
      h('polyline', { points: '10 9 9 9 8 9' })
    ])
  }
]

onMounted(() => {
  if (!chatStore.sessionId) {
    chatStore.initSession()
  }
})

watch(
  () => chatStore.messages.length,
  () => {
    nextTick(() => scrollToBottom())
  }
)

watch(
  () => chatStore.isLoading,
  () => {
    nextTick(() => scrollToBottom())
  }
)

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

const handleSend = () => {
  chatStore.sendMessage()
}

const handleSuggestion = (text: string) => {
  chatStore.currentInput = text
  nextTick(() => {
    handleSend()
  })
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const handleClear = () => {
  ElMessageBox.confirm('确定要清除当前会话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    chatStore.clearSession()
  }).catch(() => {})
}

const formatContent = (content: string) => {
  if (!content) return ''
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
}

const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.chat-page {
  height: calc(100vh - 140px);
  display: flex;
  justify-content: center;
  padding: 0;
}

.chat-container {
  width: 100%;
  max-width: 900px;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

// ========== 顶部栏 ==========
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-4 $space-5;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.ai-avatar {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  color: white;

  svg {
    width: 24px;
    height: 24px;
  }
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.header-title {
  font-size: $text-lg;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin: 0;
}

.header-status {
  display: flex;
  align-items: center;
  gap: $space-1;
  font-size: $text-xs;
  color: var(--color-success);
}

.status-dot {
  width: 8px;
  height: 8px;
  background: var(--color-success);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-3;
  border-radius: var(--radius-md);
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: $text-sm;
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    border-color: var(--color-danger);
    color: var(--color-danger);
    background: rgba($color-danger, 0.05);
  }
}

// ========== 消息列表 ==========
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: $space-5;
  display: flex;
  flex-direction: column;
  gap: $space-4;
  background: var(--bg-secondary);
}

// 欢迎区域
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: $space-10 $space-4;
}

.welcome-avatar {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-xl);
  color: white;
  margin-bottom: $space-5;

  svg {
    width: 40px;
    height: 40px;
  }
}

.welcome-title {
  font-size: $text-xl;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin-bottom: $space-2;
}

.welcome-desc {
  font-size: $text-sm;
  color: var(--text-secondary);
  margin-bottom: $space-8;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
  width: 100%;
  max-width: 500px;

  @media (max-width: $breakpoint-sm) {
    grid-template-columns: 1fr;
  }
}

.suggestion-card {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-4;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  text-align: left;
  transition: all var(--duration-fast) var(--ease-in-out);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.suggestion-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
  }

  &.primary {
    background: rgba($brand-primary, 0.1);
    color: var(--color-primary);
  }

  &.secondary {
    background: rgba($brand-secondary, 0.1);
    color: $brand-secondary;
  }

  &.accent {
    background: rgba($brand-accent, 0.1);
    color: $brand-accent;
  }

  &.success {
    background: rgba($color-success, 0.1);
    color: var(--color-success);
  }
}

.suggestion-text {
  font-size: $text-sm;
  color: var(--text-primary);
  line-height: $leading-snug;
}

// 消息项
.message-item {
  display: flex;
  gap: $space-3;
  align-items: flex-start;

  &.user {
    flex-direction: row-reverse;
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.assistant-avatar {
    background: var(--gradient-primary);
    color: white;

    svg {
      width: 22px;
      height: 22px;
    }
  }

  &.user-avatar {
    background: var(--color-primary);
    color: white;
    font-size: $text-sm;
    font-weight: $font-semibold;
  }
}

.message-bubble {
  max-width: 70%;
  min-width: 60px;
}

.message-item.assistant .message-bubble {
  background: var(--bg-primary);
  border-radius: $radius-sm var(--radius-lg) var(--radius-lg) var(--radius-lg);
  border: 1px solid var(--border-color);
}

.message-item.user .message-bubble {
  background: var(--color-primary);
  border-radius: var(--radius-lg) $radius-sm var(--radius-lg) var(--radius-lg);
}

.message-content {
  padding: $space-3 $space-4;
  line-height: $leading-relaxed;
  word-break: break-word;
  font-size: $text-sm;

  .message-item.assistant & {
    color: var(--text-primary);
  }

  .message-item.user & {
    color: white;
  }

  :deep(code) {
    background: rgba(0, 0, 0, 0.06);
    padding: 2px 6px;
    border-radius: var(--radius-sm);
    font-size: $text-xs;
    font-family: var(--font-mono);
  }

  :deep(strong) {
    font-weight: $font-semibold;
  }
}

.message-time {
  padding: 0 $space-4 $space-2;
  font-size: $text-xs;
  color: var(--text-tertiary);

  .message-item.user & {
    text-align: right;
    color: rgba(255, 255, 255, 0.7);
  }
}

// 打字动画
.typing-indicator {
  padding: $space-4 $space-5;
  display: flex;
  gap: 6px;
  align-items: center;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--text-tertiary);
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

// ========== 输入区域 ==========
.chat-input {
  padding: $space-4 $space-5;
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  display: flex;
  gap: $space-3;
  align-items: flex-end;
}

.message-input {
  flex: 1;

  :deep(.el-textarea__inner) {
    padding: $space-3 $space-4;
    border-radius: var(--radius-lg);
    background: var(--bg-secondary);
    border: 1px solid transparent;
    font-size: $text-sm;
    line-height: $leading-normal;
    resize: none;
    transition: all var(--duration-fast) var(--ease-in-out);

    &:hover {
      border-color: var(--border-color);
    }

    &:focus {
      border-color: var(--color-primary);
      background: var(--bg-primary);
      box-shadow: 0 0 0 3px rgba($brand-primary, 0.1);
    }
  }
}

.send-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  transition: all var(--duration-fast) var(--ease-in-out);
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
  }

  &.active {
    background: var(--color-primary);
    color: white;

    &:hover {
      background: var(--color-primary-dark);
      transform: scale(1.05);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.input-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-1;
  margin-top: $space-2;
  font-size: $text-xs;
  color: var(--text-tertiary);

  svg {
    width: 12px;
    height: 12px;
  }
}

// ========== 过渡动画 ==========
.message-enter-active,
.message-leave-active {
  transition: all var(--duration-normal) var(--ease-out);
}

.message-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.message-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--duration-fast) var(--ease-in-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ========== 响应式 ==========
@media (max-width: $breakpoint-sm) {
  .hidden-sm {
    display: none;
  }

  .message-bubble {
    max-width: 85%;
  }
}
</style>
