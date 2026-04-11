<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>操作日志</h3>
        </div>
      </template>

      <el-form
        :inline="true"
        :model="searchForm"
        class="search-form"
      >
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入操作人"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select
            v-model="searchForm.operationType"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option
              label="登录"
              value="login"
            />
            <el-option
              label="新增"
              value="create"
            />
            <el-option
              label="修改"
              value="update"
            />
            <el-option
              label="删除"
              value="delete"
            />
            <el-option
              label="审核"
              value="audit"
            />
            <el-option
              label="查询"
              value="query"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            style="width: 280px"
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
          prop="id"
          label="ID"
          width="80"
        />
        <el-table-column
          prop="username"
          label="用户"
          width="120"
        />
        <el-table-column
          prop="userRole"
          label="角色"
          width="120"
        >
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.userRole)" size="small">
              {{ getRoleName(row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="operationType"
          label="操作类型"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="getActionType(row.operationType)" size="small">
              {{ getActionLabel(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="module"
          label="操作模块"
          width="120"
        />
        <el-table-column
          prop="description"
          label="操作描述"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createTime"
          label="操作时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/api'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const searchForm = ref({
  username: '',
  operationType: ''
})
const dateRange = ref<string[]>([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])

// 角色类型映射
const roleTypeMap: Record<string, string> = {
  admin: 'danger',
  content_admin: 'warning'
}

const getRoleType = (role: string) => {
  return roleTypeMap[role] || 'info'
}

const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    admin: '系统管理员',
    content_admin: '内容管理员',
    user: '普通游客'
  }
  return roleMap[role] || '未知'
}

// 操作类型映射
const actionTypeMap: Record<string, string> = {
  login: 'success',
  create: 'primary',
  update: 'warning',
  delete: 'danger',
  audit: 'info',
  query: ''
}

const getActionType = (action: string) => {
  return actionTypeMap[action] || ''
}

const getActionLabel = (action: string) => {
  const map: Record<string, string> = {
    login: '登录',
    create: '新增',
    update: '修改',
    delete: '删除',
    audit: '审核',
    query: '查询'
  }
  return map[action] || action
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (searchForm.value.username) {
      params.username = searchForm.value.username
    }
    if (searchForm.value.operationType) {
      params.operationType = searchForm.value.operationType
    }
    if (dateRange.value && dateRange.value.length === 2) {
      const [start, end] = dateRange.value
      params.startTime = start + ' 00:00:00'
      params.endTime = end + ' 23:59:59'
    }

    const res = await axios.get('/admin/logs', { params })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error: any) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    operationType: ''
  }
  dateRange.value = []
  currentPage.value = 1
  loadData()
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
</style>
