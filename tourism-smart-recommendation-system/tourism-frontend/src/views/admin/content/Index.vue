<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>攻略管理</h3>
        </div>
      </template>

      <el-form
        :inline="true"
        :model="searchForm"
        class="search-form"
      >
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入标题"
            clearable
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.auditStatus"
            placeholder="请选择"
            clearable
          >
            <el-option
              label="待审核"
              :value="0"
            />
            <el-option
              label="已通过"
              :value="1"
            />
            <el-option
              label="已驳回"
              :value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目的地">
          <el-input
            v-model="searchForm.destination"
            placeholder="请输入目的地"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="list"
        style="width: 100%"
      >
        <el-table-column
          label="攻略ID"
          width="100"
        >
          <template #default="{ $index }">
            {{ getDisplayId($index) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="title"
          label="标题"
          min-width="200"
        />
        <el-table-column
          prop="destination"
          label="目的地"
          width="120"
        />
        <el-table-column
          prop="authorName"
          label="作者"
          width="120"
        />
        <el-table-column
          prop="viewCount"
          label="浏览量"
          width="100"
        />
        <el-table-column
          label="审核状态"
          width="120"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.auditStatus === 0"
              type="warning"
            >
              待审核
            </el-tag>
            <el-tag
              v-else-if="row.auditStatus === 1"
              type="success"
            >
              已通过
            </el-tag>
            <el-tag
              v-else-if="row.auditStatus === 2"
              type="danger"
            >
              已驳回
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) || row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="280"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="success"
              size="small"
              @click="handleAudit(row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.auditStatus === 1"
              type="warning"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button
              v-if="row.auditStatus === 2"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="攻略详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentDetail" class="strategy-detail">
        <div class="detail-item">
          <span class="label">标题：</span>
          <span class="value">{{ currentDetail.title }}</span>
        </div>
        <div class="detail-item">
          <span class="label">目的地：</span>
          <span class="value">{{ currentDetail.destination }}</span>
        </div>
        <div class="detail-item">
          <span class="label">作者：</span>
          <span class="value">{{ currentDetail.authorName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">浏览量：</span>
          <span class="value">{{ currentDetail.viewCount }}</span>
        </div>
        <div class="detail-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDateTime(currentDetail.createTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">审核状态：</span>
          <el-tag v-if="currentDetail.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentDetail.auditStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="currentDetail.auditStatus === 2" type="danger">已驳回</el-tag>
        </div>
        <div class="detail-item full">
          <span class="label">内容：</span>
          <div class="content" v-html="currentDetail.content"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回攻略"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectRules"
      >
        <el-form-item label="驳回原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="rejectLoading"
          @click="handleConfirmReject"
        >
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminStrategyList,
  auditStrategy,
  updateStrategyStatus,
  deleteAdminStrategy
} from '@/api/admin'
import { getDetail } from '@/api/strategy'
import { formatDateTime } from '@/utils/format'
import { recordSuccess, recordFailure, OperationType, OperationModule } from '@/utils/operateLog'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = ref({
  title: '',
  auditStatus: undefined,
  destination: ''
})

const detailDialogVisible = ref(false)
const currentDetail = ref<any>(null)

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref()
const rejectingRow = ref<any>(null)
const rejectForm = ref({ reason: '' })
const rejectRules = {
  reason: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 2, message: '驳回原因至少2个字', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAdminStrategyList({
      title: searchForm.value.title,
      auditStatus: searchForm.value.auditStatus,
      destination: searchForm.value.destination,
      page: currentPage.value,
      size: pageSize.value
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const getDisplayId = (index: number) => {
  return total.value - (currentPage.value - 1) * pageSize.value - index
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    title: '',
    auditStatus: undefined,
    destination: ''
  }
  currentPage.value = 1
  loadData()
}

const handleViewDetail = async (row: any) => {
  try {
    const detail = await getDetail(row.id)
    currentDetail.value = { ...row, ...detail }
  } catch {
    currentDetail.value = row
  }
  detailDialogVisible.value = true
}

const handleAudit = async (row: any, status: number) => {
  const startTime = Date.now()
  try {
    await auditStrategy(row.id, {
      auditStatus: status,
      auditReason: status === 2 ? '审核不通过' : ''
    })
    ElMessage.success('审核成功')
    loadData()

    // 记录操作日志
    const executeTime = Date.now() - startTime
    const action = status === 1 ? '审核通过' : '审核不通过'
    await recordSuccess(
      OperationType.AUDIT,
      OperationModule.STRATEGY,
      `${action}攻略：${row.title}`,
      executeTime
    )
  } catch (error: any) {
    ElMessage.error(error.message || '审核失败')

    // 记录失败日志
    const executeTime = Date.now() - startTime
    await recordFailure(
      OperationType.AUDIT,
      OperationModule.STRATEGY,
      `审核攻略失败：${row.title}`,
      executeTime
    )
  }
}

const handleToggleStatus = async (row: any) => {
  const startTime = Date.now()
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateStrategyStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '上架成功' : '下架成功')
    loadData()

    // 记录操作日志
    const executeTime = Date.now() - startTime
    await recordSuccess(
      OperationType.UPDATE,
      OperationModule.STRATEGY,
      `${newStatus === 1 ? '上架' : '下架'}攻略：${row.title}`,
      executeTime
    )
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')

    // 记录失败日志
    const executeTime = Date.now() - startTime
    await recordFailure(
      OperationType.UPDATE,
      OperationModule.STRATEGY,
      `修改攻略状态失败：${row.title}`,
      executeTime
    )
  }
}

const handleDelete = async (row: any) => {
  const startTime = Date.now()
  try {
    await ElMessageBox.confirm('确定删除该攻略？删除后无法恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteAdminStrategy(row.id)
    ElMessage.success('删除成功')
    loadData()

    const executeTime = Date.now() - startTime
    await recordSuccess(
      OperationType.DELETE,
      OperationModule.STRATEGY,
      `删除攻略：${row.title}`,
      executeTime
    )
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')

      const executeTime = Date.now() - startTime
      await recordFailure(
        OperationType.DELETE,
        OperationModule.STRATEGY,
        `删除攻略失败：${row.title}`,
        executeTime
      )
    }
  }
}

const handleReject = (row: any) => {
  rejectingRow.value = row
  rejectForm.value = { reason: '' }
  rejectDialogVisible.value = true
}

const handleConfirmReject = async () => {
  await rejectFormRef.value.validate()

  const row = rejectingRow.value
  const startTime = Date.now()
  rejectLoading.value = true
  try {
    await auditStrategy(row.id, {
      auditStatus: 2,
      auditReason: rejectForm.value.reason
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadData()

    // 记录操作日志
    const executeTime = Date.now() - startTime
    await recordSuccess(
      OperationType.AUDIT,
      OperationModule.STRATEGY,
      `驳回攻略：${row.title}，原因：${rejectForm.value.reason}`,
      executeTime
    )
  } catch (error: any) {
    ElMessage.error(error.message || '驳回失败')

    // 记录失败日志
    const executeTime = Date.now() - startTime
    await recordFailure(
      OperationType.AUDIT,
      OperationModule.STRATEGY,
      `驳回攻略失败：${row.title}`,
      executeTime
    )
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.admin-page {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.strategy-detail {
  .detail-item {
    display: flex;
    margin-bottom: 15px;

    .label {
      font-weight: bold;
      width: 80px;
      color: #606266;
      flex-shrink: 0;
    }

    .value {
      color: #303133;
      flex: 1;
    }

    &.full {
      flex-direction: column;

      .label {
        margin-bottom: 8px;
      }
    }
  }

  .content {
    color: #303133;
    line-height: 1.6;
    max-height: 400px;
    overflow-y: auto;
    padding: 10px;
    background: #f5f5f5;
    border-radius: 4px;
  }
}
</style>
