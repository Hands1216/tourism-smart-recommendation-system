<template>
  <div class="drafts-page">
    <!-- 返回按钮 -->
    <BackButton fallback="/strategy" class="page-back-btn" />

    <el-card>
      <template #header>
        <div class="page-header">
          <h2>我的草稿</h2>
          <el-button
            type="primary"
            @click="router.push('/strategy/create')"
          >
            <el-icon><Plus /></el-icon> 新建攻略
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="list"
        empty-text="暂无草稿"
        style="width: 100%"
      >
        <el-table-column
          label="标题"
          min-width="200"
        >
          <template #default="{ row }">
            <span
              class="draft-title"
              @click="editDraft(row.id)"
            >{{ row.title || '无标题草稿' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="目的地"
          prop="destination"
          width="120"
        />
        <el-table-column
          label="天数"
          prop="days"
          width="80"
          align="center"
        />
        <el-table-column
          label="预算"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <span v-if="row.budget">¥{{ row.budget }}</span>
            <span
              v-else
              class="text-muted"
            >-</span>
          </template>
        </el-table-column>
        <el-table-column
          label="更新时间"
          width="160"
          align="center"
        >
          <template #default="{ row }">
            {{ formatTime(row.updateTime || row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editDraft(row.id)"
            >
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除该草稿？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button
                  type="danger"
                  size="small"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getDrafts, deleteStrategy } from '@/api/strategy'
import { ElMessage } from 'element-plus'
import BackButton from '@/components/BackButton.vue'

const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getDrafts({ page: page.value, size: size.value })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const editDraft = (id: number) => {
  router.push(`/strategy/create?draftId=${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await deleteStrategy(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.drafts-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-back-btn {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.draft-title {
  color: #409eff;
  cursor: pointer;
  font-weight: 500;
}

.draft-title:hover {
  text-decoration: underline;
}

.text-muted {
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
