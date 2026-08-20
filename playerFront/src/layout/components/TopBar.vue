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

    <!-- 右侧：用户区 -->
    <div class="topbar-right">
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
</template>

<script setup>
import { ref } from 'vue'
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
  SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

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
      background: #e9edf3;
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
  margin-left: auto;
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
