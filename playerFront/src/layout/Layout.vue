<template>
  <!-- data-theme 绑定在根元素上：暗色 token 仅作用于登录后的主界面，
       登录/注册门面（无 app-layout）保持亮色 -->
  <div class="app-layout" :data-theme="theme">
    <!-- 左侧导航 -->
    <Sidebar />

    <!-- 右侧主区域 -->
    <div class="main-section">
      <!-- 顶部栏 -->
      <TopBar />

      <!-- 内容滚动区 -->
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- 底部播放栏 -->
      <PlayerBar />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import Sidebar from './components/Sidebar.vue'
import TopBar from './components/TopBar.vue'
import PlayerBar from './components/PlayerBar.vue'
import { useTheme } from '@/utils/theme'
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()
// 主题已在 main.js 全局初始化，这里只需响应式绑定
const { theme } = useTheme()

onMounted(() => {
  playerStore.initAudioEvents()
})
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  /* 关键：让 .app-layout 自身承载页面底色（而非透出 #app 的亮色）
     data-theme 绑定在本元素上，此处读到的 --st-canvas-soft 随主题切换 */
  background: var(--st-canvas-soft);
}

.main-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--spacing-xl) var(--spacing-2xl);
}

/* 页面切换淡入淡出（animation.md 第三章：250ms ease-out）*/
.fade-enter-active,
.fade-leave-active {
  transition: opacity 250ms ease-out;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
