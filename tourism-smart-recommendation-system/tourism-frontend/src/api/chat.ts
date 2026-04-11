import { post } from './index'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export interface ChatParams {
  sessionId: string
  message: string
  history?: ChatMessage[]
}

// 发送聊天消息
export function sendMessage(params: ChatParams) {
  return post('/chat/send', params)
}

// 获取聊天历史
export function getChatHistory(sessionId: string) {
  return post('/chat/history', { sessionId })
}

// 清除聊天会话
export function clearChatSession(sessionId: string) {
  return post('/chat/clear', { sessionId })
}
