<template>
  <div class="admin-attraction-page">
    <el-card>
      <template #header>
        <div class="header-actions">
          <h3>景点管理</h3>
          <div class="actions">
            <el-button
              type="primary"
              :icon="Plus"
              @click="handleAdd"
            >
              添加景点
            </el-button>
            <el-button
              :icon="DataAnalysis"
              @click="goToAnalytics"
            >
              数据分析
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form
        :inline="true"
        :model="searchForm"
        class="search-form"
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="景点名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="省份">
          <el-select
            v-model="searchForm.province"
            placeholder="选择省份"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="province in provinces"
              :key="province"
              :label="province"
              :value="province"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input
            v-model="searchForm.city"
            placeholder="城市名称"
            clearable
            style="width: 150px"
          />
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

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="list"
        style="width: 100%"
      >
        <el-table-column
          label="序号"
          width="80"
        >
          <template #default="{ $index }">
            {{ (searchForm.page - 1) * searchForm.size + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="景点名称"
          width="200"
        />
        <el-table-column
          prop="province"
          label="省份"
          width="100"
        />
        <el-table-column
          prop="city"
          label="城市"
          width="100"
        />
        <el-table-column
          prop="scenicLevel"
          label="等级"
          width="100"
        />
        <el-table-column
          prop="ticketPrice"
          label="门票"
          width="100"
        >
          <template #default="{ row }">
            <span
              v-if="row.chargeType === 0"
              class="free"
            >免费</span>
            <span v-else>¥{{ row.ticketPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="rating"
          label="评分"
          width="100"
        >
          <template #default="{ row }">
            {{ row.rating?.toFixed(1) || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="viewCount"
          label="浏览量"
          width="100"
        />
        <el-table-column
          prop="favoriteCount"
          label="收藏数"
          width="100"
        />
        <el-table-column
          label="季节性状态"
          width="120"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.seasonalStatus === 0"
              type="success"
            >
              正常开放
            </el-tag>
            <el-tag
              v-else-if="row.seasonalStatus === 1"
              type="warning"
            >
              季节性关闭
            </el-tag>
            <el-tag
              v-else
              type="info"
            >
              临时关闭
            </el-tag>
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
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="warning"
              size="small"
              :icon="Setting"
              @click="handleSetSeasonal(row)"
            >
              状态
            </el-button>
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <!-- 编辑/添加对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="editForm.id ? '编辑景点' : '添加景点'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="景点名称"
              prop="name"
            >
              <el-input
                v-model="editForm.name"
                placeholder="请输入景点名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="景点类型"
              prop="categoryId"
            >
              <el-select
                v-model="editForm.categoryId"
                placeholder="选择类型"
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
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item
              label="省份"
              prop="province"
            >
              <el-select
                v-model="editForm.province"
                placeholder="选择省份"
                filterable
              >
                <el-option
                  v-for="province in provinces"
                  :key="province"
                  :label="province"
                  :value="province"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item
              label="城市"
              prop="city"
            >
              <el-input
                v-model="editForm.city"
                placeholder="请输入城市"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item
              label="区县"
              prop="district"
            >
              <el-input
                v-model="editForm.district"
                placeholder="请输入区县"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item
          label="详细地址"
          prop="address"
        >
          <el-input
            v-model="editForm.address"
            placeholder="请输入详细地址"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="景区等级">
              <el-select
                v-model="editForm.scenicLevel"
                placeholder="选择等级"
              >
                <el-option
                  label="5A级景区"
                  value="5A级景区"
                />
                <el-option
                  label="4A级景区"
                  value="4A级景区"
                />
                <el-option
                  label="3A级景区"
                  value="3A级景区"
                />
                <el-option
                  label="2A级景区"
                  value="2A级景区"
                />
                <el-option
                  label="A级景区"
                  value="A级景区"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收费类型">
              <el-select v-model="editForm.chargeType">
                <el-option
                  label="免费"
                  :value="0"
                />
                <el-option
                  label="收费"
                  :value="1"
                />
                <el-option
                  label="需预约"
                  :value="2"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item
              v-if="editForm.chargeType === 1"
              label="门票价格"
            >
              <el-input-number
                v-model="editForm.ticketPrice"
                :min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="建议游玩时长">
              <el-select v-model="editForm.suggestedDuration">
                <el-option
                  label="1-2小时"
                  value="1-2小时"
                />
                <el-option
                  label="2-3小时"
                  value="2-3小时"
                />
                <el-option
                  label="半天"
                  value="半天"
                />
                <el-option
                  label="1天"
                  value="1天"
                />
                <el-option
                  label="2天"
                  value="2天"
                />
                <el-option
                  label="3天以上"
                  value="3天以上"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开放时间">
              <el-input
                v-model="editForm.openTime"
                placeholder="如：08:00-18:00"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="最佳季节">
          <el-checkbox-group v-model="editForm.bestMonths">
            <el-checkbox label="春季" />
            <el-checkbox label="夏季" />
            <el-checkbox label="秋季" />
            <el-checkbox label="冬季" />
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="场景类型">
          <el-checkbox-group v-model="editForm.sceneTypes">
            <el-checkbox label="山水" />
            <el-checkbox label="古镇" />
            <el-checkbox label="海滨" />
            <el-checkbox label="草原" />
          </el-checkbox-group>
        </el-form-item>

        <el-form-item
          label="景点介绍"
          prop="description"
        >
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入景点介绍"
          />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input
            v-model="editForm.contactPhone"
            placeholder="请输入联系电话"
          />
        </el-form-item>

        <el-form-item label="官方网站">
          <el-input
            v-model="editForm.officialWebsite"
            placeholder="请输入官方网站"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-input
            v-model="editForm.tagsInput"
            placeholder="多个标签用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="特色">
          <el-input
            v-model="editForm.featuresInput"
            placeholder="多个特色用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="游玩提示">
          <el-input
            v-model="editForm.tipsInput"
            type="textarea"
            :rows="3"
            placeholder="多条提示用换行分隔"
          />
        </el-form-item>

        <el-form-item label="景点图片">
          <div class="image-upload-area">
            <div
              v-for="(url, index) in (editForm.images || [])"
              :key="index"
              class="image-upload-item"
            >
              <el-image
                :src="url.startsWith('/') ? '/api' + url : url"
                fit="cover"
                style="width: 100px; height: 100px"
              />
              <div class="image-upload-actions">
                <el-icon @click="handleImagePreview(url)"><ZoomIn /></el-icon>
                <el-icon @click="handleImageRemove(Number(index))"><Delete /></el-icon>
              </div>
            </div>
            <el-upload
              v-if="(editForm.images?.length || 0) < 3"
              class="image-uploader"
              action=""
              :show-file-list="false"
              :http-request="handleImageUpload"
              :disabled="imageUploading"
              accept="image/jpeg,image/png,image/gif,image/webp"
            >
              <div class="image-upload-trigger">
                <el-icon v-if="!imageUploading"><Plus /></el-icon>
                <el-icon v-else class="is-loading"><Loading /></el-icon>
              </div>
            </el-upload>
          </div>
          <div class="el-upload__tip">最多上传3张，支持 JPG/PNG/GIF/WebP，单张不超过10MB</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" alt="预览" style="width: 100%" />
    </el-dialog>

    <!-- 季节性状态对话框 -->
    <el-dialog
      v-model="showSeasonalDialog"
      title="设置季节性状态"
      width="500px"
    >
      <el-form
        :model="seasonalForm"
        label-width="100px"
      >
        <el-form-item label="状态">
          <el-radio-group v-model="seasonalForm.seasonalStatus">
            <el-radio :label="0">
              正常开放
            </el-radio>
            <el-radio :label="1">
              季节性关闭
            </el-radio>
            <el-radio :label="2">
              临时关闭
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="seasonalForm.seasonalNote"
            type="textarea"
            :rows="3"
            placeholder="请输入关闭原因或说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSeasonalDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitSeasonal"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, Search, Setting, DataAnalysis, Loading, ZoomIn } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  adminGetAttractionList,
  adminCreateAttraction,
  adminUpdateAttraction,
  adminDeleteAttraction,
  adminSetSeasonalStatus
} from '@/api/attraction'
import { uploadImage } from '@/api/upload'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const imageUploading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const list = ref<any[]>([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  province: '',
  city: '',
  page: 1,
  size: 10
})

const showEditDialog = ref(false)
const showSeasonalDialog = ref(false)
const formRef = ref<FormInstance>()

const editForm = ref<any>({
  id: null,
  name: '',
  categoryId: undefined,
  province: '',
  city: '',
  district: '',
  address: '',
  scenicLevel: '',
  chargeType: 0,
  ticketPrice: 0,
  suggestedDuration: '',
  openTime: '',
  bestMonths: [],
  sceneTypes: [],
  description: '',
  images: [],
  longitude: null,
  latitude: null,
  contactPhone: '',
  officialWebsite: '',
  tagsInput: '',
  featuresInput: '',
  tipsInput: '',
  seasonalStatus: 0,
  seasonalNote: ''
})

const seasonalForm = ref({
  id: 0,
  seasonalStatus: 0,
  seasonalNote: ''
})

const provinces = [
  '北京', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江',
  '上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南',
  '湖北', '湖南', '广东', '广西', '海南', '重庆', '四川', '贵州',
  '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆', '台湾',
  '香港', '澳门'
]

const rules: FormRules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择景点类型', trigger: 'change' }],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  description: [{ required: true, message: '请输入景点介绍', trigger: 'blur' }]
}

// 自定义图片上传
const handleImageUpload = async (options: any) => {
  const file = options.file
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP 格式')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return
  }

  imageUploading.value = true
  try {
    const url = await uploadImage(file) as unknown as string
    if (!editForm.value.images) {
      editForm.value.images = []
    }
    editForm.value.images.push(url)
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    imageUploading.value = false
  }
}

// 删除图片
const handleImageRemove = (index: number) => {
  if (!editForm.value.images) return
  editForm.value.images.splice(index, 1)
}

// 预览图片
const handleImagePreview = (url: string) => {
  previewUrl.value = url.startsWith('/') ? '/api' + url : url
  previewVisible.value = true
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminGetAttractionList(searchForm)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.province = ''
  searchForm.city = ''
  searchForm.page = 1
  loadData()
}

// 添加
const handleAdd = () => {
  editForm.value = {
    id: null,
    name: '',
    categoryId: undefined,
    province: '',
    city: '',
    district: '',
    address: '',
    scenicLevel: '',
    chargeType: 0,
    ticketPrice: 0,
    suggestedDuration: '',
    openTime: '',
    bestMonths: [],
    sceneTypes: [],
    description: '',
    images: [],
    contactPhone: '',
    officialWebsite: '',
    tagsInput: '',
    featuresInput: '',
    tipsInput: ''
  }
  showEditDialog.value = true
}

// 编辑
const handleEdit = (row: any) => {
  // bestMonths 从字符串转为数组（如 "春季,夏季" -> ["春季", "夏季"]）
  let bestMonthsArray: string[] = []
  if (row.bestMonths) {
    bestMonthsArray = row.bestMonths.split(',').map((m: string) => m.trim()).filter((m: string) => m)
  }

  editForm.value = {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    description: row.description,
    images: Array.isArray(row.images) ? [...row.images] : [],
    address: row.address,
    province: row.province,
    city: row.city,
    district: row.district,
    scenicLevel: row.scenicLevel,
    longitude: row.longitude,
    latitude: row.latitude,
    openTime: row.openTime,
    ticketPrice: row.ticketPrice,
    chargeType: row.chargeType,
    suggestedDuration: row.suggestedDuration,
    bestMonths: bestMonthsArray,
    sceneTypes: row.sceneType ? row.sceneType.split(',') : [],
    tagsInput: row.tags ? (row.tags?.join(',') || '') : '',
    featuresInput: row.features ? (row.features?.join(',') || '') : '',
    tipsInput: row.tips ? (row.tips?.join('\n') || '') : '',
    contactPhone: row.contactPhone || '',
    officialWebsite: row.officialWebsite || '',
    seasonalStatus: row.seasonalStatus || 0,
    seasonalNote: row.seasonalNote || ''
  }
  showEditDialog.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 后端tags、features、tips字段现在是String类型，我们直接发送JSON字符串
      // 后端不再手动序列化，直接存储到数据库
      const data = {
        id: editForm.value.id,
        name: editForm.value.name,
        categoryId: editForm.value.categoryId,
        description: editForm.value.description,
        images: editForm.value.images,
        address: editForm.value.address,
        province: editForm.value.province,
        city: editForm.value.city,
        district: editForm.value.district,
        scenicLevel: editForm.value.scenicLevel,
        longitude: editForm.value.longitude,
        latitude: editForm.value.latitude,
        openTime: editForm.value.openTime,
        ticketPrice: editForm.value.ticketPrice,
        chargeType: editForm.value.chargeType,
        suggestedDuration: editForm.value.suggestedDuration,
        // bestMonths 从数组转为逗号分隔的字符串
        bestMonths: Array.isArray(editForm.value.bestMonths) ? editForm.value.bestMonths.join(',') : editForm.value.bestMonths,
        // sceneType是字符串类型（逗号分隔）
        sceneType: editForm.value.sceneTypes.join(','),
        // tags发送为数组类型
        tags: editForm.value.tagsInput ? editForm.value.tagsInput.split(',').map((t: string) => t.trim()).filter((t: string) => t) : null,
        // features发送为数组类型
        features: editForm.value.featuresInput ? editForm.value.featuresInput.split(',').map((f: string) => f.trim()).filter((f: string) => f) : null,
        contactPhone: editForm.value.contactPhone,
        officialWebsite: editForm.value.officialWebsite,
        // tips发送为数组类型
        tips: editForm.value.tipsInput ? editForm.value.tipsInput.split('\n').map((t: string) => t.trim()).filter((t: string) => t) : null,
        seasonalStatus: editForm.value.seasonalStatus,
        seasonalNote: editForm.value.seasonalNote
      }

      if (data.id) {
        await adminUpdateAttraction(data.id, data)
        ElMessage.success('更新成功')
      } else {
        await adminCreateAttraction(data)
        ElMessage.success('添加成功')
      }

      showEditDialog.value = false
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该景点吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminDeleteAttraction(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  })
}

// 设置季节性状态
const handleSetSeasonal = (row: any) => {
  seasonalForm.value = {
    id: row.id,
    seasonalStatus: row.seasonalStatus || 0,
    seasonalNote: row.seasonalNote || ''
  }
  showSeasonalDialog.value = true
}

// 提交季节性状态
const submitSeasonal = async () => {
  try {
    // 使用URLSearchParams正确传递参数
    await adminSetSeasonalStatus(
      seasonalForm.value.id,
      seasonalForm.value.seasonalStatus,
      seasonalForm.value.seasonalNote
    )
    ElMessage.success('设置成功')
    showSeasonalDialog.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
  }
}

// 跳转到数据分析
const goToAnalytics = () => {
  router.push('/admin/attraction/analytics')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.admin-attraction-page {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
  }

  .actions {
    display: flex;
    gap: 10px;
  }
}

.search-form {
  margin-bottom: 20px;
}

.free {
  color: #67c23a;
  font-weight: bold;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-upload-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;

  .image-upload-actions {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    background: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.2s;
    cursor: pointer;

    .el-icon {
      color: #fff;
      font-size: 18px;
    }
  }

  &:hover .image-upload-actions {
    opacity: 1;
  }
}

.image-uploader {
  :deep(.el-upload) {
    width: 100px;
    height: 100px;
    border: 1px dashed #dcdfe6;
    border-radius: 6px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: border-color 0.2s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.image-upload-trigger {
  font-size: 24px;
  color: #8c939d;
}
</style>
