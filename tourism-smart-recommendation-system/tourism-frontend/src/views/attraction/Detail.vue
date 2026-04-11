<template>
  <div
    v-loading="loading"
    class="attraction-detail-page"
  >
    <!-- 返回按钮 -->
    <BackButton fallback="/attraction" class="page-back-btn" />

    <div v-if="detail">
      <!-- 图片轮播 -->
      <div class="image-gallery">
        <el-carousel
          height="400px"
          indicator-position="inside"
        >
          <el-carousel-item
            v-for="(img, index) in detail.images"
            :key="index"
          >
            <el-image
              :src="img"
              fit="cover"
              style="width: 100%; height: 100%"
            />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 基本信息 -->
      <el-card class="info-card">
        <div class="info-header">
          <div class="title-section">
            <h1>{{ detail.name }}</h1>
            <div class="scenic-levels">
              <el-tag
                v-for="level in (detail.scenicLevel || '').split(',').filter((l: string) => l.trim())"
                :key="level"
                type="warning"
                size="large"
              >
                {{ level.trim() }}
              </el-tag>
            </div>
          </div>
          <div class="rating-section">
            <el-rate
              v-model="displayRating"
              disabled
              show-score
              text-color="#ff9900"
            />
            <span class="rating-count">({{ detail.ratingStats?.totalCount || 0 }}条评价)</span>
          </div>
        </div>

        <!-- 季节状态提示 -->
        <el-alert
          v-if="isClosed"
          type="warning"
          :closable="false"
          show-icon
          class="seasonal-alert"
        >
          <template #title>
            <span class="seasonal-title">暂停开放</span>
          </template>
          <template #default>
            <span v-if="detail.seasonalNote">{{ detail.seasonalNote }}</span>
            <span v-else>该景点当前暂停对外开放，请关注后续通知</span>
          </template>
        </el-alert>

        <!-- 详细信息 -->
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="所在地区">
            {{ detail.province }} · {{ detail.city }} · {{ detail.district }}
          </el-descriptions-item>
          <el-descriptions-item label="景点类型">
            {{ detail.categoryName }}
          </el-descriptions-item>
          <el-descriptions-item label="门票价格">
            <span
              v-if="detail.chargeType === 0"
              class="price free"
            >免费</span>
            <span
              v-else-if="detail.chargeType === 1"
              class="price"
            >¥{{ detail.ticketPrice }}</span>
            <span
              v-else
              class="price"
            >需预约</span>
          </el-descriptions-item>
          <el-descriptions-item label="建议游玩">
            {{ detail.suggestedDuration || '半天' }}
          </el-descriptions-item>
          <el-descriptions-item label="开放时间">
            {{ detail.openTime || '全天开放' }}
          </el-descriptions-item>
          <el-descriptions-item label="最佳季节">
            {{ detail.bestMonths || '四季皆宜' }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.contactPhone"
            label="联系电话"
          >
            {{ detail.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.officialWebsite"
            label="官方网站"
          >
            <el-link
              :href="detail.officialWebsite"
              target="_blank"
              type="primary"
            >
              访问官网
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item
            label="详细地址"
            :span="2"
          >
            {{ detail.address }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            :type="detail.isFavorited ? 'danger' : 'default'"
            :icon="detail.isFavorited ? StarFilled : Star"
            @click="toggleFavorite"
          >
            {{ detail.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            :type="detail.isVisited ? 'success' : 'default'"
            :icon="Check"
            @click="toggleVisited"
          >
            {{ detail.isVisited ? '已去过' : '标记去过' }}
          </el-button>
          <el-button
            :icon="Share"
            @click="handleShare"
          >
            分享
          </el-button>
        </div>
      </el-card>

      <!-- 景点介绍 -->
      <el-card class="detail-card">
        <template #header>
          <h2>景点介绍</h2>
        </template>
        <div class="description">
          {{ detail.description }}
        </div>
      </el-card>

      <!-- 特色与标签 -->
      <el-card
        v-if="detail.tags?.length || detail.features?.length"
        class="detail-card"
      >
        <template #header>
          <h2>特色标签</h2>
        </template>
        <div class="tags">
          <el-tag
            v-for="tag in detail.tags"
            :key="tag"
            type="primary"
            size="large"
          >
            {{ tag }}
          </el-tag>
          <el-tag
            v-for="feature in detail.features"
            :key="feature"
            type="success"
            size="large"
          >
            {{ feature }}
          </el-tag>
        </div>
      </el-card>

      <!-- 游玩提示 -->
      <el-card
        v-if="detail.tips?.length"
        class="detail-card"
      >
        <template #header>
          <h2>游玩提示</h2>
        </template>
        <el-alert
          v-for="(tip, index) in detail.tips"
          :key="index"
          :title="tip"
          type="info"
          :closable="false"
          style="margin-bottom: 10px"
        />
      </el-card>

      <!-- 评分统计 -->
      <el-card
        v-if="detail.ratingStats"
        class="detail-card"
      >
        <template #header>
          <h2>游客评价</h2>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="rating-overview">
              <div class="overall-score">
                {{ detail.ratingStats.totalCount > 0 ? detail.ratingStats.avgOverallScore?.toFixed(1) : '0.0' }}
              </div>
              <el-rate
                v-model="displayRating"
                disabled
              />
              <div class="total-reviews">
                {{ detail.ratingStats.totalCount }}条评价
              </div>
            </div>
          </el-col>
          <el-col :span="16">
            <div class="rating-dimensions">
              <div class="dimension-item">
                <span class="label">景色：</span>
                <el-rate
                  v-model="sceneryRating"
                  disabled
                  show-score
                />
              </div>
              <div class="dimension-item">
                <span class="label">趣味性：</span>
                <el-rate
                  v-model="funRating"
                  disabled
                  show-score
                />
              </div>
              <div class="dimension-item">
                <span class="label">性价比：</span>
                <el-rate
                  v-model="valueRating"
                  disabled
                  show-score
                />
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 我的评分 -->
        <el-divider />
        <div v-if="detail.userRating">
          <h3>我的评分</h3>
          <div class="my-rating">
            <p>
              景色：<el-rate
                v-model="detail.userRating.sceneryScore"
                disabled
              />
            </p>
            <p>
              趣味性：<el-rate
                v-model="detail.userRating.funScore"
                disabled
              />
            </p>
            <p>
              性价比：<el-rate
                v-model="detail.userRating.valueScore"
                disabled
              />
            </p>
          </div>
          <el-button
            type="primary"
            @click="handleReRate"
          >
            重新评分
          </el-button>
        </div>
        <div v-else>
          <el-button
            type="primary"
            @click="showRatingDialog = true"
          >
            写评价
          </el-button>
        </div>

        <!-- 所有评论列表 -->
        <el-divider />
        <h3>全部评论 ({{ commentTotal }})</h3>

        <!-- 写评论 -->
        <div class="add-comment-area">
          <el-input
            v-model="newAttractionComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            maxlength="500"
            show-word-limit
          />
          <div class="add-comment-actions">
            <el-button
              type="primary"
              :disabled="!newAttractionComment.trim()"
              @click="submitAttractionComment"
            >
              发表评论
            </el-button>
          </div>
        </div>

        <div class="attraction-comments">
          <div
            v-for="rating in attractionRatings"
            :key="rating.id"
            class="rating-comment-item"
          >
            <div class="rating-comment-header">
              <span class="rating-user">{{ rating.nickname || '匿名用户' }}</span>
              <span class="rating-time">{{ formatRatingTime(rating.createTime) }}</span>
            </div>
            <div class="rating-comment-content">
              {{ rating.comment }}
            </div>
            <div class="rating-comment-actions">
              <el-button
                v-if="rating.userId === currentUserId"
                type="danger"
                size="small"
                text
                @click="handleDeleteRating(rating.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <el-empty
            v-if="attractionRatings.length === 0"
            description="暂无评论"
          />
          <div
            v-if="commentTotal > commentPageSize"
            class="comment-pagination"
          >
            <el-pagination
              v-model:current-page="commentPage"
              :page-size="commentPageSize"
              :total="commentTotal"
              layout="prev, pager, next"
              @current-change="loadRatings"
            />
          </div>
        </div>
      </el-card>

      <!-- 相似景点 -->
      <el-card
        v-if="similarAttractions.length"
        class="detail-card"
      >
        <template #header>
          <h2>相似景点推荐</h2>
        </template>
        <el-row :gutter="15">
          <el-col
            v-for="item in similarAttractions"
            :key="item.id"
            :xs="24"
            :sm="12"
            :md="8"
          >
            <div
              class="similar-card"
              @click="goToAttraction(item.id)"
            >
              <el-image
                :src="item.images?.[0]"
                fit="cover"
                style="height: 120px"
              />
              <div class="similar-info">
                <h4>{{ item.name }}</h4>
                <p>📍 {{ item.city }}</p>
                <p class="rating">
                  ⭐ {{ item.ratingCount > 0 && item.rating ? item.rating.toFixed(1) : '0.0' }}
                </p>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 相关攻略 -->
      <el-card
        v-if="relatedStrategies.length"
        class="detail-card"
      >
        <template #header>
          <h2>相关攻略</h2>
        </template>
        <div class="strategy-list">
          <div
            v-for="strategy in relatedStrategies"
            :key="strategy.id"
            class="strategy-item"
            @click="goToStrategy(strategy.id)"
          >
            <el-image
              :src="strategy.coverImage"
              fit="cover"
              style="width: 100px; height: 80px"
            />
            <div class="strategy-content">
              <h4>{{ strategy.title }}</h4>
              <p class="author">
                作者：{{ strategy.authorName }}
              </p>
              <p class="stats">
                👁 {{ strategy.viewCount }} · 👍 {{ strategy.likeCount }}
              </p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="showRatingDialog"
      title="评价景点"
      width="500px"
    >
      <el-form
        :model="ratingForm"
        label-width="80px"
      >
        <el-form-item label="景色">
          <el-rate
            v-model="ratingForm.sceneryScore"
            show-score
          />
        </el-form-item>
        <el-form-item label="趣味性">
          <el-rate
            v-model="ratingForm.funScore"
            show-score
          />
        </el-form-item>
        <el-form-item label="性价比">
          <el-rate
            v-model="ratingForm.valueScore"
            show-score
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRatingDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitRating"
        >
          提交评价
        </el-button>
      </template>
    </el-dialog>

    <!-- 标记去过对话框 -->
    <el-dialog
      v-model="showVisitDialog"
      title="标记去过"
      width="400px"
    >
      <el-form
        :model="visitForm"
        label-width="80px"
      >
        <el-form-item label="游玩日期">
          <el-date-picker
            v-model="visitForm.visitDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="visitForm.note"
            type="textarea"
            :rows="3"
            placeholder="记录你的旅行回忆..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVisitDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitVisit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Star, StarFilled, Share, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BackButton from '@/components/BackButton.vue'
import {
  getAttractionDetailEnhanced,
  favoriteAttraction,
  unfavoriteAttraction,
  rateAttraction,
  markVisited,
  unmarkVisited,
  getSimilarAttractions,
  getRelatedStrategies,
  getAttractionRatings,
  deleteAttractionRating,
  addAttractionComment
} from '@/api/attraction'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<any>(null)
const similarAttractions = ref<any[]>([])
const relatedStrategies = ref<any[]>([])
const attractionRatings = ref<any[]>([])
const commentPage = ref(1)
const commentPageSize = 5
const commentTotal = ref(0)
const newAttractionComment = ref('')

const showRatingDialog = ref(false)
const showVisitDialog = ref(false)

const ratingForm = ref({
  sceneryScore: 5,
  funScore: 5,
  valueScore: 5,
  comment: ''
})

const visitForm = ref({
  visitDate: new Date(),
  note: ''
})

const currentUserId = computed(() => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info.id || 0
  } catch {
    return 0
  }
})

const formatRatingTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString()
}

// 计算评分显示：仅当存在真实评分时才展示平均分，无评价时统一显示 0
const displayRating = computed(() => {
  const totalCount = detail.value?.ratingStats?.totalCount || 0
  if (totalCount === 0) {
    return 0
  }
  return detail.value?.ratingStats?.avgOverallScore || detail.value?.rating || 0
})

const sceneryRating = computed(() => {
  return detail.value?.ratingStats?.avgSceneryScore || 0
})

const funRating = computed(() => {
  return detail.value?.ratingStats?.avgFunScore || 0
})

const valueRating = computed(() => {
  return detail.value?.ratingStats?.avgValueScore || 0
})

// 是否暂停开放（seasonalStatus: 0=正常开放, 1=季节性关闭, 2=临时关闭）
const isClosed = computed(() => {
  const status = detail.value?.seasonalStatus
  return status === 1 || status === '1' || status === 2 || status === '2'
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const data = await getAttractionDetailEnhanced(id)

    // 确保数组字段有默认值，防止渲染错误
    detail.value = {
      ...data,
      images: data.images || [],
      tags: data.tags || [],
      features: data.features || [],
      tips: data.tips || [],
      similarAttractions: data.similarAttractions || [],
      relatedStrategies: data.relatedStrategies || []
    }

    // 加载相似景点
    const similar = await getSimilarAttractions(id)
    similarAttractions.value = similar || []

    // 加载相关攻略
    const strategies = await getRelatedStrategies(id)
    relatedStrategies.value = strategies || []

    // 加载所有评论
    await loadRatings()
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载评论列表
const loadRatings = async () => {
  try {
    const id = Number(route.params.id)
    const res = await getAttractionRatings(id, { page: commentPage.value, size: commentPageSize })
    attractionRatings.value = res.records || []
    commentTotal.value = res.total || 0
  } catch {
    attractionRatings.value = []
    commentTotal.value = 0
  }
}

// 切换收藏
const toggleFavorite = async () => {
  try {
    if (detail.value.isFavorited) {
      await unfavoriteAttraction(detail.value.id)
      detail.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteAttraction(detail.value.id)
      detail.value.isFavorited = true
      ElMessage.success('已收藏')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 切换去过标记
const toggleVisited = async () => {
  if (detail.value.isVisited) {
    try {
      await unmarkVisited(detail.value.id)
      detail.value.isVisited = false
      ElMessage.success('已取消标记')
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    }
  } else {
    showVisitDialog.value = true
  }
}

// 提交游览标记
const submitVisit = async () => {
  try {
    await markVisited({
      attractionId: detail.value.id,
      visitDate: visitForm.value.visitDate.toISOString().split('T')[0],
      note: visitForm.value.note
    })
    detail.value.isVisited = true
    showVisitDialog.value = false
    ElMessage.success('标记成功')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 提交评分（纯评分，不带评论）
const submitRating = async () => {
  try {
    await rateAttraction({
      attractionId: detail.value.id,
      sceneryScore: ratingForm.value.sceneryScore,
      funScore: ratingForm.value.funScore,
      valueScore: ratingForm.value.valueScore
    })
    showRatingDialog.value = false
    ElMessage.success('评分成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 提交评论（纯评论，不带评分）
const submitAttractionComment = async () => {
  try {
    await addAttractionComment(detail.value.id, newAttractionComment.value.trim())
    newAttractionComment.value = ''
    ElMessage.success('评论成功')
    await loadRatings()
  } catch (error: any) {
    ElMessage.error(error.message || '评论失败')
  }
}

// 重新评分
const handleReRate = () => {
  if (detail.value?.userRating) {
    ratingForm.value.sceneryScore = detail.value.userRating.sceneryScore || 5
    ratingForm.value.funScore = detail.value.userRating.funScore || 5
    ratingForm.value.valueScore = detail.value.userRating.valueScore || 5
    ratingForm.value.comment = ''
  }
  showRatingDialog.value = true
}

// 删除评论
const handleDeleteRating = async (ratingId: number) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', { type: 'warning' })
    await deleteAttractionRating(detail.value.id, ratingId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 分享
const handleShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('请手动复制链接')
  })
}

// 跳转到景点
const goToAttraction = (id: number) => {
  router.push(`/attraction/${id}`)
  loadData()
}

// 跳转到攻略
const goToStrategy = (id: number) => {
  router.push(`/strategy/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.attraction-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 40px;
}

.page-back-btn {
  margin-bottom: 20px;
}

.image-gallery {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-card {
  margin-bottom: 20px;
}

.info-header {
  margin-bottom: 20px;

  .title-section {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 10px;
    flex-wrap: wrap;

    h1 {
      font-size: 28px;
      margin: 0;
    }

    .scenic-levels {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .rating-section {
    display: flex;
    align-items: center;
    gap: 10px;

    .rating-count {
      color: #999;
      font-size: 14px;
    }
  }
}

.seasonal-alert {
  margin-bottom: 20px;

  .seasonal-title {
    font-weight: bold;
  }
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;

  &.free {
    color: #67c23a;
  }
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.detail-card {
  margin-bottom: 20px;

  h2 {
    font-size: 20px;
    margin: 0;
  }
}

.description {
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rating-overview {
  text-align: center;
  padding: 20px;

  .overall-score {
    font-size: 48px;
    font-weight: bold;
    color: #ff9900;
    margin-bottom: 10px;
  }

  .total-reviews {
    color: #999;
    margin-top: 10px;
  }
}

.rating-dimensions {
  .dimension-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;

    .label {
      width: 80px;
      font-weight: bold;
    }
  }
}

.my-rating {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 12px;

  p {
    margin: 10px 0;
  }
}

.attraction-comments {
  margin-top: 16px;
}

.rating-comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.rating-comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.rating-user {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.rating-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.rating-comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-top: 4px;
}

.rating-comment-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.add-comment-area {
  margin-bottom: 20px;
}

.add-comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.similar-card {
  cursor: pointer;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  margin-bottom: 15px;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .similar-info {
    padding: 10px;

    h4 {
      margin: 0 0 5px 0;
      font-size: 14px;
    }

    p {
      margin: 3px 0;
      font-size: 12px;
      color: #666;
    }

    .rating {
      color: #ff9900;
      font-weight: bold;
    }
  }
}

.strategy-list {
  .strategy-item {
    display: flex;
    gap: 15px;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 8px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f5f7fa;
    }

    .strategy-content {
      flex: 1;

      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
      }

      p {
        margin: 5px 0;
        font-size: 13px;
        color: #666;
      }

      .stats {
        color: #999;
      }
    }
  }
}
</style>
