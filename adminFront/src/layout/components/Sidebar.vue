<template>
  <div class="sidebar">
    <!-- Logo 区 -->
    <div class="sidebar-logo">
      <div class="logo-mark">
        <el-icon class="logo-icon"><Headset /></el-icon>
      </div>
      <transition name="fade-width">
        <span v-show="!isCollapse" class="logo-text">MusicDreamer</span>
      </transition>
    </div>

    <!-- 菜单：从 navData 动态生成，支持角色过滤 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      class="wf-menu"
      router
    >
      <!-- 一级菜单 -->
      <template v-for="item in menuList" :key="item.path">
        <!-- 有子菜单：渲染 sub-menu -->
        <el-sub-menu
          v-if="item.children && item.children.length"
          :index="'/' + item.path"
        >
          <template #title>
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <span class="menu-title">{{ item.title }}</span>
          </template>
          <!-- 二级菜单（按角色过滤） -->
          <el-menu-item
            v-for="child in filterChildren(item.children)"
            :key="child.path"
            :index="'/' + item.path + '/' + child.path"
            class="wf-menu-item wf-menu-item--child"
          >
            <el-icon class="menu-icon"><component :is="child.icon" /></el-icon>
            <template #title>
              <span class="menu-title">{{ child.title }}</span>
            </template>
            <span class="active-bar"></span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 无子菜单：渲染 menu-item -->
        <el-menu-item v-else :index="'/' + item.path" class="wf-menu-item">
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <template #title>
            <span class="menu-title">{{ item.title }}</span>
          </template>
          <span class="active-bar"></span>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Headset } from '@element-plus/icons-vue'
import navData from '@/utils/navData'
import { useUserStore } from '@/store/user'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

// 按角色过滤一级菜单
const menuList = computed(() => {
  return navData.filter((item) => {
    if (item.show === 'all') return true
    if (item.show === 'admin' && userInfo.value?.role === 0) return true
    if (item.show === 'singer' && userInfo.value?.role === 1) return true
    return false
  })
})

// 按角色过滤二级菜单
function filterChildren(children) {
  return children.filter((child) => {
    if (child.show === 'all') return true
    if (child.show === 'admin' && userInfo.value?.role === 0) return true
    if (child.show === 'singer' && userInfo.value?.role === 1) return true
    return false
  })
}

const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* Logo 区 */
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 0 16px;
  color: var(--wf-ink);
  background: var(--wf-canvas);
  border-bottom: 1px solid var(--wf-hairline);

  .logo-mark {
    width: 32px;
    height: 32px;
    flex-shrink: 0;
    border-radius: var(--rounded-md);
    background: linear-gradient(135deg, var(--brand-accent) 0%, var(--wf-accent-blue) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .logo-icon {
    font-size: 18px;
    color: var(--wf-on-primary);
  }
  .logo-text {
    font-size: 15px;
    font-weight: 600;
    letter-spacing: -0.2px;
    white-space: nowrap;
    color: var(--wf-ink);
  }
}

.fade-width-enter-active,
.fade-width-leave-active {
  transition: opacity 200ms ease;
}
.fade-width-enter-from,
.fade-width-leave-to {
  opacity: 0;
}

/* 菜单样式 */
.wf-menu {
  flex: 1;
  background: var(--wf-canvas);
  border-right: none;
  padding: var(--spacing-sm) 0;
  overflow-y: auto;
  overflow-x: hidden;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    position: relative;
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    padding: 0 12px !important;
    border-radius: var(--rounded-sm);
    color: var(--wf-body-mid);
    font-size: 14px;
    font-weight: 500;
    transition: background 150ms ease, color 150ms ease;
  }
  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background: var(--wf-row-hover);
    color: var(--wf-ink);
  }
  :deep(.el-menu-item.is-active) {
    background: var(--wf-row-hover);
    color: var(--wf-ink);
    font-weight: 600;
  }
  :deep(.el-sub-menu.is-active > .el-sub-menu__title),
  :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
    color: var(--wf-ink);
  }
  :deep(.el-menu-item .el-icon),
  :deep(.el-sub-menu__title .el-icon) {
    font-size: 18px;
    margin-right: 12px;
    color: var(--wf-mute);
  }
  :deep(.el-menu-item:hover .el-icon),
  :deep(.el-sub-menu__title:hover .el-icon) {
    color: var(--wf-body-mid);
  }
  :deep(.el-menu-item.is-active .el-icon) {
    color: var(--brand-accent);
  }
  :deep(.el-sub-menu__icon-arrow) {
    color: var(--wf-mute);
  }
  :deep(.el-menu-item.wf-menu-item--child) {
    height: 40px;
    line-height: 40px;
    padding-left: 36px !important;
    font-size: 13.5px;
    color: var(--wf-body-mid);
  }
  :deep(.el-menu-item.wf-menu-item--child .el-icon) {
    font-size: 16px;
    margin-right: 10px;
    color: var(--wf-mute);
  }
  :deep(.el-menu-item.wf-menu-item--child.is-active) {
    color: var(--wf-ink);
    background: var(--wf-row-hover);
  }
  :deep(.el-menu-item.wf-menu-item--child.is-active .el-icon) {
    color: var(--brand-accent);
  }
  &.el-menu--collapse :deep(.el-menu-item .el-icon),
  &.el-menu--collapse :deep(.el-sub-menu__title .el-icon) {
    margin-right: 0;
  }
}

/* 激活项竖条 */
.active-bar {
  position: absolute;
  left: -8px;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 2px;
  height: 20px;
  border-radius: 2px;
  background: var(--brand-accent);
  opacity: 0;
  transition: transform 200ms ease-out, opacity 200ms ease-out;
  pointer-events: none;
}
:deep(.el-menu-item.is-active .active-bar) {
  transform: translateY(-50%) scaleY(1);
  opacity: 1;
}
</style>