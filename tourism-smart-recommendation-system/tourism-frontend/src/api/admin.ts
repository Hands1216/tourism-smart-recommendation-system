import { get, post, put, del } from './index'

export interface DashboardStats {
  userCount: number
  attractionCount: number
  strategyCount: number
  todayViewCount: number
  pendingAuditCount: number
}

export interface AuditDTO {
  auditStatus: number
  auditReason?: string
}

// 获取仪表盘统计
export function getDashboardStats() {
  return get<DashboardStats>('/admin/dashboard/stats')
}

// 获取最新攻略
export function getRecentStrategies() {
  return get<any[]>('/admin/dashboard/recent-strategies')
}

// 管理后台攻略列表
export function getAdminStrategyList(params: {
  title?: string
  auditStatus?: number
  destination?: string
  page?: number
  size?: number
}) {
  return get<any>('/admin/strategies', params)
}

// 审核攻略
export function auditStrategy(id: number, data: AuditDTO) {
  return put<void>(`/admin/strategy/${id}/audit`, data)
}

// 修改攻略状态
export function updateStrategyStatus(id: number, status: number) {
  return put<void>(`/admin/strategy/${id}/status`, null, { params: { status } })
}

// 删除攻略
export function deleteAdminStrategy(id: number) {
  return del<void>(`/admin/strategy/${id}`)
}

// 获取用户列表
export function getUserList(params: {
  keyword?: string
  page?: number
  size?: number
}) {
  return get<any>('/admin/users', params)
}

// 修改用户状态
export function updateUserStatus(id: number, status: number) {
  return put<void>(`/admin/user/${id}/status`, null, { params: { status } })
}

// 修改用户角色
export function updateUserRole(id: number, role: string) {
  return put<void>(`/admin/user/${id}/role`, null, { params: { role } })
}
