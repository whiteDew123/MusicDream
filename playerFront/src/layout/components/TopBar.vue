<template>
  <header class="topbar">
    <!-- 左侧：导航按钮 + 页面标题 -->
    <div class="topbar-left">
      <button class="nav-btn" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <button class="nav-btn" @click="router.forward()">
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 中间：搜索框 -->
    <div class="search-wrap">
      <el-input
        v-model="keyword"
        placeholder="搜索歌曲、歌手、歌单"
        :prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- 右侧：好友 + 用户区 -->
    <div class="topbar-right">
      <!-- 主题切换：亮↔暗（首次跟随系统，选择持久化）
           图标左右滚动切换：变暗时太阳向左滚出、月亮从右滚入；变亮则反向 -->
      <button
        class="theme-btn"
        :title="theme === 'dark' ? '切换到亮色模式' : '切换到暗色模式'"
        aria-label="切换主题"
        @click="handleToggleTheme"
      >
        <Transition :name="slideDir" mode="out-in">
          <el-icon :key="theme">
            <Sunny v-if="theme === 'dark'" />
            <Moon v-else />
          </el-icon>
        </Transition>
      </button>

      <!-- 好友入口 -->
      <div class="friend-entry" @click="friendDrawerVisible = true">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-icon><User /></el-icon>
        </el-badge>
      </div>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-trigger">
          <div class="avatar">
            <el-icon v-if="!userInfo?.imageUrl"><UserFilled /></el-icon>
            <img v-else :src="userInfo.imageUrl" alt="avatar" />
          </div>
          <span class="username">{{ userInfo?.username || '未登录' }}</span>
          <el-icon class="caret"><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>

  <!-- 好友抽屉面板 -->
  <FriendDrawer
    v-model="friendDrawerVisible"
    @close="loadUnreadCount"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Search,
  UserFilled,
  CaretBottom,
  User,
  Setting,
  SwitchButton,
  Moon,
  Sunny
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useTheme } from '@/utils/theme'
import { getReceivedRequestsApi } from '@/api/friend'
import FriendDrawer from '@/components/FriendDrawer.vue'

const router = useRouter()
const userStore = useUserStore()
const { theme, toggleTheme } = useTheme()
const keyword = ref('')
const unreadCount = ref(0)
const friendDrawerVisible = ref(false)

// 图标滚动方向：变暗（light→dark）向左滚；变亮（dark→light）向右滚
const slideDir = ref('slide-left')

function handleToggleTheme() {
  // 先定方向，再切换：transition name 在动画开始前确定
  slideDir.value = theme.value === 'dark' ? 'slide-right' : 'slide-left'
  toggleTheme()
}

const userInfo = userStore.userInfo

function handleSearch() {
  if (!keyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: keyword.value } })
}

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/my')
      break
    case 'settings':
      router.push('/my/settings')
      break
    case 'logout':
      ElMessageBox.confirm('确定退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
        router.push('/login')
      }).catch(() => {})
      break
  }
}

// 加载未读好友请求数
async function loadUnreadCount() {
  try {
    const res = await getReceivedRequestsApi()
    unreadCount.value = (res.data || []).length
  } catch (error) {
    // 静默失败
  }
}

onMounted(() => {
  loadUnreadCount()
})
</script>

<style scoped lang="scss">
.topbar {
  height: var(--topbar-height);
  background: var(--st-canvas);
  border-bottom: 1px solid var(--st-hairline);
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-xl);
  gap: var(--spacing-lg);
  flex-shrink: 0;
}

/* 左侧导航按钮 */
.topbar-left {
  display: flex;
  gap: 6px;
}

.nav-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: var(--st-canvas-hover);
  color: var(--st-ink-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  transition: all 150ms ease;

  &:hover {
    background: var(--st-primary-subdued);
    color: var(--st-primary);
  }
}

/* 搜索框 */
.search-wrap {
  flex: 1;
  max-width: 400px;

  .search-input {
    :deep(.el-input__wrapper) {
      background: var(--st-input-bg);
      border: 1px solid transparent;
      border-radius: var(--rounded-pill);
      padding: 4px 16px;
      box-shadow: none;
      transition: all 200ms ease;
    }
    :deep(.el-input__wrapper:hover) {
      background: var(--st-input-hover);
    }
    :deep(.el-input__wrapper.is-focus) {
      border-color: var(--st-primary);
      background: var(--st-canvas);
      box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
    }
    :deep(.el-input__inner) {
      color: var(--st-ink);
      font-size: 14px;
      height: 36px;
    }
    :deep(.el-input__inner::placeholder) {
      color: var(--st-ink-mute);
    }
    :deep(.el-input__prefix-inner) {
      color: var(--st-ink-mute);
    }
  }
}

/* 右侧用户区 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 好友入口 */
.friend-entry {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--st-canvas-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 150ms ease;
  position: relative;

  .el-icon {
    font-size: 18px;
    color: var(--st-ink-secondary);
  }

  &:hover {
    background: var(--st-primary-subdued);
    .el-icon {
      color: var(--st-primary);
    }
  }
}

/* 主题切换按钮：圆形 32px（与 nav-btn 同几何语言） */
.theme-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: var(--st-canvas-hover);
  color: var(--st-ink-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  /* 裁剪滑出滑入的图标，形成"滚出按钮边缘"的滚动感 */
  overflow: hidden;
  transition: background 200ms ease, color 200ms ease;

  &:hover {
    background: var(--st-primary-subdued);
    color: var(--st-primary);
  }
}

/* 图标左右滚动切换动画（out-in：旧图标先滚出，新图标再滚入） */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 280ms cubic-bezier(0.4, 0, 0.2, 1), opacity 280ms ease;
}

/* 向左滚：旧图标向左出（-120%），新图标从右侧入（+120%）*/
.slide-left-enter-from {
  transform: translateX(120%);
  opacity: 0;
}
.slide-left-leave-to {
  transform: translateX(-120%);
  opacity: 0;
}

/* 向右滚：旧图标向右出（+120%），新图标从左侧入（-120%）*/
.slide-right-enter-from {
  transform: translateX(-120%);
  opacity: 0;
}
.slide-right-leave-to {
  transform: translateX(120%);
  opacity: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: var(--rounded-pill);
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);
  }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: var(--st-primary-subdued);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    color: var(--st-primary);
    font-size: 16px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .username {
    font-size: 14px;
    color: var(--st-ink-secondary);
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .caret {
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}
</style>