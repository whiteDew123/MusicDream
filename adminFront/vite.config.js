import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

// Vite 配置
// - dev server 端口 5173
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
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      // 静态资源（音乐/封面/歌词）：统一由 Mod_upload (8005) 从 musicBack/resource/ 提供
      // 兼容 /music /img /lyric（历史数据）
      '/music': { target: 'http://localhost:8005', changeOrigin: true },
      '/img': { target: 'http://localhost:8005', changeOrigin: true },
      '/lyric': { target: 'http://localhost:8005', changeOrigin: true }
    }
  }
})