<template>
  <div class="home-page">
    <!-- Hero 搜索区域 -->
    <section class="hero-section" aria-label="搜索区域">
      <div class="hero-bg">
        <div class="hero-pattern"></div>
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">探索</span>你的下一个目的地
        </h1>
        <p class="hero-subtitle">发现精彩景点，规划完美旅程</p>
        <div class="search-wrapper">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索景点、城市..."
              size="large"
              class="search-input"
              aria-label="搜索景点"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21l-4.35-4.35" />
                </svg>
              </template>
            </el-input>
            <el-button
              type="primary"
              size="large"
              class="search-btn"
              @click="handleSearch"
            >
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门城市 -->
    <section class="section" aria-labelledby="hot-cities-title">
      <h2 id="hot-cities-title" class="section-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
          <circle cx="12" cy="10" r="3" />
        </svg>
        热门城市
      </h2>
      <div class="city-list" role="list">
        <button
          v-for="city in hotCities"
          :key="city"
          class="city-item"
          role="listitem"
          @click="goToCity(city)"
        >
          {{ city }}
        </button>
      </div>
    </section>

    <!-- 推荐景点 -->
    <section class="section" aria-labelledby="recommend-title">
      <h2 id="recommend-title" class="section-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
        推荐景点
      </h2>

      <!-- 骨架屏 -->
      <div v-if="loading" class="attraction-grid">
        <div
          v-for="i in 8"
          :key="i"
          class="skeleton-card"
        >
          <div class="skeleton-image"></div>
          <div class="skeleton-content">
            <div class="skeleton-title"></div>
            <div class="skeleton-text"></div>
            <div class="skeleton-text short"></div>
          </div>
        </div>
      </div>

      <!-- 景点列表 -->
      <TransitionGroup
        v-else
        name="list"
        tag="div"
        class="attraction-grid"
      >
        <article
          v-for="(item, index) in recommendAttractions"
          :key="item.id"
          class="attraction-card"
          :style="{ '--delay': `${index * 50}ms` }"
          @click="goToAttraction(item.id)"
        >
          <div class="card-image">
            <el-image
              :src="item.images?.[0] || defaultImage"
              :alt="`${item.name}景点图片`"
              fit="cover"
              lazy
            >
              <template #placeholder>
                <div class="image-placeholder">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                    <circle cx="8.5" cy="8.5" r="1.5" />
                    <polyline points="21 15 16 10 5 21" />
                  </svg>
                </div>
              </template>
            </el-image>
            <span class="card-rating">
              <svg viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
              {{ item.ratingCount > 0 && item.rating ? item.rating.toFixed(1) : '0.0' }}
            </span>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ item.name }}</h3>
            <p class="card-location">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                <circle cx="12" cy="10" r="3" />
              </svg>
              {{ item.city }}
            </p>
            <p class="card-price">
              <template v-if="item.ticketPrice > 0">
                ¥{{ item.ticketPrice }}
              </template>
              <template v-else>
                <span class="free-tag">免费</span>
              </template>
            </p>
          </div>
        </article>
      </TransitionGroup>
    </section>

    <!-- 快捷服务 -->
    <section class="section" aria-labelledby="services-title">
      <h2 id="services-title" class="section-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7" />
          <rect x="14" y="3" width="7" height="7" />
          <rect x="14" y="14" width="7" height="7" />
          <rect x="3" y="14" width="7" height="7" />
        </svg>
        快捷服务
      </h2>
      <div class="service-grid">
        <article
          v-for="(service, index) in services"
          :key="service.path"
          class="service-card"
          :style="{ '--delay': `${index * 100}ms` }"
          @click="router.push(service.path)"
        >
          <div class="service-icon" :class="service.color">
            <component :is="service.icon" />
          </div>
          <h3 class="service-title">{{ service.title }}</h3>
          <p class="service-desc">{{ service.desc }}</p>
          <div class="service-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { getAttractionList, getRecommendAttractions } from '@/api/attraction'

const router = useRouter()

const searchKeyword = ref('')
const loading = ref(true)
const hotCities = ['北京', '上海', '杭州', '成都', '西安', '厦门', '三亚', '丽江']
const defaultImage = 'https://via.placeholder.com/300x200?text=景点'

const recommendAttractions = ref<any[]>([])

// 服务卡片配置
const services = [
  {
    path: '/attraction',
    title: '景点浏览',
    desc: '发现更多精彩景点',
    color: 'primary',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z' }),
      h('circle', { cx: '12', cy: '10', r: '3' })
    ])
  },
  {
    path: '/chat',
    title: 'AI助手',
    desc: '24小时智能旅行顾问',
    color: 'accent',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M12 2L2 7l10 5 10-5-10-5z' }),
      h('path', { d: 'M2 17l10 5 10-5' }),
      h('path', { d: 'M2 12l10 5 10-5' })
    ])
  },
  {
    path: '/plan',
    title: '行程规划',
    desc: '智能生成旅行计划',
    color: 'secondary',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('rect', { x: '3', y: '4', width: '18', height: '18', rx: '2', ry: '2' }),
      h('line', { x1: '16', y1: '2', x2: '16', y2: '6' }),
      h('line', { x1: '8', y1: '2', x2: '8', y2: '6' }),
      h('line', { x1: '3', y1: '10', x2: '21', y2: '10' })
    ])
  },
  {
    path: '/strategy',
    title: '旅游攻略',
    desc: '精选攻略供您参考',
    color: 'success',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z' }),
      h('polyline', { points: '14 2 14 8 20 8' }),
      h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
      h('line', { x1: '16', y1: '17', x2: '8', y2: '17' }),
      h('polyline', { points: '10 9 9 9 8 9' })
    ])
  },
  {
    path: '/user/favorites',
    title: '我的收藏',
    desc: '查看收藏的景点',
    color: 'danger',
    icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
      h('path', { d: 'M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z' })
    ])
  }
]

const handleSearch = () => {
  router.push({ path: '/attraction', query: { keyword: searchKeyword.value } })
}

const goToCity = (city: string) => {
  router.push({ path: '/attraction', query: { city } })
}

const goToAttraction = (id: number) => {
  router.push(`/attraction/${id}`)
}

const loadRecommend = async () => {
  loading.value = true
  try {
    const res = await getRecommendAttractions(8)
    recommendAttractions.value = res || []
  } catch (error) {
    console.error('加载推荐失败', error)
    try {
      const fallback = await getAttractionList({ page: 1, size: 8 })
      recommendAttractions.value = fallback.records || []
    } catch (e) {
      console.error('加载景点列表也失败', e)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecommend()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.home-page {
  padding-bottom: $space-10;
}

// ========== Hero 区域 ==========
.hero-section {
  position: relative;
  padding: $space-16 $space-4;
  margin: calc(-1 * $space-5) calc(-1 * $space-4) $space-10;
  overflow: hidden;

  @media (min-width: $breakpoint-lg) {
    padding: $space-20 $space-6;
    margin: calc(-1 * $space-6) calc(-1 * $space-6) $space-12;
  }
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: var(--gradient-primary);
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.08'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.5;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-size: $text-3xl;
  font-weight: $font-bold;
  color: white;
  margin-bottom: $space-3;
  line-height: $leading-tight;

  @media (min-width: $breakpoint-md) {
    font-size: $text-4xl;
  }

  .gradient-text {
    background: linear-gradient(90deg, #FFFFFF, #E0E7FF);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-subtitle {
  font-size: $text-base;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: $space-8;

  @media (min-width: $breakpoint-md) {
    font-size: $text-lg;
  }
}

.search-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  gap: $space-3;
  align-items: stretch;
}

.search-input {
  flex: 1;

  :deep(.el-input__wrapper) {
    padding: $space-2 $space-4;
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    background: var(--bg-primary);

    &:hover,
    &.is-focus {
      box-shadow: var(--shadow-xl), 0 0 0 3px rgba(255, 255, 255, 0.2);
    }
  }
}

.search-icon {
  width: 20px;
  height: 20px;
  color: var(--text-tertiary);
}

.search-btn {
  border-radius: var(--radius-xl);
  padding: $space-2 $space-8;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border: none;
  font-weight: $font-semibold;
  font-size: $text-base;
  letter-spacing: 1px;
  box-shadow: 0 4px 15px rgba(238, 90, 36, 0.4);
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;

  &:hover {
    background: linear-gradient(135deg, #ff5252 0%, #d63031 100%);
    box-shadow: 0 6px 20px rgba(238, 90, 36, 0.5);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 10px rgba(238, 90, 36, 0.4);
  }
}

// ========== 通用区块 ==========
.section {
  margin-bottom: $space-10;
}

.section-title {
  display: flex;
  align-items: center;
  gap: $space-2;
  font-size: $text-xl;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin-bottom: $space-5;
  padding-left: $space-3;
  border-left: 3px solid var(--color-primary);

  @media (min-width: $breakpoint-md) {
    font-size: $text-2xl;
  }
}

.title-icon {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

// ========== 热门城市 ==========
.city-list {
  display: flex;
  flex-wrap: wrap;
  gap: $space-3;
}

.city-item {
  padding: $space-2 $space-5;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: $text-sm;
  font-weight: $font-medium;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);

  &:hover {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &:active {
    transform: translateY(0);
  }
}

// ========== 景点卡片网格 ==========
.attraction-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: $space-5;

  @media (min-width: $breakpoint-sm) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: $breakpoint-md) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (min-width: $breakpoint-lg) {
    grid-template-columns: repeat(4, 1fr);
  }
}

// 骨架屏
.skeleton-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.skeleton-image {
  height: 180px;
  background: linear-gradient(
    90deg,
    var(--bg-tertiary) 25%,
    var(--bg-secondary) 50%,
    var(--bg-tertiary) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: $space-4;
}

.skeleton-title {
  height: 20px;
  width: 70%;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  margin-bottom: $space-3;
  animation: shimmer 1.5s infinite;
  background-size: 200% 100%;
}

.skeleton-text {
  height: 14px;
  width: 100%;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  margin-bottom: $space-2;
  animation: shimmer 1.5s infinite;
  background-size: 200% 100%;

  &.short {
    width: 50%;
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 景点卡片
.attraction-card {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: transform var(--duration-normal) var(--ease-out),
              box-shadow var(--duration-normal) var(--ease-out);
  animation: fadeInUp var(--duration-normal) var(--ease-out) backwards;
  animation-delay: var(--delay, 0ms);

  &:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: var(--shadow-lg);

    .card-image :deep(.el-image__inner) {
      transform: scale(1.08);
    }
  }

  &:active {
    transform: translateY(-2px) scale(0.99);
  }
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;

  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }

  :deep(.el-image__inner) {
    transition: transform var(--duration-slow) var(--ease-out);
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-tertiary);

  svg {
    width: 48px;
    height: 48px;
  }
}

.card-rating {
  position: absolute;
  top: $space-3;
  right: $space-3;
  display: flex;
  align-items: center;
  gap: $space-1;
  padding: $space-1 $space-2;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  color: white;
  border-radius: var(--radius-md);
  font-size: $text-xs;
  font-weight: $font-medium;

  svg {
    width: 12px;
    height: 12px;
    color: #FFD700;
  }
}

.card-content {
  padding: $space-4;
}

.card-title {
  font-size: $text-base;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin-bottom: $space-2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-location {
  display: flex;
  align-items: center;
  gap: $space-1;
  color: var(--text-secondary);
  font-size: $text-sm;
  margin-bottom: $space-2;

  svg {
    width: 14px;
    height: 14px;
    flex-shrink: 0;
  }
}

.card-price {
  font-size: $text-base;
  font-weight: $font-semibold;
  color: var(--color-danger);
}

.free-tag {
  display: inline-block;
  padding: $space-1 $space-2;
  background: rgba($color-success, 0.1);
  color: var(--color-success);
  border-radius: var(--radius-sm);
  font-size: $text-xs;
}

// ========== 服务卡片 ==========
.service-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: $space-4;

  @media (min-width: $breakpoint-sm) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: $breakpoint-md) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (min-width: $breakpoint-lg) {
    grid-template-columns: repeat(5, 1fr);
  }
}

.service-card {
  position: relative;
  background: var(--bg-primary);
  padding: $space-6 $space-4;
  text-align: center;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
  animation: fadeInUp var(--duration-normal) var(--ease-out) backwards;
  animation-delay: var(--delay, 0ms);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: var(--gradient-primary);
    transform: scaleX(0);
    transition: transform var(--duration-normal) var(--ease-out);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);

    &::before {
      transform: scaleX(1);
    }

    .service-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &:active {
    transform: translateY(-2px);
  }
}

.service-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto $space-4;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  transition: transform var(--duration-normal) var(--ease-bounce);

  svg {
    width: 28px;
    height: 28px;
  }

  &.primary {
    background: rgba($brand-primary, 0.1);
    color: var(--color-primary);
  }

  &.secondary {
    background: rgba($brand-secondary, 0.1);
    color: $brand-secondary;
  }

  &.accent {
    background: rgba($brand-accent, 0.1);
    color: $brand-accent;
  }

  &.success {
    background: rgba($color-success, 0.1);
    color: var(--color-success);
  }

  &.danger {
    background: rgba($color-danger, 0.1);
    color: var(--color-danger);
  }

  .service-card:hover & {
    transform: scale(1.1);
  }
}

.service-title {
  font-size: $text-base;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin-bottom: $space-2;
}

.service-desc {
  font-size: $text-sm;
  color: var(--text-secondary);
}

.service-arrow {
  position: absolute;
  right: $space-4;
  top: 50%;
  transform: translateX(10px) translateY(-50%);
  opacity: 0;
  transition: all var(--duration-normal) var(--ease-out);
  color: var(--color-primary);

  svg {
    width: 20px;
    height: 20px;
  }
}

// ========== 列表动画 ==========
.list-enter-active,
.list-leave-active {
  transition: all var(--duration-normal) var(--ease-out);
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.list-move {
  transition: transform var(--duration-normal) var(--ease-out);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
