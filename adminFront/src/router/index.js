import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'

// 路由表
// - /login、/register：公开页面
// - 其余路由需登录且 role=0（管理员），统一使用 Layout 主布局
const routes = [
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
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'Odometer' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/placeholder/Placeholder.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'music',
        name: 'MusicManage',
        component: () => import('@/views/placeholder/Placeholder.vue'),
        meta: { title: '音乐管理', icon: 'Headset' }
      },
      {
        path: 'singer',
        name: 'SingerManage',
        component: () => import('@/views/placeholder/Placeholder.vue'),
        meta: { title: '歌手管理', icon: 'Microphone' }
      },
      {
        path: 'songList',
        name: 'SongListManage',
        component: () => import('@/views/placeholder/Placeholder.vue'),
        meta: { title: '歌单管理', icon: 'Files' }
      },
      {
        path: 'msg',
        name: 'MsgManage',
        component: () => import('@/views/placeholder/Placeholder.vue'),
        meta: { title: '消息管理', icon: 'ChatDotRound' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/placeholder/NotFound.vue'),
    meta: { title: '404', public: true }
  }
]

export { routes }

const router = createRouter({
  history: createWebHistory(),
  routes
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
