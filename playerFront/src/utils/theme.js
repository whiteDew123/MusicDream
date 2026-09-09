// 主题状态管理（播放端 · 亮/暗两态 + 首次跟随系统）
// 双轨同步机制：
//   - html.dark        → Element Plus 官方暗色 css-vars（浮层 dialog/message 挂 body 也能变暗）
//   - data-theme 属性   → 由 Layout.vue 绑定在 .app-layout 根元素上，
//                        驱动自有 --st-* 变量（作用域隔离，登录/注册门面不被覆盖）
import { ref } from 'vue'

const THEME_KEY = 'md_theme'

// 模块级单例响应式：Layout 与 TopBar 共享同一主题状态
const theme = ref('light') // 'light' | 'dark'

// 应用主题：写入响应式状态 + html.dark（EP 组件）+ localStorage（持久化）
function applyTheme(value) {
  theme.value = value
  document.documentElement.classList.toggle('dark', value === 'dark')
  localStorage.setItem(THEME_KEY, value)
}

// 初始化（由 Layout onMounted 调用，仅登录后的主界面执行）：
// 有用户存储 → 用之；无存储 → 跟随系统 prefers-color-scheme
// index.html 的内联脚本会先同步 html.dark 防闪白，这里做同一逻辑的"权威落地"
function initTheme() {
  const stored = localStorage.getItem(THEME_KEY)
  const preferDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  applyTheme(stored || (preferDark ? 'dark' : 'light'))
}

// 切换（TopBar 按钮）：亮 ↔ 暗，互斥 toggle
function toggleTheme() {
  applyTheme(theme.value === 'dark' ? 'light' : 'dark')
}

// 组合式 API：Layout 初始化，TopBar 显示/切换
export function useTheme() {
  return { theme, initTheme, toggleTheme }
}
