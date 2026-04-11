import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/api/chat'
import { sendMessage as sendChatMessage } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const sessionId = ref<string>('')
  const messages = ref<ChatMessage[]>([])
  const isLoading = ref<boolean>(false)
  const currentInput = ref<string>('')

  // 初始化会话
  function initSession() {
    sessionId.value = Date.now().toString()
    messages.value = []
  }

  // 发送消息
  async function sendMessage() {
    if (!currentInput.value.trim() || isLoading.value) {
      return
    }

    const userMessage: ChatMessage = {
      role: 'user',
      content: currentInput.value,
      timestamp: Date.now()
    }

    messages.value.push(userMessage)
    const content = currentInput.value
    currentInput.value = ''
    isLoading.value = true

    try {
      const response = await sendChatMessage({
        sessionId: sessionId.value,
        message: content,
        history: messages.value
      })

      const assistantMessage: ChatMessage = {
        role: 'assistant',
        content: response.content || response,
        timestamp: Date.now()
      }

      messages.value.push(assistantMessage)
    } catch (error) {
      console.error('发送消息失败', error)
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我遇到了一些问题，请稍后再试。',
        timestamp: Date.now()
      })
    } finally {
      isLoading.value = false
    }
  }

  // 清除会话
  function clearSession() {
    initSession()
  }

  return {
    sessionId,
    messages,
    isLoading,
    currentInput,
    initSession,
    sendMessage,
    clearSession
  }
})
