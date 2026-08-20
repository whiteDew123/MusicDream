import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

// Vite 配置
// - dev server 端口 5174（与后台 5173 错开）
// - /api 请求代理到网关 9000，避免跨域
// - 自动导入 Element Plus 组件与 API
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    open: true,
    proxy: {
      // 所有 /api/** 请求转发到网关，由网关路由到各微服务
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true
      }
    }
  }
})
