<template>
  <aside class="sidebar">
    <!-- Logo 区域 -->
    <div class="logo-area" @click="router.push('/discover')">
      <div class="logo-icon">
        <el-icon><Headset /></el-icon>
      </div>
      <span class="logo-text">MusicDreamer</span>
    </div>

    <!-- 主导航菜单 -->
    <nav class="nav-menu">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        active-class="active"
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- 我的（可折叠分组） -->
    <div class="my-section">
      <div
        class="group-header"
        :class="{ active: isMyActive }"
        @click="toggleMyExpand"
      >
        <el-icon class="nav-icon"><User /></el-icon>
        <span class="nav-label">我的</span>
        <el-icon class="caret" :class="{ expanded: myExpanded }">
          <ArrowRight />
        </el-icon>
      </div>

      <transition name="expand">
        <div v-show="myExpanded" class="sub-menu">
          <router-link
            v-for="item in myItems"
            :key="item.path"
            :to="item.path"
            class="sub-item"
            active-class="active"
          >
            <el-icon class="sub-icon"><component :is="item.icon" /></el-icon>
            <span class="sub-label">{{ item.label }}</span>
          </router-link>
        </div>
      </transition>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 主导航项（参考 QQ音乐/网易云音乐）
const menuItems = [
  { path: '/discover', label: '推荐', icon: 'House' },
  { path: '/singer', label: '歌手', icon: 'Microphone' },
  { path: '/recognize', label: '听歌识曲', icon: 'Microphone' },
  { path: '/rank', label: '排行', icon: 'TrophyBase' },
  { path: '/songlist', label: '歌单', icon: 'Files' },
  { path: '/rooms', label: '播放室', icon: 'Headset' },
  { path: '/capsule/plaza', label: '时空胶囊', icon: 'MagicStick' },
  { path: '/Musicbox/plaza', label: '盲盒广场', icon: 'Present' }
]

// 我的子项
const myItems = [
  { path: '/my/liked', label: '我喜欢的音乐', icon: 'Star' },
  { path: '/my/capsules', label: '我的胶囊', icon: 'MagicStick' },
  { path: '/Musicbox/my', label: '我的盲盒', icon: 'Present' },
  { path: '/my/favorite', label: '收藏歌单', icon: 'Collection' },
  { path: '/my/created', label: '创建歌单', icon: 'FolderAdd' },
  { path: '/my/notify', label: '消息通知', icon: 'Bell' },
  { path: '/my/settings', label: '设置', icon: 'Setting' }
]

// 当前是否在 /my/* 路由下
const isMyActive = computed(() => route.path.startsWith('/my/'))

// 折叠状态：默认收起，但当前在 /my 下时自动展开
const myExpanded = ref(isMyActive.value)

// 切换展开
function toggleMyExpand() {
  myExpanded.value = !myExpanded.value
}

// 路由变化时，进入 /my 自动展开
watch(isMyActive, (val) => {
  if (val) myExpanded.value = true
})
</script>

<style scoped lang="scss">
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--st-canvas);
  border-right: 1px solid var(--st-hairline);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow-y: auto;
}

/* Logo 区域 */
.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px;
  cursor: pointer;
  transition: opacity 200ms ease;

  &:hover {
    opacity: 0.8;
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: var(--rounded-md);
    background: linear-gradient(135deg, var(--st-primary) 0%, var(--st-primary-soft) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    box-shadow: 0 4px 12px rgba(94, 92, 230, 0.3);
  }

  .logo-text {
    font-size: 17px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.3px;
  }
}

/* 主导航菜单 */
.nav-menu {
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: var(--rounded-md);
  color: var(--st-ink-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all 150ms ease;
  cursor: pointer;

  .nav-icon {
    font-size: 18px;
  }

  &:hover {
    background: var(--st-canvas-hover);
    color: var(--st-ink);
  }

  &.active {
    background: rgba(94, 92, 230, 0.1);
    color: var(--st-primary);
    font-weight: 600;
  }
}

/* 我的分组 */
.my-section {
  margin-top: 8px;
  padding: 0 12px;
  border-top: 1px solid var(--st-hairline);
  padding-top: 8px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: var(--rounded-md);
  color: var(--st-ink-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease;

  .nav-icon {
    font-size: 18px;
  }

  .caret {
    margin-left: auto;
    font-size: 12px;
    color: var(--st-ink-mute);
    transition: transform 250ms ease;
  }

  .caret.expanded {
    transform: rotate(90deg);
  }

  &:hover {
    background: var(--st-canvas-hover);
    color: var(--st-ink);
  }

  &.active {
    color: var(--st-primary);
    font-weight: 600;
  }
}

/* 子菜单 */
.sub-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 0 4px 12px;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: var(--rounded-md);
  color: var(--st-ink-secondary);
  font-size: 13px;
  font-weight: 400;
  transition: all 150ms ease;
  cursor: pointer;

  .sub-icon {
    font-size: 16px;
    color: var(--st-ink-mute);
    transition: color 150ms ease;
  }

  .sub-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:hover {
    background: var(--st-canvas-hover);
    color: var(--st-ink);

    .sub-icon {
      color: var(--st-ink-secondary);
    }
  }

  &.active {
    background: rgba(94, 92, 230, 0.1);
    color: var(--st-primary);
    font-weight: 600;

    .sub-icon {
      color: var(--st-primary);
    }
  }
}

/* 折叠展开过渡 */
.expand-enter-active,
.expand-leave-active {
  transition: opacity 200ms ease, max-height 250ms ease;
  overflow: hidden;
  max-height: 300px;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}
</style>