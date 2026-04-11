<template>
  <div class="attraction-list-page">
    <!-- 高级筛选区域 -->
    <el-card
      class="filter-card"
      shadow="never"
    >
      <template #header>
        <div class="filter-header">
          <span>筛选条件</span>
          <el-button
            text
            @click="resetFilters"
          >
            重置
          </el-button>
        </div>
      </template>

      <el-form
        :model="filters"
        label-width="80px"
      >
        <el-row :gutter="20">
          <!-- 关键词搜索 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="关键词">
              <el-input
                v-model="filters.keyword"
                placeholder="搜索景点名称"
                clearable
                @change="handleSearch"
              />
            </el-form-item>
          </el-col>

          <!-- 景点类型 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="景点类型">
              <el-select
                v-model="filters.categoryId"
                placeholder="选择类型"
                clearable
                @change="handleSearch"
              >
                <el-option
                  label="自然风光"
                  :value="1"
                />
                <el-option
                  label="历史文化"
                  :value="2"
                />
                <el-option
                  label="主题乐园"
                  :value="3"
                />
                <el-option
                  label="城市观光"
                  :value="4"
                />
                <el-option
                  label="休闲度假"
                  :value="5"
                />
                <el-option
                  label="特色小镇"
                  :value="6"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 省份 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="省份">
              <el-select
                v-model="filters.province"
                placeholder="选择省份"
                clearable
                filterable
                @change="handleProvinceChange"
              >
                <el-option
                  v-for="province in sortedProvinces"
                  :key="province"
                  :label="province"
                  :value="province"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 地区（城市） -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="地区">
              <el-select
                v-model="filters.city"
                placeholder="请先选择省份"
                clearable
                filterable
                :disabled="!filters.province"
                @change="handleSearch"
              >
                <el-option
                  v-for="city in cityOptions"
                  :key="city"
                  :label="city"
                  :value="city"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 景区等级 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="景区等级">
              <el-select
                v-model="filters.scenicLevel"
                placeholder="选择等级"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="level in scenicLevelOptions"
                  :key="level"
                  :label="level"
                  :value="level"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 场景类型 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="场景类型">
              <el-select
                v-model="filters.sceneType"
                placeholder="选择场景"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="scene in sceneTypeOptions"
                  :key="scene"
                  :label="scene"
                  :value="scene"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 价格区间 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="价格区间">
              <div class="price-range">
                <div class="price-input-wrapper">
                  <span
                    class="price-arrow up"
                    @click="adjustPrice('min', 50)"
                  >▲</span>
                  <el-input
                    v-model.number="filters.minPrice"
                    placeholder="最低价"
                    type="number"
                    :min="0"
                    @change="handlePriceChange"
                  />
                  <span
                    class="price-arrow down"
                    @click="adjustPrice('min', -50)"
                  >▼</span>
                </div>
                <span class="price-separator">-</span>
                <div class="price-input-wrapper">
                  <span
                    class="price-arrow up"
                    @click="adjustPrice('max', 50)"
                  >▲</span>
                  <el-input
                    v-model.number="filters.maxPrice"
                    placeholder="最高价"
                    type="number"
                    :min="0"
                    @change="handlePriceChange"
                  />
                  <span
                    class="price-arrow down"
                    @click="adjustPrice('max', -50)"
                  >▼</span>
                </div>
              </div>
            </el-form-item>
          </el-col>

          <!-- 建议游玩时长 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="游玩时长">
              <el-select
                v-model="filters.suggestedDuration"
                placeholder="选择时长"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="duration in durationOptions"
                  :key="duration"
                  :label="duration"
                  :value="duration"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 最佳季节 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="最佳季节">
              <el-select
                v-model="filters.bestSeason"
                placeholder="选择季节"
                clearable
                @change="handleSearch"
              >
                <el-option
                  v-for="season in seasonOptions"
                  :key="season"
                  :label="season"
                  :value="season"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 排序方式 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label="排序方式">
              <el-select
                v-model="filters.sortBy"
                placeholder="选择排序"
                @change="handleSearch"
              >
                <el-option
                  label="热度优先"
                  value="hot"
                />
                <el-option
                  label="评分优先"
                  value="rating"
                />
                <el-option
                  label="价格从低到高"
                  value="price_asc"
                />
                <el-option
                  label="价格从高到低"
                  value="price_desc"
                />
                <el-option
                  label="距离最近"
                  value="distance"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 搜索按钮 -->
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
          >
            <el-form-item label=" ">
              <el-button
                type="primary"
                class="filter-search-btn"
                @click="handleSearch"
              >
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 结果统计 -->
    <div class="result-info">
      <span>共找到 <strong>{{ total }}</strong> 个景点</span>
    </div>

    <!-- 景点列表 -->
    <el-row
      v-loading="loading"
      :gutter="20"
    >
      <el-col
        v-for="item in list"
        :key="item.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <div
          class="attraction-card"
          @click="goToDetail(item.id)"
        >
          <div class="card-image">
            <el-image
              :src="item.images?.[0] || defaultImage"
              fit="cover"
            />
            <span class="card-rating">⭐ {{ item.ratingCount > 0 && item.rating ? item.rating.toFixed(1) : '0.0' }}</span>
            <div class="scenic-levels">
              <el-tag
                v-for="level in (item.scenicLevel || '').split(',').filter((l: string) => l.trim())"
                :key="level"
                type="warning"
                size="small"
              >
                {{ level.trim() }}
              </el-tag>
            </div>
            <el-button
              v-if="item.isFavorited"
              class="favorite-btn active"
              :icon="Star"
              circle
              @click.stop="toggleFavorite(item)"
            />
            <el-button
              v-else
              class="favorite-btn"
              :icon="StarFilled"
              circle
              @click.stop="toggleFavorite(item)"
            />
          </div>
          <div class="card-content">
            <h3>{{ item.name }}</h3>
            <p class="location">
              📍 {{ item.province }} · {{ item.city }}
            </p>
            <div class="tags">
              <el-tag
                v-for="tag in item.tags?.slice(0, 2)"
                :key="tag"
                size="small"
                type="info"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="card-footer">
              <span
                v-if="item.ticketPrice > 0"
                class="price"
              >¥{{ item.ticketPrice }}</span>
              <span
                v-else
                class="price free"
              >免费</span>
              <span class="view-count">👁 {{ item.viewCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无符合条件的景点"
    />

    <!-- 分页 -->
    <div
      v-if="total > 0"
      class="pagination"
    >
      <el-pagination
        v-model:current-page="filters.page"
        v-model:page-size="filters.size"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadData"
        @size-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Star, StarFilled, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getAttractionListAdvanced,
  getFilterOptions,
  favoriteAttraction,
  unfavoriteAttraction,
  getCitiesByProvince,
  type FilterOptions
} from '@/api/attraction'

const router = useRouter()

const defaultImage = 'https://via.placeholder.com/300x200?text=景点'

const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

// 国务院规定的34个省级行政区标准排序
const provinceOrder = [
  '北京市', '天津市', '河北省', '山西省', '内蒙古自治区',
  '辽宁省', '吉林省', '黑龙江省',
  '上海市', '江苏省', '浙江省', '安徽省', '福建省', '江西省', '山东省',
  '河南省', '湖北省', '湖南省', '广东省', '广西壮族自治区', '海南省',
  '重庆市', '四川省', '贵州省', '云南省', '西藏自治区',
  '陕西省', '甘肃省', '青海省', '宁夏回族自治区', '新疆维吾尔自治区',
  '香港特别行政区', '澳门特别行政区', '台湾省'
]

// 硬编码的筛选选项
const scenicLevelOptions = [
  'A级景区', '2A级景区', '3A级景区', '4A级景区', '5A级景区',
  '世界文化遗产', '世界自然遗产'
]

const sceneTypeOptions = ['独自出行', '朋友出行', '情侣出行', '家庭出行']

const durationOptions = ['≤30分钟', '30分钟-1小时', '1-2小时', '2-3小时', '3-5小时']

const seasonOptions = ['春季', '夏季', '秋季', '冬季']

const filters = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  province: '',
  city: '',
  district: '',
  scenicLevel: '',
  sceneType: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  suggestedDuration: '',
  bestSeason: '',
  sortBy: 'hot',
  sortOrder: 'desc',
  userLat: undefined as number | undefined,
  userLng: undefined as number | undefined,
  page: 1,
  size: 12
})

const filterOptions = ref<FilterOptions>({
  provinces: [],
  scenicLevels: [],
  sceneTypes: [],
  durations: [],
  provinceCityMap: {},
  cityDistrictMap: {}
})

// 城市选项（根据省份动态加载）
const cityOptions = ref<string[]>([])

// 按国务院标准排序的省份列表
const sortedProvinces = computed(() => {
  const provinces = filterOptions.value.provinces
  if (!provinces || provinces.length === 0) return []
  return [...provinces].sort((a, b) => {
    const indexA = provinceOrder.indexOf(a)
    const indexB = provinceOrder.indexOf(b)
    // 不在标准列表中的排到最后
    const orderA = indexA === -1 ? 999 : indexA
    const orderB = indexB === -1 ? 999 : indexB
    return orderA - orderB
  })
})

// 加载筛选选项
const loadFilterOptions = async () => {
  try {
    const options = await getFilterOptions()
    filterOptions.value = options
  } catch (error) {
    console.error('加载筛选选项失败', error)
  }
}

// 加载景点列表
const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...filters }

    // 修复问题8：价格排序参数转换
    if (params.sortBy === 'price_asc') {
      params.sortBy = 'price'
      params.sortOrder = 'asc'
    } else if (params.sortBy === 'price_desc') {
      params.sortBy = 'price'
      params.sortOrder = 'desc'
    }

    // 将bestSeason传给后端
    if (params.bestSeason) {
      params.bestSeason = params.bestSeason
    }

    const res = await getAttractionListAdvanced(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载失败', error)
    ElMessage.error('加载景点列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  filters.page = 1
  loadData()
}

// 处理省份变化 — 联动加载城市列表
const handleProvinceChange = async () => {
  // 清空城市选择
  filters.city = ''
  cityOptions.value = []

  if (filters.province) {
    try {
      const cities = await getCitiesByProvince(filters.province)
      cityOptions.value = cities || []
    } catch (error) {
      console.error('加载城市列表失败', error)
      cityOptions.value = []
    }
  }

  handleSearch()
}

// 价格调整（步进50）
const adjustPrice = (type: 'min' | 'max', delta: number) => {
  if (type === 'min') {
    const current = filters.minPrice ?? 0
    const newVal = Math.max(0, current + delta)
    // 最低价不能大于最高价
    if (filters.maxPrice !== undefined && newVal > filters.maxPrice) return
    filters.minPrice = newVal
  } else {
    const current = filters.maxPrice ?? 0
    const newVal = Math.max(0, current + delta)
    // 最高价不能小于最低价
    if (filters.minPrice !== undefined && newVal < filters.minPrice) return
    filters.maxPrice = newVal
  }
  handleSearch()
}

// 价格输入变化校验
const handlePriceChange = () => {
  // 确保最低价不大于最高价
  if (
    filters.minPrice !== undefined &&
    filters.maxPrice !== undefined &&
    filters.minPrice > filters.maxPrice
  ) {
    ElMessage.warning('最低价格不能大于最高价格')
    // 自动修正：将最低价设为最高价
    filters.minPrice = filters.maxPrice
  }
  handleSearch()
}

// 重置筛选条件
const resetFilters = () => {
  filters.keyword = ''
  filters.categoryId = undefined
  filters.province = ''
  filters.city = ''
  filters.district = ''
  filters.scenicLevel = ''
  filters.sceneType = ''
  filters.minPrice = undefined
  filters.maxPrice = undefined
  filters.suggestedDuration = ''
  filters.bestSeason = ''
  filters.sortBy = 'hot'
  filters.page = 1
  cityOptions.value = []
  loadData()
}

// 跳转详情
const goToDetail = (id: number) => {
  router.push(`/attraction/${id}`)
}

// 切换收藏
const toggleFavorite = async (item: any) => {
  try {
    if (item.isFavorited) {
      await unfavoriteAttraction(item.id)
      item.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteAttraction(item.id)
      item.isFavorited = true
      ElMessage.success('已收藏')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 获取用户位置（用于距离排序）
const getUserLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        filters.userLat = position.coords.latitude
        filters.userLng = position.coords.longitude
      },
      (error) => {
        console.log('获取位置失败', error)
      }
    )
  }
}

onMounted(() => {
  const query = router.currentRoute.value.query
  if (query.keyword) filters.keyword = query.keyword as string
  if (query.city) filters.city = query.city as string
  if (query.province) filters.province = query.province as string

  loadFilterOptions()
  getUserLocation()
  loadData()

  // 如果URL带了省份参数，联动加载城市
  if (filters.province) {
    getCitiesByProvince(filters.province).then(cities => {
      cityOptions.value = cities || []
    }).catch(() => {})
  }
})
</script>

<style scoped lang="scss">
.attraction-list-page {
  padding: 20px 0;
}

.filter-card {
  margin-bottom: 20px;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.filter-search-btn {
  width: 120px;
  height: 40px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }
}

.price-range {
  display: flex;
  align-items: center;
  width: 100%;
}

.price-input-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;

  .price-arrow {
    cursor: pointer;
    user-select: none;
    font-size: 10px;
    color: #909399;
    line-height: 1;
    padding: 2px 0;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }

    &.up {
      margin-bottom: 2px;
    }

    &.down {
      margin-top: 2px;
    }
  }

  :deep(.el-input) {
    width: 100%;
  }

  :deep(.el-input__inner) {
    text-align: center;

    // 隐藏 number 类型输入框的原生 spinner 按钮
    // Chrome, Safari, Edge, Opera
    &::-webkit-outer-spin-button,
    &::-webkit-inner-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }

    // Firefox
    -moz-appearance: textfield;
  }
}

.price-separator {
  margin: 0 8px;
  color: #999;
  flex-shrink: 0;
}

.result-info {
  margin-bottom: 20px;
  color: #666;

  strong {
    color: #409eff;
    font-size: 18px;
  }
}

.attraction-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
}

.card-image {
  position: relative;
  height: 180px;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.card-rating {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 193, 7, 0.95);
  color: #333;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.scenic-levels {
  position: absolute;
  bottom: 10px;
  left: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  max-width: calc(100% - 20px);
}

.favorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);

  &.active {
    color: #f56c6c;
  }
}

.card-content {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;

  h3 {
    font-size: 16px;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .location {
    color: #666;
    font-size: 13px;
    margin-bottom: 8px;
  }

  .tags {
    display: flex;
    gap: 5px;
    margin-bottom: 10px;
    flex-wrap: wrap;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;

    .price {
      color: #f56c6c;
      font-weight: bold;
      font-size: 16px;

      &.free {
        color: #67c23a;
      }
    }

    .view-count {
      color: #999;
      font-size: 12px;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
