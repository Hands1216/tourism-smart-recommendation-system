<template>
  <div class="plan-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>行程规划</h1>
        <p>AI 智能生成您的专属旅行计划</p>
      </div>
      <el-button
        type="primary"
        plain
        @click="drawerVisible = true"
      >
        <el-icon><Notebook /></el-icon>
        行程记录
      </el-button>
    </div>

    <!-- 规划表单 -->
    <el-card
      v-if="!planResult"
      class="form-card"
    >
      <el-form
        ref="formRef"
        :model="planForm"
        :rules="rules"
        label-width="100px"
      >
        <!-- 出发地 -->
        <el-divider content-position="left">
          出发地
        </el-divider>
        <el-row :gutter="20">
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item
              label="出发省份"
              prop="departureProvince"
            >
              <el-select
                v-model="planForm.departureProvince"
                placeholder="选择省份"
                filterable
                style="width: 100%"
                @change="onDepartureProvinceChange"
              >
                <el-option
                  v-for="p in regionData"
                  :key="p.code"
                  :label="p.name"
                  :value="p.name"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item
              label="出发城市"
              prop="departureCity"
            >
              <el-select
                v-model="planForm.departureCity"
                placeholder="请先选择省份"
                filterable
                style="width: 100%"
                :disabled="!planForm.departureProvince"
              >
                <el-option
                  v-for="c in departureCities"
                  :key="c.code"
                  :label="c.name"
                  :value="c.name"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 目的地 -->
        <el-divider content-position="left">
          目的地
          <span class="dest-hint">（最多可选 {{ maxDestinations }} 个城市）</span>
        </el-divider>
        <el-row :gutter="20">
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item label="目的地省份">
              <el-select
                v-model="destProvinceTemp"
                placeholder="选择省份以筛选城市"
                filterable
                clearable
                style="width: 100%"
                @change="onDestProvinceChange"
              >
                <el-option
                  v-for="p in regionData"
                  :key="p.code"
                  :label="p.name"
                  :value="p.name"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item label="选择城市">
              <el-select
                v-model="destCityTemp"
                placeholder="选择城市后点击添加"
                filterable
                style="width: 100%"
                :disabled="!destProvinceTemp"
              >
                <el-option
                  v-for="c in destCityOptions"
                  :key="c.code"
                  :label="c.name"
                  :value="c.name"
                  :disabled="planForm.destinations.includes(c.name)"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item prop="destinations">
          <div class="dest-actions">
            <el-button
              type="primary"
              size="small"
              :disabled="!destCityTemp || planForm.destinations.length >= maxDestinations"
              @click="addDestination"
            >
              添加目的地
            </el-button>
          </div>
          <div
            v-if="planForm.destinations.length"
            class="dest-tags"
          >
            <el-tag
              v-for="(dest, index) in planForm.destinations"
              :key="index"
              closable
              type="primary"
              size="large"
              @close="removeDestination(index)"
            >
              {{ dest }}
            </el-tag>
          </div>
          <div
            v-else
            class="dest-empty"
          >
            <span>请添加目的地城市</span>
          </div>
        </el-form-item>

        <!-- 行程参数 -->
        <el-divider content-position="left">
          行程参数
        </el-divider>
        <el-row :gutter="20">
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item
              label="出发日期"
              prop="startDate"
            >
              <el-date-picker
                v-model="planForm.startDate"
                type="date"
                placeholder="选择出发日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disablePastDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col
            :xs="24"
            :sm="12"
          >
            <el-form-item
              label="返回日期"
              prop="endDate"
            >
              <el-date-picker
                v-model="planForm.endDate"
                type="date"
                placeholder="选择返回日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disableEndDate"
                style="width: 100%"
                :disabled="!planForm.startDate"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="computedDays > 0">
          <el-col :span="24">
            <div class="trip-days-info">
              共 <strong>{{ computedDays }}</strong> 天
            </div>
          </el-col>
        </el-row>

        <el-form-item label="同行人员">
          <el-radio-group v-model="planForm.companion">
            <el-radio value="">
              不限
            </el-radio>
            <el-radio value="独自出行">
              独自出行
            </el-radio>
            <el-radio value="情侣出行">
              情侣出行
            </el-radio>
            <el-radio value="朋友出行">
              朋友出行
            </el-radio>
            <el-radio value="家庭出行">
              家庭出行
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="风格偏好">
          <el-checkbox-group
            v-model="planForm.stylePreferences"
            @change="onStyleChange"
          >
            <el-checkbox
              value="自然风景"
              label="自然风景"
            />
            <el-checkbox
              value="城市风光"
              label="城市风光"
            />
            <el-checkbox
              value="历史文化"
              label="历史文化"
            />
            <el-checkbox
              value="美食体验"
              label="美食体验"
            />
            <el-checkbox
              value="休闲度假"
              label="休闲度假"
            />
          </el-checkbox-group>
          <div
            v-if="planForm.stylePreferences.length >= 3"
            class="form-tip"
          >
            最多选择3项
          </div>
        </el-form-item>

        <el-form-item
          label="行程节奏"
          prop="pace"
        >
          <el-radio-group v-model="planForm.pace">
            <el-radio value="紧凑">
              紧凑（每天3-4个景点）
            </el-radio>
            <el-radio value="适中">
              适中（每天2-3个景点）
            </el-radio>
            <el-radio value="宽松">
              宽松（每天1-2个景点）
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="planForm.remark"
            type="textarea"
            :rows="3"
            placeholder="提出您的个性需求，如：想体验当地夜市、需要无障碍设施等..."
          />
        </el-form-item>

        <div class="form-buttons">
          <el-button
            type="primary"
            size="large"
            :loading="generating"
            @click="generatePlan"
          >
            <el-icon><MagicStick /></el-icon>
            生成行程规划
          </el-button>
          <el-button
            size="large"
            @click="resetForm"
          >
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 行程结果 -->
    <div
      v-if="planResult"
      class="result-section"
    >
      <el-card class="result-header">
        <div class="result-header-content">
          <div>
            <h2>{{ planResult.title || '您的专属行程' }}</h2>
            <div class="result-stats">
              <el-tag type="primary">
                {{ planForm.startDate }} 至 {{ planForm.endDate }}
              </el-tag>
              <el-tag type="success">
                共{{ computedDays || planForm.days }}天
              </el-tag>
              <el-tag
                v-if="planResult.totalAttractions"
                type="warning"
              >
                {{ planResult.totalAttractions }}个景点
              </el-tag>
              <el-tag type="info">
                {{ planForm.departureCity }} → {{ planForm.destinations.join(' → ') }}
              </el-tag>
            </div>
          </div>
          <div class="result-actions">
            <el-button
              type="primary"
              :loading="saving"
              @click="savePlan"
            >
              <el-icon><DocumentAdd /></el-icon>
              保存行程
            </el-button>
            <el-button @click="regeneratePlan">
              <el-icon><Refresh /></el-icon>
              重新生成
            </el-button>
            <el-button @click="backToForm">
              <el-icon><Back /></el-icon>
              返回修改
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 每日行程时间轴 -->
      <el-timeline class="plan-timeline">
        <el-timeline-item
          v-for="(day, index) in planResult.days"
          :key="index"
          :timestamp="`第${index + 1}天 · ${getDayDate(index)}`"
          placement="top"
          size="large"
          :color="'#667eea'"
        >
          <el-card
            class="day-card"
            shadow="hover"
          >
            <template #header>
              <div class="day-header">
                <h3>{{ day.title || `第${index + 1}天` }}</h3>
                <span class="day-date">{{ getDayDate(index) }}</span>
              </div>
            </template>

            <div class="activity-list">
              <div
                v-for="(activity, actIndex) in day.activities"
                :key="actIndex"
                class="activity-item"
              >
                <div class="activity-type-icon">
                  <div
                    class="icon-circle"
                    :class="activity.type"
                  >
                    <span v-if="activity.type === 'attraction'">🏞️</span>
                    <span v-else-if="activity.type === 'restaurant'">🍜</span>
                    <span v-else-if="activity.type === 'hotel'">🏨</span>
                    <span v-else>🚗</span>
                  </div>
                </div>

                <div class="activity-body">
                  <div class="activity-title-row">
                    <h4>{{ activity.title }}</h4>
                    <el-tag
                      v-if="activity.time"
                      size="small"
                      type="info"
                    >
                      {{ activity.time }}
                    </el-tag>
                  </div>
                  <p
                    v-if="activity.description"
                    class="activity-desc"
                  >
                    {{ activity.description }}
                  </p>
                  <div class="activity-tags">
                    <span
                      v-if="activity.duration"
                      class="meta-tag"
                    >⏱ {{ activity.duration }}</span>
                    <span
                      v-if="activity.distance"
                      class="meta-tag"
                    >📍 {{ activity.distance }}</span>
                    <span
                      v-if="activity.type !== 'transport' && activity.transport"
                      class="meta-tag"
                    >🚗 {{ activity.transport }}</span>
                    <span
                      v-if="activity.cost"
                      class="meta-tag"
                    >💰 {{ activity.cost }}</span>
                    <a
                      v-if="activity.bookingUrl"
                      :href="activity.bookingUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="booking-link"
                    >
                      <el-tag type="warning" size="small" effect="plain">
                        <span v-if="activity.type === 'transport'">🎫 购票</span>
                        <span v-else-if="activity.type === 'attraction'">🔍 查看</span>
                        <span v-else-if="activity.type === 'hotel'">🏨 预订</span>
                        <span v-else-if="activity.type === 'restaurant'">📍 查看</span>
                        <span v-else>🔗 详情</span>
                      </el-tag>
                    </a>
                  </div>
                  <el-alert
                    v-if="activity.tips"
                    :title="activity.tips"
                    type="info"
                    :closable="false"
                    show-icon
                    class="activity-tips"
                  />
                </div>

                <el-image
                  v-if="activity.image"
                  :src="activity.image"
                  fit="cover"
                  class="activity-img"
                />
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 生成进度遮罩 -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="generating"
          class="generating-overlay"
        >
          <div class="generating-modal">
            <div class="generating-icon">
              <div class="planet">
                <div class="ring"></div>
              </div>
              <div class="stars">
                <span></span><span></span><span></span><span></span><span></span>
              </div>
            </div>
            <h3>AI 正在为您规划行程</h3>
            <p class="generating-tip">{{ currentTip }}</p>
            <div class="progress-container">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: progressPercent + '%' }"
                ></div>
              </div>
              <span class="progress-text">{{ progressPercent }}%</span>
            </div>
            <p class="generating-hint">预计需要 15-30 秒，请耐心等待...</p>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 行程记录抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="行程记录"
      direction="rtl"
      size="420px"
    >
      <div v-loading="loadingPlans">
        <el-empty
          v-if="myPlans.length === 0"
          description="暂无保存的行程"
        />
        <div
          v-else
          class="drawer-plan-list"
        >
          <div
            v-for="plan in myPlans"
            :key="plan.id"
            class="drawer-plan-item"
          >
            <div
              class="plan-info"
              @click="viewPlan(plan)"
            >
              <h4>{{ plan.title || '未命名行程' }}</h4>
              <p class="plan-route">
                {{ plan.departureCity }} → {{ plan.destinations?.join(' → ') }}
              </p>
              <div class="plan-meta">
                <el-tag size="small">
                  {{ plan.days }}天
                </el-tag>
                <el-tag
                  v-if="plan.pace"
                  size="small"
                  type="info"
                >
                  {{ plan.pace }}
                </el-tag>
                <span class="plan-date">{{ formatDate(plan.createTime) }}</span>
              </div>
            </div>
            <el-button
              type="danger"
              text
              size="small"
              @click="handleDeletePlan(plan.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import {
  MagicStick, DocumentAdd, Refresh, Back, Notebook
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  generateRoutePlan,
  saveRoutePlan,
  getRoutePlans,
  deleteRoutePlan,
  type RoutePlanParams,
  type RoutePlanResult
} from '@/api/recommend'
import { CHINA_REGION_DATA } from '@/utils/china-region-data'

const regionData = CHINA_REGION_DATA

const formRef = ref<FormInstance>()
const generating = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const loadingPlans = ref(false)

// ========== 进度条相关 ==========
const progressPercent = ref(0)
const currentTip = ref('')
let progressTimer: ReturnType<typeof setInterval> | null = null

const generatingTips = [
  '正在分析您的出行偏好...',
  '正在搜索热门景点信息...',
  '正在规划最佳游览路线...',
  '正在查询实时天气数据...',
  '正在优化行程时间安排...',
  '正在生成个性化推荐...',
  '正在整理行程详情...',
  '即将完成，请稍候...'
]

const startProgress = () => {
  progressPercent.value = 0
  currentTip.value = generatingTips[0]
  let tipIndex = 0

  progressTimer = setInterval(() => {
    // 进度增长：前80%快，后20%慢
    if (progressPercent.value < 80) {
      progressPercent.value = Math.floor(progressPercent.value + Math.random() * 3 + 1)
    } else if (progressPercent.value < 95) {
      progressPercent.value = Math.floor(progressPercent.value + Math.random() * 0.5 + 0.3)
    }
    progressPercent.value = Math.min(progressPercent.value, 95)

    // 更新提示文字
    const newTipIndex = Math.min(
      Math.floor(progressPercent.value / 12),
      generatingTips.length - 1
    )
    if (newTipIndex !== tipIndex) {
      tipIndex = newTipIndex
      currentTip.value = generatingTips[tipIndex]
    }
  }, 300)
}

const stopProgress = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  progressPercent.value = 100
  currentTip.value = '生成完成！'
}

// ========== 表单数据 ==========
const planForm = reactive<RoutePlanParams>({
  departureProvince: '',
  departureCity: '',
  destinations: [],
  days: 3,
  month: undefined,
  startDate: '',
  endDate: '',
  companion: '',
  stylePreferences: [],
  pace: '适中',
  remark: ''
})

const planResult = ref<RoutePlanResult | null>(null)
const myPlans = ref<any[]>([])

// ========== 出发地联动 ==========
const departureCities = computed(() => {
  const province = regionData.find(p => p.name === planForm.departureProvince)
  return province ? province.cities : []
})

const onDepartureProvinceChange = () => {
  planForm.departureCity = ''
}

// ========== 目的地联动 ==========
const destProvinceTemp = ref('')
const destCityTemp = ref('')

const destCityOptions = computed(() => {
  const province = regionData.find(p => p.name === destProvinceTemp.value)
  return province ? province.cities : []
})

const onDestProvinceChange = () => {
  destCityTemp.value = ''
}

// 目的地最大数量 = 行程天数 - 1（至少1个）
const maxDestinations = computed(() => Math.max(1, planForm.days - 1))

// ========== 日期选择相关 ==========
const computedDays = computed(() => {
  if (!planForm.startDate || !planForm.endDate) return 0
  const start = new Date(planForm.startDate)
  const end = new Date(planForm.endDate)
  const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  return diff > 0 ? diff : 0
})

// 根据行程天数索引计算具体日期（如 2026-03-15 → 03月15日 周日）
const getDayDate = (dayIndex: number): string => {
  if (!planForm.startDate) return ''
  const start = new Date(planForm.startDate)
  const target = new Date(start.getTime() + dayIndex * 86400000)
  const month = String(target.getMonth() + 1).padStart(2, '0')
  const day = String(target.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[target.getDay()]
  return `${month}月${day}日 ${weekDay}`
}

// 日期变化时自动更新 days 和 month
watch(computedDays, (newDays) => {
  if (newDays > 0) {
    planForm.days = newDays
  }
})

watch(() => planForm.startDate, (val) => {
  if (val) {
    planForm.month = new Date(val).getMonth() + 1
  }
  // 如果结束日期早于开始日期，清空结束日期
  if (planForm.endDate && val && planForm.endDate < val) {
    planForm.endDate = ''
  }
})

const disablePastDate = (date: Date) => {
  return date.getTime() < Date.now() - 86400000
}

const disableEndDate = (date: Date) => {
  if (!planForm.startDate) return true
  const start = new Date(planForm.startDate)
  // 结束日期不能早于开始日期，且最多7天
  return date.getTime() < start.getTime() || date.getTime() > start.getTime() + 6 * 86400000
}

// 天数变化时，截断已选目的地
watch(() => planForm.days, (newDays) => {
  const max = Math.max(1, newDays - 1)
  if (planForm.destinations.length > max) {
    planForm.destinations = planForm.destinations.slice(0, max)
    ElMessage.warning(`${newDays}天行程最多选择${max}个目的地城市`)
  }
})

const addDestination = () => {
  if (!destCityTemp.value) return
  if (planForm.destinations.length >= maxDestinations.value) {
    ElMessage.warning(`当前行程天数最多选择${maxDestinations.value}个目的地`)
    return
  }
  if (planForm.destinations.includes(destCityTemp.value)) {
    ElMessage.warning('该城市已添加')
    return
  }
  planForm.destinations.push(destCityTemp.value)
  destCityTemp.value = ''
}

const removeDestination = (index: number) => {
  planForm.destinations.splice(index, 1)
}

// ========== 风格偏好限制 ==========
const onStyleChange = (val: string[]) => {
  if (val.length > 3) {
    planForm.stylePreferences = val.slice(0, 3)
    ElMessage.warning('风格偏好最多选择3项')
  }
}

// ========== 表单校验 ==========
const rules: FormRules = {
  departureProvince: [{ required: true, message: '请选择出发省份', trigger: 'change' }],
  departureCity: [{ required: true, message: '请选择出发城市', trigger: 'change' }],
  destinations: [{
    required: true,
    validator: (_rule: any, value: string[], callback: any) => {
      if (!value || value.length === 0) {
        callback(new Error('请添加至少一个目的地'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  startDate: [{ required: true, message: '请选择出发日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择返回日期', trigger: 'change' }],
  pace: [{ required: true, message: '请选择行程节奏', trigger: 'change' }]
}

// ========== 生成行程 ==========
const generatePlan = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    generating.value = true
    startProgress()
    try {
      const result = await generateRoutePlan(planForm)
      stopProgress()
      // 短暂延迟让用户看到100%
      await new Promise(resolve => setTimeout(resolve, 500))
      planResult.value = result
      ElMessage.success('行程生成成功！')
    } catch (error: any) {
      ElMessage.error(error.message || '生成失败，请稍后重试')
    } finally {
      stopProgress()
      generating.value = false
    }
  })
}

// ========== 保存行程 ==========
const savePlan = async () => {
  if (!planResult.value) return
  saving.value = true
  try {
    await saveRoutePlan({
      ...planResult.value,
      departureProvince: planForm.departureProvince,
      departureCity: planForm.departureCity,
      destinations: planForm.destinations,
      tripDays: planForm.days,
      month: planForm.month,
      companion: planForm.companion,
      pace: planForm.pace,
      remark: planForm.remark
    })
    ElMessage.success('行程已保存')
    loadMyPlans()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ========== 重新生成 ==========
const regeneratePlan = () => {
  planResult.value = null
  generatePlan()
}

// ========== 返回表单 ==========
const backToForm = () => {
  planResult.value = null
}

// ========== 重置表单 ==========
const resetForm = () => {
  formRef.value?.resetFields()
  planForm.departureProvince = ''
  planForm.departureCity = ''
  planForm.destinations = []
  planForm.days = 3
  planForm.month = undefined
  planForm.startDate = ''
  planForm.endDate = ''
  planForm.companion = ''
  planForm.stylePreferences = []
  planForm.pace = '适中'
  planForm.remark = ''
  destProvinceTemp.value = ''
  destCityTemp.value = ''
}

// ========== 行程记录 ==========
const loadMyPlans = async () => {
  loadingPlans.value = true
  try {
    const plans = await getRoutePlans()
    myPlans.value = plans || []
  } catch (error) {
    console.error('加载行程失败', error)
  } finally {
    loadingPlans.value = false
  }
}

const viewPlan = (plan: any) => {
  drawerVisible.value = false
  // 恢复表单数据
  planForm.departureProvince = plan.departureProvince || ''
  planForm.departureCity = plan.departureCity || ''
  planForm.destinations = plan.destinations || []
  planForm.days = plan.days || 3
  planForm.month = plan.month
  planForm.companion = plan.companion || ''
  planForm.pace = plan.pace || '适中'
  planForm.remark = plan.remark || ''

  // 解析 planData JSON 获取完整行程数据
  let parsedPlan: any = null
  if (plan.planData) {
    try {
      parsedPlan = typeof plan.planData === 'string'
        ? JSON.parse(plan.planData)
        : plan.planData
    } catch (e) {
      console.error('解析行程数据失败', e)
    }
  }

  planResult.value = {
    title: plan.title || parsedPlan?.title,
    totalAttractions: parsedPlan?.totalAttractions || plan.totalAttractions,
    totalDistance: parsedPlan?.totalDistance || plan.totalDistance,
    days: parsedPlan?.days || []
  }
}

const handleDeletePlan = (id: number) => {
  ElMessageBox.confirm('确定要删除这个行程吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRoutePlan(id)
      ElMessage.success('删除成功')
      loadMyPlans()
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadMyPlans()
})
</script>

<style scoped lang="scss">
.plan-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  margin: -20px -20px 30px -20px;
  color: white;
  border-radius: 0 0 12px 12px;

  h1 {
    font-size: 28px;
    margin: 0 0 6px 0;
  }

  p {
    margin: 0;
    opacity: 0.9;
    font-size: 14px;
  }
}

.form-card {
  border-radius: 10px;
  margin-bottom: 20px;
}

.dest-hint {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
  margin-left: 8px;
}

.dest-actions {
  margin-bottom: 10px;
}

.dest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dest-empty {
  color: #c0c4cc;
  font-size: 14px;
}

.trip-days-info {
  text-align: center;
  font-size: 14px;
  color: #606266;
  padding: 4px 0 12px;

  strong {
    color: #409eff;
    font-size: 18px;
    margin: 0 4px;
  }
}

.form-tip {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 4px;
}

.form-buttons {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 10px;
}

/* 结果区域 */
.result-section {
  .result-header {
    margin-bottom: 24px;
    border-radius: 10px;
  }

  .result-header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 16px;

    h2 {
      margin: 0 0 10px 0;
      font-size: 22px;
    }

    .result-stats {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .result-actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
}

.plan-timeline {
  padding: 10px 0;
}

.day-card {
  border-radius: 10px;

  .day-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 17px;
    }

    .day-date {
      color: #909399;
      font-size: 13px;
    }
  }
}

.activity-list {
  .activity-item {
    display: flex;
    gap: 14px;
    padding: 16px 0;
    border-bottom: 1px solid #f0f2f5;

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }
  }
}

.activity-type-icon {
  flex-shrink: 0;

  .icon-circle {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;

    &.attraction { background: #ecf5ff; }
    &.restaurant { background: #f0f9eb; }
    &.hotel { background: #fdf6ec; }
    &.transport { background: #f4f4f5; }
  }
}

.activity-body {
  flex: 1;
  min-width: 0;

  .activity-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;

    h4 {
      margin: 0;
      font-size: 15px;
      color: #303133;
    }
  }

  .activity-desc {
    color: #606266;
    font-size: 13px;
    line-height: 1.6;
    margin: 0 0 8px 0;
  }

  .activity-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;

    .meta-tag {
      font-size: 12px;
      color: #909399;
    }

    .booking-link {
      text-decoration: none;

      .el-tag {
        cursor: pointer;
        transition: opacity 0.2s;

        &:hover {
          opacity: 0.75;
        }
      }
    }
  }

  .activity-tips {
    margin-top: 10px;
  }
}

.activity-img {
  width: 110px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
}

/* 抽屉行程列表 */
.drawer-plan-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.drawer-plan-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    border-color: #667eea;
    background: #f8f9ff;
  }

  .plan-info {
    flex: 1;
    cursor: pointer;
    min-width: 0;

    h4 {
      margin: 0 0 6px 0;
      font-size: 15px;
      color: #303133;
    }

    .plan-route {
      margin: 0 0 8px 0;
      color: #606266;
      font-size: 13px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .plan-meta {
      display: flex;
      align-items: center;
      gap: 8px;

      .plan-date {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

/* 生成进度遮罩 */
.generating-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.generating-modal {
  background: linear-gradient(145deg, #ffffff 0%, #f8f9ff 100%);
  border-radius: 20px;
  padding: 40px 50px;
  text-align: center;
  box-shadow: 0 25px 80px rgba(102, 126, 234, 0.3),
              0 10px 30px rgba(0, 0, 0, 0.1);
  max-width: 420px;
  width: 90%;
  animation: modalIn 0.4s ease-out;

  h3 {
    margin: 0 0 8px 0;
    font-size: 22px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .generating-tip {
    color: #606266;
    font-size: 14px;
    margin: 0 0 24px 0;
    min-height: 20px;
  }

  .generating-hint {
    color: #909399;
    font-size: 12px;
    margin: 16px 0 0 0;
  }
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 动画图标 */
.generating-icon {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
}

.planet {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.5),
              inset -8px -8px 20px rgba(0, 0, 0, 0.2),
              inset 8px 8px 20px rgba(255, 255, 255, 0.2);
  animation: planetPulse 2s ease-in-out infinite;

  .ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) rotateX(75deg);
    width: 80px;
    height: 80px;
    border: 3px solid transparent;
    border-top-color: #667eea;
    border-bottom-color: #764ba2;
    border-radius: 50%;
    animation: ringRotate 3s linear infinite;
  }
}

@keyframes planetPulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.05); }
}

@keyframes ringRotate {
  from { transform: translate(-50%, -50%) rotateX(75deg) rotateZ(0deg); }
  to { transform: translate(-50%, -50%) rotateX(75deg) rotateZ(360deg); }
}

.stars {
  position: absolute;
  width: 100%;
  height: 100%;

  span {
    position: absolute;
    width: 4px;
    height: 4px;
    background: #667eea;
    border-radius: 50%;
    animation: starTwinkle 1.5s ease-in-out infinite;

    &:nth-child(1) { top: 10%; left: 20%; animation-delay: 0s; }
    &:nth-child(2) { top: 5%; left: 70%; animation-delay: 0.3s; }
    &:nth-child(3) { top: 80%; left: 15%; animation-delay: 0.6s; }
    &:nth-child(4) { top: 85%; left: 80%; animation-delay: 0.9s; }
    &:nth-child(5) { top: 40%; left: 90%; animation-delay: 1.2s; }
  }
}

@keyframes starTwinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}

/* 进度条 */
.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.4) 50%,
      transparent 100%
    );
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  from { transform: translateX(-100%); }
  to { transform: translateX(100%); }
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  width: 45px;
  text-align: right;
  flex-shrink: 0;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
