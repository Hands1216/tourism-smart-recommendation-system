<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>用户管理</h3>
        </div>
      </template>

      <el-form
        :inline="true"
        :model="searchForm"
        class="search-form"
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchKeyword"
            placeholder="手机号或昵称"
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
          label="用户ID"
          width="80"
        >
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column
          label="头像"
          width="80"
        >
          <template #default="{ row }">
            <el-avatar :src="getAvatarUrl(row.avatar)">
              {{ row.nickname?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          prop="phone"
          label="手机号"
          width="130"
        />
        <el-table-column
          prop="nickname"
          label="昵称"
          width="120"
        />
        <el-table-column
          label="角色"
          width="120"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.role === 'admin'"
              type="danger"
            >
              系统管理员
            </el-tag>
            <el-tag
              v-else-if="row.role === 'content_admin'"
              type="warning"
            >
              内容管理员
            </el-tag>
            <el-tag
              v-else
              type="info"
            >
              普通用户
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="注册时间"
          width="180"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) || row.createTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="250"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEditRole(row)"
            >
              修改角色
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 修改角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="修改用户角色"
      width="400px"
    >
      <el-form
        :model="roleForm"
        label-width="80px"
      >
        <el-form-item label="用户">
          <el-input
            :value="roleForm.nickname"
            disabled
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="roleForm.role"
            style="width: 100%"
          >
            <el-option
              label="普通用户"
              value="user"
            />
            <el-option
              label="内容管理员"
              value="content_admin"
            />
            <el-option
              label="系统管理员"
              value="admin"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="confirmEditRole"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, updateUserRole } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { recordSuccess, OperationType, OperationModule } from '@/utils/operateLog'

const getAvatarUrl = (avatar: string | null | undefined) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return '/api' + avatar
}

const loading = ref(false)
const submitting = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const roleDialogVisible = ref(false)
const roleForm = ref({
  id: 0,
  nickname: '',
  role: 'user',
  oldRole: 'user'
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchKeyword.value,
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

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadData()
}

const handleEditRole = (row: any) => {
  roleForm.value = {
    id: row.id,
    nickname: row.nickname,
    role: row.role,
    oldRole: row.role
  }
  roleDialogVisible.value = true
}

const confirmEditRole = async () => {
  const startTime = Date.now()
  try {
    submitting.value = true
    await updateUserRole(roleForm.value.id, roleForm.value.role)
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    loadData()

    // 记录操作日志
    const executeTime = Date.now() - startTime
    await recordSuccess(
      OperationType.UPDATE,
      OperationModule.USER,
      `修改用户[${roleForm.value.id}]角色: ${roleForm.value.oldRole}->${roleForm.value.role}`,
      executeTime
    )
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')

    // 记录失败日志
    const executeTime = Date.now() - startTime
    await recordFailure(
      OperationType.UPDATE,
      OperationModule.USER,
      `修改用户角色失败：${roleForm.value.nickname}`,
      executeTime
    )
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row: any) => {
  const startTime = Date.now()
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      type: 'warning'
    })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()

    // 记录操作日志
    const executeTime = Date.now() - startTime
    await recordSuccess(
      OperationType.UPDATE,
      OperationModule.USER,
      `${action}用户：${row.nickname}`,
      executeTime
    )
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')

      // 记录失败日志
      const executeTime = Date.now() - startTime
      await recordFailure(
        OperationType.UPDATE,
        OperationModule.USER,
        `${action}用户失败：${row.nickname}`,
        executeTime
      )
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.admin-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
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
