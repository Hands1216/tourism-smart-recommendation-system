import { get, post, put, del } from './index'

export interface AttractionQueryParams {
  keyword?: string
  categoryId?: string
  city?: string
  page: number
  size: number
}

// 高级查询参数
export interface AttractionAdvancedQueryParams {
  keyword?: string
  categoryId?: number
  province?: string
  city?: string
  district?: string
  scenicLevel?: string
  sceneType?: string
  minPrice?: number
  maxPrice?: number
  suggestedDuration?: string
  bestMonth?: number
  bestSeason?: string
  sortBy?: string
  sortOrder?: string
  userLat?: number
  userLng?: number
  page: number
  size: number
}

export interface AttractionDetail {
  id: string
  name: string
  description: string
  images: string[]
  address: string
  longitude: number
  latitude: number
  openTime: string
  ticketPrice: number
  rating: number
  category: string
  tags: string[]
}

// 评分DTO
export interface AttractionRatingDTO {
  attractionId: number
  sceneryScore: number
  funScore: number
  valueScore: number
  comment?: string
}

// 足迹DTO
export interface FootprintDTO {
  attractionId: number
  visitDate: string
  note?: string
}

// 筛选选项VO
export interface FilterOptions {
  provinces: string[]
  scenicLevels: string[]
  sceneTypes: string[]
  durations: string[]
  provinceCityMap: Record<string, string[]>
  cityDistrictMap: Record<string, string[]>
}

// 获取景点列表
export function getAttractionList(params: AttractionQueryParams) {
  return get('/attraction/list', params)
}

// 高级筛选景点列表
export function getAttractionListAdvanced(params: AttractionAdvancedQueryParams) {
  return post('/attraction/list/advanced', params)
}

// 获取景点详情
export function getAttractionDetail(id: string) {
  return get(`/attraction/${id}`)
}

// 获取增强版景点详情
export function getAttractionDetailEnhanced(id: number) {
  return get(`/attraction/${id}/enhanced`)
}

// 获取筛选选项
export function getFilterOptions() {
  return get('/attraction/filter-options')
}

// 评价景点
export function rateAttraction(data: AttractionRatingDTO) {
  return post(`/attraction/${data.attractionId}/rate`, data)
}

// 获取景点评论列表（分页，仅有评论内容的）
export function getAttractionRatings(attractionId: number, params?: { page?: number; size?: number }) {
  return get<any>(`/attraction/${attractionId}/ratings`, params)
}

// 删除景点评论
export function deleteAttractionRating(attractionId: number, ratingId: number) {
  return del(`/attraction/${attractionId}/ratings/${ratingId}`)
}

// 添加景点评论
export function addAttractionComment(attractionId: number, comment: string) {
  return post(`/attraction/${attractionId}/comment`, { comment })
}

// 获取我的评价
export function getMyRating(attractionId: number) {
  return get(`/attraction/${attractionId}/my-rating`)
}

// 标记已游览
export function markVisited(data: FootprintDTO) {
  return post(`/attraction/${data.attractionId}/visited`, data)
}

// 取消已游览
export function unmarkVisited(attractionId: number) {
  return del(`/attraction/${attractionId}/visited`)
}

// 获取相似景点
export function getSimilarAttractions(attractionId: number) {
  return get(`/attraction/${attractionId}/similar`)
}

// 获取相关攻略
export function getRelatedStrategies(attractionId: number) {
  return get(`/attraction/${attractionId}/strategies`)
}

// 获取我的足迹
export function getMyFootprints(params: { page: number; size: number }) {
  return get('/attraction/footprints', params)
}

// 收藏景点
export function favoriteAttraction(id: string) {
  return post(`/attraction/${id}/favorite`)
}

// 取消收藏
export function unfavoriteAttraction(id: string) {
  return del(`/attraction/${id}/favorite`)
}

// 获取推荐景点（基于用户偏好）
export function getRecommendAttractions(limit: number = 8) {
  return get('/attraction/recommend', { limit })
}

// ==================== 管理端API ====================

// 管理端：获取景点列表
export function adminGetAttractionList(params: any) {
  return get('/admin/attractions', params)
}

// 管理端：创建景点
export function adminCreateAttraction(data: any) {
  return post('/admin/attraction', data)
}

// 管理端：更新景点
export function adminUpdateAttraction(id: number, data: any) {
  return put(`/admin/attraction/${id}`, data)
}

// 管理端：删除景点
export function adminDeleteAttraction(id: number) {
  return del(`/admin/attraction/${id}`)
}

// 管理端：设置季节性状态
export function adminSetSeasonalStatus(id: number, seasonalStatus: number, seasonalNote?: string) {
  const params: any = { status: seasonalStatus }
  if (seasonalNote !== undefined && seasonalNote !== '') {
    params.note = seasonalNote
  }
  return put(`/admin/attraction/${id}/seasonal`, null, { params })
}

// 管理端：获取热门景点统计
export function adminGetHotAttractions(params: { days: number; limit: number }) {
  return get('/admin/analytics/hot-attractions', params)
}

// 管理端：获取增长潜力景点
export function adminGetGrowthAttractions(params: { days: number; limit: number }) {
  return get('/admin/analytics/growth-attractions', params)
}

// ==================== 地区数据API ====================

// 根据省份获取城市列表
export function getCitiesByProvince(province: string) {
  return get('/attraction/cities', { province })
}

// 根据城市获取区县列表
export function getDistrictsByCity(city: string) {
  return get('/attraction/districts', { city })
}
