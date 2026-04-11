<template>
  <div class="my-strategy-page">
    <!-- 返回按钮 -->
    <BackButton fallback="/strategy" class="page-back-btn" />

    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的攻略</span>
          <el-button
            type="primary"
            :icon="Plus"
            @click="router.push('/strategy/create')"
          >
            创建攻略
          </el-button>
        </div>
      </template>

      <!-- 状态筛选 -->
      <el-tabs
        v-model="activeTab"
        @tab-change="handleTabChange"
      >
        <el-tab-pane
          label="全部"
          name="all"
        />
        <el-tab-pane
          label="草稿"
          name="draft"
        />
        <el-tab-pane
          label="审核中"
          name="pending"
        />
        <el-tab-pane
          label="已发布"
          name="published"
        />
        <el-tab-pane
          label="已驳回"
          name="rejected"
        />
      </el-tabs>

      <!-- 攻略列表 -->
      <div v-loading="loading">
        <el-empty
          v-if="!loading && list.length === 0"
          description="暂无攻略"
        />
        <div
          v-else
          class="strategy-list"
        >
          <div
            v-for="item in list"
            :key="item.id"
            class="strategy-item"
          >
            <div
              class="strategy-cover"
              @click="handleView(item)"
            >
              <el-image
                :src="item.coverImage || defaultImage"
                fit="cover"
              />
            </div>
            <div class="strategy-content">
              <div class="strategy-header">
                <h3 @click="handleView(item)">
                  {{ item.title || '无标题' }}
                </h3>
                <el-tag :type="getStatusType(item)">
                  {{ getStatusText(item) }}
                </el-tag>
              </div>
              <p class="destination">
                <el-icon><Location /></el-icon>
                {{ item.destination || '未设置目的地' }}
                <span
                  v-if="item.days"
                  class="days"
                >· {{ item.days }}天</span>
                <span
                  v-if="item.budget"
                  class="budget"
                >· ¥{{ item.budget }}/人</span>
              </p>
              <p
                v-if="item.auditStatus === 2 && item.auditReason"
                class="reject-reason"
              >
                驳回原因：{{ item.auditReason }}
              </p>
              <div class="strategy-footer">
                <div class="stats">
                  <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
                  <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
                  <span><el-icon><ChatDotRound /></el-icon> {{ item.commentCount || 0 }}</span>
                </div>
                <div class="actions">
                  <el-button
                    v-if="item.status === 0"
                    type="primary"
                    size="small"
                    @click="handleEdit(item.id!)"
                  >
                    编辑草稿
                  </el-button>
                  <el-button
                    v-else-if="item.auditStatus === 2"
                    type="primary"
                    size="small"
                    @click="handleEdit(item.id!)"
                  >
                    重新编辑
                  </el-button>
                  <el-button
                    v-else
                    type="default"
                    size="small"
                    @click="handleEdit(item.id!)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleDelete(item.id!)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

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
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Location, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { getMyList, deleteStrategy } from '@/api/strategy'
import type { StrategyVO } from '@/api/strategy'
import { ElMessage, ElMessageBox } from 'element-plus'
import BackButton from '@/components/BackButton.vue'

const router = useRouter()

const defaultImage = 'https://via.placeholder.com/300x200?text=攻略'
const loading = ref(false)
const list = ref<StrategyVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeTab = ref('all')

const getStatusType = (item: StrategyVO) => {
  if (item.auditStatus === 0) return 'warning' // 审核中
  if (item.auditStatus === 1) return item.status === 1 ? 'success' : 'info' // 已发布/已下架
  if (item.auditStatus === 2) return 'danger' // 已驳回
  if (item.status === 0) return 'info' // 草稿
  return 'info'
}

const getStatusText = (item: StrategyVO) => {
  if (item.auditStatus === 0) return '审核中'
  if (item.auditStatus === 1 && item.status === 1) return '已发布'
  if (item.auditStatus === 1 && item.status === 0) return '已下架'
  if (item.auditStatus === 2) return '已驳回'
  if (item.status === 0) return '草稿'
  return '未知'
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getMyList({
      page: page.value,
      size: size.value
    })

    // 根据 tab 筛选
    let filteredList = res.records || []
    if (activeTab.value === 'draft') {
      filteredList = filteredList.filter(item => item.status === 0 && item.auditStatus !== 0)
    } else if (activeTab.value === 'pending') {
      filteredList = filteredList.filter(item => item.auditStatus === 0)
    } else if (activeTab.value === 'published') {
      filteredList = filteredList.filter(item => item.status === 1 && item.auditStatus === 1)
    } else if (activeTab.value === 'rejected') {
      filteredList = filteredList.filter(item => item.auditStatus === 2)
    }

    list.value = filteredList
    total.value = filteredList.length
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  loadData()
}

const handleView = (item: StrategyVO) => {
  if (item.status === 0) {
    ElMessage.warning('草稿无法查看，请先发布')
    return
  }
  router.push(`/strategy/${item.id}`)
}

const handleEdit = (id: number) => {
  router.push(`/strategy/edit/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该攻略？删除后无法恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteStrategy(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-strategy-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-back-btn {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strategy-list {
  margin-top: 20px;
}

.strategy-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.strategy-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.strategy-cover {
  width: 200px;
  height: 150px;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
}

.strategy-cover .el-image {
  width: 100%;
  height: 100%;
}

.strategy-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.strategy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.strategy-header h3 {
  margin: 0;
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;
}

.strategy-header h3:hover {
  color: #409eff;
}

.destination {
  color: #666;
  font-size: 14px;
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 5px;
}

.days, .budget {
  color: #999;
}

.reject-reason {
  color: #f56c6c;
  font-size: 13px;
  margin: 8px 0;
  padding: 8px;
  background-color: #fef0f0;
  border-radius: 4px;
}

.strategy-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 10px;
}

.stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 13px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
