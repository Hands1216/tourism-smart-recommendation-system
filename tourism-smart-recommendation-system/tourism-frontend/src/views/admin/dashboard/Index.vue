<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col
        v-for="stat in stats"
        :key="stat.title"
        :span="6"
      >
        <el-card
          class="stat-card"
          shadow="hover"
        >
          <div class="stat-content">
            <div
              class="stat-icon"
              :style="{ background: stat.color }"
            >
              <el-icon :size="32">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stat.value }}
              </div>
              <div class="stat-title">
                {{ stat.title }}
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
      <!-- 热门景点 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门景点（按浏览量）</span>
              <el-button
                type="text"
                @click="$router.push('/admin/attraction')"
              >
                查看更多
              </el-button>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="hotAttractions"
            max-height="400"
          >
            <el-table-column
              prop="name"
              label="景点名称"
              show-overflow-tooltip
            />
            <el-table-column
              prop="city"
              label="城市"
              width="100"
            />
            <el-table-column
              prop="viewCount"
              label="浏览量"
              width="100"
              sortable
            />
          </el-table>
        </el-card>
      </el-col>

      <!-- 热门攻略 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门攻略（按浏览量）</span>
              <el-button
                type="text"
                @click="$router.push('/admin/content')"
              >
                查看更多
              </el-button>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="hotStrategies"
            max-height="400"
          >
            <el-table-column
              prop="title"
              label="标题"
              show-overflow-tooltip
            />
            <el-table-column
              prop="authorName"
              label="作者"
              width="100"
            />
            <el-table-column
              prop="viewCount"
              label="浏览量"
              width="100"
              sortable
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
import { ElMessage } from 'element-plus'
import { User, Document, TrendCharts } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/admin'
import axios from '@/api'

const router = useRouter()
const loading = ref(false)

const stats = ref([
  { title: '用户总数', value: 0, icon: User, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '景点总数', value: 0, icon: TrendCharts, color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '攻略总数', value: 0, icon: Document, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
])

const hotAttractions = ref<any[]>([])
const hotStrategies = ref<any[]>([])

const loadStats = async () => {
  try {
    const data = await getDashboardStats()
    stats.value[0].value = data.userCount
    stats.value[1].value = data.attractionCount
    stats.value[2].value = data.strategyCount
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  }
}

const loadHotAttractions = async () => {
  try {
    loading.value = true
    const data = await axios.get('/admin/dashboard/hot-attractions', { params: { limit: 15 } })
    hotAttractions.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载热门景点失败')
  } finally {
    loading.value = false
  }
}

const loadHotStrategies = async () => {
  try {
    loading.value = true
    const data = await axios.get('/admin/dashboard/hot-strategies', { params: { limit: 15 } })
    hotStrategies.value = data
  } catch (error: any) {
    ElMessage.error(error.message || '加载热门攻略失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadHotAttractions()
  loadHotStrategies()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
