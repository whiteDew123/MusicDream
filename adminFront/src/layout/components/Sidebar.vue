<template>
  <div class="sidebar">
    <!-- Logo 区 -->
    <div class="sidebar-logo">
      <el-icon class="logo-icon"><Headset /></el-icon>
      <span v-show="!isCollapse" class="logo-text">MusicDreamer</span>
    </div>

    <!-- 菜单：根据路由表生成 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item
        v-for="item in menuRoutes"
        :key="item.path"
        :index="'/' + item.path"
      >
        <el-icon><component :is="item.meta.icon" /></el-icon>
        <template #title>{{ item.meta.title }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { routes } from '@/router'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()

// 取 Layout 下的 children 作为菜单项
const menuRoutes = computed(() => {
  const layout = routes.find((r) => r.path === '/')
  return layout ? layout.children : []
})

// 当前激活菜单
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background-color: #2b3a4d;

  .logo-icon {
    font-size: 24px;
    color: #409eff;
  }

  .logo-text {
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.el-menu {
  border-right: none;
}
</style>
