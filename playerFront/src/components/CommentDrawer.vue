<template>
  <transition name="drawer">
    <div v-if="visible" class="comment-drawer" @click.self="close">
      <div class="drawer-content">
        <div class="drawer-header">
          <h3 class="drawer-title">评论 ({{ total }})</h3>
          <button class="close-btn" @click="close">
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list" ref="listRef">
          <div v-if="loading" class="loading-tip">加载中...</div>
          <div v-else-if="comments.length === 0" class="empty-tip">
            暂无评论，快来抢沙发吧
          </div>
          <div v-else class="comment-item" v-for="item in comments" :key="item.id">
            <div class="avatar">
              <img v-if="item.userAvatar" :src="item.userAvatar" :alt="item.username" />
              <el-icon v-else><UserFilled /></el-icon>
            </div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="username">{{ item.username }}</span>
                <span class="time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="comment-text">{{ item.content }}</div>
              <div v-if="item.isMine" class="comment-actions">
                <button class="del-btn" @click="handleDelete(item)">删除</button>
              </div>
            </div>
          </div>
          <div v-if="hasMore" class="load-more" @click="loadMore">加载更多</div>
        </div>

        <!-- 发表评论输入区 -->
        <div class="input-area">
          <el-input
            v-model="newComment"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 3 }"
            :rows="1"
            placeholder="写下你的评论..."
            maxlength="200"
            show-word-limit
            resize="none"
            @keydown.enter.ctrl="handleSubmit"
          />
          <button
            class="send-btn"
            :disabled="!newComment.trim() || submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, UserFilled } from '@element-plus/icons-vue'
import { commentListApi, createCommentApi, deleteCommentApi } from '@/api/interaction'
import { useUserStore } from '@/store/user'

const props = defineProps({
  visible: Boolean,
  songId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:visible', 'comment-count'])

const userStore = useUserStore()

const comments = ref([])
const total = ref(0)
const pn = ref(1)
const size = 20
const hasMore = ref(false)
const loading = ref(false)
const submitting = ref(false)
const newComment = ref('')
const listRef = ref(null)

// 格式化时间
function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

// 加载评论列表
async function loadList() {
  if (!props.songId) return
  loading.value = true
  try {
    const res = await commentListApi(props.songId, pn.value, size)
    if (pn.value === 1) {
      comments.value = res.data?.records || []
    } else {
      comments.value.push(...(res.data?.records || []))
    }
    total.value = res.data?.total || 0
    hasMore.value = comments.value.length < total.value
  } catch (e) {
    // 静默失败
  } finally {
    loading.value = false
  }
}

function loadMore() {
  pn.value++
  loadList()
}

// 发表评论
async function handleSubmit() {
  const content = newComment.value.trim()
  if (!content || submitting.value) return
  submitting.value = true
  try {
    await createCommentApi(props.songId, {
      content
    })
    newComment.value = ''
    ElMessage.success('评论成功')
    // 重置页码并刷新
    pn.value = 1
    await loadList()
    emit('comment-count', total.value)
  } catch (e) {
    // request.js 已弹窗
  } finally {
    submitting.value = false
  }
}

// 删除评论
async function handleDelete(item) {
  try {
    await deleteCommentApi(item.id)
    ElMessage.success('删除成功')
    // 从列表中移除
    comments.value = comments.value.filter((c) => c.id !== item.id)
    total.value = Math.max(0, total.value - 1)
    emit('comment-count', total.value)
  } catch (e) {
    // request.js 已弹窗
  }
}

function close() {
  emit('update:visible', false)
}

// 打开时加载评论
watch(
  () => props.visible,
  (val) => {
    if (val && props.songId) {
      pn.value = 1
      loadList()
    }
  }
)
</script>

<style scoped lang="scss">
.comment-drawer {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
}

.drawer-content {
  width: 100%;
  max-height: 70vh;
  background: var(--st-canvas);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.2);
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--st-hairline);
  flex-shrink: 0;
}

.drawer-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--st-ink);
}

.close-btn {
  border: none;
  background: var(--st-input-bg);
  color: var(--st-ink-mute);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    background: var(--st-hairline);
    color: var(--st-ink);
  }
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
}

.loading-tip,
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--st-ink-mute);
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--st-hairline);

  &:last-child {
    border-bottom: none;
  }
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--st-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .el-icon {
    font-size: 20px;
    color: var(--st-ink-mute);
  }
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--st-ink);
}

.time {
  font-size: 12px;
  color: var(--st-ink-mute);
}

.comment-text {
  font-size: 15px;
  color: var(--st-ink-secondary);
  line-height: 1.5;
  word-break: break-all;
}

.comment-actions {
  margin-top: 6px;
}

.del-btn {
  border: none;
  background: transparent;
  color: var(--st-ink-mute);
  font-size: 12px;
  cursor: pointer;
  padding: 0;

  &:hover {
    color: var(--st-ruby, #ea2261);
  }
}

.load-more {
  text-align: center;
  padding: 16px;
  color: var(--st-primary);
  font-size: 14px;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--st-hairline);
  background: var(--st-canvas);
  flex-shrink: 0;

  :deep(.el-textarea) {
    flex: 1;

    .el-textarea__inner {
      border-radius: 20px;
      padding: 8px 14px;
      font-size: 14px;
      resize: none;
    }
  }
}

.send-btn {
  border: none;
  background: var(--st-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: var(--rounded-pill);
  cursor: pointer;
  transition: all 200ms ease;
  white-space: nowrap;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover);
    transform: scale(1.05);
  }

  &:active:not(:disabled) {
    transform: scale(0.95);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* 抽屉动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 300ms ease;

  .drawer-content {
    transition: transform 300ms ease;
  }
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;

  .drawer-content {
    transform: translateY(100%);
  }
}
</style>
