import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'
import navData from '@/utils/navData'

// 动态组件映射：path → 组件
const componentMap = {
  dashboard: () => import('@/views/dashboard/Dashboard.vue'),
  user: () => import('@/views/user/UserManage.vue'),
  music: () => import('@/views/music/MusicManage.vue'),
  log: () => import('@/views/log/LogManage.vue'),
  publish: () => import('@/views/msg/MsgPublish.vue'),
  notify: () => import('@/views/msg/MsgList.vue'),
  data: () => import('@/views/placeholder/Placeholder.vue'),
  setting: () => import('@/views/setting/Setting.vue')
}

// 为没有独立组件的菜单复用 Placeholder
const placeholder = () => import('@/views/placeholder/Placeholder.vue')

function getComponent(path) {
  return componentMap[path] || placeholder
}

// 从 navData 生成路由表
function generateMenuRoutes() {
  const menu = []

  navData.forEach((item) => {
    const route = {
      path: item.path,
      meta: { title: item.title, icon: item.icon, show: item.show }
    }

    if (item.children && item.children.length) {
      // 有子菜单：不设 component，使用 pushChildren 生成嵌套路由
      route.children = pushChildren(item.children)
    } else {
      // 无子菜单：直接关联组件
      route.component = getComponent(item.path)
    }

    menu.push(route)
  })

  return menu
}

function pushChildren(children) {
  const childRoutes = []
  children.forEach((child) => {
    childRoutes.push({
      path: child.path,
      meta: { title: child.title, icon: child.icon, show: child.show },
      component: getComponent(child.path)
    })
  })
  return childRoutes
}

// 静态路由（公开页面）
const publicRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: generateMenuRoutes()
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/placeholder/NotFound.vue'),
    meta: { title: '404', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: publicRoutes
})

// 全局前置守卫
// - public 路由直接放行
// - 非 public：未登录跳 /login；已登录但非管理员拒绝进入
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - MusicDreamer 后台` : 'MusicDreamer 后台'

  if (to.meta.public) {
    return next()
  }

  const token = getToken()
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  const userStore = useUserStore()
  if (!userStore.isAdmin()) {
    return next({ path: '/login' })
  }

  next()
})

export default router