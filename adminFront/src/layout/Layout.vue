<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="layout-header">
        <Header v-model:is-collapse="isCollapse" />
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'

const isCollapse = ref(false)
</script>

<style scoped lang="scss">
/* ===== Webflow 亮色管理端布局（design-webflow.md）===== */
.layout-container {
  width: 100%;
  height: 100vh;
  background: var(--wf-canvas-soft);
}

/* 侧边栏：白色 canvas + 右侧 hairline 边框 */
.layout-aside {
  background: var(--wf-canvas);
  border-right: 1px solid var(--wf-hairline);
  transition: width 250ms ease-in-out;
  overflow: hidden;
}

/* 顶栏：白色 canvas + 底部 hairline 边框 */
.layout-header {
  background: var(--wf-canvas);
  border-bottom: 1px solid var(--wf-hairline);
  padding: 0;
  height: 56px;
  line-height: 56px;
}

/* 主内容区：浅灰页面底色（Design-standards.md §66）*/
.layout-main {
  background: var(--wf-canvas-soft);
  padding: var(--spacing-2xl);
  overflow-y: auto;
}

/* ===== 路由切换淡入淡出（animation.md 第三章：250ms ease-out）===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 250ms ease-out;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
