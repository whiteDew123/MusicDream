<template>
  <div class="friend-page">
    <!-- ========== 左侧：会话/好友列表 ========== -->
    <div class="friend-sidebar">
      <!-- Tab 切换 -->
      <div class="sidebar-tabs">
        <div
          :class="['tab-item', activeList === 'conversations' && 'active']"
          @click="switchList('conversations')"
        >
          消息
          <el-badge
            v-if="totalUnread > 0"
            :value="totalUnread"
            class="tab-badge"
          />
        </div>
        <div
          :class="['tab-item', activeList === 'friends' && 'active']"
          @click="switchList('friends')"
        >
          好友 ({{ friendList.length }})
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div class="section-title">搜索结果</div>
        <div
          v-for="user in searchResults"
          :key="user.id"
          class="user-item"
        >
          <div class="user-info">
            <el-avatar :size="40" :src="user.imageUrl">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="user-detail">
              <span class="username">{{ user.username }}</span>
              <span class="user-about">{{ user.about || '这个人很懒' }}</span>
            </div>
          </div>
          <el-button size="small" type="primary" @click="showAddFriendDialog(user)">
            添加
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-else-if="activeList === 'conversations'" class="conversation-list">
        <div v-if="conversationList.length === 0" class="empty-tip">
          暂无消息，快去和好友聊聊天吧
        </div>
        <div
          v-for="conv in conversationList"
          :key="conv.id"
          :class="['conv-item', currentFriendId === conv.friendId && 'active']"
          @click="openChat(conv)"
        >
          <el-avatar :size="44" :src="conv.friendAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="conv-info">
            <div class="conv-top">
              <span class="conv-name">{{ conv.friendName }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMsgTime) }}</span>
            </div>
            <div class="conv-preview">
              <span class="preview-text">{{ conv.lastMessage || '暂无消息' }}</span>
              <el-badge
                v-if="conv.unreadCount > 0"
                :value="conv.unreadCount"
                class="unread-badge"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 好友列表 -->
      <div v-else class="friend-list">
        <div class="request-entry" @click="showRequestDialog = true">
          <el-icon><Bell /></el-icon>
          <span>好友请求</span>
          <el-badge v-if="unreadRequestCount > 0" :value="unreadRequestCount" class="req-badge" />
        </div>
        <div v-if="friendList.length === 0" class="empty-tip">
          还没有好友，快去搜索添加吧
        </div>
        <div
          v-for="friend in friendList"
          :key="friend.id"
          :class="['friend-item', currentFriendId === friend.friendId && 'active']"
          @click="openChatWithFriend(friend)"
        >
          <el-avatar :size="40" :src="friend.friendAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="friend-info">
            <span class="friend-name">{{ friend.friendName }}</span>
            <span class="friend-time">{{ formatTime(friend.createTime) }}</span>
          </div>
          <el-dropdown trigger="click" @click.stop>
            <el-icon class="more-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDeleteFriend(friend)">删除好友</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- ========== 右侧：聊天面板 ========== -->
    <div class="chat-main" v-if="currentFriendId">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-header-left">
          <el-avatar :size="40" :src="currentFriendAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="chat-header-meta">
            <span class="chat-header-name">{{ currentFriendName }}</span>
            <span class="chat-header-status">在线</span>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="msgListRef" class="msg-list">
        <div class="msg-time-divider" v-if="messageList.length > 0">今天</div>
        <div
          v-for="msg in messageList"
          :key="msg.id"
          :class="['msg-row', msg.senderId === currentUserId && 'me']"
        >
          <el-avatar v-if="msg.senderId !== currentUserId" :size="28" :src="msg.senderAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="bubble-wrap">
            <div :class="['bubble', msg.senderId === currentUserId ? 'me' : 'other']">
              {{ msg.content }}
            </div>
            <div class="msg-meta">{{ formatTime(msg.createTime) }}</div>
          </div>
          <el-avatar v-if="msg.senderId === currentUserId" :size="28">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
        </div>
        <div v-if="messageList.length === 0" class="empty-chat">
          开始和 <b>{{ currentFriendName }}</b> 聊天吧 🎉
        </div>
      </div>

      <!-- 输入区 -->
      <div class="composer">
        <el-input
          v-model="inputContent"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入消息..."
          :disabled="sending"
          @keydown.enter.ctrl="handleSend"
        />
        <el-button
          type="primary"
          :loading="sending"
          :disabled="!inputContent.trim()"
          @click="handleSend"
        >
          发送
        </el-button>
      </div>
    </div>

    <!-- 未选好友时的空态 -->
    <div v-else class="empty-chat-main">
      <div class="empty-chat-icon">💬</div>
      <div class="empty-chat-text">选择一个好友开始聊天</div>
    </div>

    <!-- ========== 好友请求弹窗 ========== -->
    <el-dialog v-model="showRequestDialog" title="好友请求" width="520px">
      <el-tabs v-model="activeRequestTab">
        <el-tab-pane label="收到的" name="received">
          <div v-if="receivedRequests.length === 0" class="empty-tip">暂无收到的请求</div>
          <div v-for="req in receivedRequests" :key="req.id" class="request-item">
            <el-avatar :size="40" :src="req.senderAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="request-info">
              <span class="request-name">{{ req.senderName }}</span>
              <span class="request-msg">{{ req.message || '想添加你为好友' }}</span>
            </div>
            <div class="request-actions">
              <el-button size="small" type="primary" @click="handleAccept(req.id)">接受</el-button>
              <el-button size="small" @click="handleReject(req.id)">拒绝</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我发送的" name="sent">
          <div v-if="sentRequests.length === 0" class="empty-tip">暂无发送的请求</div>
          <div v-for="req in sentRequests" :key="req.id" class="request-item">
            <el-avatar :size="40" :src="req.receiverAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="request-info">
              <span class="request-name">{{ req.receiverName }}</span>
              <span class="request-msg">{{ req.message || '想添加对方为好友' }}</span>
              <el-tag :type="req.status === 0 ? 'warning' : req.status === 1 ? 'success' : 'info'" size="small">
                {{ req.status === 0 ? '待处理' : req.status === 1 ? '已接受' : '已拒绝' }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- ========== 添加好友弹窗 ========== -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="400px">
      <div v-if="targetUser" class="add-friend-form">
        <div class="target-user-info">
          <el-avatar :size="50" :src="targetUser.imageUrl">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="target-name">{{ targetUser.username }}</span>
        </div>
        <el-input
          v-model="addMessage"
          type="textarea"
          :rows="3"
          placeholder="发送验证消息（可选）"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="sendingReq" @click="handleSendRequest">发送请求</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, UserFilled, Bell, MoreFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { createFriendSocket } from '@/utils/friend-socket'
import {
  searchUsersApi,
  getFriendListApi,
  getReceivedRequestsApi,
  getSentRequestsApi,
  sendFriendRequestApi,
  acceptFriendRequestApi,
  rejectFriendRequestApi,
  deleteFriendApi,
  getConversationsApi,
  sendMessageApi,
  getMessageHistoryApi,
  markMessageReadApi,
  getUnreadCountApi
} from '@/api/friend'

const userStore = useUserStore()
const route = useRoute()
const currentUserId = computed(() => userStore.userInfo?.id)

// ========== 列表切换 ==========
const activeList = ref('conversations')
const switchList = (tab) => {
  activeList.value = tab
  if (tab === 'conversations') loadConversations()
  if (tab === 'friends') loadFriendList()
}

// ========== 搜索 ==========
const searchKeyword = ref('')
const searchResults = ref([])
async function handleSearch() {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await searchUsersApi(searchKeyword.value.trim())
    searchResults.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '搜索失败')
  }
}

// ========== 会话列表 ==========
const conversationList = ref([])
const totalUnread = ref(0)
async function loadConversations() {
  try {
    const res = await getConversationsApi()
    conversationList.value = res.data || []
    totalUnread.value = conversationList.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  } catch (e) {
    // 会话表可能还没数据，静默失败
  }
}

// ========== 好友列表 ==========
const friendList = ref([])
async function loadFriendList() {
  try {
    const res = await getFriendListApi()
    friendList.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载好友列表失败')
  }
}

// ========== 好友请求 ==========
const showRequestDialog = ref(false)
const activeRequestTab = ref('received')
const receivedRequests = ref([])
const sentRequests = ref([])
const unreadRequestCount = ref(0)
async function loadFriendRequests() {
  try {
    const [r1, r2] = await Promise.all([getReceivedRequestsApi(), getSentRequestsApi()])
    receivedRequests.value = r1.data || []
    sentRequests.value = r2.data || []
    unreadRequestCount.value = receivedRequests.value.filter(r => r.status === 0).length
  } catch (e) {}
}

// ========== 聊天状态 ==========
const currentFriendId = ref(null)
const currentFriendName = ref('')
const currentFriendAvatar = ref('')
const messageList = ref([])
const inputContent = ref('')
const sending = ref(false)
const msgListRef = ref(null)

// ========== WebSocket ==========
let friendSocket = null
const wsConnected = ref(false)

// 收到新消息（从 WS 推送）
function onNewMessage(data) {
  // data 是 Result<FriendMessage> 结构
  const msg = data?.data || data
  if (!msg || !msg.id) return

  // 如果当前正在和这个好友聊天，直接追加到消息列表
  if (currentFriendId.value &&
      (msg.senderId === currentFriendId.value || msg.receiverId === currentFriendId.value)) {
    messageList.value.push(msg)
    scrollToBottom()
    // 如果是对方发来的，自动标记已读
    if (msg.senderId === currentFriendId.value) {
      markMessageReadApi(currentFriendId.value).catch(() => {})
    }
  }
  // 刷新会话列表（更新最后消息 + 未读数）
  loadConversations()
}

// 发送消息：优先用 WebSocket，失败回退 HTTP
async function handleSend() {
  const content = inputContent.value.trim()
  if (!content || !currentFriendId.value || sending.value) return
  sending.value = true

  // 乐观渲染：先把消息加到列表
  const optimisticMsg = {
    id: Date.now(), // 临时 ID，WS 成功后会被覆盖
    senderId: currentUserId.value,
    receiverId: currentFriendId.value,
    content,
    createTime: new Date().toISOString()
  }
  messageList.value.push(optimisticMsg)
  inputContent.value = ''
  scrollToBottom()

  // 优先 WebSocket
  if (friendSocket && friendSocket.connected) {
    const ok = friendSocket.sendMessage({ friendId: currentFriendId.value, content, msgType: 0 })
    if (ok) {
      // WS 发送成功后，会通过 /user/queue/friend/new 收到自己的消息推送
      // 删掉乐观渲染的临时消息，等服务端回推
      messageList.value = messageList.value.filter(m => m.id !== optimisticMsg.id)
      sending.value = false
      return
    }
  }

  // 回退 HTTP
  try {
    const res = await sendMessageApi({ friendId: currentFriendId.value, content, msgType: 0 })
    // 替换乐观渲染的临时消息为服务端返回的真实消息
    const idx = messageList.value.findIndex(m => m.id === optimisticMsg.id)
    if (idx >= 0) {
      messageList.value.splice(idx, 1, res.data || optimisticMsg)
    }
    loadConversations()
  } catch (e) {
    // 删除失败的乐观消息
    messageList.value = messageList.value.filter(m => m.id !== optimisticMsg.id)
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

// 打开聊天（从会话列表点击）
async function openChat(conv) {
  currentFriendId.value = conv.friendId
  currentFriendName.value = conv.friendName
  currentFriendAvatar.value = conv.friendAvatar
  messageList.value = []
  inputContent.value = ''

  // 标记已读
  if (conv.unreadCount > 0) {
    try { await markMessageReadApi(conv.friendId) } catch (e) {}
    loadConversations()
  }

  // 加载历史
  await loadHistory()
  scrollToBottom()
}

// 从好友列表点击打开聊天
function openChatWithFriend(friend) {
  currentFriendId.value = friend.friendId
  currentFriendName.value = friend.friendName
  currentFriendAvatar.value = friend.friendAvatar
  messageList.value = []
  inputContent.value = ''
  loadHistory().then(scrollToBottom)
  // 切到消息 Tab 并刷新会话列表（确保会话存在）
  loadConversations()
}

// 加载历史消息
async function loadHistory() {
  if (!currentFriendId.value) return
  try {
    const res = await getMessageHistoryApi(currentFriendId.value, null, 50)
    messageList.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载消息失败')
  }
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

// ========== 添加好友 ==========
const showAddDialog = ref(false)
const targetUser = ref(null)
const addMessage = ref('')
const sendingReq = ref(false)
function showAddFriendDialog(user) {
  targetUser.value = user
  addMessage.value = ''
  showAddDialog.value = true
}
async function handleSendRequest() {
  if (!targetUser.value) return
  sendingReq.value = true
  try {
    await sendFriendRequestApi({ receiverId: targetUser.value.id, message: addMessage.value })
    ElMessage.success('请求已发送')
    showAddDialog.value = false
    searchResults.value = searchResults.value.filter(u => u.id !== targetUser.value.id)
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sendingReq.value = false
  }
}
async function handleAccept(id) {
  try {
    await acceptFriendRequestApi(id)
    ElMessage.success('已接受')
    receivedRequests.value = receivedRequests.value.filter(r => r.id !== id)
    loadFriendList()
    loadFriendRequests()
  } catch (e) { ElMessage.error(e.message) }
}
async function handleReject(id) {
  try {
    await rejectFriendRequestApi(id)
    receivedRequests.value = receivedRequests.value.filter(r => r.id !== id)
    loadFriendRequests()
  } catch (e) { ElMessage.error(e.message) }
}
function handleDeleteFriend(friend) {
  ElMessageBox.confirm(`确定删除好友"${friend.friendName}"吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteFriendApi(friend.friendId)
      ElMessage.success('已删除')
      loadFriendList()
      loadConversations()
    } catch (e) { ElMessage.error(e.message) }
  }).catch(() => {})
}

// ========== 工具 ==========
function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

// ========== 生命周期 ==========
onMounted(async () => {
  await Promise.all([loadConversations(), loadFriendList(), loadFriendRequests()])

  // 从 FriendDrawer 跳转过来时，URL 带 ?friendId=xxx，自动打开聊天
  const friendIdParam = route.query.friendId
  if (friendIdParam) {
    const fid = Number(friendIdParam)
    // 先在会话列表里找
    let conv = conversationList.value.find(c => c.friendId === fid)
    if (conv) {
      openChat(conv)
    } else {
      // 会话列表没有（可能还没聊过），从好友列表找
      const friend = friendList.value.find(f => f.friendId === fid)
      if (friend) openChatWithFriend(friend)
    }
  }

  // 初始化 WebSocket（登录态才有意义）
  if (currentUserId.value) {
    try {
      friendSocket = createFriendSocket({
        onNewMessage,
        onStatus: (connected) => { wsConnected.value = connected }
      })
    } catch (e) {
      console.warn('[FriendWS] 初始化失败，将使用 HTTP 模式:', e)
    }
  }
})

onUnmounted(() => {
  if (friendSocket) {
    friendSocket.disconnect()
    friendSocket = null
  }
})
</script>

<style scoped lang="scss">
.friend-page {
  display: flex;
  height: calc(100vh - 64px);
  background: #f6f9fc;
}

/* ========== 左侧栏 ========== */
.friend-sidebar {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e3e8ee;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #e3e8ee;

  .tab-item {
    flex: 1;
    padding: 14px 0;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    color: #697386;
    cursor: pointer;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: color 150ms;

    &.active {
      color: #5e5ce6;
    }

    &.active::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 30%;
      right: 30%;
      height: 2px;
      background: #5e5ce6;
      border-radius: 2px;
    }

    .tab-badge {
      :deep(.el-badge__content) {
        background-color: #5e5ce6;
      }
    }
  }
}

.search-section {
  padding: 12px 16px;
  border-bottom: 1px solid #e3e8ee;
}

.search-results,
.conversation-list,
.friend-list {
  flex: 1;
  overflow-y: auto;
}

.section-title {
  padding: 12px 16px 8px;
  font-size: 13px;
  color: #697386;
}

.empty-tip {
  padding: 40px 16px;
  text-align: center;
  color: #697386;
  font-size: 14px;
}

/* 搜索结果项 */
.user-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background 150ms;

  &:hover { background: #f0f1fe; }

  .user-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .username { font-size: 14px; font-weight: 500; color: #1a1f36; }
    .user-about { font-size: 12px; color: #697386; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }
}

/* 会话列表项 */
.conv-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 150ms;

  &:hover { background: #f0f1fe; }
  &.active { background: #f0f1fe; }

  .conv-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .conv-top {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .conv-name { font-size: 14px; font-weight: 500; color: #1a1f36; }
    .conv-time { font-size: 11px; color: #697386; }
  }

  .conv-preview {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;

    .preview-text {
      font-size: 12px;
      color: #697386;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
    }

    .unread-badge {
      flex-shrink: 0;
      :deep(.el-badge__content) {
        background-color: #5e5ce6;
        border: none;
      }
    }
  }
}

/* 好友列表项 */
.request-entry {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #1a1f36;

  &:hover { background: #f0f1fe; }

  .el-icon { font-size: 18px; color: #5e5ce6; }

  .req-badge {
    margin-left: auto;
    :deep(.el-badge__content) { background-color: #5e5ce6; border: none; }
  }
}

.friend-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 150ms;

  &:hover { background: #f0f1fe; }
  &.active { background: #f0f1fe; }

  .friend-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .friend-name { font-size: 14px; font-weight: 500; color: #1a1f36; }
    .friend-time { font-size: 12px; color: #697386; }
  }

  .more-icon {
    font-size: 16px;
    color: #697386;
    cursor: pointer;
    padding: 4px;

    &:hover { color: #5e5ce6; }
  }
}

/* ========== 聊天主区 ========== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f6f9fc;
  min-width: 0;
}

.chat-header {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e3e8ee;

  .chat-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .chat-header-meta {
    display: flex;
    flex-direction: column;

    .chat-header-name { font-size: 15px; font-weight: 600; color: #1a1f36; }
    .chat-header-status { font-size: 12px; color: #00875a; }
  }
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-time-divider {
  text-align: center;
  font-size: 11px;
  color: #697386;
  margin: 8px 0 12px;
}

.msg-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;

  &.me { flex-direction: row-reverse; }
}

.bubble-wrap {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bubble {
  padding: 8px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;

  &.other {
    background: #fff;
    border: 1px solid #e3e8ee;
    border-bottom-left-radius: 4px;
    color: #1a1f36;
  }

  &.me {
    background: #5e5ce6;
    color: #fff;
    border-bottom-right-radius: 4px;
  }
}

.msg-meta {
  font-size: 10px;
  color: #697386;
  padding: 0 4px;

  .me & { text-align: right; }
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #697386;
  font-size: 14px;

  b { color: #5e5ce6; }
}

/* 输入区 */
.composer {
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid #e3e8ee;
  display: flex;
  gap: 8px;
  align-items: flex-end;

  :deep(.el-textarea__inner) {
    border-radius: 8px;
    border-color: #e3e8ee;
    font-size: 14px;
    resize: none;

    &:focus { border-color: #5e5ce6; }
  }

  .el-button {
    flex-shrink: 0;
    height: 32px;
  }
}

/* ========== 空态 ========== */
.empty-chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;

  .empty-chat-icon {
    font-size: 64px;
    opacity: 0.5;
  }

  .empty-chat-text {
    color: #697386;
    font-size: 14px;
  }
}

/* ========== 弹窗内 ========== */
.request-item {
  padding: 12px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e3e8ee;

  &:last-child { border-bottom: none; }

  .request-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .request-name { font-size: 14px; font-weight: 500; color: #1a1f36; }
    .request-msg { font-size: 12px; color: #697386; }
  }

  .request-actions {
    display: flex;
    gap: 8px;
  }
}

.add-friend-form {
  .target-user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .target-name { font-size: 16px; font-weight: 500; color: #1a1f36; }
  }
}
</style>
