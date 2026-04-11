<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: isCollapsed }">
      <!-- Logo -->
      <div class="sidebar-header">
        <div class="logo" @click="router.push('/admin/dashboard')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 15a3 3 0 100-6 3 3 0 000 6z" />
              <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z" />
            </svg>
          </div>
          <span v-show="!isCollapsed" class="logo-text">管理后台</span>
        </div>
        <button
          class="collapse-btn"
          @click="isCollapsed = !isCollapsed"
          :aria-label="isCollapsed ? '展开侧边栏' : '收起侧边栏'"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline :points="isCollapsed ? '9 18 15 12 9 6' : '15 18 9 12 15 6'" />
          </svg>
        </button>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav" role="navigation" aria-label="管理导航">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
          @click="router.push(item.path)"
          role="menuitem"
          :aria-current="activeMenu === item.path ? 'page' : undefined"
        >
          <component :is="item.icon" class="nav-icon" />
          <span v-show="!isCollapsed" class="nav-text">{{ item.title }}</span>
          <span v-if="!isCollapsed && item.badge" class="nav-badge">{{ item.badge }}</span>
        </div>
      </nav>

      <!-- 底部信息 -->
      <div class="sidebar-footer">
        <button
          class="back-btn"
          @click="goToFrontend"
          :title="isCollapsed ? '返回前台' : ''"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          <span v-show="!isCollapsed">返回前台</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <!-- 主题切换 -->
          <button
            class="header-btn"
            @click="themeStore.toggleTheme"
            :aria-label="themeStore.isDark ? '切换到亮色模式' : '切换到深色模式'"
          >
            <svg v-if="themeStore.isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5" />
              <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
            </svg>
          </button>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <button class="user-btn">
              <el-avatar :size="32" :src="authStore.avatarUrl" class="user-avatar">
                {{ authStore.user?.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="user-name">{{ roleLabel }}</span>
              <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9l6 6 6-6" />
              </svg>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="frontend">
                  <svg class="menu-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                  </svg>
                  返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
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
        </div>
      </header>

      <!-- 内容区 -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const isCollapsed = ref(false)
const activeMenu = computed(() => route.path)

// 是否为系统管理员
const isAdmin = computed(() => authStore.userRole === 'admin')

// 角色标签
const roleLabel = computed(() => {
  if (authStore.userRole === 'admin') {
    return '系统管理员'
  } else if (authStore.userRole === 'content_admin') {
    return '内容管理员'
  }
  return '管理员'
})

// 当前页面标题
const currentPageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/content': '攻略管理',
    '/admin/attraction': '景点管理',
    '/admin/user': '用户管理',
    '/admin/log': '操作日志'
  }
  return titles[route.path] || '管理后台'
})

// 菜单项
const menuItems = computed(() => {
  const items = [
    {
      path: '/admin/dashboard',
      title: '仪表盘',
      icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
        h('rect', { x: '3', y: '3', width: '7', height: '9' }),
        h('rect', { x: '14', y: '3', width: '7', height: '5' }),
        h('rect', { x: '14', y: '12', width: '7', height: '9' }),
        h('rect', { x: '3', y: '16', width: '7', height: '5' })
      ])
    },
    {
      path: '/admin/content',
      title: '攻略管理',
      icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
        h('path', { d: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z' }),
        h('polyline', { points: '14 2 14 8 20 8' }),
        h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
        h('line', { x1: '16', y1: '17', x2: '8', y2: '17' })
      ])
    },
    {
      path: '/admin/attraction',
      title: '景点管理',
      icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
        h('path', { d: 'M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z' }),
        h('circle', { cx: '12', cy: '10', r: '3' })
      ])
    }
  ]

  if (isAdmin.value) {
    items.push(
      {
        path: '/admin/user',
        title: '用户管理',
        icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
          h('path', { d: 'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2' }),
          h('circle', { cx: '9', cy: '7', r: '4' }),
          h('path', { d: 'M23 21v-2a4 4 0 00-3-3.87' }),
          h('path', { d: 'M16 3.13a4 4 0 010 7.75' })
        ])
      },
      {
        path: '/admin/log',
        title: '操作日志',
        icon: () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2' }, [
          h('path', { d: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z' }),
          h('polyline', { points: '14 2 14 8 20 8' }),
          h('line', { x1: '12', y1: '18', x2: '12', y2: '12' }),
          h('line', { x1: '9', y1: '15', x2: '15', y2: '15' })
        ])
      }
    )
  }

  return items
})

// 返回前台
const goToFrontend = () => {
  router.push('/index')
}

// 处理下拉命令
const handleCommand = (command: string) => {
  if (command === 'frontend') {
    goToFrontend()
  } else if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/design-tokens' as *;

.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-secondary);
}

// ========== 侧边栏 ==========
.admin-sidebar {
  width: 240px;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  transition: width var(--duration-normal) var(--ease-out);

  &.collapsed {
    width: 72px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-4;
  border-bottom: 1px solid var(--border-color);
}

.logo {
  display: flex;
  align-items: center;
  gap: $space-3;
  cursor: pointer;
  overflow: hidden;
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  color: white;
  flex-shrink: 0;

  svg {
    width: 22px;
    height: 22px;
  }
}

.logo-text {
  font-size: $text-base;
  font-weight: $font-semibold;
  color: var(--text-primary);
  white-space: nowrap;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: var(--text-tertiary);
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--bg-tertiary);
    color: var(--text-primary);
  }
}

// 导航菜单
.sidebar-nav {
  flex: 1;
  padding: $space-3;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3;
  margin-bottom: $space-1;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  position: relative;

  &:hover {
    background: var(--bg-tertiary);
    color: var(--text-primary);
  }

  &.active {
    background: rgba($brand-primary, 0.1);
    color: var(--color-primary);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 60%;
      background: var(--color-primary);
      border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    }
  }
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-text {
  font-size: $text-sm;
  font-weight: $font-medium;
  white-space: nowrap;
}

.nav-badge {
  margin-left: auto;
  padding: 2px 8px;
  background: var(--color-danger);
  color: white;
  font-size: $text-xs;
  border-radius: var(--radius-full);
}

// 底部
.sidebar-footer {
  padding: $space-3;
  border-top: 1px solid var(--border-color);
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-2;
  width: 100%;
  padding: $space-3;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: $text-sm;
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--bg-tertiary);
    color: var(--color-primary);
  }
}

// ========== 主内容区 ==========
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部栏
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-4 $space-6;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.page-title {
  font-size: $text-xl;
  font-weight: $font-semibold;
  color: var(--text-primary);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.header-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  transition: all var(--duration-fast) var(--ease-in-out);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: var(--bg-secondary);
    color: var(--color-primary);
  }
}

.user-btn {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-1 $space-3 $space-1 $space-1;
  border-radius: var(--radius-full);
  background: var(--bg-tertiary);
  transition: all var(--duration-fast) var(--ease-in-out);

  &:hover {
    background: var(--bg-secondary);
  }
}

.user-avatar {
  background: var(--gradient-primary);
  color: white;
  font-size: $text-sm;
  font-weight: $font-semibold;
}

.user-name {
  font-size: $text-sm;
  font-weight: $font-medium;
  color: var(--text-primary);
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: var(--text-tertiary);
}

.menu-icon {
  width: 16px;
  height: 16px;
  margin-right: $space-2;
  color: var(--text-tertiary);
}

// 内容区
.admin-content {
  flex: 1;
  padding: $space-6;
  overflow-y: auto;
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
</style>
