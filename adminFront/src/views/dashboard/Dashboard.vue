<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userInfo?.username }} 👋</h2>
          <p>这里是 MusicDreamer 音乐网站后台管理系统，今日也是元气满满的一天～</p>
        </div>
        <el-icon class="welcome-icon"><Headset /></el-icon>
      </div>
    </el-card>

    <!-- 统计卡片占位（数据需对接后续业务微服务） -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :style="{ background: item.color }">
              <component :is="item.icon" />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 说明卡片 -->
    <el-card class="intro-card" shadow="never">
      <template #header>
        <span>系统说明</span>
      </template>
      <ul class="intro-list">
        <li>本系统基于 Spring Cloud 微服务架构，前端 Vue3 + Element Plus。</li>
        <li>登录、注册、邮箱验证码由 <code>Mod_login</code>（端口 8001）提供。</li>
        <li>所有请求经由网关 <code>music_gateway</code>（端口 9000）统一路由与 JWT 鉴权。</li>
        <li>左侧菜单的业务模块（用户/音乐/歌手/歌单/消息）需后续开发对应微服务接口。</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 占位统计数据，后续对接业务接口
const stats = [
  { title: '用户总数', value: '--', icon: 'User', color: '#409eff' },
  { title: '音乐总数', value: '--', icon: 'Headset', color: '#67c23a' },
  { title: '歌手总数', value: '--', icon: 'Microphone', color: '#e6a23c' },
  { title: '歌单总数', value: '--', icon: 'Files', color: '#f56c6c' }
]
</script>

<style scoped lang="scss">
.dashboard {
  .welcome-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    :deep(.el-card__body) {
      padding: 24px;
    }

    .welcome {
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: #fff;

      .welcome-text {
        h2 {
          font-size: 22px;
          margin-bottom: 8px;
        }

        p {
          font-size: 14px;
          opacity: 0.9;
        }
      }

      .welcome-icon {
        font-size: 64px;
        opacity: 0.6;
      }
    }
  }

  .stat-row {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 8px;
        color: #fff;
        font-size: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }

        .stat-title {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .intro-card {
    .intro-list {
      padding-left: 20px;
      line-height: 2;
      color: #606266;

      code {
        background: #f5f7fa;
        padding: 2px 6px;
        border-radius: 3px;
        color: #c7254e;
        font-size: 13px;
      }
    }
  }
}
</style>
