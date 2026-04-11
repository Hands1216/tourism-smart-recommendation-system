<template>
  <div class="default-layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-content">
          <!-- Logo -->
          <div
            class="logo"
            @click="router.push('/index')"
            role="button"
            tabindex="0"
            aria-label="返回首页"
            @keydown.enter="router.push('/index')"
          >
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span class="logo-text">旅游智慧推荐</span>
          </div>

          <!-- 导航菜单 -->
          <nav
            class="nav-menu"
            role="navigation"
            aria-label="主导航"
          >
            <el-menu
              :default-active="activeMenu"
              mode="horizontal"
              :ellipsis="false"
            >
              <el-menu-item index="/index" @click="handleNavClick('/index')">
                首页
              </el-menu-item>
              <el-menu-item index="/attraction" @click="handleNavClick('/attraction')">
                景点
              </el-menu-item>
              <el-menu-item index="/chat" @click="handleNavClick('/chat')">
                AI助手
              </el-menu-item>
              <el-menu-item index="/plan" @click="handleNavClick('/plan')">
                行程规划
              </el-menu-item>
              <el-menu-item index="/strategy" @click="handleNavClick('/strategy')">
                旅游攻略
              </el-menu-item>
            </el-menu>
          </nav>

          <!-- 右侧操作区 -->
          <div class="header-actions">
            <!-- 主题切换按钮 -->
            <button
              class="theme-toggle"
              @click="themeStore.toggleTheme"
              :aria-label="themeStore.isDark ? '切换到亮色模式' : '切换到深色模式'"
              :title="themeStore.isDark ? '切换到亮色模式' : '切换到深色模式'"
            >
              <Transition name="scale" mode="out-in">
                <svg
                  v-if="themeStore.isDark"
                  key="sun"
                  class="theme-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <circle cx="12" cy="12" r="5" />
                  <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
                </svg>
                <svg
                  v-else
                  key="moon"
                  class="theme-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
                </svg>
              </Transition>
            </button>

            <!-- 用户区域 -->
            <div class="user-area">
              <template v-if="authStore.isLogin">
                <el-dropdown @command="handleCommand" trigger="click">
                  <button class="user-trigger" aria-label="用户菜单">
                    <el-avatar
                      :size="36"
                      :src="authStore.avatarUrl"
                      class="user-avatar"
                    >
                      {{ authStore.user?.nickname?.charAt(0) }}
                    </el-avatar>
                    <span class="user-name hidden-md">{{ authStore.user?.nickname }}</span>
                    <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M6 9l6 6 6-6" />
                    </svg>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                          <circle cx="12" cy="7" r="4" />
                        </svg>
                        个人信息
                      </el-dropdown-item>
                      <el-dropdown-item command="my-strategy">
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
                          <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                        </svg>
                        我的攻略
                      </el-dropdown-item>
                      <el-dropdown-item command="favorites">
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
                        </svg>
                        我的收藏
                      </el-dropdown-item>
                      <el-dropdown-item command="history">
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10" />
                          <polyline points="12 6 12 12 16 14" />
                        </svg>
                        行程记录
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="authStore.userRole === 'admin' || authStore.userRole === 'content_admin'"
                        divided
                        command="admin"
                      >
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 15a3 3 0 100-6 3 3 0 000 6z" />
                          <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z" />
                        </svg>
                        管理后台
                      </el-dropdown-item>
                      <el-dropdown-item
                        :divided="authStore.userRole !== 'admin' && authStore.userRole !== 'content_admin'"
                        command="logout"
                      >
                        <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
                          <polyline points="16 17 21 12 16 7" />
                          <line x1="21" y1="12" x2="9" y2="12" />
                        </svg>
                        退出登录
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template v-else>
                <el-button
                  type="primary"
                  class="login-btn"
                  @click="router.push('/login')"
                >
                  登录
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </el-main>

      <!-- 页脚 -->
      <el-footer class="footer">
        <div class="footer-content">
          <p>© 2025 旅游智慧推荐系统 | 作者：韩东升</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const activeMenu = computed(() => route.path)

// 需要登录才能访问的路由
const authRequiredRoutes = ['/chat', '/plan', '/strategy']

// 处理导航点击
const handleNavClick = (path: string) => {
  if (authRequiredRoutes.includes(path) && !authStore.isLogin) {
    ElMessage.warning('请先完成登录！')
    return
  }
  router.push(path)
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'my-strategy':
      router.push('/strategy/my')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'history':
      router.push('/user/history')
      break
    case 'admin':
      router.push('/admin/dashboard')
      break
    case 'logout':
      authStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.default-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// ========== 顶部导航栏 ==========
.header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--border-color);
  padding: 0;
  height: auto !important;
}

.header-content {
  display: flex;
  align-items: center;
  max-width: $container-xl;
  margin: 0 auto;
  padding: $space-3 $space-4;
  gap: $space-6;

  @media (min-width: $breakpoint-lg) {
    padding: $space-3 $space-6;
  }
}

// Logo
.logo {
  display: flex;
  align-items: center;
  gap: $space-2;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity var(--duration-fast) var(--ease-in-out);

  &:hover {
    opacity: 0.8;
  }

  &:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
    border-radius: var(--radius-md);
  }
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  color: white;

  svg {
    width: 20px;
    height: 20px;
  }
}

.logo-text {
  font-size: $text-lg;
  font-weight: $font-semibold;
  color: var(--text-primary);
  white-space: nowrap;

  @media (max-width: $breakpoint-md) {
    display: none;
  }
}

// 导航菜单
.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;

  :deep(.el-menu) {
    border-bottom: none;
    background: transparent;

    .el-menu-item {
      font-size: $text-sm;
      font-weight: $font-medium;
      color: var(--text-secondary);
      border-bottom: none;
      padding: $space-2 $space-4;
      margin: 0 $space-1;
      border-radius: var(--radius-md);
      transition: all var(--duration-fast) var(--ease-in-out);

      &:hover {
        background: var(--bg-tertiary);
        color: var(--text-primary);
      }

      &.is-active {
        background: rgba($brand-primary, 0.1);
        color: var(--color-primary);
      }
    }
  }

  @media (max-width: $breakpoint-md) {
    display: none;
  }
}

// 右侧操作区
.header-actions {
  display: flex;
  align-items: center;
  gap: $space-3;
}

// 主题切换按钮
.theme-toggle {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  transition: all var(--duration-fast) var(--ease-in-out);

  &:hover {
    background: var(--bg-secondary);
    color: var(--color-primary);
    transform: rotate(15deg);
  }

  &:active {
    transform: scale(0.95);
  }
}

.theme-icon {
  width: 20px;
  height: 20px;
}

// 用户区域
.user-area {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-1;
  border-radius: var(--radius-full);
  transition: background var(--duration-fast) var(--ease-in-out);

  &:hover {
    background: var(--bg-tertiary);
  }
}

.user-avatar {
  border: 2px solid var(--border-color);
}

.user-name {
  font-size: $text-sm;
  font-weight: $font-medium;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: var(--text-tertiary);
}

// 下拉菜单图标
.menu-icon {
  width: 16px;
  height: 16px;
  margin-right: $space-2;
  color: var(--text-tertiary);
}

// 登录按钮
.login-btn {
  border-radius: var(--radius-full);
  padding: $space-2 $space-5;
}

// ========== 主内容区 ==========
.main-content {
  flex: 1;
  max-width: $container-xl;
  width: 100%;
  margin: 0 auto;
  padding: $space-5 $space-4;
  min-height: calc(100vh - 140px);

  @media (min-width: $breakpoint-lg) {
    padding: $space-6;
  }
}

// ========== 页脚 ==========
.footer {
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  height: auto !important;
  padding: $space-4 0;
}

.footer-content {
  max-width: $container-xl;
  margin: 0 auto;
  padding: 0 $space-4;
  text-align: center;

  p {
    color: var(--text-tertiary);
    font-size: $text-sm;
  }
}

// ========== 过渡动画 ==========
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--duration-normal) var(--ease-in-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.scale-enter-active,
.scale-leave-active {
  transition: all var(--duration-fast) var(--ease-bounce);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.8) rotate(-90deg);
}

// ========== 响应式 ==========
@media (max-width: $breakpoint-md) {
  .hidden-md {
    display: none;
  }
}
</style>
