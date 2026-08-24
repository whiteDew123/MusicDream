<template>
  <div class="msg-list">
    <div class="page-header">
      <div>
        <p class="eyebrow">MESSAGE · NOTIFY</p>
        <h2 class="page-title">消息通知</h2>
        <p class="page-desc">查看和管理收到的消息。</p>
      </div>
    </div>

    <div class="panel list-panel">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="unread-badge" />
        </div>
        <div class="toolbar-right">
          <el-button :disabled="unreadCount === 0 || loading" @click="handleMarkAllRead">
            全部标记已读
          </el-button>
          <el-button :loading="loading" @click="fetchMessages">刷新</el-button>
        </div>
      </div>

      <div v-loading="loading" class="message-list">
        <el-empty v-if="!loading && messages.length === 0" description="暂无消息" />

        <div
          v-for="message in messages"
          :key="message.id"
          :class="['message-item', { 'is-unread': message.isread === 0 }]"
          @click="handleMarkRead(message)"
        >
          <div class="message-indicator" />
          <div class="message-body">
            <div class="message-header">
              <span class="message-title">{{ message.title }}</span>
              <span class="message-time">{{ formatTime(message.createTime) }}</span>
            </div>
            <p class="message-text">{{ message.msg }}</p>
          </div>
          <div class="message-status">
            <el-tag :type="message.isread === 0 ? 'danger' : 'info'" size="small" effect="light">
              {{ message.isread === 0 ? '未读' : '已读' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyMessages, markAsRead, markAllAsRead, getUnreadCount } from '@/api/msg'

const messages = ref([])
const unreadCount = ref(0)
const loading = ref(false)

async function fetchMessages() {
  loading.value = true
  try {
    const [msgRes, countRes] = await Promise.all([
      getMyMessages(),
      getUnreadCount()
    ])
    messages.value = msgRes.data || []
    unreadCount.value = countRes.data || 0
  } catch {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(message) {
  if (message.isread === 1) return
  try {
    await markAsRead(message.id)
    message.isread = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
  } catch {
    ElMessage.error('标记失败')
  }
}

async function handleMarkAllRead() {
  if (unreadCount.value === 0) return
  try {
    await markAllAsRead()
    messages.value.forEach((msg) => { msg.isread = 1 })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch {
    ElMessage.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped lang="scss">
.msg-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.page-header {
  .eyebrow {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    color: var(--brand-accent);
    margin-bottom: 8px;
  }
  .page-title {
    font-size: 24px;
    font-weight: 600;
    letter-spacing: -0.4px;
    color: var(--wf-ink);
    margin-bottom: 6px;
  }
  .page-desc {
    font-size: 14px;
    color: var(--wf-body-mid);
  }
}

.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  box-shadow: var(--shadow-sm);
}

.list-panel {
  padding: 0;
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--wf-hairline);
}

.message-list {
  min-height: 200px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--wf-hairline);
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--wf-row-hover);
  }

  &.is-unread {
    background: rgba(99, 102, 241, 0.03);
  }

  &:last-child {
    border-bottom: none;
  }
}

.message-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--brand-accent);
  margin-top: 6px;
  flex-shrink: 0;
  opacity: 0;

  .is-unread & {
    opacity: 1;
  }
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--wf-ink);
}

.message-time {
  font-size: 12px;
  color: var(--wf-mute);
  flex-shrink: 0;
  margin-left: var(--spacing-md);
}

.message-text {
  font-size: 13px;
  color: var(--wf-body-mid);
  line-height: 1.5;
  margin: 0;
}

.message-status {
  flex-shrink: 0;
  margin-top: 2px;
}
</style>