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
      },
      // 静态资源（音乐/封面/歌词）：统一由 Mod_upload (8007) 从 musicBack/resource/ 提供
      // 兼容 /music /img /lyric（历史数据）与 /uploads（新上传）四种前缀
      '/music': { target: 'http://localhost:8007', changeOrigin: true },
      '/img': { target: 'http://localhost:8007', changeOrigin: true },
      '/lyric': { target: 'http://localhost:8007', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8007', changeOrigin: true }
    }
  }
})