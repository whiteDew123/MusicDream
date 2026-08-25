import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'

// 路由表
// - /login、/register：公开页面
// - 其余路由需登录且 role=0（管理员）或 role=1（歌手），统一使用 Layout 主布局
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
        component: () => import('@/views/dashboard/DashboardV2.vue'),
        meta: { title: '首页', icon: 'Odometer' }
      },
      {
        path: 'manage',
        name: 'Manage',
        meta: { title: '管理中心', icon: 'Operation' },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/manage/UserManage.vue'),
            meta: { title: '用户管理', icon: 'User', roles: [0] }
          },
          {
            path: 'music',
            name: 'MusicManage',
            component: () => import('@/views/manage/MusicManage.vue'),
            meta: { title: '歌曲管理', icon: 'Headset', roles: [0] }
          },
          {
            path: 'review',
            name: 'MusicReview',
            component: () => import('@/views/manage/MusicReview.vue'),
            meta: { title: '歌曲审核', icon: 'Checked', roles: [0] }
          },
          {
            path: 'upload',
            name: 'MusicUpload',
            component: () => import('@/views/manage/MusicUpload.vue'),
            meta: { title: '发布歌曲', icon: 'Upload', roles: [1] }
          },
          {
            path: 'my-songs',
            name: 'MySongs',
            component: () => import('@/views/manage/MusicManage.vue'),
            meta: { title: '我的歌曲', icon: 'Headset', roles: [1] }
          },
          {
            path: 'log',
            name: 'LogManage',
            component: () => import('@/views/manage/LogManage.vue'),
            meta: { title: '操作日志', icon: 'Document', roles: [0] }
          }
        ]
      },
      {
        path: 'msg',
        name: 'Message',
        meta: { title: '消息中心', icon: 'ChatDotRound' },
        children: [
          {
            path: 'publish',
            name: 'MsgPublish',
            component: () => import('@/views/msg/MsgPublish.vue'),
            meta: { title: '发布消息', icon: 'Promotion' }
          },
          {
            path: 'notify',
            name: 'MsgNotify',
            component: () => import('@/views/msg/MsgList.vue'),
            meta: { title: '消息通知', icon: 'Bell' }
          }
        ]
      },
      {
        path: 'setting',
        name: 'Setting',
        component: () => import('@/views/setting/Setting.vue'),
        meta: { title: '设置', icon: 'Setting' }
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
// - public 路由直接放行
// - 非 public：未登录跳 /login；已登录但非管理员/歌手拒绝进入
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
