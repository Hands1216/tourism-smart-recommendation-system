import { get, post, del } from './index'

/**
 * 行程规划请求参数
 */
export interface RoutePlanParams {
  departureProvince: string      // 出发省份
  departureCity: string          // 出发城市
  destinations: string[]          // 目的地列表
  days: number                    // 出行天数（自动计算）
  month?: number                  // 出行月份（自动从startDate提取）
  startDate?: string             // 出发日期 yyyy-MM-dd
  endDate?: string               // 返回日期 yyyy-MM-dd
  companion?: string               // 同行伙伴
  stylePreferences?: string[]     // 风格偏好
  pace?: string                   // 行程节奏
  remark?: string                 // 备注说明
}

/**
 * 行程活动项
 */
export interface Activity {
  type: 'attraction' | 'restaurant' | 'hotel' | 'transport'  // 活动类型
  title: string                   // 活动标题
  description: string              // 活动描述
  time?: string                  // 时间
  duration?: string              // 游览时长
  distance?: string              // 距离
  transport?: string             // 交通方式
  cost?: string                  // 预估费用
  tips?: string                  // 提示信息
  image?: string                 // 图片URL
  bookingUrl?: string            // 预订/购票链接
}

/**
 * 日程
 */
export interface DayPlan {
  title?: string                 // 当天标题
  date?: string                  // 日期
  activities: Activity[]           // 活动列表
}

/**
 * 行程规划结果
 */
export interface RoutePlanResult {
  title?: string                 // 行程标题
  totalAttractions?: number     // 总景点数
  totalDistance?: string         // 总里程
  days: DayPlan[]               // 每日行程
}

/**
 * 保存的行程（需匹配后端 RoutePlanVO 结构）
 */
export interface SavedRoutePlan {
  title?: string                 // 行程标题
  totalAttractions?: number     // 总景点数
  totalDistance?: string         // 总里程
  days: DayPlan[]               // 每日行程
  // 保存所需的字段
  departureProvince: string
  departureCity: string
  destinations: string[]
  tripDays: number
  month?: number
  companion?: string
  pace?: string
  remark?: string
}

/**
 * 路线项（旧版，保持兼容）
 */
export interface RouteItem {
  day: number
  attractions: AttractionItem[]
  restaurant?: RestaurantItem
  hotel?: HotelItem
}

export interface AttractionItem {
  id: string
  name: string
  address: string
  duration: number
  order: number
}

/**
 * 路线规划（旧版）
 */
export interface RoutePlan {
  id: string
  title: string
  days: number
  items: RouteItem[]
}

/**
 * 餐厅项
 */
export interface RestaurantItem {
  name: string
  address: string
  price: string
}

/**
 * 酒店项
 */
export interface HotelItem {
  name: string
  address: string
  price: string
}

// 生成路线规划（调用AI）
export function generateRoutePlan(params: RoutePlanParams): Promise<RoutePlanResult> {
  return post('/recommend/route-plan', params)
}

// 保存路线规划
export function saveRoutePlan(plan: SavedRoutePlan): Promise<void> {
  return post('/recommend/route-plan/save', plan)
}

// 获取路线规划详情（返回后端 RoutePlan 实体）
export function getRoutePlan(id: string): Promise<any> {
  return get(`/recommend/route-plan/${id}`)
}

// 获取用户的路线规划列表
export function getRoutePlans(): Promise<any[]> {
  return get('/recommend/route-plan/list')
}

// 删除路线规划
export function deleteRoutePlan(id: number): Promise<boolean> {
  return del(`/recommend/route-plan/${id}`)
}

