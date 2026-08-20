<template>
  <div class="msg-list-container">
    <div class="page-header">
      <h1 class="page-title">消息通知</h1>
      <p class="page-desc">查看和管理收到的消息</p>
    </div>

    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="header-title">消息列表</span>
            <el-badge
              v-if="unreadCount > 0"
              :value="unreadCount"
              :max="99"
              class="unread-badge"
            />
          </div>
          <div class="header-right">
            <el-button
              type="primary"
              :disabled="unreadCount === 0 || loading"
              @click="handleMarkAllRead"
              class="mark-all-btn"
            >
              全部标记为已读
            </el-button>
            <el-button @click="fetchMessages" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="message-list">
        <el-empty
          v-if="!loading && messages.length === 0"
          description="暂无消息"
        />

        <div
          v-for="message in messages"
          :key="message.id"
          :class="['message-item', { 'is-unread': message.isread === 0 }]"
          @click="handleMarkRead(message)"
        >
          <div class="message-indicator" />
          <div class="message-content">
            <div class="message-header">
              <h3 class="message-title">{{ message.title }}</h3>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <p class="message-text">{{ message.msg }}</p>
          </div>
          <div class="message-status">
            <el-tag
              :type="message.isread === 0 ? 'danger' : 'info'"
              size="small"
              effect="plain"
            >
              {{ message.isread === 0 ? '未读' : '已读' }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyMessages, markAsRead, markAllAsRead, getUnreadCount } from '@/api/msg'

const messages = ref([])
const unreadCount = ref(0)
const loading = ref(false)

const fetchMessages = async () => {
  loading.value = true
  try {
    const [msgRes, countRes] = await Promise.all([
      getMyMessages(),
      getUnreadCount()
    ])
    messages.value = msgRes.data || []
    unreadCount.value = countRes.data || 0
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const handleMarkRead = async (message) => {
  if (message.isread === 1) return

  try {
    await markAsRead(message.id)
    message.isread = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('标记失败')
  }
}

const handleMarkAllRead = async () => {
  if (unreadCount.value === 0) return

  try {
    await markAllAsRead()
    messages.value.forEach(msg => {
      msg.isread = 1
    })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped lang="scss">
.msg-list-container {
  padding: 32px;
  background-color: #f5f6f8;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #080808;
  margin: 0 0 8px 0;
  letter-spacing: -0.24px;
}

.page-desc {
  font-size: 14px;
  color: #5a5a5a;
  margin: 0;
}

.list-card {
  background-color: #ffffff;
  border: 1px solid #d8d8d8;
  border-radius: 8px;

  :deep(.el-card__header) {
    padding: 16px 32px;
    border-bottom: 1px solid #d8d8d8;
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: #080808;
}

.unread-badge {
  :deep(.el-badge__content) {
    background-color: #ee1d36;
  }
}

.header-right {
  display: flex;
  gap: 12px;
}

.mark-all-btn {
  background-color: #080808;
  border-color: #080808;
  border-radius: 4px;
  padding: 8px 16px;
  font-weight: 500;

  &:hover:not(:disabled) {
    background-color: #222222;
    border-color: #222222;
  }

  &:active:not(:disabled) {
    background-color: #363636;
    border-color: #363636;
  }
}

.message-list {
  min-height: 400px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 32px;
  border-bottom: 1px solid #d8d8d8;
  cursor: pointer;
  transition: background-color 0.15s ease;
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: #f5f6f8;

    .message-indicator {
      background-color: #4353ff;
    }
  }

  &.is-unread {
    background-color: #fafbfc;

    .message-title {
      color: #080808;
      font-weight: 500;
    }
  }
}

.message-indicator {
  width: 2px;
  min-height: 100%;
  background-color: transparent;
  position: absolute;
  left: 0;
  top: 0;
  transition: background-color 0.2s ease-out;
}

.message-content {
  flex: 1;
  margin-left: 16px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-title {
  font-size: 15px;
  color: #363636;
  margin: 0;
  font-weight: 400;
}

.message-time {
  font-size: 12px;
  color: #898989;
  white-space: nowrap;
}

.message-text {
  font-size: 14px;
  color: #5a5a5a;
  margin: 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-status {
  margin-left: 16px;
  flex-shrink: 0;
}

:deep(.el-tag) {
  border-radius: 4px;
}

:deep(.el-button) {
  border-radius: 4px;
  padding: 8px 16px;
  font-weight: 500;
}
</style>