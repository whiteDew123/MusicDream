<template>
  <div class="header">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="header-left">
      <button class="collapse-btn" @click="toggleCollapse" :title="isCollapse ? '展开' : '折叠'">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </button>
      <el-breadcrumb separator="/" class="wf-breadcrumb">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：用户下拉 -->
    <div class="header-right">
      <el-dropdown @command="handleCommand" class="user-dropdown">
        <div class="user-info">
          <div class="user-avatar">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
          </div>
          <span class="username">{{ userInfo?.username }}</span>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="wf-dropdown-menu">
            <el-dropdown-item command="info" class="wf-dropdown-item">
              <el-icon><User /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided class="wf-dropdown-item">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  Fold,
  Expand,
  ArrowDown,
  User,
  SwitchButton
} from '@element-plus/icons-vue'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:isCollapse'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const currentTitle = computed(() => route.meta.title)

function toggleCollapse() {
  emit('update:isCollapse', !props.isCollapse)
}

function handleCommand(command) {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'info') {
    router.push('/setting/profile')
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'wf-message-box'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
/* ===== Webflow 亮色顶栏 ===== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
}

/* 左侧 */
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  .collapse-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--rounded-sm);
    border: 1px solid transparent;
    background: transparent;
    color: var(--wf-body-mid);
    cursor: pointer;
    transition: all 150ms ease;

    .el-icon {
      font-size: 18px;
    }
  }
  .collapse-btn:hover {
    background: var(--wf-row-hover);
    color: var(--wf-ink);
  }
}

/* Webflow 面包屑（design-webflow.md：body-sm-strong 文字色 ink）*/
.wf-breadcrumb {
  :deep(.el-breadcrumb__inner) {
    color: var(--wf-mute);
    font-size: 13px;
    font-weight: 500;
  }
  :deep(.el-breadcrumb__inner.is-link:hover) {
    color: var(--brand-accent);
  }
  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: var(--wf-ink);
    font-weight: 600;
  }
  :deep(.el-breadcrumb__separator) {
    color: var(--wf-mute-soft);
    margin: 0 8px;
  }
}

/* 右侧 */
.header-right {
  .user-dropdown {
    cursor: pointer;
  }
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 4px 8px 4px 4px;
    border-radius: var(--rounded-pill);
    border: 1px solid transparent;
    transition: all 150ms ease;
  }
  .user-info:hover {
    background: var(--wf-row-hover);
    border-color: var(--wf-hairline);
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    /* 头像渐变使用 Webflow 蓝（管理端强调色）*/
    background: linear-gradient(
      135deg,
      var(--brand-accent) 0%,
      var(--wf-accent-blue) 100%
    );
    color: var(--wf-on-primary);
    font-size: 13px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .username {
    font-size: 14px;
    color: var(--wf-ink);
    font-weight: 500;
  }
  .arrow-icon {
    font-size: 12px;
    color: var(--wf-mute);
  }
}

/* 下拉菜单亮色（design-webflow.md：card-feature + 重阴影）*/
:deep(.wf-dropdown-menu) {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-sm);
  min-width: 180px;
  box-shadow: var(--shadow-lg);

  .el-dropdown-menu__item {
    border-radius: var(--rounded-sm);
    padding: var(--spacing-sm) var(--spacing-md);
    color: var(--wf-ink);
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .el-dropdown-menu__item:hover {
    background: var(--wf-row-hover);
    color: var(--wf-ink);
  }
  .el-dropdown-menu__item--divided {
    border-top: 1px solid var(--wf-hairline);
    margin-top: var(--spacing-xs);
    padding-top: var(--spacing-sm);
  }
  .el-dropdown-menu__item .el-icon {
    font-size: 16px;
    color: var(--wf-mute);
  }
}
</style>
