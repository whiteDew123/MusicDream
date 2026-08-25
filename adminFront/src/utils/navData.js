import {
  Odometer,
  User,
  Headset,
  Upload,
  Operation,
  ChatDotRound,
  Promotion,
  Bell,
  Setting,
  FolderOpened,
  Document,
  Edit,
  UserFilled,
  WarningFilled,
  DocumentAdd,
  Collection,
  Tickets
} from '@element-plus/icons-vue'

// 菜单配置
// - path: 路由路径（一级菜单用相对路径，如 'dashboard'）
// - title: 菜单标题
// - icon: Element Plus 图标组件
// - show: 可见性控制
//   - 'all': 所有角色可见
//   - 'admin': 仅管理员可见（role=0）
//   - 'singer': 仅歌手可见（role=1）
//   - 'user': 仅普通用户可见（role=2）
// - children: 子菜单数组
const navData = [
  {
    path: 'dashboard',
    title: '首页',
    icon: Odometer,
    show: 'all'
  },
  {
    path: 'admin',
    title: '管理中心',
    icon: Operation,
    show: 'all',
    children: [
      {
        path: 'publishSong',
        title: '发布歌曲',
        icon: DocumentAdd,
        show: 'singer'
      },
      {
        path: 'mySongs',
        title: '我的歌曲',
        icon: Collection,
        show: 'singer'
      },
      {
        path: 'user',
        title: '用户管理',
        icon: User,
        show: 'admin'
      },
      {
        path: 'music',
        title: '歌曲审核',
        icon: Headset,
        show: 'admin'
      },
      {
        path: 'songManage',
        title: '歌曲管理',
        icon: Tickets,
        show: 'admin'
      },
      {
        path: 'log',
        title: '日志管理',
        icon: Document,
        show: 'admin'
      }
    ]
  },
  {
    path: 'msg',
    title: '消息中心',
    icon: ChatDotRound,
    show: 'admin',
    children: [
      {
        path: 'publish',
        title: '发布消息',
        icon: Promotion,
        show: 'admin'
      },
      {
        path: 'notify',
        title: '消息通知',
        icon: Bell,
        show: 'admin'
      }
    ]
  },
  {
    path: 'setting',
    title: '设置中心',
    icon: Setting,
    show: 'all',
    children: [
      {
        path: 'profile',
        title: '个人设置',
        icon: UserFilled,
        show: 'all'
      },
      {
        path: 'safeSetting',
        title: '安全设置',
        icon: WarningFilled,
        show: 'all'
      }
    ]
  }
]

export default navData