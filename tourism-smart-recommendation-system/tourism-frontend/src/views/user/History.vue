<template>
  <div class="history-page">
    <!-- 返回按钮 -->
    <BackButton fallback="/index" class="page-back-btn" />

    <h2>行程记录</h2>

    <div
      v-if="loading"
      class="loading-container"
    >
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <p>加载中...</p>
    </div>

    <div
      v-else-if="plans && plans.length > 0"
      class="plan-list"
    >
      <el-card
        v-for="plan in plans"
        :key="plan.id"
        class="plan-card"
      >
        <template #header>
          <div class="plan-header">
            <div>
              <h3>{{ plan.title || '未命名行程' }}</h3>
              <p class="plan-meta">
                {{ plan.destination || '未知目的地' }} · {{ plan.days || 0 }}天
                <el-tag
                  v-if="plan.isAiGenerated"
                  type="info"
                  size="small"
                >
                  AI生成
                </el-tag>
              </p>
            </div>
            <div class="plan-actions">
              <el-button
                type="primary"
                size="small"
                @click="viewPlanDetail(plan)"
              >
                查看详情
              </el-button>
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                @click="deletePlan(plan.id)"
              />
            </div>
          </div>
        </template>

        <div class="plan-summary">
          <p v-if="plan.budget">
            预算：¥{{ plan.budget }}
          </p>
          <p>创建时间：{{ formatCreateTime(plan) }}</p>
        </div>
      </el-card>
    </div>

    <el-empty
      v-else
      description="暂无行程记录"
    >
      <el-button
        type="primary"
        @click="router.push('/plan')"
      >
        创建行程
      </el-button>
    </el-empty>

    <!-- 行程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentPlan ? (currentPlan.title || '行程详情') : '行程详情'"
      width="900px"
      :close-on-click-modal="false"
    >
      <div
        v-if="currentPlan"
        class="plan-detail-content"
      >
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4 class="section-title">
            基本信息
          </h4>
          <el-descriptions
            :column="2"
            border
          >
            <el-descriptions-item label="目的地">
              {{ currentPlan.destination || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="天数">
              {{ currentPlan.days || 0 }}天
            </el-descriptions-item>
            <el-descriptions-item
              v-if="currentPlan.budget"
              label="预算"
            >
              ¥{{ currentPlan.budget }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatCreateTime(currentPlan) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 行程详情 -->
        <div
          v-if="planDetailData"
          class="detail-section"
        >
          <h4 class="section-title">
            每日行程
          </h4>
          <div class="daily-plans">
            <div
              v-for="(day, index) in planDetailData.days"
              :key="index"
              class="day-plan"
            >
              <div class="day-header">
                <span class="day-number">第{{ index + 1 }}天</span>
                <span
                  v-if="day.title"
                  class="day-title"
                >{{ day.title }}</span>
              </div>

              <el-timeline class="day-timeline">
                <el-timeline-item
                  v-for="(activity, actIndex) in day.activities"
                  :key="actIndex"
                  :timestamp="activity.time"
                  placement="top"
                  :type="getTimelineType(activity.type)"
                  class="timeline-item"
                >
                  <el-card
                    class="activity-card"
                    :class="'activity-' + activity.type"
                  >
                    <div class="activity-header">
                      <el-tag
                        :type="getActivityTagType(activity.type)"
                        size="small"
                      >
                        {{ getActivityTypeName(activity.type) }}
                      </el-tag>
                      <h4 class="activity-title">
                        {{ activity.title }}
                      </h4>
                    </div>
                    <p class="activity-desc">
                      {{ activity.description }}
                    </p>

                    <!-- 额外信息 -->
                    <div class="activity-extra">
                      <div
                        v-if="activity.duration"
                        class="extra-item"
                      >
                        <el-icon><Clock /></el-icon>
                        <span>建议游览时长：{{ activity.duration }}</span>
                      </div>
                      <div
                        v-if="activity.distance"
                        class="extra-item"
                      >
                        <el-icon><Position /></el-icon>
                        <span>距离：{{ activity.distance }}</span>
                      </div>
                      <div
                        v-if="activity.cost"
                        class="extra-item cost"
                      >
                        <el-icon><Coin /></el-icon>
                        <span>预估费用：{{ activity.cost }}</span>
                      </div>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </div>

        <el-empty
          v-else
          description="暂无详细行程数据"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Clock, Position, Coin, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoutePlans, getRoutePlan, deleteRoutePlan as deletePlanApi } from '@/api/recommend'
import BackButton from '@/components/BackButton.vue'

const router = useRouter()
const loading = ref(false)
const plans = ref<any[]>([])
const detailDialogVisible = ref(false)
const currentPlan = ref<any>(null)
const planDetailData = ref<any>(null)

const loadPlans = async () => {
  loading.value = true
  try {
    const result = await getRoutePlans()
    console.log('行程列表API返回:', result)
    if (Array.isArray(result)) {
      plans.value = result
    } else {
      plans.value = []
    }
  } catch (error) {
    console.error('加载失败', error)
    ElMessage.error('加载行程列表失败: ' + (error.message || error))
    plans.value = []
  } finally {
    loading.value = false
  }
}

const viewPlanDetail = async (plan: any) => {
  currentPlan.value = plan
  detailDialogVisible.value = true
  planDetailData.value = null

  // 获取行程详情
  try {
    const result = await getRoutePlan(plan.id)
    console.log('行程详情API返回:', result)

    if (result && result.planData) {
      // 解析 planData JSON 字符串
      try {
        planDetailData.value = JSON.parse(result.planData)
        console.log('解析后的行程详情:', planDetailData.value)
      } catch (e) {
        console.error('解析 planData JSON 失败:', e)
        ElMessage.error('行程数据格式错误')
      }
    } else {
      console.warn('行程详情为空或没有 planData 字段')
      planDetailData.value = null
    }
  } catch (error) {
    console.error('加载行程详情失败', error)
    ElMessage.error('加载行程详情失败: ' + (error.message || error))
  }
}

const deletePlan = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该行程吗？', '提示', {
      type: 'warning'
    })

    console.log('删除行程ID:', id)
    const result = await deletePlanApi(id)
    console.log('删除结果:', result)

    if (result) {
      plans.value = plans.value.filter(p => p.id !== id)
      ElMessage.success('已删除')
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除行程失败:', error)
    // 用户取消删除时不显示错误
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatCreateTime = (plan: any) => {
  if (!plan) return '-'
  const timeValue = plan.createTime
  if (!timeValue) return '-'

  try {
    // 处理两种格式：
    // 1. ISO 8601 字符串: "2026-02-13T19:41:23"
    // 2. Jackson 序列化的数组: [2026, 2, 13, 19, 41, 23]
    let date: Date

    if (Array.isArray(timeValue)) {
      // Jackson 数组格式: [year, month, day, hour, minute, second]
      const [year, month, day, hour, minute, second] = timeValue
      date = new Date(year, month - 1, day, hour, minute, second)
    } else {
      // ISO 8601 字符串格式
      const dateStr = String(timeValue)
      const normalized = dateStr.replace('T', ' ').split('.')[0]
      date = new Date(normalized)
    }

    if (isNaN(date.getTime())) {
      return String(timeValue)
    }

    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    console.error('日期格式化失败:', e, timeValue)
    return String(timeValue)
  }
}

const getTimelineType = (type: string) => {
  const typeMap: Record<string, any> = {
    'attraction': 'primary',
    'restaurant': 'success',
    'hotel': 'warning',
    'transport': 'info'
  }
  return typeMap[type] || 'primary'
}

const getActivityTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    'attraction': 'primary',
    'restaurant': 'success',
    'hotel': 'warning',
    'transport': 'info'
  }
  return typeMap[type] || ''
}

const getActivityTypeName = (type: string) => {
  const nameMap: Record<string, string> = {
    'attraction': '景点',
    'restaurant': '美食',
    'hotel': '住宿',
    'transport': '交通'
  }
  return nameMap[type] || '活动'
}

onMounted(() => {
  loadPlans()
})
</script>

<style scoped>
.history-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-back-btn {
  margin-bottom: 20px;
}

.history-page h2 {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.plan-card {
  margin-bottom: 20px;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.plan-header h3 {
  margin: 0 0 8px 0;
}

.plan-meta {
  margin: 0;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.plan-actions {
  display: flex;
  gap: 10px;
}

.plan-summary p {
  margin: 5px 0;
  color: #666;
}

.plan-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e9ecef;
}

.daily-plans {
  margin-top: 16px;
}

.day-plan {
  margin-bottom: 24px;
}

.day-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}

.day-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
}

.day-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.day-timeline {
  padding-left: 20px;
}

.timeline-item :deep(.el-timeline-item__timestamp) {
  font-weight: 600;
  color: #667eea;
}

.activity-card {
  margin-bottom: 12px;
  border-left: 4px solid #667eea;
}

.activity-card.activity-restaurant {
  border-left-color: #67c23a;
}

.activity-card.activity-hotel {
  border-left-color: #e6a23c;
}

.activity-card.activity-transport {
  border-left-color: #909399;
  border-left-style: dashed;
}

.activity-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.activity-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.activity-desc {
  color: #606266;
  line-height: 1.8;
  margin: 10px 0;
}

.activity-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.extra-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #606266;
}

.extra-item .el-icon {
  color: #667eea;
}

.extra-item.cost {
  color: #e6a23c;
  font-weight: 600;
}
</style>
