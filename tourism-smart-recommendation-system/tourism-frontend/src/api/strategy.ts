import { get, post, put, del } from './index'

export interface Strategy {
  id?: number
  userId?: number
  title: string
  coverImage?: string
  content: string
  destination: string
  days?: number
  budget?: number
  season?: string
  images?: string[]
  tags?: string[]
  viewCount?: number
  likeCount?: number
  favoriteCount?: number
  commentCount?: number
  isAiGenerated?: boolean
  status?: number
  visibility?: number
  summary?: string
}

export interface StrategyVO extends Strategy {
  authorName?: string
  authorAvatar?: string
  isLiked?: boolean
  isFavorited?: boolean
  auditStatus?: number
  auditReason?: string
  createTime?: string
}

export interface StrategyListResponse {
  records: StrategyVO[]
  total: number
}

// 获取攻略列表
export function getList(params: {
  destination?: string
  keyword?: string
  tag?: string
  orderBy?: string
  minBudget?: number
  maxBudget?: number
  minDays?: number
  maxDays?: number
  season?: string
  page?: number
  size?: number
}) {
  return get<StrategyListResponse>('/strategy/list', params)
}

// 获取攻略详情
export function getDetail(id: number) {
  return get<StrategyVO>(`/strategy/${id}`)
}

// 创建攻略
export function create(data: Strategy) {
  return post<number>('/strategy/create', data)
}

// 更新攻略
export function update(id: number, data: Strategy) {
  return put<void>(`/strategy/${id}`, data)
}

// 删除攻略
export function deleteStrategy(id: number) {
  return del<void>(`/strategy/${id}`)
}

// AI生成攻略
export function aiGenerate(params: {
  destination: string
  days: number
  budget?: number
  interests?: string
}) {
  return post<string>('/strategy/ai-generate', null, { params })
}

// 点赞攻略
export function like(id: number) {
  return post<void>(`/strategy/${id}/like`)
}

// 收藏攻略
export function favorite(id: number) {
  return post<void>(`/strategy/${id}/favorite`)
}

// 获取我的攻略列表
export function getMyList(params: {
  page?: number
  size?: number
}) {
  return get<StrategyListResponse>('/strategy/my', params)
}

// 自动保存草稿
export function autoSave(data: Strategy) {
  return post<number>('/strategy/auto-save', data)
}

// 获取草稿列表
export function getDrafts(params?: { page?: number; size?: number }) {
  return get<StrategyListResponse>('/strategy/drafts', params)
}

// 获取相关推荐
export function getRelated(id: number, limit?: number) {
  return get<StrategyVO[]>(`/strategy/${id}/related`, { limit })
}

// 获取热门搜索词
export function getHotKeywords() {
  return get<string[]>('/strategy/hot-keywords')
}

// ========== 评论相关 ==========

export interface CommentVO {
  id: number
  strategyId: number
  userId: number
  parentId?: number
  content: string
  authorName?: string
  authorAvatar?: string
  likeCount: number
  isLiked?: boolean
  createTime: string
  replies?: CommentVO[]
}

export interface CommentListResponse {
  records: CommentVO[]
  total: number
}

// 获取评论列表
export function getComments(strategyId: number, params?: { page?: number; size?: number }) {
  return get<CommentListResponse>(`/strategy/${strategyId}/comments`, params)
}

// 发表评论
export function addComment(strategyId: number, data: { content: string; parentId?: number }) {
  return post<CommentVO>(`/strategy/${strategyId}/comments`, data)
}

// 删除评论
export function deleteComment(strategyId: number, commentId: number) {
  return del<void>(`/strategy/${strategyId}/comments/${commentId}`)
}

// 点赞评论
export function likeComment(strategyId: number, commentId: number) {
  return post<void>(`/strategy/${strategyId}/comments/${commentId}/like`)
}
