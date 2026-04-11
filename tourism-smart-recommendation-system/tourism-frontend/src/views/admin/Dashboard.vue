<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="background: #409eff"
            >
              👥
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.userCount }}
              </div>
              <div class="stat-label">
                用户总数
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="background: #67c23a"
            >
              🏛️
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.attractionCount }}
              </div>
              <div class="stat-label">
                景点总数
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div
              class="stat-icon"
              style="background: #e6a23c"
            >
              📝
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.strategyCount }}
              </div>
              <div class="stat-label">
                攻略总数
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row
      :gutter="20"
      style="margin-top: 20px"
    >
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>热门景点</h3>
          </template>
          <el-table
            :data="popularAttractions"
            style="width: 100%"
          >
            <el-table-column
              prop="name"
              label="景点名称"
            />
            <el-table-column
              prop="city"
              label="城市"
            />
            <el-table-column
              prop="viewCount"
              label="浏览量"
            />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>热门攻略</h3>
          </template>
          <el-table
            :data="hotStrategies"
            style="width: 100%"
          >
            <el-table-column
              prop="title"
              label="标题"
            />
            <el-table-column
              prop="destination"
              label="目的地"
            />
            <el-table-column
              prop="authorName"
              label="作者"
            />
            <el-table-column
              prop="viewCount"
              label="浏览量"
            />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/api/index'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const stats = ref({
  userCount: 0,
  attractionCount: 0,
  strategyCount: 0
})

const popularAttractions = ref<any[]>([])
const hotStrategies = ref<any[]>([])

// 加载仪表盘统计数据
const loadDashboardStats = async () => {
  try {
    const res = await get('/admin/dashboard/stats')
    if (res) {
      stats.value = res
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载热门景点（按浏览量排序前15）
const loadHotAttractions = async () => {
  try {
    const res = await get('/admin/dashboard/hot-attractions', { limit: 15 })
    if (res) {
      popularAttractions.value = res
    }
  } catch (error) {
    console.error('加载热门景点失败', error)
  }
}

// 加载热门攻略（按浏览量排序前15）
const loadHotStrategies = async () => {
  try {
    const res = await get('/admin/dashboard/hot-strategies', { limit: 15 })
    if (res) {
      hotStrategies.value = res
    }
  } catch (error) {
    console.error('加载热门攻略失败', error)
  }
}

const loadDashboard = async () => {
  await Promise.all([
    loadDashboardStats(),
    loadHotAttractions(),
    loadHotStrategies()
  ])
}

// 点击景点行跳转详情
const handleAttractionRowClick = (row: any) => {
  router.push({ name: 'AttractionDetail', params: { id: row.id } })
}

// 点击攻略行跳转详情
const handleStrategyRowClick = (row: any) => {
  router.push({ name: 'StrategyDetail', params: { id: row.id } })
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  gap: 15px;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #999;
  font-size: 14px;
}

// 表格行可点击
:deep(.el-table__body tr) {
  cursor: pointer;
}

:deep(.el-table__body tr:hover) {
  background-color: #f5f5f5;
}
</style>
