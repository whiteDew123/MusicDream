<template>
  <div class="notify-page">
    <!-- 页面标题 -->
    <header class="page-header">
      <div class="header-left">
        <span class="header-icon-wrap">
          <el-icon class="header-icon"><Bell /></el-icon>
        </span>
        <h2 class="header-title">消息通知</h2>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
      </div>
      <button
        class="read-all-btn"
        :disabled="readAllLoading || !messages.length || unreadCount === 0"
        @click="handleMarkAllRead"
      >
        <el-icon class="btn-icon"><Check /></el-icon>
        <span>{{ readAllLoading ? '处理中…' : '全部已读' }}</span>
      </button>
    </header>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-list">
      <div class="skeleton-card" v-for="n in 5" :key="n">
        <div class="skeleton-line skeleton-title"></div>
        <div class="skeleton-line skeleton-content"></div>
        <div class="skeleton-line skeleton-time"></div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div v-else-if="messages.length" class="msg-list">
      <article
        v-for="msg in messages"
        :key="msg.id"
        class="msg-card"
        :class="{ unread: msg.isread === 0 }"
        @click="handleMsgClick(msg)"
      >
        <span v-if="msg.isread === 0" class="unread-dot" aria-hidden="true"></span>
        <div class="msg-body">
          <h3 class="msg-title">{{ msg.title }}</h3>
          <p class="msg-content">{{ msg.msg }}</p>
          <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
        </div>
      </article>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-icon class="empty-icon"><Bell /></el-icon>
      <p class="empty-text">暂无消息</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Check } from '@element-plus/icons-vue'
import { myMessagesApi, markReadApi, markAllReadApi } from '@/api/msg'

// 消息列表与加载态
const loading = ref(true)
const readAllLoading = ref(false)
const messages = ref([])

// 未读数：由列表计算得出
const unreadCount = computed(() => messages.value.filter((m) => m.isread === 0).length)

// 相对时间格式化：1 分钟内「刚刚」、1 小时内「X 分钟前」、
// 1 天内「X 小时前」、7 天内「X 天前」，更早则显示 YYYY-MM-DD
function formatTime(date) {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const diff = now - d // 毫秒差
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`

  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

// 加载消息列表
async function loadMessages() {
  loading.value = true
  try {
    const res = await myMessagesApi()
    messages.value = res.data || []
  } catch (e) {
    console.error('加载消息失败:', e)
  } finally {
    loading.value = false
  }
}

// 点击未读消息：标记单条已读
async function handleMsgClick(msg) {
  if (msg.isread === 1) return // 已读消息不重复处理
  try {
    await markReadApi(msg.id)
    msg.isread = 1
  } catch (e) {
    // 错误提示已由请求拦截器统一处理
  }
}

// 全部已读
async function handleMarkAllRead() {
  readAllLoading.value = true
  try {
    await markAllReadApi()
    messages.value.forEach((m) => {
      m.isread = 1
    })
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    // 错误提示已由请求拦截器统一处理
  } finally {
    readAllLoading.value = false
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="scss">
.notify-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 8px 0 32px;
}

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: var(--rounded-md);
    background: rgba(94, 92, 230, 0.1);
  }

  .header-icon {
    font-size: 22px;
    color: var(--st-primary);
  }

  .header-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.2px;
  }

  .unread-badge {
    min-width: 22px;
    height: 22px;
    padding: 0 7px;
    border-radius: var(--rounded-pill);
    background: #f0413f;
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 22px;
    text-align: center;
    font-feature-settings: 'tnum';
  }
}

/* ===== 全部已读按钮 ===== */
.read-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 18px;
  border-radius: var(--rounded-pill);
  border: 1px solid rgba(94, 92, 230, 0.4);
  background: rgba(94, 92, 230, 0.08);
  color: var(--st-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 200ms ease;

  .btn-icon {
    font-size: 16px;
  }

  &:hover:not(:disabled) {
    background: rgba(94, 92, 230, 0.16);
    border-color: var(--st-primary);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* ===== 骨架屏 ===== */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  padding: 20px 24px;
  border-radius: var(--rounded-lg);
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
}

.skeleton-line {
  border-radius: var(--rounded-sm);
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
}

.skeleton-title {
  width: 40%;
  height: 18px;
  margin-bottom: 12px;
}

.skeleton-content {
  width: 90%;
  height: 14px;
  margin-bottom: 10px;
}

.skeleton-time {
  width: 120px;
  height: 12px;
}

@keyframes skeletonPulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* ===== 消息列表 ===== */
.msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-card {
  position: relative;
  padding: 18px 24px;
  border-radius: var(--rounded-lg);
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  cursor: pointer;
  transition: background 200ms ease, box-shadow 200ms ease, transform 200ms ease;

  &:hover {
    background: var(--st-canvas-hover);
    box-shadow: var(--shadow-sm);
    transform: translateY(-1px);
  }

  /* 未读：左侧红色竖条 */
  &.unread::before {
    content: '';
    position: absolute;
    left: 0;
    top: 14px;
    bottom: 14px;
    width: 3px;
    border-radius: 0 2px 2px 0;
    background: #f0413f;
  }

  /* 未读：标题加粗 */
  &.unread .msg-title {
    font-weight: 600;
  }
}

/* 未读红点 */
.unread-dot {
  position: absolute;
  top: 18px;
  right: 24px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f0413f;
  box-shadow: 0 0 0 3px rgba(240, 65, 63, 0.18);
}

.msg-body {
  max-width: 100%;
}

.msg-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--st-ink);
  margin-bottom: 6px;
  line-height: 1.4;
}

.msg-content {
  font-size: 13px;
  color: var(--st-ink-mute);
  line-height: 1.6;
  margin-bottom: 10px;
  /* 最多两行 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.msg-time {
  font-size: 12px;
  color: var(--st-ink-mute);
  font-feature-settings: 'tnum';
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 96px 0;
  text-align: center;

  .empty-icon {
    font-size: 56px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    color: var(--st-ink-mute);
  letter-spacing: 1px;
  }
}
</style>
