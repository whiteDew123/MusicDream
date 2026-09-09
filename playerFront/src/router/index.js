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
    path: '/player',
    name: 'SwipePlayer',
    component: () => import('@/views/player/SwipePlayer.vue'),
    meta: { title: '播放器' }
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
        path: 'recognize',
        name: 'Recognize',
        component: () => import('@/views/recognize/RecognizePage.vue'),
        meta: { title: '听歌识曲', icon: 'Microphone' }
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
        // 时空胶囊广场（公开）
        path: 'capsule/plaza',
        name: 'CapsulePlaza',
        component: () => import('@/views/capsule/CapsulePlaza.vue'),
        meta: { title: '时空胶囊', icon: 'MagicStick', public: true }
      },
      {
        // 胶囊详情
        path: 'capsule/:id',
        name: 'CapsuleDetail',
        component: () => import('@/views/capsule/CapsuleDetail.vue'),
        meta: { title: '胶囊详情', public: true }
      },
      {
        // 歌单详情页
        path: 'songlist/:id',
        name: 'SongListDetail',
        component: () => import('@/views/songlist/SongListDetail.vue'),
        meta: { title: '歌单详情' }
      },
      {
        // 一起听·播放室 房间列表
        path: 'rooms',
        name: 'RoomList',
        component: () => import('@/views/room/RoomListPage.vue'),
        meta: { title: '播放室', icon: 'Headset' }
      },
      {
        // 好友（重定向到发现页，实际通过 TopBar 抽屉交互）
        path: 'friend',
        redirect: '/discover',
        meta: { title: '好友', icon: 'User' }
      },
      {
        // 盲盒广场
        path: 'Musicbox/plaza',
        name: 'MusicBoxPlaza',
        component: () => import('@/views/musicbox/BoxPlaza.vue'),
        meta: { title: '盲盒广场', icon: 'Present' }
      },
      {
        // 创建盲盒
        path: 'Musicbox/create',
        name: 'CreateMusicBox',
        component: () => import('@/views/musicbox/CreateBox.vue'),
        meta: { title: '创建盲盒' }
      },
      {
        // 盲盒详情
        path: 'Musicbox/:id',
        name: 'BoxDetail',
        component: () => import('@/views/musicbox/BoxDetail.vue'),
        meta: { title: '盲盒详情' }
      },
      {
        // 我的盲盒
        path: 'Musicbox/my',
        name: 'MyMusicBox',
        component: () => import('@/views/musicbox/MyBox.vue'),
        meta: { title: '我的盲盒' }
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
            path: 'capsules',
            name: 'MyCapsules',
            component: () => import('@/views/capsule/MyCapsules.vue'),
            meta: { title: '我的胶囊', icon: 'MagicStick' }
          },
          {
            path: 'capsule/create',
            name: 'CreateCapsule',
            component: () => import('@/views/capsule/CreateCapsule.vue'),
            meta: { title: '创建胶囊' }
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
    // 播放室主界面（沉浸式全屏，独立于主布局；需登录）
    path: '/room/:id',
    name: 'RoomDetail',
    component: () => import('@/views/room/RoomDetail.vue'),
    meta: { title: '播放室' }
  },
  {
    // 邀请落地页（公开，未登录也可查看房间信息）
    path: '/invite/:code',
    name: 'InviteLanding',
    component: () => import('@/views/room/InviteLanding.vue'),
    meta: { title: '邀请加入', public: true }
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