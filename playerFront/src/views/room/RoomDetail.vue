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
          <div class="player-main">
            <div class="album-cover" :style="{ background: coverBg }">
              <img v-if="currentSongCover" :src="currentSongCover" :alt="currentSongName" />
              <span v-else class="album-symbol">♪</span>
              <div class="glow"></div>
            </div>
            <div class="player-info">
              <div class="now-title">{{ currentSongName }}</div>
              <div class="now-sub">
                {{ syncState.isPlaying === 1 ? '播放中' : '空闲' }}
                <span class="dot">·</span>
                <span>{{ connected ? '实时同步已连接' : '连接中…' }}</span>
              </div>
              <div class="player-controls">
                <el-button v-if="isOwner" class="play-btn" round :disabled="!syncState.musicId && playlist.length === 0" @click="toggleRoomPlay">
                  <el-icon><VideoPlay /></el-icon>
                  {{ roomPlaying ? '暂停' : '开始播放' }}
                </el-button>
                <el-button class="skip-btn" round :disabled="!syncState.musicId || myVoted" @click="onInitiateSkip">
                  <el-icon><RefreshRight /></el-icon>
                  发起切歌
                </el-button>
                <el-button class="skip-btn" round :disabled="!syncState.musicId || myVoted" @click="onAgreeVote">
                  <el-icon><Select /></el-icon>
                  附议
                </el-button>
              </div>
              <div v-if="voteState.active" class="vote-status">
                <span class="vote-label">切歌投票</span>
                <span class="vote-num">{{ voteState.agreeCount }}/{{ voteState.required }}</span>
                <span class="vote-remain">剩 {{ voteState.remaining }}s</span>
                <span v-if="myVoted" class="vote-done">已投票</span>
              </div>
              <p class="sync-hint">{{ isOwner ? '你正在主持播放，进度每 5s 同步一次' : '房主播放会自动同步给所有成员，误差 &lt; 2s' }}</p>
            </div>
          </div>
          <!-- 歌词（对标 FullPlayer：当前行居中高亮 + 上下渐隐 + 点击定位 + 滚轮手动偏移）-->
          <div class="lyrics-panel">
            <div class="lyrics-head">
              <el-icon><Document /></el-icon>
              歌词
            </div>
            <div class="lyrics-view" @wheel.prevent="handleLyricWheel">
              <div
                v-for="line in visibleRoomLyrics"
                :key="line.index"
                class="room-lyric-line"
                :class="{ active: line.offset === 0 }"
                :style="getRoomLyricStyle(line)"
                @click="seekToRoomLyric(line)"
              >
                {{ line.text || '♪' }}
              </div>
              <div v-if="!roomLyrics.length" class="no-room-lyrics">纯音乐，请欣赏 ♪</div>
            </div>
          </div>
          <div v-if="!isOwner && !audioUnlocked" class="unlock-mask" @click="unlockAudio">
            <el-icon><VideoPlay /></el-icon>
            <span>点击开始同步收听</span>
          </div>
        </div>

        <!-- 聊天 -->
        <div class="chat-panel">
          <div class="chat-title">
            <el-icon><ChatDotRound /></el-icon>
            一起聊
            <span class="chat-status" :class="{ on: connected }">{{ connected ? '已连接' : '连接中' }}</span>
          </div>
          <div class="chat-list" ref="chatListRef">
            <div v-if="messages.length === 0" class="chat-empty">开始聊天吧～（支持文字 / Emoji）</div>
            <div v-for="msg in messages" :key="msg.id" class="chat-item" :class="{ me: msg.userId === myUserId, sys: msg.type === 2 }">
              <template v-if="msg.type === 2">
                <span class="sys-text">{{ msg.username ? msg.username + ' ' + msg.content : msg.content }}</span>
              </template>
              <template v-else>
                <span class="chat-name">{{ msg.username || '系统' }}</span>
                <span class="chat-text">{{ msg.content }}</span>
              </template>
            </div>
          </div>
          <div class="chat-input">
            <div class="emoji-bar">
              <button v-for="e in quickEmojis" :key="e" class="emoji-btn" @click="sendChat(1, e)">{{ e }}</button>
            </div>
            <el-input v-model="chatInput" placeholder="说点什么…" @keyup.enter="sendChat(0)">
              <template #append>
                <el-button @click="sendChat(0)">发送</el-button>
              </template>
            </el-input>
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
              <button v-if="isOwner && m.role !== 0" class="transfer-btn" @click="transferOwnership(m)">转让</button>
              <button v-if="isOwner && m.role !== 0" class="kick-btn" @click="kickMember(m)">踢出</button>
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
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Link, UserFilled, VideoPlay, RefreshRight, ChatDotRound,
  List, Plus, Close, Headset, User, Search, Select, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'
import {
  roomDetailApi, roomPlaylistApi, addRoomPlaylistApi, removeRoomPlaylistApi,
  leaveRoomApi, closeRoomApi, roomMessagesApi, kickRoomApi, transferRoomApi
} from '@/api/room'
import { searchSongsApi } from '@/api/music'
import { createRoomSocket } from '@/utils/room-socket'
import { parseLrc, fetchLrc } from '@/utils/lrc'

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

const isOwner = computed(() => room.value && String(room.value.ownerId ?? '') === String(userStore.userInfo?.userId ?? ''))
const myUserId = computed(() => userStore.userInfo?.userId)

// ===== 实时同步状态 =====
const syncState = reactive({ musicId: null, progress: 0, isPlaying: 0 })
const connected = ref(false)
const messages = ref([])
const chatInput = ref('')
const chatListRef = ref(null)
const quickEmojis = ['🎵', '👍', '❤️', '😂', '🔥']

let socket = null
let roomAudio = null
let lastPublishTs = 0
let hasConnectedOnce = false
let lastMusicId = null
let lastSeq = 0
let voteTimer = null
const audioUnlocked = ref(false)

// 切歌投票实时状态（附议数/所需/剩余秒）
const voteState = reactive({ musicId: null, votes: 0, agreeCount: 0, required: 0, remaining: 0, active: false })
const myVoted = ref(false)

// ===== 歌词（对标 FullPlayer，但使用 Stripe 亮色基调）=====
const roomLyrics = ref([])
const currentLyricIndex = ref(-1)
const lyricManualOffset = ref(0)
const LYRIC_LINE_HEIGHT = 44
const LYRIC_HALF = 8
let lyricAutoResetTimer = null

const currentSongName = computed(() => {
  const s = playlist.value.find((x) => x.musicId === syncState.musicId)
  if (s) return s.musicName
  return room.value?.currentMusicName || '房间里的第一首歌'
})
const currentSongCover = computed(() => {
  const s = playlist.value.find((x) => x.musicId === syncState.musicId)
  return s ? s.cover : room.value?.currentMusicCover
})
const roomPlaying = computed(() => syncState.isPlaying === 1)
const currentRoomSong = computed(() => playlist.value.find((x) => x.musicId === syncState.musicId))

// ===== 歌词窗口（当前行 ± LYRIC_HALF，支持滚轮手动偏移，2s 后回位）=====
const visibleRoomLyrics = computed(() => {
  const idx = currentLyricIndex.value
  if (!roomLyrics.value.length || idx < 0) return []
  const baseCenter = idx + lyricManualOffset.value
  const start = Math.max(0, baseCenter - LYRIC_HALF)
  const end = Math.min(roomLyrics.value.length, baseCenter + LYRIC_HALF + 1)
  const result = []
  for (let i = start; i < end; i++) {
    result.push({ ...roomLyrics.value[i], index: i, offset: i - baseCenter })
  }
  return result
})

onMounted(async () => {
  await loadRoom()
  await loadPlaylist()
  // 歌单就绪后补齐当前歌曲歌词（避免 watch 在歌单为空时错过初始歌曲）
  loadRoomLyrics(currentRoomSong.value)
  initRoomAudio()
  // 成员端：应用 REST 返回的初始播放状态，不等 WS 首条推送
  if (!isOwner.value && syncState.musicId) {
    applySyncState({ musicId: syncState.musicId, progress: syncState.progress, isPlaying: syncState.isPlaying })
  }
  // 房主刷新后：恢复音频源和进度，但不自动播放，由房主手动控制
  if (isOwner.value && syncState.musicId) {
    const song = playlist.value.find((x) => x.musicId === syncState.musicId)
    if (song && song.musicUrl) {
      lastMusicId = syncState.musicId
      roomAudio.src = song.musicUrl
      roomAudio.currentTime = syncState.progress || 0
    }
  }
  connectRoomSocket()
  loadMessages()
})

onUnmounted(() => {
  if (socket) socket.disconnect()
  stopVoteTicker()
  if (lyricAutoResetTimer) clearTimeout(lyricAutoResetTimer)
  if (roomAudio) {
    roomAudio.pause()
    roomAudio.removeAttribute('src')
  }
  // 注意：离开页面【不】关闭房间。房间仅由房主在确认弹窗后点「关闭房间」关闭，
  // 否则一次误跳转/刷新/被踢出就会硬删除房间，导致全员被赶出。
})

async function loadRoom() {
  try {
    const res = await roomDetailApi(roomId)
    room.value = res.data
    members.value = res.data?.members || []
    // 同步初始播放状态
    syncState.musicId = res.data?.currentMusicId ?? null
    syncState.progress = res.data?.currentProgress ?? 0
    syncState.isPlaying = res.data?.isPlaying ?? 0
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

// 移出成员（仅房主）
function kickMember(m) {
  ElMessageBox.confirm(`确定将 ${m.username || ('用户' + m.userId)} 移出房间？`, '移出成员', {
    confirmButtonText: '移出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await kickRoomApi(roomId, m.userId)
    ElMessage.success('已移出')
    await loadRoom()
  }).catch(() => {})
}

// 转让房主（仅房主）
function transferOwnership(m) {
  ElMessageBox.confirm(`确定将房主转让给 ${m.username || ('用户' + m.userId)}？`, '转让房主', {
    confirmButtonText: '转让',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await transferRoomApi(roomId, { userId: m.userId })
    ElMessage.success('已转让房主')
    await loadRoom()
  }).catch(() => {})
}

// ===== 房间音频（本地 Audio，用于房间内播放）=====
function initRoomAudio() {
  if (typeof window === 'undefined') return
  roomAudio = new Audio()
  roomAudio.preload = 'auto'
  roomAudio.addEventListener('play', () => {
    if (isOwner.value) publishSync({ isPlaying: 1 })
  })
  roomAudio.addEventListener('pause', () => {
    if (isOwner.value) publishSync({ isPlaying: 0 })
  })
  roomAudio.addEventListener('timeupdate', () => {
    // 歌词跟随（成员也随本地校准后的 currentTime 走）
    updateLyricIndex()
    if (isOwner.value) maybePublishProgress()
  })
  roomAudio.addEventListener('ended', () => {
    if (isOwner.value) publishSync({ isPlaying: 0 })
  })
  roomAudio.addEventListener('error', () => {
    // 暴露真实加载错误，便于定位是 404 / 编码 / 上传服务未启动
    ElMessage.error('歌曲加载失败，请确认音频可访问：' + (roomAudio.src || ''))
  })
}

// ===== 歌词加载与跟随（对标 FullPlayer：自动滚动 + 当前行居中高亮）=====
async function loadRoomLyrics(song) {
  if (!song || !song.lyric) {
    roomLyrics.value = []
    currentLyricIndex.value = -1
    return
  }
  try {
    const text = await fetchLrc(song.lyric)
    roomLyrics.value = parseLrc(text)
    currentLyricIndex.value = -1
    lyricManualOffset.value = 0
  } catch (e) {
    roomLyrics.value = []
    currentLyricIndex.value = -1
  }
}

// 依据当前播放进度计算高亮歌词行（最后一个 time <= currentTime 的行）
function updateLyricIndex() {
  if (!roomLyrics.value.length || !roomAudio) return
  const t = roomAudio.currentTime || 0
  let idx = -1
  for (let i = 0; i < roomLyrics.value.length; i++) {
    if (roomLyrics.value[i].time <= t) idx = i
    else break
  }
  currentLyricIndex.value = idx
}

// 点击歌词行定位
function seekToRoomLyric(line) {
  if (line.time == null || !roomAudio) return
  roomAudio.currentTime = line.time
  lyricManualOffset.value = 0
  if (isOwner.value) publishSync({ progress: line.time })
}

// 滚轮手动偏移歌词，2s 后自动回位
function handleLyricWheel(e) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? 1 : -1
  lyricManualOffset.value += delta
  const idx = currentLyricIndex.value
  const maxOffset = roomLyrics.value.length - 1 - idx
  const minOffset = -idx
  lyricManualOffset.value = Math.max(minOffset, Math.min(maxOffset, lyricManualOffset.value))
  if (lyricAutoResetTimer) clearTimeout(lyricAutoResetTimer)
  lyricAutoResetTimer = setTimeout(() => { lyricManualOffset.value = 0 }, 2000)
}

function getRoomLyricStyle(line) {
  const isActive = line.offset === 0
  return {
    transform: `translateY(${line.offset * LYRIC_LINE_HEIGHT}px)`,
    fontSize: isActive ? '22px' : '15px',
    color: isActive ? 'var(--st-primary)' : 'var(--st-ink-mute)'
  }
}

// 切歌时重新加载歌词
watch(
  () => syncState.musicId,
  (id) => {
    const song = playlist.value.find((x) => x.musicId === id)
    loadRoomLyrics(song)
  }
)

// 成员端：点击解锁自动播放权限（浏览器 autoplay 限制需要一次用户手势）
function unlockAudio() {
  audioUnlocked.value = true
  if (roomAudio && roomAudio.src) {
    const p = roomAudio.play()
    if (p) p.then(() => { if (syncState.isPlaying !== 1) roomAudio.pause() }).catch(() => {})
  }
}

// ===== WebSocket 连接 =====
function connectRoomSocket() {
  socket = createRoomSocket({
    roomId,
    onStatus: (s) => {
      connected.value = s
      if (s) {
        // 断线重连后重新拉取播放状态 / 歌单 / 消息，恢复同步
        if (hasConnectedOnce) {
          loadRoom()
          loadPlaylist()
          loadMessages(true) // 只补漏，避免重复
          loadRoomLyrics(currentRoomSong.value)
        }
        hasConnectedOnce = true
      }
    },
    onState: (msg) => applySyncState(msg),
    onMembers: (list) => {
      const arr = list || []
      members.value = arr
      if (room.value) room.value.memberCount = arr.length
      // 被踢判定：仅当列表非空、且能确认自己身份但在列表中找不到时，才视为被移出。
      // 类型统一转字符串比较，避免 Long(number) 与 localStorage String 误判（否则房主会被误判"被踢"）。
      const me = String(myUserId.value ?? '')
      if (arr.length > 0 && me && !arr.some(m => String(m.userId) === me)) {
        ElMessage.warning('你已被移出房间')
        router.replace('/rooms')
      }
    },
    onMessage: (msg) => {
      // 按 seq 去重，防止重连补漏重复
      if (msg.seq && messages.value.some(x => x.seq === msg.seq)) return
      messages.value.push(msg)
      lastSeq = Math.max(lastSeq, msg.seq || 0)
      scrollChat()
      // 注意：不能按"内容=被移出房间"来判定"我被踢"——该系统消息是广播给全房间的（无目标人），
      // 否则房主踢人时房主自己也会被当成被踢而跳走。被踢判定改用 onMembers（自己不在成员列表才被踢）。
      // 房间关闭的提示/跳转统一由 /closed 主题（onClosed）处理，避免与 onMessage 重复弹两条。
    },
    onVote: (payload) => handleVote(payload),
    onPlaylist: (list) => {
      console.log('[Room] 歌单热更新，共', (list || []).length, '首')
      playlist.value = list || []
      playlistLoading.value = false
    },
    onRoom: (detail) => {
      if (detail && detail.id) {
        room.value = { ...room.value, ...detail }
      }
    },
    onClosed: () => {
      // 房主主动关闭：closeRoom() 已弹绿色"房间已关闭"并跳转，此处跳过避免重复提示
      if (isOwner.value) return
      ElMessage.info('房间已关闭')
      router.push('/rooms')
    }
  })
}

// 成员端应用服务端广播的播放状态
function applySyncState(msg) {
  syncState.musicId = msg.musicId
  syncState.progress = msg.progress
  syncState.isPlaying = msg.isPlaying
  // 同步更新房间级别的当前歌曲/播放状态（热更新）
  if (room.value) {
    if (msg.musicId !== undefined) room.value.currentMusicId = msg.musicId
    if (msg.isPlaying !== undefined) room.value.isPlaying = msg.isPlaying
    if (msg.status !== undefined) room.value.status = msg.status
  }
  // 仅成员端被动跟播；房主本地已自行控制，避免被自己的广播反覆盖而暂停
  if (isOwner.value || !roomAudio) return
  const song = playlist.value.find((x) => x.musicId === msg.musicId)
  // 仅切歌时才重新设置 src，避免每次同步都重载音频导致 error 事件
  if (song && song.musicUrl && msg.musicId !== lastMusicId) {
    lastMusicId = msg.musicId
    roomAudio.src = song.musicUrl
  }
  if (roomAudio.src && roomAudio.src !== window.location.origin + '/') {
    // 偏差超过 2s 才校准，避免频繁跳动
    if (typeof msg.progress === 'number' && Math.abs((roomAudio.currentTime || 0) - msg.progress) > 2) {
      roomAudio.currentTime = msg.progress
    }
    if (msg.isPlaying === 1) roomAudio.play().catch(() => {})
    else roomAudio.pause()
  }
}

// 房主：进度节流同步（每 5s 一次）
function maybePublishProgress() {
  if (!roomAudio) return
  const now = Date.now()
  if (now - lastPublishTs < 5000) return
  lastPublishTs = now
  publishSync({ progress: roomAudio.currentTime })
}

// 上报播放状态（仅房主角色有效，服务端会校验）
function publishSync(overrides = {}) {
  if (!socket) return
  const body = {
    musicId: overrides.musicId !== undefined ? overrides.musicId : syncState.musicId,
    progress: overrides.progress !== undefined ? overrides.progress : (roomAudio ? roomAudio.currentTime : syncState.progress),
    isPlaying: overrides.isPlaying !== undefined ? overrides.isPlaying : (roomAudio ? (roomAudio.paused ? 0 : 1) : syncState.isPlaying)
  }
  socket.publish('/sync', body)
}

// 房主播放 / 暂停
async function toggleRoomPlay() {
  if (!roomAudio) return
  if (roomPlaying.value) {
    roomAudio.pause()
    syncState.isPlaying = 0
    publishSync({ isPlaying: 0 })
    return
  }
  // 无当前歌曲时，取歌单第一首
  if (!syncState.musicId && playlist.value.length > 0) {
    const first = playlist.value[0]
    syncState.musicId = first.musicId
    roomAudio.src = first.musicUrl || ''
  }
  if (!roomAudio.src) {
    ElMessage.info('请先在歌单里添加歌曲')
    return
  }
  try {
    await roomAudio.play()
    syncState.isPlaying = 1
    publishSync({ isPlaying: 1 })
  } catch (e) {
    ElMessage.error('播放失败：' + (roomAudio.src || '无音频地址') + ' — ' + (e && e.message ? e.message : ''))
  }
}

// ===== 聊天 =====
function sendChat(type, content) {
  const text = content !== undefined ? content : chatInput.value
  if (!text || !String(text).trim()) return
  if (!socket) return
  socket.publish('/chat', { type, content: String(text).trim() })
  if (type === 0) chatInput.value = ''
}

async function loadMessages(backfill = false) {
  try {
    const after = backfill ? (lastSeq || null) : null
    const res = await roomMessagesApi(roomId, after)
    const list = res.data || []
    if (!backfill) {
      // 首次全量：直接替换 + 记录最大 seq
      messages.value = list
      lastSeq = list.reduce((mx, m) => Math.max(mx, m.seq || 0), 0)
    } else {
      // 重连补漏：按 seq 去重合并
      const known = new Set(messages.value.map(m => m.seq))
      for (const m of list) {
        if (m.seq && !known.has(m.seq)) messages.value.push(m)
      }
      messages.value.sort((a, b) => (a.seq || 0) - (b.seq || 0))
      lastSeq = messages.value.reduce((mx, m) => Math.max(mx, m.seq || 0), 0)
    }
    scrollChat()
  } catch (e) {
    console.error('加载消息失败:', e)
  }
}

function scrollChat() {
  nextTick(() => {
    if (chatListRef.value) chatListRef.value.scrollTop = chatListRef.value.scrollHeight
  })
}

// ===== 切歌投票（走 WS，实时反馈）=====
function onInitiateSkip() {
  if (!socket || !syncState.musicId || myVoted.value) return
  myVoted.value = true
  socket.publish('/skip-vote', { musicId: syncState.musicId })
}

function onAgreeVote() {
  if (!socket || !syncState.musicId || myVoted.value) return
  myVoted.value = true
  socket.publish('/skip-vote/agree', { musicId: syncState.musicId })
}

function handleVote(payload) {
  const s = payload.stats
  if (s) {
    voteState.musicId = s.musicId
    voteState.votes = s.votes || 0
    voteState.agreeCount = s.agreeCount || 0
    voteState.required = s.required || 0
    voteState.remaining = s.remainingSeconds || 0
    voteState.active = !!s.active
    startVoteTicker()
  }
  if (payload.action === 'passed') {
    ElMessage.success('切歌成功')
    myVoted.value = false
    stopVoteTicker()
    voteState.active = false
    loadRoom()
    loadPlaylist()
  } else if (payload.action === 'expired') {
    myVoted.value = false
    stopVoteTicker()
    voteState.active = false
    ElMessage.info('切歌投票已超时失效')
  } else if (payload.action === 'init') {
    // 新投票由他人发起时，我仍可附议
    if (s && s.initiatorUserId !== myUserId.value) {
      myVoted.value = false
    }
  }
}

// 投票倒计时：每秒递减，到 0 本地收起（服务端会广播 expired）
function startVoteTicker() {
  stopVoteTicker()
  voteTimer = setInterval(() => {
    if (voteState.remaining > 0) {
      voteState.remaining -= 1
    }
    if (voteState.remaining <= 0) {
      stopVoteTicker()
      voteState.active = false
      myVoted.value = false
    }
  }, 1000)
}
function stopVoteTicker() {
  if (voteTimer) {
    clearInterval(voteTimer)
    voteTimer = null
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
  // 房主返回 = 关闭房间（带确认，方案 §8.1：房主退出即解散）；成员返回 = 仅离开页面
  if (isOwner.value) {
    closeRoom()
  } else {
    router.push('/rooms')
  }
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
  position: relative;
  flex: 1;
  display: flex;
  gap: 32px;
  align-items: stretch;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  padding: 32px;
  box-shadow: var(--shadow-1);
  overflow: hidden;

  /* Stripe 标志性的氛围渐变网格（克制、浅色） */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(50% 60% at 12% 18%, rgba(245, 233, 212, 0.5), transparent 60%),
      radial-gradient(42% 52% at 88% 12%, rgba(249, 107, 238, 0.1), transparent 60%),
      radial-gradient(48% 56% at 78% 76%, rgba(99, 65, 255, 0.1), transparent 62%);
    pointer-events: none;
    z-index: 0;
  }

  .player-main {
    position: relative;
    z-index: 1;
    flex: 1;
    min-width: 0;
    display: flex;
    gap: 32px;
    align-items: center;
  }

  .unlock-mask {
    position: absolute;
    inset: 0;
    border-radius: inherit;
    background: rgba(30, 30, 30, 0.45);
    backdrop-filter: blur(2px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 15px;
    cursor: pointer;
    transition: background 150ms ease;
    z-index: 2;
    &:hover { background: rgba(30, 30, 30, 0.55); }
    .el-icon { font-size: 40px; }
  }

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
      font-size: 26px;
      font-weight: 300;
      letter-spacing: -0.5px;
      line-height: 1.12;
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
        transition: transform 350ms cubic-bezier(0.22, 1, 0.36, 1);
        &:active { transform: scale(0.9); }
      }
      .skip-btn {
        height: 44px;
        padding: 0 20px;
        transition: transform 200ms ease;
        &:active { transform: scale(0.94); }
      }
    }
    .sync-hint {
      margin-top: 14px;
      font-size: 12px;
      color: var(--st-ink-mute);
    }

    .vote-status {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 12px;
      font-size: 12px;
      color: var(--st-ink-mute);
      .vote-label { color: var(--st-ink); }
      .vote-num { font-variant-numeric: tabular-nums; font-weight: 600; color: var(--st-primary); }
      .vote-remain { font-variant-numeric: tabular-nums; }
      .vote-done {
        padding: 1px 8px;
        border-radius: var(--rounded-pill);
        background: var(--st-primary-subdued);
        color: var(--st-primary);
      }
    }
  }
}

/* 播放卡右侧：歌词（对标 FullPlayer 的居中高亮 + 上下渐隐）*/
.lyrics-panel {
  position: relative;
  z-index: 1;
  width: 44%;
  max-width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-left: 1px solid var(--st-hairline);
  padding-left: 32px;

  .lyrics-head {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    letter-spacing: 0.4px;
    color: var(--st-ink-mute);
    margin-bottom: 12px;
    font-feature-settings: 'tnum';
    .el-icon { font-size: 13px; }
  }

  .lyrics-view {
    position: relative;
    height: 356px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    mask-image: linear-gradient(
      to bottom,
      transparent 0%,
      rgba(0, 0, 0, 0.4) 12%,
      rgba(0, 0, 0, 1) 26%,
      rgba(0, 0, 0, 1) 74%,
      rgba(0, 0, 0, 0.4) 88%,
      transparent 100%
    );
    -webkit-mask-image: linear-gradient(
      to bottom,
      transparent 0%,
      rgba(0, 0, 0, 0.4) 12%,
      rgba(0, 0, 0, 1) 26%,
      rgba(0, 0, 0, 1) 74%,
      rgba(0, 0, 0, 0.4) 88%,
      transparent 100%
    );
  }

  .room-lyric-line {
    position: absolute;
    left: 0;
    right: 0;
    text-align: center;
    padding: 0 20px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    letter-spacing: 0.02em;
    line-height: 44px;
    cursor: pointer;
    transition: transform 400ms cubic-bezier(0.22, 1, 0.36, 1),
      font-size 300ms ease,
      color 200ms ease;
  }

  .no-room-lyrics {
    color: var(--st-ink-mute);
    font-size: 14px;
  }
}

.chat-panel {
  height: 260px;
  flex-shrink: 0;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  padding: 20px;
  display: flex;
  flex-direction: column;

  .chat-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: var(--st-ink);
    margin-bottom: 12px;
    .el-icon { color: var(--st-primary); }
    .chat-status {
      margin-left: auto;
      font-size: 11px;
      color: var(--st-ink-mute);
      &.on { color: var(--st-primary); }
    }
  }

  .chat-list {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
    .chat-empty { color: var(--st-ink-mute); font-size: 13px; text-align: center; padding: 16px 0; }
    .chat-item {
      display: flex;
      align-items: baseline;
      gap: 8px;
      font-size: 13px;
      .chat-name { color: var(--st-primary); font-weight: 600; flex-shrink: 0; }
      .chat-text { color: var(--st-ink); word-break: break-word; }
      &.me { justify-content: flex-end; .chat-name { color: var(--st-ink-mute); } }
      &.sys { justify-content: center; }
      .sys-text { color: var(--st-ink-mute); font-size: 12px; text-align: center; }
    }
  }

  .chat-input {
    .emoji-bar { display: flex; gap: 6px; margin-bottom: 8px; }
    .emoji-btn {
      background: var(--st-canvas-hover);
      border: none;
      border-radius: var(--rounded-sm);
      padding: 2px 8px;
      font-size: 16px;
      cursor: pointer;
      &:hover { background: var(--st-primary-subdued); }
    }
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
  .kick-btn {
    flex-shrink: 0;
    background: none;
    border: 1px solid var(--st-hairline);
    border-radius: var(--rounded-pill);
    padding: 2px 10px;
    font-size: 11px;
    color: var(--st-ruby, #ea2261);
    cursor: pointer;
    &:hover { border-color: var(--st-ruby, #ea2261); background: rgba(234, 34, 97, 0.08); }
  }
  .transfer-btn {
    flex-shrink: 0;
    background: none;
    border: 1px solid var(--st-primary);
    border-radius: var(--rounded-pill);
    padding: 2px 10px;
    font-size: 11px;
    color: var(--st-primary);
    cursor: pointer;
    &:hover { background: rgba(99, 65, 255, 0.08); }
  }
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