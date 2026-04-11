<template>
  <div class="favorites-page">
    <!-- 返回按钮 -->
    <BackButton fallback="/index" class="page-back-btn" />

    <el-card>
      <template #header>
        <h2>我的收藏</h2>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="景点收藏"
          name="attraction"
        >
          <div
            v-if="attractions.length > 0"
            class="item-list"
          >
            <div
              v-for="item in attractions"
              :key="item.id"
              class="item-card"
              @click="goToAttraction(item.id)"
            >
              <el-image
                :src="item.images?.[0] || defaultImage"
                fit="cover"
              />
              <div class="item-info">
                <h4>{{ item.name }}</h4>
                <p>📍 {{ item.city }}</p>
                <p>⭐ {{ item.rating }}</p>
              </div>
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                @click.stop="removeFavorite(item.id, 'attraction')"
              />
            </div>
          </div>
          <el-empty
            v-else
            description="暂无收藏"
          />
        </el-tab-pane>

        <el-tab-pane
          label="攻略收藏"
          name="strategy"
        >
          <div
            v-if="strategies.length > 0"
            class="item-list"
          >
            <div
              v-for="item in strategies"
              :key="item.id"
              class="item-card"
              @click="goToStrategy(item.id)"
            >
              <el-image
                :src="item.coverImage || defaultImage"
                fit="cover"
              />
              <div class="item-info">
                <h4>{{ item.title }}</h4>
                <p>📍 {{ item.destination }}</p>
                <p>👍 {{ item.likeCount }}赞</p>
              </div>
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                @click.stop="removeFavorite(item.id, 'strategy')"
              />
            </div>
          </div>
          <el-empty
            v-else
            description="暂无收藏"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unfavoriteAttraction } from '@/api/attraction'
import { getFavorites } from '@/api/user'
import BackButton from '@/components/BackButton.vue'

const router = useRouter()
const defaultImage = 'https://via.placeholder.com/100x100?text=暂无图片'

const activeTab = ref('attraction')
const attractions = ref<any[]>([])
const strategies = ref<any[]>([])

const loadFavorites = async () => {
  try {
    const res = await getFavorites()
    // 根据 itemType 分类，提取嵌套的景点/攻略信息
    attractions.value = res
      .filter((item: any) => item.itemType === 'attraction' && item.attraction)
      .map((item: any) => ({ ...item.attraction, favoriteId: item.id }))
    strategies.value = res
      .filter((item: any) => item.itemType === 'strategy' && item.strategy)
      .map((item: any) => ({ ...item.strategy, favoriteId: item.id }))
  } catch (error) {
    console.error('加载失败', error)
  }
}

const goToAttraction = (id: number) => {
  router.push(`/attraction/${id}`)
}

const goToStrategy = (id: number) => {
  router.push(`/strategy/${id}`)
}

const removeFavorite = async (id: number, type: string) => {
  try {
    if (type === 'attraction') {
      await unfavoriteAttraction(String(id))
    }
    ElMessage.success('已取消收藏')
    // 重新加载
    loadFavorites()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-back-btn {
  margin-bottom: 20px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.item-card .el-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.item-info p {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}
</style>
