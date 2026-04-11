<template>
  <div class="analytics-page">
    <el-page-header
      title="返回"
      @back="goBack"
    >
      <template #content>
        <span class="page-title">景点数据分析</span>
      </template>
    </el-page-header>

    <!-- 时间范围选择 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="统计周期">
          <el-select
            v-model="days"
            @change="loadData"
          >
            <el-option
              label="最近7天"
              :value="7"
            />
            <el-option
              label="最近30天"
              :value="30"
            />
            <el-option
              label="最近90天"
              :value="90"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="显示数量">
          <el-select
            v-model="limit"
            @change="loadData"
          >
            <el-option
              label="前10名"
              :value="10"
            />
            <el-option
              label="前20名"
              :value="20"
            />
            <el-option
              label="前50名"
              :value="50"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 热门景点排行 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <h3>🔥 热门景点排行</h3>
          <span class="subtitle">基于浏览量、收藏数和评分的综合热度</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="hotAttractions"
        style="width: 100%"
      >
        <el-table-column
          type="index"
          label="排名"
          width="80"
        >
          <template #default="{ $index }">
            <el-tag
              v-if="$index === 0"
              type="danger"
              effect="dark"
            >
              🥇
            </el-tag>
            <el-tag
              v-else-if="$index === 1"
              type="warning"
              effect="dark"
            >
              🥈
            </el-tag>
            <el-tag
              v-else-if="$index === 2"
              type="success"
              effect="dark"
            >
              🥉
            </el-tag>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="景点名称"
          width="200"
        />
        <el-table-column
          prop="city"
          label="城市"
          width="120"
        />
        <el-table-column
          prop="scenicLevel"
          label="等级"
          width="120"
        />
        <el-table-column
          prop="viewCount"
          label="浏览量"
          width="120"
          sortable
        >
          <template #default="{ row }">
            <el-tag type="info">
              {{ row.viewCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="favoriteCount"
          label="收藏数"
          width="120"
          sortable
        >
          <template #default="{ row }">
            <el-tag type="warning">
              {{ row.favoriteCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="rating"
          label="评分"
          width="120"
          sortable
        >
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="hotScore"
          label="热度分数"
          width="120"
          sortable
        >
          <template #default="{ row }">
            <el-tag
              type="danger"
              effect="dark"
            >
              {{ row.hotScore?.toFixed(2) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 增长潜力景点 -->
    <el-card class="data-card">
      <template #header>
        <div class="card-header">
          <h3>📈 增长潜力景点</h3>
          <span class="subtitle">近期热度增长最快的景点</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="growthAttractions"
        style="width: 100%"
      >
        <el-table-column
          type="index"
          label="排名"
          width="80"
        />
        <el-table-column
          prop="name"
          label="景点名称"
          width="200"
        />
        <el-table-column
          prop="city"
          label="城市"
          width="120"
        />
        <el-table-column
          prop="viewCount"
          label="当前浏览量"
          width="120"
        >
          <template #default="{ row }">
            <el-tag type="info">
              {{ row.viewCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="favoriteCount"
          label="当前收藏数"
          width="120"
        >
          <template #default="{ row }">
            <el-tag type="warning">
              {{ row.favoriteCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="growthRate"
          label="增长率"
          width="150"
          sortable
        >
          <template #default="{ row }">
            <el-tag
              type="success"
              effect="dark"
            >
              +{{ (row.growthRate * 100).toFixed(1) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="趋势"
          width="120"
        >
          <template #default="{ row }">
            <el-icon
              :size="24"
              color="#67c23a"
            >
              <TrendCharts />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="120"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 数据概览 -->
    <el-row
      :gutter="20"
      class="stats-row"
    >
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card class="stat-card">
          <el-statistic
            title="总景点数"
            :value="totalCount"
          >
            <template #prefix>
              <el-icon color="#409eff">
                <Location />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card class="stat-card">
          <el-statistic
            title="总浏览量"
            :value="totalViews"
          >
            <template #prefix>
              <el-icon color="#67c23a">
                <View />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card class="stat-card">
          <el-statistic
            title="总收藏数"
            :value="totalFavorites"
          >
            <template #prefix>
              <el-icon color="#e6a23c">
                <Star />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col
        :xs="24"
        :sm="12"
        :md="6"
      >
        <el-card class="stat-card">
          <el-statistic
            title="平均评分"
            :value="avgRating"
            :precision="1"
          >
            <template #prefix>
              <el-icon color="#f56c6c">
                <StarFilled />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Location, View, Star, StarFilled, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminGetHotAttractions, adminGetGrowthAttractions } from '@/api/attraction'
import { getDashboardStats } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const days = ref(30)
const limit = ref(10)

const hotAttractions = ref<any[]>([])
const growthAttractions = ref<any[]>([])
const totalCount = ref(0)

const totalViews = computed(() => {
  return hotAttractions.value.reduce((sum, item) => sum + (item.viewCount || 0), 0)
})

const totalFavorites = computed(() => {
  return hotAttractions.value.reduce((sum, item) => sum + (item.favoriteCount || 0), 0)
})

const avgRating = computed(() => {
  if (hotAttractions.value.length === 0) return 0
  const sum = hotAttractions.value.reduce((sum, item) => sum + (item.rating || 0), 0)
  return sum / hotAttractions.value.length
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = { days: days.value, limit: limit.value }

    // 并行加载数据
    const [hot, growth, stats] = await Promise.all([
      adminGetHotAttractions(params),
      adminGetGrowthAttractions(params),
      getDashboardStats()
    ])

    hotAttractions.value = hot || []
    growthAttractions.value = growth || []
    totalCount.value = stats?.attractionCount ?? 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 查看详情
const viewDetail = (id: number) => {
  router.push(`/attraction/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.analytics-page {
  padding: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.filter-card {
  margin: 20px 0;
}

.data-card {
  margin-bottom: 20px;

  .card-header {
    h3 {
      margin: 0 0 5px 0;
      font-size: 18px;
    }

    .subtitle {
      color: #999;
      font-size: 13px;
    }
  }
}

.stats-row {
  margin-top: 20px;
}

.stat-card {
  margin-bottom: 20px;
  text-align: center;

  :deep(.el-statistic__head) {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
  }

  :deep(.el-statistic__content) {
    font-size: 28px;
    font-weight: bold;
  }
}
</style>
