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
  DataAnalysis,
  Document
} from '@element-plus/icons-vue'

// 菜单配置
// - path: 路由路径（一级菜单用相对路径，如 'dashboard'）
// - title: 菜单标题
// - icon: Element Plus 图标组件
// - show: 可见性控制
//   - 'all': 所有角色可见
//   - 'admin': 仅管理员可见（role=0）
//   - 'singer': 仅歌手可见（role=1）
// - children: 子菜单数组
const navData = [
  {
    path: 'dashboard',
    title: '首页',
    icon: Odometer,
    show: 'all'
  },
  {
    path: 'user',
    title: '用户管理',
    icon: User,
    show: 'admin'
  },
  {
    path: 'music',
    title: '歌曲管理',
    icon: Headset,
    show: 'admin'
  },
  {
    path: 'log',
    title: '日志管理',
    icon: Document,
    show: 'admin'
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
    path: 'data',
    title: '数据统计',
    icon: DataAnalysis,
    show: 'admin'
  },
  {
    path: 'setting',
    title: '系统设置',
    icon: Setting,
    show: 'admin'
  }
]

export default navData