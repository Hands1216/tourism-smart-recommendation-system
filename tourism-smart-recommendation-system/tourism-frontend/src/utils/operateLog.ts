/**
 * 操作日志记录工具
 * 用于记录管��员在后台的操作行为
 */

import axios from '@/api'
import { useAuthStore } from '@/stores/auth'

// 操作类型常量
export const OperationType = {
  LOGIN: 'login',
  CREATE: 'create',
  UPDATE: 'update',
  DELETE: 'delete',
  AUDIT: 'audit',
  QUERY: 'query',
  EXPORT: 'export'
}

// 操作模块常量
export const OperationModule = {
  USER: '用户管理',
  ATTRACTION: '景点管理',
  STRATEGY: '攻略管理',
  CONTENT: '内容审核',
  DASHBOARD: '仪表盘',
  SYSTEM: '系统设置'
}

/**
 * 记录操作日志
 * @param operationType 操作类型
 * @param module 操作模块
 * @param description 操作描述
 * @param status 执行状态（0-失败，1-成功）
 * @param executeTime 执行时长（毫秒）
 */
export async function recordOperateLog(
  operationType: string,
  module: string,
  description: string,
  status: number = 1,
  executeTime?: number
) {
  try {
    const authStore = useAuthStore()
    const user = authStore.user

    if (!user) {
      return
    }

    await axios.post('/admin/logs/record', {
      userId: user.id,
      username: user.nickname || user.phone,
      operationType,
      module,
      description,
      requestMethod: 'POST',
      requestParams: '',
      ipAddress: '',
      status,
      executeTime
    })
  } catch (error) {
    // 日志记录失败不影响主流程���只在控制台输出
    console.error('记录操作日志失败:', error)
  }
}

/**
 * 记录成功的操作
 */
export async function recordSuccess(
  operationType: string,
  module: string,
  description: string,
  executeTime?: number
) {
  return recordOperateLog(operationType, module, description, 1, executeTime)
}

/**
 * 记录失败的操作
 */
export async function recordFailure(
  operationType: string,
  module: string,
  description: string,
  executeTime?: number
) {
  return recordOperateLog(operationType, module, description, 0, executeTime)
}
