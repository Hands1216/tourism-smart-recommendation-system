<template>
  <div :class="['strategy-detail-page', { 'immersive-mode': isImmersive }]">
    <!-- 返回按钮 -->
    <BackButton fallback="/strategy" class="page-back-btn" />

    <el-row :gutter="20">
      <!-- 主内容区 -->
      <el-col :span="isImmersive ? 24 : 18">
        <el-card v-loading="loading">
          <!-- 封面图 -->
          <div
            v-if="strategy.coverImage"
            class="cover-image"
          >
            <img
              :src="strategy.coverImage"
              :alt="strategy.title"
            >
          </div>

          <!-- 标题和元信息 -->
          <div class="header">
            <h1>{{ strategy.title }}</h1>
            <div class="meta">
              <el-avatar
                :src="strategy.authorAvatar"
                :size="40"
              >
                {{ strategy.authorName?.charAt(0) }}
              </el-avatar>
              <div class="author-info">
                <div class="author-name">
                  {{ strategy.authorName }}
                </div>
                <div class="publish-time">
                  {{ formatTime(strategy.createTime) }}
                </div>
              </div>
              <div class="stats">
                <span><el-icon><View /></el-icon> {{ strategy.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ strategy.likeCount }}</span>
                <span><el-icon><Collection /></el-icon> {{ strategy.favoriteCount }}</span>
              </div>
            </div>
          </div>

          <!-- 标签 -->
          <div
            v-if="strategy.tags && strategy.tags.length > 0"
            class="tags"
          >
            <el-tag
              v-for="tag in strategy.tags"
              :key="tag"
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>

          <!-- 行程信息 -->
          <div class="trip-info">
            <el-descriptions
              :column="4"
              border
            >
              <el-descriptions-item label="目的地">
                <el-icon><Location /></el-icon> {{ strategy.destination }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="strategy.days"
                label="行程天数"
              >
                <el-icon><Calendar /></el-icon> {{ strategy.days }} 天
              </el-descriptions-item>
              <el-descriptions-item
                v-if="strategy.budget"
                label="人均预算"
              >
                <span class="budget-value">¥{{ strategy.budget }}</span>
              </el-descriptions-item>
              <el-descriptions-item
                v-if="strategy.season"
                label="适合季节"
              >
                <el-tag
                  :type="seasonTagType(strategy.season)"
                  size="small"
                >
                  {{ seasonLabel(strategy.season) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="攻略类型">
                <el-tag
                  v-if="strategy.isAiGenerated"
                  type="success"
                >
                  AI生成
                </el-tag>
                <el-tag
                  v-else
                  type="primary"
                >
                  原创
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 摘要 -->
          <div
            v-if="strategy.summary"
            class="summary-box"
          >
            <el-alert
              :title="strategy.summary"
              type="info"
              :closable="false"
              show-icon
            />
          </div>

          <!-- 沉浸阅读切换 -->
          <div class="reading-toolbar">
            <el-button
              :type="isImmersive ? 'primary' : 'default'"
              size="small"
              @click="toggleImmersive"
            >
              <el-icon><Reading /></el-icon>
              {{ isImmersive ? '退出沉浸阅读' : '沉浸阅读' }}
            </el-button>
          </div>

          <!-- 富文本内容 -->
          <div
            class="content"
            v-html="strategy.content"
          />

          <!-- 操作按钮 -->
          <div class="actions">
            <el-button
              :type="strategy.isLiked ? 'primary' : 'default'"
              :icon="Star"
              @click="handleLike"
            >
              {{ strategy.isLiked ? '已点赞' : '点赞' }} ({{ strategy.likeCount }})
            </el-button>
            <el-button
              :type="strategy.isFavorited ? 'warning' : 'default'"
              :icon="Collection"
              @click="handleFavorite"
            >
              {{ strategy.isFavorited ? '已收藏' : '收藏' }} ({{ strategy.favoriteCount }})
            </el-button>
            <el-dropdown
              trigger="click"
              @command="handleShareCommand"
            >
              <el-button :icon="Share">
                分享 <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="link">
                    复制链接
                  </el-dropdown-item>
                  <el-dropdown-item command="poster">
                    生成海报
                  </el-dropdown-item>
                  <el-dropdown-item command="pdf">
                    导出PDF
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 海报预览对话框 -->
          <el-dialog
            v-model="posterVisible"
            title="分享海报"
            width="450px"
            align-center
          >
            <div class="poster-container">
              <canvas
                ref="posterCanvas"
                width="400"
                height="600"
              />
            </div>
            <template #footer>
              <el-button @click="posterVisible = false">
                关闭
              </el-button>
              <el-button
                type="primary"
                @click="downloadPoster"
              >
                下载海报
              </el-button>
            </template>
          </el-dialog>

          <!-- 评论区 -->
          <CommentSection
            v-if="strategy.id"
            :strategy-id="strategy.id"
          />
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col
        v-show="!isImmersive"
        :span="6"
      >
        <el-affix :offset="20">
          <!-- 目录导航 -->
          <el-card class="toc-card">
            <template #header>
              <div class="card-header">
                <span>目录</span>
              </div>
            </template>
            <div class="toc">
              <div
                v-for="(item, index) in toc"
                :key="index"
                :class="['toc-item', `toc-level-${item.level}`, { active: activeAnchor === item.id }]"
                @click="scrollToAnchor(item.id)"
              >
                {{ item.text }}
              </div>
              <div
                v-if="toc.length === 0"
                class="toc-empty"
              >
                暂无目录
              </div>
            </div>
          </el-card>

          <!-- 相关推荐 -->
          <el-card
            v-if="relatedList.length > 0"
            class="related-card"
          >
            <template #header>
              <div class="card-header">
                <span>相关推荐</span>
              </div>
            </template>
            <div
              v-for="item in relatedList"
              :key="item.id"
              class="related-item"
              @click="goToDetail(item.id!)"
            >
              <el-image
                :src="item.coverImage || defaultImage"
                fit="cover"
                class="related-cover"
              />
              <div class="related-info">
                <div class="related-title">
                  {{ item.title }}
                </div>
                <div class="related-meta">
                  <span v-if="item.destination">{{ item.destination }}</span>
                  <span v-if="item.days">{{ item.days }}天</span>
                  <span v-if="item.budget">¥{{ item.budget }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-affix>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Star, Collection, Share, Location, Calendar, Reading, ArrowDown } from '@element-plus/icons-vue'
import { getDetail, like, favorite, getRelated } from '@/api/strategy'
import type { StrategyVO } from '@/api/strategy'
import CommentSection from '@/components/common/CommentSection.vue'
import BackButton from '@/components/BackButton.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const isImmersive = ref(false)
const defaultImage = 'https://via.placeholder.com/300x200?text=攻略'
const strategy = ref<StrategyVO>({
  title: '',
  content: '',
  destination: '',
  viewCount: 0,
  likeCount: 0,
  favoriteCount: 0,
  commentCount: 0
})
const relatedList = ref<StrategyVO[]>([])
const posterVisible = ref(false)
const posterCanvas = ref<HTMLCanvasElement | null>(null)
interface TocItem {
  id: string
  text: string
  level: number
}

const toc = ref<TocItem[]>([])
const activeAnchor = ref('')

const seasonMap: Record<string, string> = {
  spring: '春季',
  summer: '夏季',
  autumn: '秋季',
  winter: '冬季',
  all: '四季皆宜'
}

const seasonLabel = (season: string) => seasonMap[season] || season

const seasonTagType = (season: string) => {
  const map: Record<string, string> = {
    spring: 'success',
    summer: 'danger',
    autumn: 'warning',
    winter: 'info',
    all: ''
  }
  return map[season] || ''
}

const toggleImmersive = () => {
  isImmersive.value = !isImmersive.value
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 加载攻略详情
const loadDetail = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)
    const data = await getDetail(id)
    strategy.value = data

    // 解析标签（如果是JSON字符串）
    if (typeof data.tags === 'string') {
      try {
        strategy.value.tags = JSON.parse(data.tags)
      } catch (e) {
        strategy.value.tags = []
      }
    }

    // 生成目录
    nextTick(() => {
      generateToc()
      observeScroll()
    })

    // 加载相关推荐
    loadRelated(id)
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 加载相关推荐
const loadRelated = async (id: number) => {
  try {
    const data = await getRelated(id, 5)
    relatedList.value = data || []
  } catch (e) {
    // 静默失败
  }
}

// 跳转到其他攻略详情
const goToDetail = (id: number) => {
  router.push(`/strategy/${id}`)
  // 重新加载数据
  nextTick(() => {
    loadDetail()
  })
}

// 生成目录
const generateToc = () => {
  const contentEl = document.querySelector('.content')
  if (!contentEl) return

  const headings = contentEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const tocItems: TocItem[] = []

  headings.forEach((heading, index) => {
    const id = `heading-${index}`
    heading.id = id
    const level = parseInt(heading.tagName.substring(1))
    tocItems.push({
      id,
      text: heading.textContent || '',
      level
    })
  })

  toc.value = tocItems
}

// 监听滚动，高亮当前目录项
const observeScroll = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          activeAnchor.value = entry.target.id
        }
      })
    },
    { rootMargin: '-20% 0px -70% 0px' }
  )

  toc.value.forEach((item) => {
    const el = document.getElementById(item.id)
    if (el) observer.observe(el)
  })
}

// 滚动到锚点
const scrollToAnchor = (id: string) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 点赞
const handleLike = async () => {
  try {
    await like(strategy.value.id!)
    strategy.value.isLiked = !strategy.value.isLiked
    strategy.value.likeCount = strategy.value.isLiked
      ? (strategy.value.likeCount || 0) + 1
      : Math.max(0, (strategy.value.likeCount || 0) - 1)
    ElMessage.success(strategy.value.isLiked ? '点赞成功' : '取消点赞')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 收藏
const handleFavorite = async () => {
  try {
    await favorite(strategy.value.id!)
    strategy.value.isFavorited = !strategy.value.isFavorited
    strategy.value.favoriteCount = strategy.value.isFavorited
      ? (strategy.value.favoriteCount || 0) + 1
      : Math.max(0, (strategy.value.favoriteCount || 0) - 1)
    ElMessage.success(strategy.value.isFavorited ? '收藏成功' : '取消收藏')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 分享功能
const handleShareCommand = (command: string) => {
  switch (command) {
    case 'link':
      copyLink()
      break
    case 'poster':
      generatePoster()
      break
    case 'pdf':
      exportPdf()
      break
  }
}

const copyLink = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const generatePoster = () => {
  posterVisible.value = true
  nextTick(() => {
    drawPoster()
  })
}

const drawPoster = () => {
  const canvas = posterCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 背景
  const gradient = ctx.createLinearGradient(0, 0, 0, 600)
  gradient.addColorStop(0, '#409eff')
  gradient.addColorStop(1, '#79bbff')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, 400, 600)

  // 白色内容区
  ctx.fillStyle = '#ffffff'
  roundRect(ctx, 20, 20, 360, 560, 12)
  ctx.fill()

  // 标题
  ctx.fillStyle = '#303133'
  ctx.font = 'bold 20px sans-serif'
  wrapText(ctx, strategy.value.title || '旅游攻略', 40, 60, 320, 28)

  // 目的地
  ctx.fillStyle = '#409eff'
  ctx.font = '16px sans-serif'
  ctx.fillText(`📍 ${strategy.value.destination || ''}`, 40, 120)

  // 行程信息
  ctx.fillStyle = '#666666'
  ctx.font = '14px sans-serif'
  let infoY = 155
  if (strategy.value.days) {
    ctx.fillText(`🗓 行程 ${strategy.value.days} 天`, 40, infoY)
    infoY += 25
  }
  if (strategy.value.budget) {
    ctx.fillText(`💰 人均 ¥${strategy.value.budget}`, 40, infoY)
    infoY += 25
  }
  if (strategy.value.season) {
    ctx.fillText(`🌤 ${seasonLabel(strategy.value.season)}`, 40, infoY)
    infoY += 25
  }

  // 摘要
  if (strategy.value.summary) {
    ctx.fillStyle = '#999999'
    ctx.font = '13px sans-serif'
    wrapText(ctx, strategy.value.summary, 40, infoY + 15, 320, 20)
  }

  // 统计
  ctx.fillStyle = '#999999'
  ctx.font = '12px sans-serif'
  ctx.fillText(
    `👁 ${strategy.value.viewCount || 0}  ❤ ${strategy.value.likeCount || 0}  ⭐ ${strategy.value.favoriteCount || 0}`,
    40, 500
  )

  // 作者
  ctx.fillStyle = '#666666'
  ctx.font = '14px sans-serif'
  ctx.fillText(`作者：${strategy.value.authorName || ''}`, 40, 530)

  // 底部提示
  ctx.fillStyle = '#409eff'
  ctx.font = '12px sans-serif'
  ctx.fillText('扫码或搜索查看完整攻略', 110, 560)
}

const roundRect = (ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) => {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

const wrapText = (ctx: CanvasRenderingContext2D, text: string, x: number, y: number, maxWidth: number, lineHeight: number) => {
  const chars = text.split('')
  let line = ''
  let currentY = y
  for (const char of chars) {
    const testLine = line + char
    if (ctx.measureText(testLine).width > maxWidth) {
      ctx.fillText(line, x, currentY)
      line = char
      currentY += lineHeight
    } else {
      line = testLine
    }
  }
  ctx.fillText(line, x, currentY)
}

const downloadPoster = () => {
  const canvas = posterCanvas.value
  if (!canvas) return
  const link = document.createElement('a')
  link.download = `${strategy.value.title || '攻略'}-海报.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
  ElMessage.success('海报已下载')
}

const exportPdf = () => {
  // 使用浏览器打印功能导出PDF
  const printContent = document.querySelector('.content')
  if (!printContent) return

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.error('请允许弹出窗口以导出PDF')
    return
  }

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
    <head>
      <title>${strategy.value.title || '旅游攻略'}</title>
      <style>
        body { font-family: sans-serif; padding: 40px; max-width: 800px; margin: 0 auto; color: #333; }
        h1 { font-size: 28px; margin-bottom: 10px; }
        .meta { color: #666; margin-bottom: 20px; font-size: 14px; }
        .info { background: #f5f7fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
        .info span { margin-right: 20px; }
        .content { font-size: 16px; line-height: 1.8; }
        .content img { max-width: 100%; }
        @media print { body { padding: 20px; } }
      </style>
    </head>
    <body>
      <h1>${strategy.value.title || ''}</h1>
      <div class="meta">
        作者：${strategy.value.authorName || ''} | ${formatTime(strategy.value.createTime)}
      </div>
      <div class="info">
        <span>📍 ${strategy.value.destination || ''}</span>
        ${strategy.value.days ? `<span>🗓 ${strategy.value.days}天</span>` : ''}
        ${strategy.value.budget ? `<span>💰 ¥${strategy.value.budget}/人</span>` : ''}
        ${strategy.value.season ? `<span>🌤 ${seasonLabel(strategy.value.season)}</span>` : ''}
      </div>
      ${strategy.value.summary ? `<p style="color:#666;font-style:italic;">${strategy.value.summary}</p>` : ''}
      <div class="content">${strategy.value.content || ''}</div>
    </body>
    </html>
  `)
  printWindow.document.close()
  printWindow.onload = () => {
    printWindow.print()
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.strategy-detail-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  transition: all 0.3s;
}

.page-back-btn {
  margin-bottom: 20px;
}

.strategy-detail-page.immersive-mode {
  max-width: 900px;
}

.strategy-detail-page.immersive-mode .content {
  font-size: 18px;
  line-height: 2;
}

.cover-image {
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 20px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header h1 {
  font-size: 32px;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.meta {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.author-info {
  flex: 1;
}

.author-name {
  font-weight: 500;
  font-size: 16px;
}

.publish-time {
  color: #999;
  font-size: 14px;
  margin-top: 4px;
}

.stats {
  display: flex;
  gap: 20px;
  color: #666;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.tags {
  margin-bottom: 20px;
}

.tags .el-tag {
  margin-right: 10px;
}

.trip-info {
  margin-bottom: 20px;
}

.budget-value {
  color: #e6a23c;
  font-weight: 600;
  font-size: 16px;
}

.summary-box {
  margin-bottom: 20px;
}

.reading-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 30px;
  min-height: 300px;
}

.content :deep(h1),
.content :deep(h2),
.content :deep(h3),
.content :deep(h4),
.content :deep(h5),
.content :deep(h6) {
  margin: 30px 0 15px 0;
  font-weight: 600;
  line-height: 1.4;
}

.content :deep(h1) { font-size: 28px; }
.content :deep(h2) { font-size: 24px; }
.content :deep(h3) { font-size: 20px; }

.content :deep(p) {
  margin: 15px 0;
}

.content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 20px 0;
}

.content :deep(ul),
.content :deep(ol) {
  padding-left: 30px;
  margin: 15px 0;
}

.content :deep(li) {
  margin: 8px 0;
}

.content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 15px;
  margin: 20px 0;
  color: #666;
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.actions {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-top: 1px solid #eee;
}

.toc-card {
  position: sticky;
  top: 20px;
  margin-bottom: 20px;
}

.toc {
  max-height: 400px;
  overflow-y: auto;
}

.toc-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 2px solid transparent;
  font-size: 14px;
  color: #666;
}

.toc-item:hover {
  color: #409eff;
  background: #f5f7fa;
}

.toc-item.active {
  color: #409eff;
  border-left-color: #409eff;
  background: #ecf5ff;
}

.toc-level-1 { padding-left: 12px; font-weight: 500; }
.toc-level-2 { padding-left: 24px; }
.toc-level-3 { padding-left: 36px; font-size: 13px; }
.toc-level-4 { padding-left: 48px; font-size: 13px; }

.toc-empty {
  text-align: center;
  color: #999;
  padding: 20px;
}

/* 相关推荐 */
.related-card {
  margin-top: 20px;
}

.related-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.related-item:last-child {
  border-bottom: none;
}

.related-item:hover {
  background: #f5f7fa;
}

.related-cover {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
}

.related-info {
  flex: 1;
  min-width: 0;
}

.related-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.related-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

/* 海报 */
.poster-container {
  display: flex;
  justify-content: center;
}

.poster-container canvas {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
