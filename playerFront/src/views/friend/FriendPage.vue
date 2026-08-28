<template>
  <div class="friend-page">
    <!-- 左侧：好友列表 + 搜索 -->
    <div class="friend-sidebar">
      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" class="search-btn" @click="handleSearch">
          搜索
        </el-button>
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
              <span class="user-about">{{ user.about || '这个人很懒，什么都没写' }}</span>
            </div>
          </div>
          <el-button size="small" type="primary" @click="showAddFriendDialog(user)">
            添加好友
          </el-button>
        </div>
      </div>

      <!-- 好友请求入口 -->
      <div class="request-entry" @click="showRequestDialog = true">
        <el-icon><Bell /></el-icon>
        <span>好友请求</span>
        <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
      </div>

      <!-- 好友列表 -->
      <div class="friend-list">
        <div class="section-title">我的好友 ({{ friendList.length }}/300)</div>
        <div v-if="friendList.length === 0" class="empty-tip">
          还没有好友，快去搜索添加吧
        </div>
        <div
          v-for="friend in friendList"
          :key="friend.id"
          class="friend-item"
        >
          <el-avatar :size="40" :src="friend.friendAvatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="friend-info">
            <span class="friend-name">{{ friend.friendName }}</span>
            <span class="friend-time">{{ formatTime(friend.createTime) }}</span>
          </div>
          <el-dropdown trigger="click" class="friend-actions">
            <el-icon class="more-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleDeleteFriend(friend)">
                  删除好友
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 右侧：好友请求弹窗 -->
    <el-dialog
      v-model="showRequestDialog"
      title="好友请求"
      width="600px"
    >
      <el-tabs v-model="activeTab">
        <!-- 收到的请求 -->
        <el-tab-pane label="收到的请求" name="received">
          <div v-if="receivedRequests.length === 0" class="empty-tip">
            暂无收到的请求
          </div>
          <div
            v-for="req in receivedRequests"
            :key="req.id"
            class="request-item"
          >
            <el-avatar :size="40" :src="req.senderAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="request-info">
              <span class="request-name">{{ req.senderName }}</span>
              <span class="request-msg">{{ req.message || '想添加你为好友' }}</span>
              <span class="request-time">{{ formatTime(req.createTime) }}</span>
            </div>
            <div class="request-actions">
              <el-button size="small" type="primary" @click="handleAccept(req.id)">
                接受
              </el-button>
              <el-button size="small" @click="handleReject(req.id)">
                拒绝
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 发送的请求 -->
        <el-tab-pane label="我发送的" name="sent">
          <div v-if="sentRequests.length === 0" class="empty-tip">
            暂无发送的请求
          </div>
          <div
            v-for="req in sentRequests"
            :key="req.id"
            class="request-item"
          >
            <el-avatar :size="40" :src="req.receiverAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="request-info">
              <span class="request-name">{{ req.receiverName }}</span>
              <span class="request-msg">{{ req.message || '想添加对方为好友' }}</span>
              <span class="request-time">{{ formatTime(req.createTime) }}</span>
              <el-tag
                :type="req.status === 0 ? 'warning' : req.status === 1 ? 'success' : 'info'"
                size="small"
              >
                {{ req.status === 0 ? '待处理' : req.status === 1 ? '已接受' : '已拒绝' }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 添加好友弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加好友"
      width="400px"
    >
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
        <el-button type="primary" :loading="sending" @click="handleSendRequest">
          发送请求
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  UserFilled,
  Bell,
  MoreFilled
} from '@element-plus/icons-vue'
import {
  searchUsersApi,
  getFriendListApi,
  getReceivedRequestsApi,
  getSentRequestsApi,
  sendFriendRequestApi,
  acceptFriendRequestApi,
  rejectFriendRequestApi,
  deleteFriendApi
} from '@/api/friend'

// 搜索相关
const searchKeyword = ref('')
const searchResults = ref([])

// 好友列表
const friendList = ref([])

// 好友请求
const showRequestDialog = ref(false)
const activeTab = ref('received')
const receivedRequests = ref([])
const sentRequests = ref([])
const unreadCount = ref(0)

// 添加好友
const showAddDialog = ref(false)
const targetUser = ref(null)
const addMessage = ref('')
const sending = ref(false)

// 搜索用户
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const res = await searchUsersApi(searchKeyword.value.trim())
    searchResults.value = res.data || []
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到相关用户')
    }
  } catch (error) {
    ElMessage.error(error.message || '搜索失败')
  }
}

// 加载好友列表
async function loadFriendList() {
  try {
    const res = await getFriendListApi()
    friendList.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载好友列表失败')
  }
}

// 加载好友请求
async function loadFriendRequests() {
  try {
    const [receivedRes, sentRes] = await Promise.all([
      getReceivedRequestsApi(),
      getSentRequestsApi()
    ])
    receivedRequests.value = receivedRes.data || []
    sentRequests.value = sentRes.data || []
    unreadCount.value = receivedRequests.value.length
  } catch (error) {
    ElMessage.error(error.message || '加载好友请求失败')
  }
}

// 显示添加好友弹窗
function showAddFriendDialog(user) {
  targetUser.value = user
  addMessage.value = ''
  showAddDialog.value = true
}

// 发送好友请求
async function handleSendRequest() {
  if (!targetUser.value) return
  sending.value = true
  try {
    await sendFriendRequestApi({
      receiverId: targetUser.value.id,
      message: addMessage.value
    })
    ElMessage.success('好友请求已发送')
    showAddDialog.value = false
    searchResults.value = searchResults.value.filter(u => u.id !== targetUser.value.id)
  } catch (error) {
    ElMessage.error(error.message || '发送请求失败')
  } finally {
    sending.value = false
  }
}

// 接受好友请求
async function handleAccept(requestId) {
  try {
    await acceptFriendRequestApi(requestId)
    ElMessage.success('已接受好友请求')
    receivedRequests.value = receivedRequests.value.filter(r => r.id !== requestId)
    unreadCount.value = receivedRequests.value.length
    loadFriendList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 拒绝好友请求
async function handleReject(requestId) {
  try {
    await rejectFriendRequestApi(requestId)
    ElMessage.success('已拒绝好友请求')
    receivedRequests.value = receivedRequests.value.filter(r => r.id !== requestId)
    unreadCount.value = receivedRequests.value.length
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 删除好友
function handleDeleteFriend(friend) {
  ElMessageBox.confirm(
    `确定要删除好友"${friend.friendName}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteFriendApi(friend.friendId)
      ElMessage.success('已删除好友')
      loadFriendList()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 格式化时间
function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 页面加载时获取数据
onMounted(() => {
  loadFriendList()
  loadFriendRequests()
})
</script>

<style scoped lang="scss">
.friend-page {
  display: flex;
  height: calc(100vh - var(--topbar-height));
  background: var(--st-canvas);
}

/* 左侧好友列表 */
.friend-sidebar {
  width: 320px;
  border-right: 1px solid var(--st-hairline);
  display: flex;
  flex-direction: column;
  background: #fff;
}

.search-section {
  padding: 16px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid var(--st-hairline);

  .search-btn {
    flex-shrink: 0;
  }
}

.search-results {
  max-height: 200px;
  overflow-y: auto;
  border-bottom: 1px solid var(--st-hairline);

  .section-title {
    padding: 12px 16px 8px;
    font-size: 13px;
    color: var(--st-ink-secondary);
  }
}

.request-entry {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid var(--st-hairline);
  transition: background 150ms ease;

  &:hover {
    background: var(--st-primary-subdued);
  }

  .el-icon {
    font-size: 18px;
    color: var(--st-primary);
  }

  span {
    flex: 1;
    font-size: 14px;
    color: var(--st-ink);
  }

  .unread-badge {
    :deep(.el-badge__content) {
      background-color: #f56c6c;
    }
  }
}

.friend-list {
  flex: 1;
  overflow-y: auto;

  .section-title {
    padding: 12px 16px 8px;
    font-size: 13px;
    color: var(--st-ink-secondary);
  }
}

.empty-tip {
  padding: 40px 16px;
  text-align: center;
  color: var(--st-ink-secondary);
  font-size: 14px;
}

.user-item,
.friend-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-primary-subdued);
  }
}

.user-info,
.friend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;

  .username,
  .friend-name {
    font-size: 14px;
    color: var(--st-ink);
    font-weight: 500;
  }

  .user-about,
  .friend-time {
    font-size: 12px;
    color: var(--st-ink-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.friend-actions {
  .more-icon {
    font-size: 16px;
    color: var(--st-ink-secondary);
    cursor: pointer;

    &:hover {
      color: var(--st-primary);
    }
  }
}

/* 请求弹窗 */
.request-item {
  padding: 12px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--st-hairline);

  &:last-child {
    border-bottom: none;
  }
}

.request-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;

  .request-name {
    font-size: 14px;
    color: var(--st-ink);
    font-weight: 500;
  }

  .request-msg {
    font-size: 12px;
    color: var(--st-ink-secondary);
  }

  .request-time {
    font-size: 11px;
    color: var(--st-ink-secondary);
  }
}

.request-actions {
  display: flex;
  gap: 8px;
}

/* 添加好友弹窗 */
.add-friend-form {
  .target-user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .target-name {
      font-size: 16px;
      font-weight: 500;
      color: var(--st-ink);
    }
  }
}
</style>