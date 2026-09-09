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

    <!-- 菜单：Webflow 亮色风格，支持分组子菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      class="wf-menu"
      router
    >
      <template v-for="item in menuRoutes" :key="item.path">
        <!-- 分组：带 children 的项渲染为可展开 sub-menu -->
        <el-sub-menu
          v-if="item.children && filterChildren(item.children).length"
          :index="'/' + item.path"
        >
          <template #title>
            <el-icon class="menu-icon"><component :is="item.meta.icon" /></el-icon>
            <span class="menu-title">{{ item.meta.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="'/' + item.path + '/' + child.path"
            class="wf-menu-item wf-menu-item--child"
          >
            <el-icon class="menu-icon"><component :is="child.meta.icon" /></el-icon>
            <template #title>
              <span class="menu-title">{{ child.meta.title }}</span>
            </template>
            <span class="active-bar"></span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 单项：无 children 渲染为普通 menu-item -->
        <el-menu-item v-else :index="'/' + item.path" class="wf-menu-item">
          <el-icon class="menu-icon"><component :is="item.meta.icon" /></el-icon>
          <template #title>
            <span class="menu-title">{{ item.meta.title }}</span>
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
import { routes } from '@/router'
import { useUserStore } from '@/store/user'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const userStore = useUserStore()

// 过滤子菜单：根据角色权限过滤不可见的子菜单项
function filterChildren(children) {
  if (!children || !children.length) return []
  return children.filter((child) => {
    const childRoles = child.meta?.roles
    if (!childRoles) return true
    return userStore.hasRole(...childRoles)
  })
}

const menuRoutes = computed(() => {
  const layout = routes.find((r) => r.path === '/')
  if (!layout || !layout.children) return []

  return layout.children
    .filter((item) => {
      if (item.meta?.skipMenu) return false

      if (item.children && item.children.length) {
        const filtered = item.children.filter((child) => {
          const childRoles = child.meta?.roles
          if (!childRoles) return true
          return userStore.hasRole(...childRoles)
        })
        return filtered.length > 0
      }

      const itemRoles = item.meta?.roles
      if (!itemRoles) return true
      return userStore.hasRole(...itemRoles)
    })
    .map((item) => {
      if (item.children && item.children.length) {
        return {
          ...item,
          children: item.children.filter((child) => {
            const childRoles = child.meta?.roles
            if (!childRoles) return true
            return userStore.hasRole(...childRoles)
          })
        }
      }
      return item
    })
})

const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* ===== Logo 区：白色 canvas + 底部 hairline 边框 ===== */
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
    /* logo 容器使用 Webflow 蓝色渐变 */
    background: linear-gradient(
      135deg,
      var(--brand-accent) 0%,
      var(--wf-accent-blue) 100%
    );
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

/* Logo 文字折叠过渡（animation.md 第四章：250ms ease-in-out）*/
.fade-width-enter-active,
.fade-width-leave-active {
  transition: opacity 200ms ease;
}
.fade-width-enter-from,
.fade-width-leave-to {
  opacity: 0;
}

/* ===== Webflow 亮色菜单 ===== */
.wf-menu {
  flex: 1;
  background: var(--wf-canvas);
  border-right: none;
  padding: var(--spacing-sm) 0;
  overflow-y: auto;
  overflow-x: hidden;

  /* 一级 menu-item 与 sub-menu 标题共用同一套样式 */
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

  /* 一级激活项：ink 文字 + 600 字重 */
  :deep(.el-menu-item.is-active) {
    background: var(--wf-row-hover);
    color: var(--wf-ink);
    font-weight: 600;
  }

  /* sub-menu 展开时标题保持 ink */
  :deep(.el-sub-menu.is-active > .el-sub-menu__title),
  :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
    color: var(--wf-ink);
  }

  /* 图标颜色 */
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

  /* sub-menu 展开箭头颜色 */
  :deep(.el-sub-menu__icon-arrow) {
    color: var(--wf-mute);
  }

  /* 二级 menu-item：稍缩进，颜色稍弱 */
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

  /* 折叠态：去掉 icon 的 margin-right，子项用 popup 模式 */
  &.el-menu--collapse :deep(.el-menu-item .el-icon),
  &.el-menu--collapse :deep(.el-sub-menu__title .el-icon) {
    margin-right: 0;
  }
}

/* ===== 激活项左侧蓝色竖条（animation.md 第四章：#4353ff，200ms ease-out）===== */
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