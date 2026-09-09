<template>
  <transition name="drawer">
    <div v-if="visible" class="comment-drawer" @click.self="close">
      <div class="drawer-content">
        <div class="drawer-header">
          <h3 class="drawer-title">评论 {{ total }}</h3>
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
          <template v-else>
            <div
              v-for="item in comments"
              :key="item.id"
              class="comment-item"
            >
              <!-- 头像 -->
              <div class="avatar">
                <img
                  v-if="resolveAvatar(item.avatar)"
                  :src="resolveAvatar(item.avatar)"
                  :alt="item.username"
                />
                <el-icon v-else><UserFilled /></el-icon>
              </div>

              <div class="comment-body">
                <!-- 昵称 + 时间 -->
                <div class="comment-header">
                  <span class="username">{{ item.username }}</span>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>

                <!-- 评论内容 -->
                <div class="comment-text">{{ item.content }}</div>

                <!-- 操作栏：回复 / 点赞 / 删除 -->
                <div class="comment-actions">
                  <button class="act-btn" @click="toggleReply(item)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </button>
                  <button
                    class="act-btn like-btn"
                    :class="{ active: item.liked }"
                    @click="handleLike(item)"
                  >
                    <el-icon><Star /></el-icon>
                    <span>{{ item.likes || 0 }}</span>
                  </button>
                  <button
                    v-if="isMine(item)"
                    class="act-btn del-btn"
                    @click="handleDelete(item)"
                  >
                    删除
                  </button>
                </div>

                <!-- 楼中楼回复区 -->
                <div
                  v-if="item.replyCount > 0 || expandedReplies[item.id]"
                  class="replies-block"
                >
                  <div
                    v-if="!expandedReplies[item.id]"
                    class="replies-toggle"
                    @click="loadReplies(item)"
                  >
                    查看 {{ item.replyCount }} 条回复 ▾
                  </div>

                  <template v-else>
                    <!-- 回复列表 -->
                    <div
                      v-for="r in (repliesMap[item.id] || [])"
                      :key="r.id"
                      class="reply-item"
                    >
                      <div class="reply-avatar">
                        <img
                          v-if="resolveAvatar(r.avatar)"
                          :src="resolveAvatar(r.avatar)"
                          :alt="r.username"
                        />
                        <el-icon v-else :size="14"><UserFilled /></el-icon>
                      </div>
                      <div class="reply-body">
                        <span class="reply-username">{{ r.username }}</span>
                        <template v-if="r.toUserId">
                          <span class="reply-at"> 回复 </span>
                          <span class="reply-at-name">@{{ r.toUsername }}</span>
                        </template>
                        <span class="reply-colon">：</span>
                        <span class="reply-content">{{ r.content }}</span>
                        <span class="reply-time">{{ formatTime(r.createTime) }}</span>
                        <button
                          v-if="isMine(r)"
                          class="reply-del-btn"
                          @click="handleDelete(r, item)"
                        >删除</button>
                      </div>
                    </div>

                    <!-- 收起 -->
                    <div
                      v-if="(item.replyCount || 0) > 0"
                      class="replies-toggle"
                      @click="collapseReplies(item)"
                    >收起 ▴</div>
                  </template>
                </div>

                <!-- 回复输入框 -->
                <div
                  v-if="replyingTo?.id === item.id"
                  class="reply-input"
                >
                  <input
                    v-model="replyContent"
                    type="text"
                    :placeholder="`回复 ${replyingTo.username}：`"
                    maxlength="200"
                    @keydown.enter="submitReply"
                  />
                  <button
                    class="reply-send"
                    :disabled="!replyContent.trim() || replying"
                    @click="submitReply"
                  >{{ replying ? '发送中' : '发送' }}</button>
                </div>
              </div>
            </div>

            <div v-if="hasMore" class="load-more" @click="loadMore">加载更多</div>
          </template>
        </div>

        <!-- 发表评论输入区 -->
        <div class="input-area">
          <div class="my-avatar">
            <img
              v-if="resolveAvatar(userStore.userInfo?.imageUrl)"
              :src="resolveAvatar(userStore.userInfo.imageUrl)"
            />
            <el-icon v-else :size="20"><UserFilled /></el-icon>
          </div>
          <el-input
            v-model="newComment"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 3 }"
            :rows="1"
            placeholder="写下你的评论..."
            maxlength="500"
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
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, UserFilled, ChatDotRound, Star } from '@element-plus/icons-vue'
import {
  commentListApi,
  createCommentApi,
  deleteCommentApi,
  commentCountApi,
  commentRepliesApi,
  likeCommentApi
} from '@/api/interaction'
import { useUserStore } from '@/store/user'

const props = defineProps({
  visible: Boolean,
  songId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:visible', 'comment-count'])

const userStore = useUserStore()
const GATEWAY_BASE = 'http://localhost:9000'

// ===== 状态 =====
const comments = ref([])
const total = ref(0)
const pn = ref(1)
const size = 20
const hasMore = ref(false)
const loading = ref(false)
const submitting = ref(false)
const newComment = ref('')
const listRef = ref(null)

// 楼中楼
const expandedReplies = reactive({})   // { commentId: true }
const repliesMap = reactive({})       // { commentId: [replies...] }
const replyingTo = ref(null)           // 当前正在回复哪条一级评论
const replyContent = ref('')
const replying = ref(false)

// ===== 工具 =====
function resolveAvatar(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return GATEWAY_BASE + (url.startsWith('/') ? '' : '/') + url
}

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

function isMine(item) {
  const myId = userStore.userInfo?.userId
  return myId != null && item.userId === myId
}

// ===== 加载一级评论列表 =====
async function loadList() {
  if (!props.songId) return
  loading.value = true
  try {
    const res = await commentListApi(props.songId, pn.value, size)
    const records = res.data?.records || []
    if (pn.value === 1) {
      comments.value = records
    } else {
      comments.value.push(...records)
    }
    hasMore.value = comments.value.length < (res.data?.total || 0)
  } catch (e) {
    // 静默
  } finally {
    loading.value = false
  }
}

// ===== 加载评论总数（含楼中楼）=====
async function loadCount() {
  if (!props.songId) return
  try {
    const res = await commentCountApi(props.songId)
    total.value = res.data ?? 0
  } catch (e) {
    // 静默
  }
}

function loadMore() {
  pn.value++
  loadList()
}

// ===== 发表一级评论 =====
async function handleSubmit() {
  const content = newComment.value.trim()
  if (!content || submitting.value) return
  submitting.value = true
  try {
    const res = await createCommentApi(props.songId, { content })
    newComment.value = ''
    ElMessage.success('评论成功')
    // 把新评论插入列表顶部（乐观更新）
    const newItem = res.data
    if (newItem) {
      // 补头像 URL 处理好
      comments.value.unshift(normalizeComment(newItem))
    } else {
      pn.value = 1
      await loadList()
    }
    // 更新总数
    await loadCount()
    emit('comment-count', total.value)
  } catch (e) {
    // request 已弹窗
  } finally {
    submitting.value = false
  }
}

// 后端返回的字段名和前端约定做归一化
function normalizeComment(c) {
  return {
    ...c,
    replyCount: c.replyCount ?? 0,
    avatar: c.avatar || '',
    liked: c.liked ?? false
  }
}

// ===== 点赞 =====
async function handleLike(item) {
  try {
    await likeCommentApi(item.id)
    item.likes = (item.likes || 0) + 1
    item.liked = true
  } catch (e) {
    // 静默
  }
}

// ===== 删除 =====
async function handleDelete(item, parentItem) {
  try {
    await deleteCommentApi(item.id)
    ElMessage.success('删除成功')
    if (parentItem) {
      // 删楼中楼 → 从 repliesMap 里移除
      const arr = repliesMap[parentItem.id]
      if (arr) {
        repliesMap[parentItem.id] = arr.filter(r => r.id !== item.id)
      }
      parentItem.replyCount = Math.max(0, (parentItem.replyCount || 0) - 1)
      // 如果删完了，收起
      if ((parentItem.replyCount || 0) === 0) {
        delete expandedReplies[parentItem.id]
        delete repliesMap[parentItem.id]
      }
    } else {
      // 删一级评论 → 从 comments 移除
      comments.value = comments.value.filter(c => c.id !== item.id)
    }
    await loadCount()
    emit('comment-count', total.value)
  } catch (e) {
    // request 已弹窗
  }
}

// ===== 楼中楼：展开/收起 =====
async function loadReplies(item) {
  if (repliesMap[item.id]) {
    expandedReplies[item.id] = true
    return
  }
  try {
    const res = await commentRepliesApi(item.id)
    repliesMap[item.id] = res.data || []
    expandedReplies[item.id] = true
  } catch (e) {
    // 静默
  }
}

function collapseReplies(item) {
  expandedReplies[item.id] = false
}

function toggleReply(item) {
  if (replyingTo.value?.id === item.id) {
    replyingTo.value = null
    replyContent.value = ''
  } else {
    replyingTo.value = item
    replyContent.value = ''
    // 自动展开回复列表（如果还没展开）
    if (!expandedReplies[item.id]) loadReplies(item)
  }
}

// ===== 提交回复 =====
async function submitReply() {
  if (!replyingTo.value) return
  const content = replyContent.value.trim()
  if (!content || replying.value) return
  replying.value = true
  try {
    const parent = replyingTo.value
    const res = await createCommentApi(props.songId, {
      content,
      parentId: parent.id,
      toUserId: parent.userId
    })
    replyContent.value = ''
    ElMessage.success('回复成功')
    // 乐观更新：插入到 repliesMap
    if (res.data) {
      const list = repliesMap[parent.id] || []
      repliesMap[parent.id] = [...list, normalizeComment(res.data)]
    } else {
      await loadReplies(parent)
    }
    parent.replyCount = (parent.replyCount || 0) + 1
    await loadCount()
    emit('comment-count', total.value)
  } catch (e) {
    // request 已弹窗
  } finally {
    replying.value = false
  }
}

// ===== 关闭 =====
function close() {
  emit('update:visible', false)
}

// ===== 打开时加载 =====
watch(
  () => props.visible,
  (val) => {
    if (val && props.songId) {
      pn.value = 1
      // 清空楼中楼缓存
      Object.keys(expandedReplies).forEach(k => delete expandedReplies[k])
      Object.keys(repliesMap).forEach(k => delete repliesMap[k])
      replyingTo.value = null
      loadList()
      loadCount()
    }
  }
)
</script>

<style scoped lang="scss">
.comment-drawer {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: flex-end;
}

.drawer-content {
  width: 420px;
  max-width: 90vw;
  height: 100vh;
  background: var(--st-canvas);
  border-radius: 16px 0 0 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: -8px 0 32px rgba(0, 0, 0, 0.15);
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
  &:hover { background: var(--st-hairline); color: var(--st-ink); }
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
}

.loading-tip, .empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--st-ink-mute);
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--st-hairline);
  &:last-child { border-bottom: none; }
}

.avatar, .my-avatar, .reply-avatar {
  border-radius: 50%;
  overflow: hidden;
  background: var(--st-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  img { width: 100%; height: 100%; object-fit: cover; }
  .el-icon { color: var(--st-ink-mute); }
}
.avatar { width: 40px; height: 40px; .el-icon { font-size: 20px; } }
.my-avatar { width: 36px; height: 36px; }
.reply-avatar { width: 28px; height: 28px; }

.comment-body { flex: 1; min-width: 0; }

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

/* 操作栏 */
.comment-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: var(--st-ink-mute);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
  transition: color 200ms;
  &:hover { color: var(--st-ink); }
  &.like-btn.active { color: #f5a623; }
  &.del-btn:hover { color: var(--st-ruby, #ea2261); }
}

/* 楼中楼 */
.replies-block {
  margin-top: 12px;
  background: var(--st-input-bg);
  border-radius: 10px;
  padding: 10px 12px;
}
.replies-toggle {
  font-size: 12px;
  color: var(--st-primary);
  cursor: pointer;
  padding: 4px 0;
  user-select: none;
  &:hover { text-decoration: underline; }
}
.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  & + & { border-top: 1px dashed var(--st-hairline); }
}
.reply-body {
  flex: 1;
  font-size: 13px;
  line-height: 1.6;
  color: var(--st-ink-secondary);
  word-break: break-all;
}
.reply-username { font-weight: 600; color: var(--st-ink); }
.reply-at, .reply-colon { color: var(--st-ink-mute); }
.reply-at-name { color: var(--st-primary); }
.reply-time {
  margin-left: 6px;
  font-size: 11px;
  color: var(--st-ink-mute);
}
.reply-del-btn {
  margin-left: 6px;
  border: none;
  background: transparent;
  color: var(--st-ink-mute);
  font-size: 11px;
  cursor: pointer;
  &:hover { color: var(--st-ruby, #ea2261); }
}

/* 回复输入框 */
.reply-input {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  input {
    flex: 1;
    background: var(--st-input-bg);
    border: none;
    border-radius: 16px;
    padding: 6px 14px;
    font-size: 13px;
    outline: none;
    color: var(--st-ink);
  }
  input::placeholder { color: var(--st-ink-mute); }
  .reply-send {
    background: var(--st-primary);
    color: #fff;
    border: none;
    border-radius: 16px;
    padding: 6px 14px;
    font-size: 13px;
    cursor: pointer;
    &:disabled { opacity: 0.5; cursor: not-allowed; }
    &:hover:not(:disabled) { opacity: 0.9; }
  }
}

.load-more {
  text-align: center;
  padding: 16px;
  color: var(--st-primary);
  font-size: 14px;
  cursor: pointer;
  &:hover { text-decoration: underline; }
}

/* 底部评论输入 */
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
  &:hover:not(:disabled) { background: var(--st-primary-hover); transform: scale(1.05); }
  &:active:not(:disabled) { transform: scale(0.95); }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

/* 抽屉动画（右侧滑入） */
.drawer-enter-active, .drawer-leave-active {
  transition: opacity 300ms ease;
  .drawer-content { transition: transform 300ms ease; }
}
.drawer-enter-from, .drawer-leave-to {
  opacity: 0;
  .drawer-content { transform: translateX(100%); }
}
</style>
