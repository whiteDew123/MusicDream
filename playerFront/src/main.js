import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// Element Plus 官方暗色变量（html.dark 时生效，由 utils/theme.js 同步开关）
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { useTheme } from './utils/theme'
import './styles/main.scss'

// 应用入口：注册 Pinia / Router / Element Plus（中文语言包）
const app = createApp(App)

// 全量注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 主题全局初始化：在任何页面（含独立沉浸页如播放室/全屏播放器）挂载前，
// 先确定亮/暗主题（存储值 or 跟随系统），保证 theme 响应式状态正确
const { initTheme } = useTheme()
initTheme()

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
