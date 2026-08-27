<template>
  <div class="room-page">
    <!-- 顶部栏 -->
    <header class="room-header">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </button>
      <div class="header-center">
        <span class="room-title">{{ room ? room.name : '加载中…' }}</span>
        <button v-if="room" class="invite-chip" @click="copyInvite">
          <el-icon><Link /></el-icon>
          <span class="chip-code">{{ room.inviteCode }}</span>
        </button>
      </div>
      <div class="header-actions">
        <span class="member-count">
          <el-icon><UserFilled /></el-icon>
          {{ room ? room.memberCount : 0 }}/{{ room ? room.maxMembers : 0 }}
        </span>
        <el-button v-if="isOwner" type="danger" plain round size="small" @click="closeRoom">关闭房间</el-button>
        <el-button v-else plain round size="small" @click="leaveRoom">退出房间</el-button>
      </div>
    </header>

    <!-- 主体 -->
    <div class="room-body">
      <!-- 左：播放器区 -->
      <section class="player-panel">
        <div class="player-card">
          <div class="album-cover" :style="{ background: coverBg }">
            <img v-if="room && room.currentMusicCover" :src="room.currentMusicCover" :alt="room.currentMusicName" />
            <span v-else class="album-symbol">♪</span>
            <div class="glow"></div>
          </div>
          <div class="player-info">
            <div class="now-title">{{ room && room.currentMusicName ? room.currentMusicName : '房间里的第一首歌' }}</div>
            <div class="now-sub">
              {{ room && room.isPlaying === 1 ? '播放中' : '空闲' }}
              <span class="dot">·</span>
              <span>歌曲经房主同步共享</span>
            </div>
            <div class="player-controls">
              <el-button class="play-btn" round :disabled="!room || !room.currentMusicId" @click="onPlayPlaceholder">
                <el-icon><VideoPlay /></el-icon>
                开始播放
              </el-button>
              <template v-if="!isOwner">
                <el-button class="skip-btn" round :disabled="!room || !room.currentMusicId" @click="onInitiateSkip">
                  <el-icon><RefreshRight /></el-icon>
                  发起切歌
                </el-button>
                <el-button class="skip-btn" round :disabled="!room || !room.currentMusicId" @click="onAgreeVote">
                  <el-icon><Select /></el-icon>
                  附议
                </el-button>
              </template>
            </div>
            <p class="sync-hint">* P1 将接入 WebSocket 实时播放同步，误差 &lt; 2s</p>
          </div>
        </div>

        <!-- 聊天占位 -->
        <div class="chat-panel">
          <div class="chat-title">
            <el-icon><ChatDotRound /></el-icon>
            一起聊
          </div>
          <div class="chat-body">
            <div class="chat-empty">
              <p>边听边聊（文字 / Emoji 快捷消息将在 P2 上线）</p>
              <el-input placeholder="说点什么…" disabled />
            </div>
          </div>
        </div>
      </section>

      <!-- 右：歌单 + 成员 -->
      <section class="side-panel">
        <!-- 歌单 -->
        <div class="playlist-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><List /></el-icon>
              房间歌单
            </span>
            <el-button type="primary" size="small" round @click="openAddDialog">
              <el-icon><Plus /></el-icon>
              添加歌曲
            </el-button>
          </div>

          <div v-if="playlistLoading" class="playlist-empty">加载中…</div>
          <div v-else-if="playlist.length === 0" class="playlist-empty">歌单空空，去添加歌曲吧~</div>
          <div v-else class="playlist">
            <div v-for="(song, idx) in playlist" :key="song.playlistId || song.musicId" class="playlist-item" :class="{ active: room && room.currentMusicId === song.musicId }">
              <span class="item-index">{{ idx + 1 }}</span>
              <div class="item-cover">
                <img v-if="song.cover" :src="song.cover" />
                <el-icon v-else><Headset /></el-icon>
              </div>
              <div class="item-info">
                <span class="item-name">{{ song.musicName }}</span>
                <span class="item-singer">{{ song.addedByName || '未知' }} 添加</span>
              </div>
              <span class="item-time">{{ formatTime(song.duration) }}</span>
              <button class="item-del" @click="removeSong(song)">
                <el-icon><Close /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <!-- 成员 -->
        <div class="members-panel">
          <div class="panel-header">
            <span class="panel-title">
              <el-icon><UserFilled /></el-icon>
              成员
            </span>
          </div>
          <div class="members-list">
            <div v-for="m in members" :key="m.userId" class="member-item">
              <div class="member-avatar">
                <img v-if="m.imageUrl" :src="m.imageUrl" />
                <el-icon v-else><User /></el-icon>
              </div>
              <span class="member-name">{{ m.username || ('用户' + m.userId) }}</span>
              <span v-if="m.role === 0" class="owner-badge">房主</span>
              <span v-else-if="m.isOnline === 1" class="online-dot">在线</span>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 添加歌曲弹窗 -->
    <el-dialog v-model="addVisible" title="添加歌曲到歌单" width="520px" align-center>
      <el-input v-model="keyword" placeholder="搜索歌曲" clearable @keyup.enter="doSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <div v-if="searchLoading" class="search-state">加载中…</div>
      <div v-else-if="searchResults.length === 0" class="search-state empty">暂无搜索结果</div>
      <div v-else class="search-list">
        <div v-for="song in searchResults" :key="song.musicId" class="search-item">
          <div class="item-cover">
            <img v-if="song.imageUrl" :src="song.imageUrl" />
            <el-icon v-else><Headset /></el-icon>
          </div>
          <div class="item-info">
            <span class="item-name">{{ song.musicName }}</span>
            <span class="item-singer">{{ song.singerName }}</span>
          </div>
          <el-button type="primary" size="small" round :loading="addingId === song.musicId" @click="addSong(song)">
            加入
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="addVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Link, UserFilled, VideoPlay, RefreshRight, ChatDotRound,
  List, Plus, Close, Headset, User, Search, Select
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  roomDetailApi, roomPlaylistApi, addRoomPlaylistApi, removeRoomPlaylistApi,
  leaveRoomApi, closeRoomApi, skipVoteApi, agreeVoteApi
} from '@/api/room'
import { searchSongsApi } from '@/api/music'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roomId = route.params.id
const room = ref(null)
const playlist = ref([])
const members = ref([])
const playlistLoading = ref(true)

// 添加歌曲弹窗
const addVisible = ref(false)
const keyword = ref('')
const searchLoading = ref(false)
const searchResults = ref([])
const addingId = ref(null)

const isOwner = computed(() => room.value && room.value.ownerId === userStore.userInfo?.userId)

onMounted(async () => {
  await loadRoom()
  await loadPlaylist()
})

async function loadRoom() {
  try {
    const res = await roomDetailApi(roomId)
    room.value = res.data
    members.value = res.data?.members || []
  } catch (e) {
    ElMessage.error('房间加载失败')
    router.push('/rooms')
  }
}

async function loadPlaylist() {
  playlistLoading.value = true
  try {
    const res = await roomPlaylistApi(roomId)
    playlist.value = res.data || []
  } catch (e) {
    console.error('加载歌单失败:', e)
  } finally {
    playlistLoading.value = false
  }
}

// 复制邀请码
function copyInvite() {
  const code = room.value?.inviteCode
  if (!code) return
  navigator.clipboard?.writeText(code).then(() => {
    ElMessage.success('邀请码已复制')
  }).catch(() => {
    ElMessage.info('邀请码：' + code)
  })
}

// 退出房间
function leaveRoom() {
  leaveRoomApi(roomId).then(() => {
    ElMessage.success('已退出房间')
    router.push('/rooms')
  }).catch(() => {})
}

// 关闭房间（仅房主）
function closeRoom() {
  ElMessageBox.confirm('关闭后房间将解散，所有成员将退出，确定关闭？', '关闭房间', {
    confirmButtonText: '确定关闭',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await closeRoomApi(roomId)
    ElMessage.success('房间已关闭')
    router.push('/rooms')
  }).catch(() => {})
}

// 开始播放占位（P0 骨架，P1 接入 WS 同步）
function onPlayPlaceholder() {
  ElMessage.info('实际播放同步将于 P1 接入 WebSocket 后生效')
}

// 发起切歌投票
async function onInitiateSkip() {
  const musicId = room.value?.currentMusicId
  if (!musicId) return
  try {
    await skipVoteApi(roomId, { musicId })
    ElMessage.success('已发起切歌投票，等待成员附议')
  } catch (e) {
    // 失败提示已由拦截器处理
  }
}

// 附议切歌
async function onAgreeVote() {
  const musicId = room.value?.currentMusicId
  if (!musicId) return
  try {
    const res = await agreeVoteApi(roomId, { musicId })
    if (res.data === true) {
      ElMessage.success('切歌成功')
      loadRoom()
      loadPlaylist()
    } else {
      ElMessage.success('已附议，等待更多成员')
    }
  } catch (e) {
    // 失败提示已由拦截器处理（如已投过票 / 投票过期）
  }
}

// 打开添加歌曲弹窗
function openAddDialog() {
  addVisible.value = true
  keyword.value = ''
  searchResults.value = []
}

// 搜索歌曲
async function doSearch() {
  if (!keyword.value.trim()) return
  searchLoading.value = true
  searchResults.value = []
  try {
    const res = await searchSongsApi({ keyword: keyword.value.trim(), page: 1, size: 20 })
    searchResults.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    searchLoading.value = false
  }
}

// 添加到歌单
async function addSong(song) {
  addingId.value = song.musicId
  try {
    await addRoomPlaylistApi(roomId, { musicId: song.musicId })
    ElMessage.success('已加入歌单')
    await loadPlaylist()
  } catch (e) {
    // 重复等提示已由拦截器处理
  } finally {
    addingId.value = null
  }
}

// 移除歌曲
function removeSong(song) {
  removeRoomPlaylistApi(roomId, song.musicId).then(() => {
    ElMessage.success('已移除')
    loadPlaylist()
  }).catch(() => {})
}

// 封面背景
const coverBg = computed(() => {
  const colors = ['#6341FF', '#7B5EFF', '#4E2AEE', '#1c1e54', '#665EFD']
  const idx = (room.value?.id ?? 0) % colors.length
  return `linear-gradient(135deg, ${colors[idx]}, ${colors[(idx + 1) % colors.length]})`
})

// 时长格式化
function formatTime(seconds) {
  if (!seconds) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function goBack() {
  router.push('/rooms')
}
</script>

<style scoped lang="scss">
.room-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--st-canvas-soft);
  overflow: hidden;
}

/* 顶部栏 */
.room-header {
  height: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--st-canvas);
  border-bottom: 1px solid var(--st-hairline);

  .back-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    background: none;
    border: none;
    color: var(--st-ink-secondary);
    font-size: 14px;
    cursor: pointer;
    &:hover { color: var(--st-primary); }
  }

  .header-center {
    display: flex;
    align-items: center;
    gap: 12px;
    .room-title { font-size: 16px; font-weight: 600; color: var(--st-ink); }
    .invite-chip {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      border: 1px dashed var(--st-primary);
      border-radius: var(--rounded-pill);
      background: rgba(99, 65, 255, 0.06);
      color: var(--st-primary);
      font-size: 13px;
      cursor: pointer;
      transition: background 150ms ease;
      &:hover { background: rgba(99, 65, 255, 0.12); }
      .chip-code { letter-spacing: 1px; font-family: ui-monospace, monospace; }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    .member-count {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--st-ink-mute);
    }
  }
}

/* 主体 */
.room-body {
  flex: 1;
  display: flex;
  gap: 24px;
  padding: 24px;
  min-height: 0;
}

/* 左区 */
.player-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

.player-card {
  flex: 1;
  display: flex;
  gap: 32px;
  align-items: center;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  padding: 32px;
  box-shadow: var(--shadow-1);

  .album-cover {
    position: relative;
    width: 220px;
    height: 220px;
    flex-shrink: 0;
    border-radius: var(--rounded-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    img { width: 100%; height: 100%; object-fit: cover; border-radius: inherit; }
    .album-symbol { font-size: 80px; color: rgba(255, 255, 255, 0.8); }
    .glow {
      position: absolute;
      inset: 8px;
      border-radius: inherit;
      box-shadow: 0 0 60px 8px rgba(99, 65, 255, 0.35);
      pointer-events: none;
    }
  }

  .player-info {
    flex: 1;
    min-width: 0;
    .now-title {
      font-size: 24px;
      font-weight: 300;
      letter-spacing: -0.4px;
      color: var(--st-ink);
      margin-bottom: 8px;
    }
    .now-sub {
      font-size: 13px;
      color: var(--st-ink-mute);
      margin-bottom: 20px;
      .dot { margin: 0 6px; }
    }
    .player-controls {
      display: flex;
      gap: 12px;
      .play-btn {
        height: 44px;
        padding: 0 24px;
      }
      .skip-btn {
        height: 44px;
        padding: 0 20px;
      }
    }
    .sync-hint {
      margin-top: 14px;
      font-size: 12px;
      color: var(--st-ink-mute);
    }
  }
}

.chat-panel {
  height: 220px;
  flex-shrink: 0;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  padding: 20px;
  .chat-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: var(--st-ink);
    margin-bottom: 12px;
    .el-icon { color: var(--st-primary); }
  }
  .chat-body .chat-empty {
    color: var(--st-ink-mute);
    font-size: 13px;
    p { margin-bottom: 12px; }
  }
}

/* 右区 */
.side-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 0;
}

.playlist-panel,
.members-panel {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  padding: 20px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  .panel-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: var(--st-ink);
    .el-icon { color: var(--st-primary); }
  }
}

.playlist { flex: 1; overflow-y: auto; }
.playlist-empty {
  color: var(--st-ink-mute);
  font-size: 13px;
  text-align: center;
  padding: 32px 0;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 6px;
  border-radius: var(--rounded-md);
  transition: background 150ms ease;
  &:hover { background: var(--st-canvas-hover); }
  &.active { background: rgba(99, 65, 255, 0.08); }

  .item-index { width: 20px; text-align: center; color: var(--st-ink-mute); font-size: 12px; }
  .item-cover {
    width: 40px; height: 40px; border-radius: var(--rounded-sm); overflow: hidden;
    background: var(--st-input-bg); display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    img { width: 100%; height: 100%; object-fit: cover; }
    .el-icon { color: var(--st-ink-mute); }
  }
  .item-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
  .item-name { font-size: 13px; color: var(--st-ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .item-singer { font-size: 11px; color: var(--st-ink-mute); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .item-time { font-size: 12px; color: var(--st-ink-mute); font-variant-numeric: tabular-nums; }
  .item-del {
    background: none; border: none; color: var(--st-ink-mute); cursor: pointer; display: flex; padding: 4px;
    &:hover { color: var(--st-ruby, #ea2261); }
  }
}

.members-panel .members-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  .member-avatar {
    width: 34px; height: 34px; border-radius: 50%; overflow: hidden; background: var(--st-input-bg);
    display: flex; align-items: center; justify-content: center; color: var(--st-ink-mute);
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  .member-name { flex: 1; font-size: 13px; color: var(--st-ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .owner-badge { padding: 1px 8px; border-radius: var(--rounded-pill); background: var(--st-primary); color: #fff; font-size: 11px; }
  .online-dot { font-size: 11px; color: var(--st-primary-soft); }
}

/* 添加歌曲弹窗 */
.search-state { color: var(--st-ink-mute); text-align: center; padding: 24px 0; font-size: 13px; }
.search-list { max-height: 320px; overflow-y: auto; margin-top: 12px; }
.search-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 4px;
  .item-cover {
    width: 40px; height: 40px; border-radius: var(--rounded-sm); overflow: hidden; background: var(--st-input-bg);
    display: flex; align-items: center; justify-content: center; flex-shrink: 0;
    img { width: 100%; height: 100%; object-fit: cover; }
    .el-icon { color: var(--st-ink-mute); }
  }
  .item-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
  .item-name { font-size: 13px; color: var(--st-ink); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .item-singer { font-size: 11px; color: var(--st-ink-mute); }
}
</style>
