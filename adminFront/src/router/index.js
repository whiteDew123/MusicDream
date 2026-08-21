import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'
import navData from '@/utils/navData'

// 动态组件映射：path → 组件
const componentMap = {
  dashboard: () => import('@/views/dashboard/Dashboard.vue'),
  user: () => import('@/views/user/UserManage.vue'),
  music: () => import('@/views/admin/AuditMusic.vue'),
  songManage: () => import('@/views/admin/SongManage.vue'),
  log: () => import('@/views/log/LogManage.vue'),
  publishSong: () => import('@/views/song/PublishSong.vue'),
  mySongs: () => import('@/views/song/MySongs.vue'),
  publish: () => import('@/views/msg/MsgPublish.vue'),
  notify: () => import('@/views/msg/MsgList.vue'),
  profile: () => import('@/views/Setting/components/profile.vue'),
  safeSetting: () => import('@/views/Setting/components/safeSetting.vue'),
  admin: () => import('@/views/placeholder/MenuContainer.vue'),
  msg: () => import('@/views/placeholder/MenuContainer.vue'),
  setting: () => import('@/views/Setting/index.vue')
}

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
      route.component = getComponent(item.path)
      route.redirect = `${item.path}/${item.children[0].path}`
      route.children = pushChildren(item.children)
    } else {
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

// 检查当前路由对于指定角色是否可见
function canAccessRoute(meta, role) {
  if (!meta) return true
  const show = meta.show
  if (!show || show === 'all') return true
  if (show === 'admin' && role === 0) return true
  if (show === 'singer' && role === 1) return true
  return false
}

// 全局前置守卫
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
  const role = userStore.userInfo?.role

  // 仅允许管理员 (role=0) 和歌手 (role=1) 登录后台
  if (role !== 0 && role !== 1) {
    return next({ path: '/login' })
  }

  // 检查路由级权限：如果路由对当前角色不可见，跳转到首页
  if (!canAccessRoute(to.meta, role)) {
    // 检查父路由
    if (to.matched.length > 1) {
      const parentMatched = to.matched.find((m, i) => i === to.matched.length - 2)
      if (parentMatched && !canAccessRoute(parentMatched.meta, role)) {
        return next('/dashboard')
      }
    } else {
      return next('/dashboard')
    }
  }

  next()
})

export default router
