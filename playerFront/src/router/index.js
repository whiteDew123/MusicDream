import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

// 路由表
// - /login、/register：公开页面
// - 其余路由需登录，统一使用 Layout 主布局
// - 参考 QQ音乐/网易云音乐：推荐、歌手、排行、歌单、我的（子项）
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
    redirect: '/discover',
    children: [
      {
        path: 'discover',
        name: 'Discover',
        component: () => import('@/views/home/Discover.vue'),
        meta: { title: '推荐', icon: 'House' }
      },
      {
        path: 'singer',
        name: 'Singer',
        component: () => import('@/views/singer/SingerPage.vue'),
        meta: { title: '歌手', icon: 'Microphone' }
      },
      {
        // 歌手详情页
        path: 'singer/:id',
        name: 'SingerDetail',
        component: () => import('@/views/singer/SingerDetail.vue'),
        meta: { title: '歌手详情' }
      },
      {
        path: 'rank',
        name: 'Rank',
        component: () => import('@/views/rank/Rank.vue'),
        meta: { title: '排行', icon: 'TrophyBase' }
      },
      {
        path: 'songlist',
        name: 'SongList',
        component: () => import('@/views/songlist/SongListPage.vue'),
        meta: { title: '歌单', icon: 'Files' }
      },
      {
        // 歌单详情页
        path: 'songlist/:id',
        name: 'SongListDetail',
        component: () => import('@/views/songlist/SongListDetail.vue'),
        meta: { title: '歌单详情' }
      },
      {
        // 我的（分组，redirect 到第一个子项，无 component）
        path: 'my',
        redirect: '/my/liked',
        children: [
          {
            path: 'liked',
            name: 'MyLiked',
            component: () => import('@/views/my/Liked.vue'),
            meta: { title: '我喜欢的音乐', icon: 'Star' }
          },
          {
            path: 'favorite',
            name: 'MyFavorite',
            component: () => import('@/views/my/Favorite.vue'),
            meta: { title: '收藏歌单', icon: 'Collection' }
          },
          {
            path: 'created',
            name: 'MyCreated',
            component: () => import('@/views/my/Created.vue'),
            meta: { title: '创建歌单', icon: 'FolderAdd' }
          },
          {
            path: 'notify',
            name: 'MyNotify',
            component: () => import('@/views/my/Notify.vue'),
            meta: { title: '消息通知', icon: 'Bell' }
          },
          {
            path: 'settings',
            name: 'MySettings',
            component: () => import('@/views/my/Settings.vue'),
            meta: { title: '设置', icon: 'Setting' }
          }
        ]
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/search/Search.vue'),
        meta: { title: '搜索', public: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/NotFound.vue'),
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
// - 非 public：未登录跳 /login
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - MusicDreamer` : 'MusicDreamer'
  if (to.meta.public) {
    return next()
  }
  const token = getToken()
  if (!token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
