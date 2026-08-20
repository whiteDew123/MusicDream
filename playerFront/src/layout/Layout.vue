<template>
  <div class="app-layout">
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
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()

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
