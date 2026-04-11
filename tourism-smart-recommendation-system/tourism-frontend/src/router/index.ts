import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '找回密码', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('@/views/home/Index.vue'),
        meta: { title: '首页', requiresAuth: false }
      },
      {
        path: 'attraction',
        name: 'AttractionList',
        component: () => import('@/views/attraction/List.vue'),
        meta: { title: '景点列表', requiresAuth: false }
      },
      {
        path: 'attraction/:id',
        name: 'AttractionDetail',
        component: () => import('@/views/attraction/Detail.vue'),
        meta: { title: '景点详情', requiresAuth: false }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/Index.vue'),
        meta: { title: 'AI助手', requiresAuth: true }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/plan/Index.vue'),
        meta: { title: '行程规划', requiresAuth: true }
      },
      {
        path: 'strategy',
        name: 'StrategyList',
        component: () => import('@/views/strategy/List.vue'),
        meta: { title: '旅游攻略', requiresAuth: false }
      },
      {
        path: 'strategy/:id',
        name: 'StrategyDetail',
        component: () => import('@/views/strategy/Detail.vue'),
        meta: { title: '攻略详情', requiresAuth: false }
      },
      {
        path: 'strategy/create',
        name: 'StrategyCreate',
        component: () => import('@/views/strategy/Create.vue'),
        meta: { title: '创建攻略', requiresAuth: true }
      },
      {
        path: 'strategy/edit/:id',
        name: 'StrategyEdit',
        component: () => import('@/views/strategy/Create.vue'),
        meta: { title: '编辑攻略', requiresAuth: true }
      },
      {
        path: 'strategy/my',
        name: 'MyStrategy',
        component: () => import('@/views/strategy/MyStrategy.vue'),
        meta: { title: '我的攻略', requiresAuth: true }
      },
      {
        path: 'strategy/drafts',
        name: 'StrategyDrafts',
        component: () => import('@/views/strategy/Drafts.vue'),
        meta: { title: '我的草稿', requiresAuth: true }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人信息', requiresAuth: true }
      },
      {
        path: 'user/change-password',
        name: 'ChangePassword',
        component: () => import('@/views/user/ChangePassword.vue'),
        meta: { title: '修改密码', requiresAuth: true }
      },
      {
        path: 'user/favorites',
        name: 'UserFavorites',
        component: () => import('@/views/user/Favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'user/history',
        name: 'UserHistory',
        component: () => import('@/views/user/History.vue'),
        meta: { title: '行程记录', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { title: '管理后台', requiresAuth: true, roles: ['admin', 'content_admin'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('@/views/admin/content/Index.vue'),
        meta: { title: '攻略管理' }
      },
      {
        path: 'attraction',
        name: 'AdminAttraction',
        component: () => import('@/views/admin/attraction/Index.vue'),
        meta: { title: '景点管理' }
      },
      {
        path: 'attraction/analytics',
        name: 'AdminAttractionAnalytics',
        component: () => import('@/views/admin/attraction/Analytics.vue'),
        meta: { title: '景点数据分析' }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/user/Index.vue'),
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/log/Index.vue'),
        meta: { title: '操作日志', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/index'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // 需要登录但未登录时，停留在原页面并提示的路由
  const stayOnPageRoutes = ['/chat', '/plan', '/strategy']

  // 收集所有匹配路由中的角色要求（子路由优先）
  let roles: string[] | undefined
  for (let i = to.matched.length - 1; i >= 0; i--) {
    const matchedRoles = to.matched[i].meta.roles as string[] | undefined
    if (matchedRoles && matchedRoles.length > 0) {
      roles = matchedRoles
      break
    }
  }

  // 设置页面标题
  document.title = `${to.meta.title || '旅游智慧推荐系统'}`

  if (requiresAuth && !authStore.token) {
    // 需要登录但未登录
    if (stayOnPageRoutes.includes(to.path)) {
      // 这三个页面：停留在原页面，不跳转（由导航栏处理提示）
      next(false)
    } else {
      // 其他需要登录的页面：跳转到登录页
      next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  } else if (roles && roles.length > 0) {
    // 需要特定角色
    const userRole = authStore.userRole
    if (userRole && roles.includes(userRole)) {
      next()
    } else {
      next({ name: 'Index' })
    }
  } else {
    next()
  }
})

export default router
