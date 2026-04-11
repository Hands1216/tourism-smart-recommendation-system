<template>
  <div class="strategy-list-page">
    <!-- 搜索和筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="搜索">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题、内容或作者"
            clearable
            style="width: 250px"
            @change="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="目的地">
          <el-input
            v-model="searchDestination"
            placeholder="搜索目的地"
            clearable
            style="width: 150px"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="searchTag"
            placeholder="选择标签"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option
              v-for="tag in popularTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预算范围">
          <el-select
            v-model="budgetRange"
            placeholder="不限"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option
              label="500元以下"
              value="0-500"
            />
            <el-option
              label="500-1000元"
              value="500-1000"
            />
            <el-option
              label="1000-3000元"
              value="1000-3000"
            />
            <el-option
              label="3000-5000元"
              value="3000-5000"
            />
            <el-option
              label="5000元以上"
              value="5000-999999"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="行程天数">
          <el-select
            v-model="daysRange"
            placeholder="不限"
            clearable
            style="width: 130px"
            @change="handleSearch"
          >
            <el-option
              label="1-3天"
              value="1-3"
            />
            <el-option
              label="4-7天"
              value="4-7"
            />
            <el-option
              label="8-15天"
              value="8-15"
            />
            <el-option
              label="15天以上"
              value="15-999"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="适合季节">
          <el-select
            v-model="searchSeason"
            placeholder="不限"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="春季"
              value="spring"
            />
            <el-option
              label="夏季"
              value="summer"
            />
            <el-option
              label="秋季"
              value="autumn"
            />
            <el-option
              label="冬季"
              value="winter"
            />
            <el-option
              label="四季皆宜"
              value="all"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select
            v-model="orderBy"
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option
              label="最新发布"
              value=""
            />
            <el-option
              label="最热门"
              value="hot"
            />
            <el-option
              label="最多收藏"
              value="favorite"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="Search"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 热门搜索 -->
    <div
      v-if="hotKeywords.length > 0"
      class="hot-keywords"
    >
      <span class="hot-label">热门搜索：</span>
      <el-tag
        v-for="kw in hotKeywords"
        :key="kw"
        class="hot-tag"
        effect="plain"
        round
        @click="handleHotKeyword(kw)"
      >
        {{ kw }}
      </el-tag>
    </div>

    <!-- 攻略网格 -->
    <div
      v-loading="loading"
      class="strategy-grid"
    >
      <el-card
        v-for="item in list"
        :key="item.id"
        class="strategy-card"
        shadow="hover"
        @click="goToDetail(item.id)"
      >
        <div class="strategy-cover">
          <el-image
            :src="item.coverImage || defaultImage"
            fit="cover"
          />
          <div
            v-if="item.isAiGenerated"
            class="ai-badge"
          >
            <el-tag
              type="success"
              size="small"
            >
              AI生成
            </el-tag>
          </div>
        </div>
        <div class="strategy-info">
          <h3>{{ item.title }}</h3>
          <p class="destination">
            <el-icon><Location /></el-icon> {{ item.destination }}
            <span
              v-if="item.days"
              class="days"
            >· {{ item.days }}天</span>
          </p>
          <div class="card-extra">
            <span
              v-if="item.budget"
              class="budget"
            >
              <el-icon><Wallet /></el-icon> ¥{{ item.budget }}/人
            </span>
            <span
              v-if="item.season"
              class="season"
            >
              <el-icon><Sunny /></el-icon> {{ seasonLabel(item.season) }}
            </span>
          </div>
          <div
            v-if="item.tags && item.tags.length > 0"
            class="tags"
          >
            <el-tag
              v-for="tag in parseTagsLimit(item.tags, 3)"
              :key="tag"
              size="small"
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>
          <p class="author">
            <el-avatar
              :src="item.authorAvatar"
              :size="20"
            >
              {{ item.authorName?.charAt(0) }}
            </el-avatar>
            {{ item.authorName }}
          </p>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
            <span><el-icon><Collection /></el-icon> {{ item.favoriteCount }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && list.length === 0"
      description="暂无攻略"
    />

    <!-- 分页 -->
    <div
      v-if="total > 0"
      class="pagination"
    >
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>

    <!-- 创建攻略按钮 -->
    <el-affix
      :offset="80"
      position="bottom"
    >
      <el-button
        type="primary"
        size="large"
        circle
        class="create-btn"
        @click="showCreateDialog = true"
      >
        <el-icon :size="24">
          <Plus />
        </el-icon>
      </el-button>
    </el-affix>

    <!-- 创建选择对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建攻略"
      width="400px"
      align-center
    >
      <div class="create-options">
        <el-button
          class="create-option-btn"
          @click="handleCreateNew"
        >
          <el-icon :size="32">
            <EditPen />
          </el-icon>
          <span>新建攻略</span>
        </el-button>
        <el-button
          class="create-option-btn"
          @click="handleOpenDrafts"
        >
          <el-icon :size="32">
            <FolderOpened />
          </el-icon>
          <span>草稿箱 ({{ draftCount }})</span>
        </el-button>
      </div>
    </el-dialog>

    <!-- 草稿箱对话框 -->
    <el-dialog
      v-model="showDraftsDialog"
      title="草稿箱"
      width="600px"
      align-center
    >
      <div v-loading="draftsLoading">
        <el-empty
          v-if="!draftsLoading && draftList.length === 0"
          description="暂无草稿"
        />
        <div
          v-else
          class="draft-list"
        >
          <div
            v-for="draft in draftList"
            :key="draft.id"
            class="draft-item"
          >
            <div class="draft-info">
              <h4>{{ draft.title || '无标题草稿' }}</h4>
              <p class="draft-meta">
                <span>{{ draft.destination || '未设置目的地' }}</span>
                <span>· {{ draft.createTime }}</span>
              </p>
            </div>
            <div class="draft-actions">
              <el-button
                type="primary"
                size="small"
                @click="editDraft(draft.id!)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDeleteDraft(draft.id!)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Location, View, Star, Collection, Plus, Wallet, Sunny, EditPen, FolderOpened } from '@element-plus/icons-vue'
import { getList, getHotKeywords, getDrafts, deleteStrategy } from '@/api/strategy'
import type { StrategyVO } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const defaultImage = 'https://via.placeholder.com/300x200?text=攻略'
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const searchKeyword = ref('')
const searchDestination = ref('')
const searchTag = ref('')
const orderBy = ref('')
const budgetRange = ref('')
const daysRange = ref('')
const searchSeason = ref('')
const hotKeywords = ref<string[]>([])
const showCreateDialog = ref(false)
const showDraftsDialog = ref(false)
const draftList = ref<StrategyVO[]>([])
const draftCount = ref(0)
const draftsLoading = ref(false)

const popularTags = ref([
  '美食', '自然风光', '历史文化', '亲子游', '情侣游',
  '摄影', '徒步', '古镇', '海岛', '购物'
])

const seasonMap: Record<string, string> = {
  spring: '春季',
  summer: '夏季',
  autumn: '秋季',
  winter: '冬季',
  all: '四季皆宜'
}

const seasonLabel = (season: string) => seasonMap[season] || season

const parseTagsLimit = (tags: any, limit: number = 3) => {
  if (!tags) return []
  let tagArray = []
  if (typeof tags === 'string') {
    try {
      tagArray = JSON.parse(tags)
    } catch (e) {
      return []
    }
  } else if (Array.isArray(tags)) {
    tagArray = tags
  }
  return tagArray.slice(0, limit)
}

const parseBudgetRange = () => {
  if (!budgetRange.value) return { minBudget: undefined, maxBudget: undefined }
  const [min, max] = budgetRange.value.split('-').map(Number)
  return { minBudget: min, maxBudget: max }
}

const parseDaysRange = () => {
  if (!daysRange.value) return { minDays: undefined, maxDays: undefined }
  const [min, max] = daysRange.value.split('-').map(Number)
  return { minDays: min, maxDays: max }
}

const loadData = async () => {
  try {
    loading.value = true
    const { minBudget, maxBudget } = parseBudgetRange()
    const { minDays, maxDays } = parseDaysRange()
    const res = await getList({
      destination: searchDestination.value,
      keyword: searchKeyword.value,
      tag: searchTag.value,
      orderBy: orderBy.value,
      minBudget,
      maxBudget,
      minDays,
      maxDays,
      season: searchSeason.value || undefined,
      page: page.value,
      size: size.value
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadHotKeywords = async () => {
  try {
    const data = await getHotKeywords()
    hotKeywords.value = data || []
  } catch (e) {
    // 静默失败
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleReset = () => {
  searchKeyword.value = ''
  searchDestination.value = ''
  searchTag.value = ''
  orderBy.value = ''
  budgetRange.value = ''
  daysRange.value = ''
  searchSeason.value = ''
  handleSearch()
}

const handleHotKeyword = (kw: string) => {
  searchKeyword.value = kw
  handleSearch()
}

const goToDetail = (id: number) => {
  router.push(`/strategy/${id}`)
}

const loadDraftCount = async () => {
  try {
    const res = await getDrafts({ page: 1, size: 1 })
    draftCount.value = res.total || 0
  } catch {
    draftCount.value = 0
  }
}

const handleCreateNew = () => {
  showCreateDialog.value = false
  router.push('/strategy/create')
}

const handleOpenDrafts = async () => {
  showCreateDialog.value = false
  showDraftsDialog.value = true
  draftsLoading.value = true
  try {
    const res = await getDrafts({ page: 1, size: 10 })
    draftList.value = res.records || []
  } catch (e: any) {
    ElMessage.error('加载草稿失败')
  } finally {
    draftsLoading.value = false
  }
}

const editDraft = (id: number) => {
  showDraftsDialog.value = false
  router.push(`/strategy/edit/${id}`)
}

const handleDeleteDraft = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该草稿？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStrategy(id)
    ElMessage.success('草稿已删除')
    // 刷新草稿列表
    const res = await getDrafts({ page: 1, size: 10 })
    draftList.value = res.records || []
    draftCount.value = res.total || 0
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadData()
  loadHotKeywords()
  loadDraftCount()
})
</script>

<style scoped>
.strategy-list-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
}

.hot-keywords {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-label {
  color: #999;
  font-size: 14px;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.hot-tag:hover {
  color: #409eff;
  border-color: #409eff;
}

.strategy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.strategy-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
}

.strategy-card:hover {
  transform: translateY(-5px);
}

.strategy-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.strategy-cover .el-image {
  width: 100%;
  height: 100%;
}

.ai-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.strategy-info {
  padding: 15px 0;
}

.strategy-info h3 {
  font-size: 16px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 600;
}

.destination {
  color: #666;
  font-size: 14px;
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.days {
  color: #999;
}

.card-extra {
  display: flex;
  gap: 15px;
  margin: 8px 0;
  font-size: 13px;
  color: #e6a23c;
}

.card-extra span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-extra .season {
  color: #67c23a;
}

.tags {
  margin: 10px 0;
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.author {
  color: #666;
  font-size: 14px;
  margin: 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats {
  display: flex;
  gap: 15px;
  margin-top: 10px;
  color: #999;
  font-size: 13px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.create-btn {
  position: fixed;
  bottom: 80px;
  right: 40px;
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.create-options {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px 0;
}

.create-option-btn {
  flex: 1;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
}

.draft-list {
  max-height: 400px;
  overflow-y: auto;
}

.draft-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.draft-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.draft-info {
  flex: 1;
}

.draft-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.draft-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.draft-actions {
  display: flex;
  gap: 8px;
}
</style>
