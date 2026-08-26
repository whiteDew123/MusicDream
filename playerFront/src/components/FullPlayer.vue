<template>
  <div class="full-player" ref="playerRef">
    <!-- 毛玻璃背景层 -->
    <div class="player-bg-blur" v-if="currentSong?.imageUrl">
      <img :src="currentSong.imageUrl" alt="" />
    </div>
    <!-- 顶部栏 -->
    <div class="top-bar">
      <button class="back-btn" @click.stop="$emit('close')">
        <el-icon :size="24"><ArrowDown /></el-icon>
      </button>
      <div class="top-center" v-if="currentSong">
        <span class="top-label">正在播放</span>
        <span class="top-title">{{ currentSong.musicName }}</span>
      </div>
    </div>

    <!-- 卡片堆叠区域（滑动切歌） -->
    <div class="card-stack" ref="stackRef">
      <!-- 上一首预览 -->
      <div
        v-if="prevSong"
        class="song-card prev"
        :style="prevCardStyle"
      >
        <div class="card-inner">
          <img v-if="prevSong.imageUrl" :src="prevSong.imageUrl" alt="" />
          <div v-else class="prev-cover-fallback"><el-icon :size="64"><Headset /></el-icon></div>
        </div>
      </div>

      <!-- 当前歌曲 -->
      <div
        v-if="currentSong"
        class="song-card current"
        :style="currentCardStyle"
        @touchstart="onTouchStart"
        @touchmove="onTouchMove"
        @touchend="onTouchEnd"
        @mousedown="onMouseDown"
      >
        <div class="card-inner">
          <!-- ========== 桌面端双栏布局 ========== -->
          <div class="desktop-layout" v-if="isDesktop">
            <!-- 左侧：黑胶封面 + 歌曲信息 -->
            <div class="left-panel">
              <div class="vinyl-wrapper">
                <div class="vinyl-disc" :class="{ spinning: playerStore.playing }">
                  <div class="vinyl-ring"></div>
                  <div class="vinyl-cover">
                    <img
                      v-if="currentSong.imageUrl"
                      :src="currentSong.imageUrl"
                      :alt="currentSong.musicName"
                    />
                    <el-icon v-else class="vinyl-fallback"><Headset /></el-icon>
                  </div>
                  <div class="vinyl-center"></div>
                </div>
              </div>
              <div class="song-info">
                <h1 class="song-name">{{ currentSong.musicName }}</h1>
                <p class="singer-name" @click.stop="goSinger">
                  <el-icon :size="16"><User /></el-icon>
                  {{ currentSong.singerName || '未知歌手' }}
                </p>
              </div>
            </div>

            <!-- 右侧：歌词 -->
            <div class="right-panel">
              <!-- 多行歌词滚动 -->
              <div class="lyrics-container" @wheel.prevent="handleLyricWheel">
                <div
                  v-for="line in visibleLyrics"
                  :key="line.index"
                  class="lyric-line"
                  :class="{
                    active: line.offset === 0,
                    'fade-top': line.offset < 0,
                    'fade-bottom': line.offset > 0
                  }"
                  :style="getLyricStyle(line)"
                  @click.stop="seekToLyric(line)"
                >
                  {{ line.text || '♪' }}
                </div>
                <div v-if="!playerStore.lyrics.length" class="no-lyrics">
                  暂无歌词
                </div>
              </div>
            </div>
          </div>

          <!-- ========== 移动端单列布局 ========== -->
          <div class="mobile-layout" v-else>
            <!-- 封面 + 信息 -->
            <div class="mobile-info">
              <div class="mobile-cover" :class="{ spinning: playerStore.playing }">
                <div class="mobile-cover-inner">
                  <img
                    v-if="currentSong.imageUrl"
                    :src="currentSong.imageUrl"
                    :alt="currentSong.musicName"
                  />
                  <el-icon v-else><Headset /></el-icon>
                </div>
              </div>
              <div class="mobile-song-info">
                <h1 class="song-name">{{ currentSong.musicName }}</h1>
                <p class="singer-name" @click.stop="goSinger">{{ currentSong.singerName }}</p>
              </div>
            </div>

            <!-- 多行歌词（移动端） -->
            <div class="mobile-lyrics" @wheel.prevent="handleLyricWheel">
              <div
                v-for="line in visibleLyrics"
                :key="'m-' + line.index"
                class="lyric-line mobile"
                :class="{
                  active: line.offset === 0,
                  'fade-top': line.offset < 0,
                  'fade-bottom': line.offset > 0
                }"
                :style="getMobileLyricStyle(line)"
                @click.stop="seekToLyric(line)"
              >
                {{ line.text || '♪' }}
              </div>
              <div v-if="!playerStore.lyrics.length" class="no-lyrics mobile">
                暂无歌词
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 下一首预览 -->
      <div
        v-if="nextSong"
        class="song-card next"
        :style="nextCardStyle"
      >
        <div class="card-inner">
          <img v-if="nextSong.imageUrl" :src="nextSong.imageUrl" alt="" />
          <div v-else class="next-cover-fallback"><el-icon :size="64"><Headset /></el-icon></div>
        </div>
      </div>
    </div>

    <!-- 桌面端底部播放控件（复用 PlayerBar 样式） -->
    <div 
      class="desktop-bottom" 
      v-if="isDesktop && currentSong"
      @mouseenter="showBottomControls = true"
      @mouseleave="showBottomControls = false"
    >
      <!-- 左侧：歌曲信息 -->
      <div class="bottom-left">
        <div class="bottom-cover" @click.stop="$emit('close')">
          <img v-if="currentSong.imageUrl" :src="currentSong.imageUrl" :alt="currentSong.musicName" />
          <el-icon v-else><Headset /></el-icon>
        </div>
        <div class="bottom-song-text">
          <div class="bottom-song-name">{{ currentSong.musicName }}</div>
          <div class="bottom-song-singer">{{ currentSong.singerName || '未知歌手' }}</div>
        </div>
      </div>

      <!-- 中间：播放控制 + 进度条 -->
      <div class="bottom-center">
        <div class="bottom-controls">
          <button class="b-ctrl-btn mode-btn" @click.stop="playerStore.togglePlayMode()" :title="playerStore.playModeLabels[playerStore.playMode]">
            <el-icon v-if="playerStore.playMode === 0"><Sort /></el-icon>
            <el-icon v-else-if="playerStore.playMode === 1"><Refresh /></el-icon>
            <el-icon v-else><Switch /></el-icon>
          </button>
          <button class="b-ctrl-btn" @click.stop="playerStore.playPrev()">
            <el-icon><CaretLeft /></el-icon>
          </button>
          <button class="b-ctrl-btn b-play-btn" @click.stop="playerStore.togglePlay()">
            <el-icon v-if="!playerStore.playing"><VideoPlay /></el-icon>
            <el-icon v-else><VideoPause /></el-icon>
          </button>
          <button class="b-ctrl-btn" @click.stop="playerStore.playNext()">
            <el-icon><CaretRight /></el-icon>
          </button>
          <button class="b-ctrl-btn list-btn" @click.stop="showPlaylist = !showPlaylist">
            <el-icon><Tickets /></el-icon>
          </button>
        </div>
        <div class="bottom-progress">
          <span class="b-time">{{ formatTime(playerStore.currentTime) }}</span>
          <div class="b-progress-bar" ref="bottomProgressBarRef" @mousedown.stop="handleBottomProgressMouseDown">
            <div class="b-progress-track">
              <div class="b-progress-filled" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <div class="b-progress-thumb" :style="{ left: progressPercent + '%' }"></div>
          </div>
          <span class="b-time">{{ formatTime(playerStore.duration) }}</span>
        </div>
      </div>

      <!-- 右侧：音量 + 操作按钮 -->
      <div class="bottom-right">
        <button class="b-ctrl-btn" @click.stop="playerStore.toggleMute()">
          <el-icon v-if="playerStore.muted || playerStore.volume === 0"><MuteNotification /></el-icon>
          <el-icon v-else><Microphone /></el-icon>
        </button>
        <div class="b-volume-slider">
          <div class="b-volume-track" ref="bottomVolumeBarRef" @mousedown.stop="handleBottomVolumeMouseDown">
            <div class="b-volume-filled" :style="{ width: volumePercent + '%' }"></div>
          </div>
        </div>

        <!-- 操作按钮组（鼠标移入时显示） -->
        <transition name="fade-up">
          <div class="bottom-action-btns" v-show="showBottomControls">
            <button class="b-action-btn" :class="{ active: stats.liked }" @click.stop="handleLike" title="点赞">
              <span class="heart-icon" :class="{ active: stats.liked }">♥</span>
              <span v-if="stats.likesCount > 0">{{ formatCount(stats.likesCount) }}</span>
            </button>
            <button class="b-action-btn" @click.stop="showComment = true" title="评论">
              <el-icon :size="18"><ChatDotRound /></el-icon>
              <span v-if="stats.commentCount > 0">{{ formatCount(stats.commentCount) }}</span>
            </button>
            <button class="b-action-btn" :class="{ active: favorited }" @click.stop="handleFavorite" title="收藏">
              <el-icon :size="18"><StarFilled v-if="favorited" /><Star v-else /></el-icon>
            </button>
            <button class="b-action-btn" @click.stop="handleShare" title="分享">
              <el-icon :size="18"><Promotion /></el-icon>
              <span v-if="stats.shareCount > 0">{{ formatCount(stats.shareCount) }}</span>
            </button>
          </div>
        </transition>
      </div>

      <!-- 播放列表弹层 -->
      <transition name="slide-up">
        <div v-if="showPlaylist" class="playlist-popup">
          <div class="playlist-header">
            <span>播放列表（{{ playerStore.playlist.length }}）</span>
            <button class="clear-btn" @click.stop="playerStore.clearPlaylist()">清空</button>
          </div>
          <div class="playlist-items">
            <div
              v-for="(song, idx) in playerStore.playlist"
              :key="song.musicId"
              class="playlist-item"
              :class="{ active: idx === playerStore.currentIndex }"
              @click.stop="playerStore.setPlaylist(playerStore.playlist, idx)"
            >
              <span class="item-index">{{ idx + 1 }}</span>
              <span class="item-name">{{ song.musicName }}</span>
              <span class="item-singer">{{ song.singerName }}</span>
              <button class="item-remove" @click.stop="playerStore.removeFromPlaylist(idx)">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div v-if="playerStore.playlist.length === 0" class="empty-tip">播放列表为空</div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 移动端底部播放控件 -->
    <div class="mobile-bottom" v-if="!isDesktop && currentSong">
      <div class="player-controls">
        <button class="ctrl-btn" @click.stop="playerStore.playPrev()">
          <el-icon :size="24"><CaretLeft /></el-icon>
        </button>
        <button class="play-btn" @click.stop="playerStore.togglePlay()">
          <el-icon :size="28">
            <VideoPause v-if="playerStore.playing" />
            <VideoPlay v-else />
          </el-icon>
        </button>
        <button class="ctrl-btn" @click.stop="playerStore.playNext()">
          <el-icon :size="24"><CaretRight /></el-icon>
        </button>
      </div>
      <div class="progress-area">
        <span class="time-label">{{ formatTime(playerStore.currentTime) }}</span>
        <div class="progress-wrap" @click.stop="handleProgressClick">
          <div class="progress-track">
            <div
              class="progress-filled"
              :style="{ width: progressPercent + '%' }"
            ></div>
          </div>
        </div>
        <span class="time-label">{{ formatTime(playerStore.duration) }}</span>
      </div>
      <!-- 移动端底部操作按钮 -->
      <div class="bottom-actions mobile-actions">
        <button
          class="bottom-btn"
          :class="{ active: stats.liked }"
          @click.stop="handleLike"
        >
          <el-icon :size="18"><StarFilled v-if="stats.liked" /><Star /></el-icon>
          <span>{{ stats.likesCount > 0 ? formatCount(stats.likesCount) : '赞' }}</span>
        </button>
        <button
          class="bottom-btn"
          @click.stop="showComment = true"
        >
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>{{ stats.commentCount > 0 ? formatCount(stats.commentCount) : '评' }}</span>
        </button>
        <button
          class="bottom-btn"
          :class="{ active: favorited }"
          @click.stop="handleFavorite"
        >
          <el-icon :size="18"><StarFilled v-if="favorited" /><Star /></el-icon>
          <span>{{ favorited ? '藏' : '藏' }}</span>
        </button>
        <button
          class="bottom-btn"
          @click.stop="handleShare"
        >
          <el-icon :size="18"><Promotion /></el-icon>
          <span>{{ stats.shareCount > 0 ? formatCount(stats.shareCount) : '享' }}</span>
        </button>
      </div>
    </div>

    <!-- 评论抽屉（右侧滑入）-->
    <el-drawer
      v-model="showComment"
      direction="rtl"
      size="400px"
      :show-close="false"
      :with-header="false"
      class="comment-drawer"
    >
      <div class="comment-panel">
        <div class="comment-header">
          <span class="comment-title">评论</span>
          <span class="comment-count">{{ stats.commentCount }} 条</span>
          <button class="comment-close" @click.stop="showComment = false">
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>
        <div class="comment-input-row">
          <el-input
            v-model="newComment"
            placeholder="说点什么..."
            @keyup.enter="submitComment"
            clearable
            resize="none"
          />
          <button class="send-btn" @click.stop="submitComment">发送</button>
        </div>
        <div class="comment-list">
          <div v-if="commentList.length === 0" class="comment-empty">
            暂无评论，快来发表第一条吧
          </div>
          <div
            v-for="item in commentList"
            :key="item.id"
            class="comment-item"
          >
            <div class="comment-avatar">
              <el-icon :size="20"><User /></el-icon>
            </div>
            <div class="comment-body">
              <div class="comment-user">{{ item.username || '用户' }}</div>
              <div class="comment-text">{{ item.content }}</div>
              <div class="comment-time">{{ item.createTime }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 分享弹层 -->
    <ShareModal
      v-model:visible="showShare"
      :song="currentSong"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  Headset,
  VideoPlay,
  VideoPause,
  CaretLeft,
  CaretRight,
  Sort,
  Refresh,
  Switch,
  Star,
  StarFilled,
  ChatDotRound,
  Promotion,
  User,
  Close,
  Tickets,
  MuteNotification,
  Microphone
} from '@element-plus/icons-vue'
import ShareModal from '@/components/ShareModal.vue'
import { usePlayerStore } from '@/store/player'
import { getMusicStatsApi, toggleLikeApi, shareSongApi, commentListApi, createCommentApi } from '@/api/interaction'
import { addLikedMusicApi, removeLikedMusicApi, likedMusicApi } from '@/api/like'

const props = defineProps({
  visible: Boolean
})

defineEmits(['close', 'open-comment'])

const router = useRouter()
const playerStore = usePlayerStore()

const playerRef = ref(null)
const stackRef = ref(null)

// 响应式
const isDesktop = ref(window.innerWidth >= 768)
// 底部控件显示状态
const showBottomControls = ref(false)

function handleResize() {
  isDesktop.value = window.innerWidth >= 768
}

// 拖动状态
const dragY = ref(0)
const dragStartY = ref(0)
const dragging = ref(false)
const dragStartTime = ref(0)

// 面板状态
const showComment = ref(false)
const showShare = ref(false)
const showPlaylist = ref(false)

// 底部进度条和音量条 ref
const bottomProgressBarRef = ref(null)
const bottomVolumeBarRef = ref(null)

// 交互状态
const stats = reactive({
  liked: false,
  likesCount: 0,
  commentCount: 0,
  shareCount: 0
})
const favorited = ref(false)

// 评论数据
const newComment = ref('')
const commentList = ref([])

// 歌词行高
const LYRIC_LINE_HEIGHT = 52
// 手动歌词偏移（滚轮控制）
const lyricManualOffset = ref(0)
// 自动回位定时器
let lyricAutoResetTimer = null

// 收藏歌曲 ID 集合
const favoriteIds = ref(new Set())

// ===== 计算属性 =====
const currentSong = computed(() => playerStore.currentSong)
const playlist = computed(() => playerStore.playlist)
const currentIndex = computed(() => playerStore.currentIndex)

const prevSong = computed(() => playlist.value[currentIndex.value - 1] || null)
const nextSong = computed(() => playlist.value[currentIndex.value + 1] || null)

const progressPercent = computed(() => {
  if (!playerStore.duration) return 0
  return (playerStore.currentTime / playerStore.duration) * 100
})

const volumePercent = computed(() => {
  return (playerStore.muted ? 0 : playerStore.volume) * 100
})

// 可见歌词窗口（当前行前后各 8 行，共 17 行，支持手动滚轮偏移）
const visibleLyrics = computed(() => {
  const lyrics = playerStore.lyrics
  const idx = playerStore.currentLyricIndex
  if (!lyrics.length || idx < 0) return []

  const HALF = 8
  const baseCenter = idx + lyricManualOffset.value
  const start = Math.max(0, baseCenter - HALF)
  const end = Math.min(lyrics.length, baseCenter + HALF + 1)

  const result = []
  for (let i = start; i < end; i++) {
    result.push({
      ...lyrics[i],
      index: i,
      offset: i - baseCenter
    })
  }
  return result
})

// 桌面端歌词样式
function getLyricStyle(line) {
  const isActive = line.offset === 0
  return {
    transform: `translateY(${line.offset * LYRIC_LINE_HEIGHT}px)`,
    opacity: 1,
    fontSize: isActive ? '32px' : '26px',
    fontWeight: isActive ? 700 : 400
  }
}

// 移动端歌词样式
function getMobileLyricStyle(line) {
  const isActive = line.offset === 0
  return {
    transform: `translateY(${line.offset * 38}px)`,
    opacity: 1,
    fontSize: isActive ? '26px' : '20px',
    fontWeight: isActive ? 700 : 400
  }
}

// ===== 卡片样式 =====
const currentCardStyle = computed(() => {
  let translateY = dragY.value
  let scale = 1
  let opacity = 1
  const threshold = window.innerHeight * 0.3

  if (translateY < 0) {
    const ratio = Math.min(Math.abs(translateY) / threshold, 1)
    scale = 1 - ratio * 0.05
    opacity = 1 - ratio * 0.3
  } else if (translateY > 0) {
    const ratio = Math.min(translateY / threshold, 1)
    scale = 1 - ratio * 0.05
    opacity = 1 - ratio * 0.3
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity
  }
})

const prevCardStyle = computed(() => {
  let translateY = -window.innerHeight * 0.3
  let scale = 0.9
  let opacity = 0.5

  if (dragY.value < 0) {
    const ratio = Math.min(Math.abs(dragY.value) / (window.innerHeight * 0.3), 1)
    translateY = -window.innerHeight * 0.3 + ratio * window.innerHeight * 0.5
    scale = 0.9 + ratio * 0.1
    opacity = 0.5 + ratio * 0.5
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity,
    zIndex: 1
  }
})

const nextCardStyle = computed(() => {
  let translateY = window.innerHeight * 0.3
  let scale = 0.9
  let opacity = 0.5

  if (dragY.value > 0) {
    const ratio = Math.min(dragY.value / (window.innerHeight * 0.3), 1)
    translateY = window.innerHeight * 0.3 - ratio * window.innerHeight * 0.5
    scale = 0.9 + ratio * 0.1
    opacity = 0.5 + ratio * 0.5
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity,
    zIndex: 1
  }
})

// ===== 交互方法 =====
function formatCount(n) {
  if (!n && n !== 0) return ''
  if (n === 0) return ''
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

async function loadFavoriteIds() {
  try {
    const res = await likedMusicApi()
    const list = res.data?.records || res.data?.list || res.data || []
    favoriteIds.value = new Set(list.map(item => item.musicId || item.id))
    if (currentSong.value?.musicId) {
      favorited.value = favoriteIds.value.has(currentSong.value.musicId)
    }
  } catch (e) {
    // 静默失败
  }
}

async function loadStats() {
  if (!currentSong.value?.musicId) return
  try {
    const res = await getMusicStatsApi(currentSong.value.musicId)
    stats.liked = res.data?.liked ?? false
    stats.likesCount = res.data?.likesCount ?? 0
    stats.commentCount = res.data?.commentCount ?? 0
    stats.shareCount = res.data?.shareCount ?? 0
    if (res.data?.favorited != null) {
      favorited.value = res.data.favorited
    } else {
      favorited.value = favoriteIds.value.has(currentSong.value.musicId)
    }
  } catch (e) {
    // 静默失败
  }
}

async function loadComments() {
  if (!currentSong.value?.musicId) return
  try {
    const res = await commentListApi(currentSong.value.musicId, 1, 50)
    const list = res.data?.records || res.data?.list || res.data || []
    commentList.value = list.map(item => ({
      id: item.id,
      username: item.username || item.nickname || '用户',
      content: item.content || item.contentText || '',
      createTime: item.createTime || item.createTimeStr || ''
    }))
    if (res.data?.total != null) {
      stats.commentCount = res.data.total
    }
  } catch (e) {
    // 静默失败
  }
}

async function submitComment() {
  if (!newComment.value.trim() || !currentSong.value?.musicId) return
  try {
    await createCommentApi(currentSong.value.musicId, {
      content: newComment.value.trim()
    })
    newComment.value = ''
    ElMessage.success('评论成功')
    loadComments()
    loadStats()
  } catch (e) {
    // request.js 已统一弹窗
  }
}

async function handleLike() {
  if (!currentSong.value?.musicId) return
  try {
    const res = await toggleLikeApi(currentSong.value.musicId)
    stats.liked = res.data?.liked ?? !stats.liked
    stats.likesCount = res.data?.likesCount ?? (stats.liked ? stats.likesCount + 1 : stats.likesCount - 1)
    ElMessage.success(stats.liked ? '点赞成功' : '已取消点赞')
  } catch (e) {
    // request.js 已统一弹窗
  }
}

async function handleFavorite() {
  if (!currentSong.value?.musicId) return
  try {
    if (favorited.value) {
      await removeLikedMusicApi(currentSong.value.musicId)
      favorited.value = false
      favoriteIds.value.delete(currentSong.value.musicId)
      ElMessage.success('已取消收藏')
    } else {
      await addLikedMusicApi(currentSong.value.musicId)
      favorited.value = true
      favoriteIds.value.add(currentSong.value.musicId)
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    // 静默
  }
}

async function handleShare() {
  if (!currentSong.value?.musicId) return
  try {
    const res = await shareSongApi(currentSong.value.musicId)
    if (res.data?.shareCount != null) {
      stats.shareCount = res.data.shareCount
    }
    showShare.value = true
  } catch (e) {
    showShare.value = true
  }
}

// ===== 工具方法 =====
function formatTime(sec) {
  if (!sec || isNaN(sec)) return '00:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function goSinger() {
  if (currentSong.value?.singerId) {
    router.push(`/singer/${currentSong.value.singerId}`)
  }
}

function handleProgressClick(e) {
  if (!playerStore.duration) return
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  playerStore.seekTo(percent * playerStore.duration)
}

// 底部进度条拖拽
function handleBottomProgressMouseDown(e) {
  if (!playerStore.duration) return
  const bar = bottomProgressBarRef.value
  const updateProgress = (ev) => {
    const rect = bar.getBoundingClientRect()
    const percent = Math.min(Math.max((ev.clientX - rect.left) / rect.width, 0), 1)
    playerStore.seekTo(percent * playerStore.duration)
  }
  updateProgress(e)
  const onMove = (ev) => updateProgress(ev)
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

// 底部音量条拖拽
function handleBottomVolumeMouseDown(e) {
  const bar = bottomVolumeBarRef.value
  const updateVolume = (ev) => {
    const rect = bar.getBoundingClientRect()
    const percent = Math.min(Math.max((ev.clientX - rect.left) / rect.width, 0), 1)
    playerStore.setVolume(percent)
  }
  updateVolume(e)
  const onMove = (ev) => updateVolume(ev)
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function seekToLyric(line) {
  if (line.time != null) {
    playerStore.seekTo(line.time)
    lyricManualOffset.value = 0
  }
}

function handleLyricWheel(e) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? 1 : -1
  lyricManualOffset.value += delta
  const lyrics = playerStore.lyrics
  const idx = playerStore.currentLyricIndex
  const maxOffset = lyrics.length - 1 - idx
  const minOffset = -idx
  lyricManualOffset.value = Math.max(minOffset, Math.min(maxOffset, lyricManualOffset.value))

  // 清除旧定时器，重新计时
  if (lyricAutoResetTimer) {
    clearTimeout(lyricAutoResetTimer)
  }
  lyricAutoResetTimer = setTimeout(() => {
    lyricManualOffset.value = 0
  }, 2000)
}

// ===== 触摸/鼠标事件 =====
function onTouchStart(e) {
  dragStartY.value = e.touches[0].clientY
  dragY.value = 0
  dragging.value = true
  dragStartTime.value = Date.now()
}

function onTouchMove(e) {
  if (!dragging.value) return
  dragY.value = e.touches[0].clientY - dragStartY.value
}

function onTouchEnd() {
  handleDragEnd()
}

function onMouseDown(e) {
  dragStartY.value = e.clientY
  dragY.value = 0
  dragging.value = true
  dragStartTime.value = Date.now()

  const onMove = (ev) => {
    if (!dragging.value) return
    dragY.value = ev.clientY - dragStartY.value
  }

  const onUp = () => {
    dragging.value = false
    handleDragEnd()
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function handleDragEnd() {
  const threshold = window.innerHeight * 0.2
  const velocity = Math.abs(dragY.value) / Math.max(Date.now() - dragStartTime.value, 1) * 1000

  if (dragY.value < -threshold || velocity > 1500) {
    playerStore.playNext()
  } else if (dragY.value > threshold || velocity > 1500) {
    playerStore.playPrev()
  } else {
    dragY.value = 0
  }
}

// ===== 生命周期 =====
watch(
  () => currentSong.value?.musicId,
  () => {
    if (currentSong.value?.musicId) {
      loadStats()
      if (favoriteIds.value.size > 0) {
        favorited.value = favoriteIds.value.has(currentSong.value.musicId)
      } else {
        favorited.value = false
      }
    }
  }
)

onMounted(() => {
  playerStore.initAudioEvents()
  window.addEventListener('resize', handleResize)
  loadFavoriteIds()
})

watch(showComment, (val) => {
  if (val) {
    loadComments()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.full-player {
  position: fixed;
  inset: 0;
  z-index: 1000;
  overflow: hidden;
  background: #000;
  user-select: none;
  -webkit-user-select: none;
}

/* === 毛玻璃背景层 === */
.player-bg-blur {
  position: absolute;
  inset: -40px;
  z-index: 0;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    filter: blur(60px) saturate(1.5) brightness(0.6);
    transform: scale(1.1);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
  }
}

/* === 顶部栏 === */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 20px 32px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
  pointer-events: none;
}

.back-btn {
  pointer-events: auto;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  backdrop-filter: blur(10px);
  transition: all 200ms ease;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }
}

.top-title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.top-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  max-width: 50%;
}

.top-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.top-actions {
  display: flex;
  gap: 12px;
}

/* === 卡片堆叠 === */
.card-stack {
  position: absolute;
  inset: 0;
  z-index: 10;
  overflow: hidden;
}

.song-card {
  position: absolute;
  inset: 0;
  will-change: transform, opacity;
  transition: transform 350ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 350ms cubic-bezier(0.22, 1, 0.36, 1);
  pointer-events: none;
}

.song-card.current {
  pointer-events: auto;
}

.card-inner {
  position: absolute;
  inset: 0;
}

.card-inner img,
.prev-cover-fallback,
.next-cover-fallback {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 80px;
  background: rgba(255, 255, 255, 0.05);
}

/* ========== 桌面端双栏布局 ========== */
.desktop-layout {
  display: flex;
  height: 100%;
  padding: 100px 80px 160px 80px;
  gap: 80px;
}

.left-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 40px;
  min-width: 420px;
}

/* ===== 黑胶唱片效果 ===== */
.vinyl-wrapper {
  position: relative;
  width: 400px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vinyl-disc {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    #1a1a1a,
    #333,
    #1a1a1a,
    #2a2a2a,
    #1a1a1a,
    #333,
    #1a1a1a
  );
  box-shadow:
    0 0 0 6px rgba(255, 255, 255, 0.05),
    0 0 0 8px rgba(0, 0, 0, 0.8),
    0 32px 80px rgba(0, 0, 0, 0.7),
    inset 0 0 50px rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;

  &.spinning {
    animation: vinyl-spin 12s linear infinite;
  }
}

@keyframes vinyl-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.vinyl-ring {
  position: absolute;
  inset: 14px;
  border-radius: 50%;
  background: repeating-radial-gradient(
    circle at center,
    transparent 0,
    transparent 2px,
    rgba(255, 255, 255, 0.03) 2px,
    rgba(255, 255, 255, 0.03) 3px
  );
  pointer-events: none;
}

.vinyl-cover {
  position: relative;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow:
    0 0 0 4px rgba(255, 255, 255, 0.12),
    0 10px 32px rgba(0, 0, 0, 0.5);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .vinyl-fallback {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 72px;
    color: rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.05);
  }
}

.vinyl-center {
  position: absolute;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: radial-gradient(circle, #444 0%, #222 60%, #111 100%);
  box-shadow:
    0 0 0 3px rgba(255, 255, 255, 0.15),
    0 0 0 5px rgba(0, 0, 0, 0.5),
    inset 0 0 4px rgba(0, 0, 0, 0.8);
}

/* ===== 歌曲信息（桌面端）===== */
.song-info {
  text-align: center;
  max-width: 400px;
}

.song-name {
  font-size: 46px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.7);
  word-break: break-all;
}

.singer-name {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.75);
  margin: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: color 200ms ease;

  &:hover {
    color: var(--st-primary);
  }
}

/* ===== 右侧面板 ===== */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 48px;
  max-width: 600px;
}

/* ===== 多行歌词 ===== */
.lyrics-container {
  position: relative;
  width: 100%;
  height: 520px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 20px 24px;
  mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.5) 10%,
    rgba(0, 0, 0, 1) 20%,
    rgba(0, 0, 0, 1) 80%,
    rgba(0, 0, 0, 0.5) 90%,
    transparent 100%
  );
  -webkit-mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.5) 10%,
    rgba(0, 0, 0, 1) 20%,
    rgba(0, 0, 0, 1) 80%,
    rgba(0, 0, 0, 0.5) 90%,
    transparent 100%
  );
}

.lyric-line {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.85);
  transition: transform 400ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 400ms ease,
    font-size 300ms ease,
    font-weight 300ms ease;
  padding: 0 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.02em;
  cursor: pointer;

  &:hover {
    color: rgba(255, 255, 255, 1);
  }

  &.active {
    color: #fff;
    text-shadow:
      0 2px 20px rgba(94, 92, 230, 0.6),
      0 0 60px rgba(94, 92, 230, 0.3),
      0 1px 3px rgba(0, 0, 0, 0.8);
  }

  &.fade-top {
    background: linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0));
  }

  &.fade-bottom {
    background: linear-gradient(to top, transparent, rgba(255, 255, 255, 0));
  }
}

.no-lyrics {
  color: rgba(255, 255, 255, 0.4);
  font-size: 18px;
}

/* ===== 右侧边栏（抖音风格）===== */
.side-actions {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 60;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 56px;
  padding: 10px 8px;
  background: rgba(0, 0, 0, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: all 200ms ease;
  border-radius: 16px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  &:hover {
    color: #fff;
    background: rgba(0, 0, 0, 0.5);
    border-color: rgba(255, 255, 255, 0.2);
    transform: scale(1.08);
  }

  &:active {
    transform: scale(0.95);
  }

  &.active {
    color: var(--st-primary);
    border-color: rgba(94, 92, 230, 0.5);
    background: rgba(94, 92, 230, 0.15);
  }

  span {
    font-size: 12px;
    font-weight: 500;
  }
}

/* ========== 移动端单列布局 ========== */
.mobile-layout {
  display: none;
  height: 100%;
  flex-direction: column;
  padding: 90px 32px 180px 32px;
  gap: 20px;
}

.mobile-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;
}

.mobile-cover {
  width: 280px;
  height: 280px;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.4);

  &.spinning {
    animation: none;
  }
}

.mobile-cover-inner {
  width: 100%;
  height: 100%;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .el-icon {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 80px;
    color: rgba(255, 255, 255, 0.3);
  }
}

.mobile-song-info {
  max-width: 100%;

  .song-name {
    font-size: 32px;
    margin: 0 0 8px 0;
  }

  .singer-name {
    font-size: 16px;
    margin: 0;
  }
}

/* 移动端歌词 */
.mobile-lyrics {
  position: relative;
  flex: 1;
  min-height: 160px;
  max-height: 200px;
  overflow: hidden;
  mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.3) 15%,
    rgba(0, 0, 0, 1) 35%,
    rgba(0, 0, 0, 1) 65%,
    rgba(0, 0, 0, 0.3) 85%,
    transparent 100%
  );
  -webkit-mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.3) 15%,
    rgba(0, 0, 0, 1) 35%,
    rgba(0, 0, 0, 1) 65%,
    rgba(0, 0, 0, 0.3) 85%,
    transparent 100%
  );
}

.lyric-line.mobile {
  padding: 0 60px 0 0;
}

.no-lyrics.mobile {
  padding-right: 60px;
}

/* ========== 播放控件 ========== */
.player-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48px;
  align-self: center;
}

.ctrl-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  backdrop-filter: blur(10px);
  transition: all 200ms ease;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.2);
  }
}

.play-btn {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--st-primary);
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(94, 92, 230, 0.4);
  transition: all 200ms ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 12px 40px rgba(94, 92, 230, 0.5);
  }

  &:active {
    transform: scale(0.95);
  }
}

.progress-area {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.time-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-feature-settings: 'tnum';
  min-width: 45px;
  flex-shrink: 0;
}

.progress-wrap {
  flex: 1;
  min-width: 0;
  cursor: pointer;
  padding: 10px 0;
}

.progress-track {
  position: relative;
  height: 6px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 3px;
  overflow: visible;
  transition: height 150ms ease;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.4);
}

.progress-wrap:hover .progress-track {
  height: 6px;
}

.progress-filled {
  height: 100%;
  background: var(--st-primary);
  border-radius: 2px;
  transition: width 150ms linear;
  box-shadow: 0 0 8px rgba(94, 92, 230, 0.5);
}

.progress-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  transform: translate(-50%, -50%) scale(0);
  box-shadow: 0 2px 8px rgba(94, 92, 230, 0.5);
  transition: transform 150ms ease;
  pointer-events: none;
}

.progress-wrap:hover .progress-thumb {
  transform: translate(-50%, -50%) scale(1);
}

/* ========== 桌面端底部控件（复用 PlayerBar 样式） ========== */
.desktop-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 20px;
  height: 72px;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.85) 0%,
    rgba(0, 0, 0, 0.5) 70%,
    transparent 100%
  );
  backdrop-filter: blur(12px);
  transition: opacity 300ms ease, transform 300ms ease;
  opacity: 0;
  transform: translateY(10px);

  &:hover {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧：歌曲信息 */
.bottom-left {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
  flex-shrink: 0;

  .bottom-cover {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: box-shadow 200ms ease;

    &:hover {
      box-shadow: 0 0 0 2px var(--st-primary);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 22px;
      color: rgba(255, 255, 255, 0.4);
    }
  }

  .bottom-song-text {
    overflow: hidden;

    .bottom-song-name {
      font-size: 14px;
      color: #fff;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-weight: 500;
    }

    .bottom-song-singer {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.6);
      margin-top: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

/* 中间：控制 + 进度 */
.bottom-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  max-width: 600px;
  margin: 0 auto;
}

.bottom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.b-ctrl-btn {
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 16px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 200ms ease;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.1);
  }

  &.b-play-btn {
    width: 36px !important;
    height: 36px !important;
    background: var(--st-primary) !important;
    color: #fff !important;
    font-size: 18px !important;

    &:hover {
      background: var(--st-primary-hover, #7a78eb) !important;
      transform: scale(1.1);
    }

    &:active {
      transform: scale(0.85);
    }
  }
}

.bottom-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;

  .b-time {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.6);
    font-feature-settings: 'tnum';
    min-width: 36px;
    text-align: center;
  }
}

.b-progress-bar {
  flex: 1;
  height: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;

  .b-progress-track {
    width: 100%;
    height: 4px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
    overflow: hidden;
  }

  .b-progress-filled {
    height: 100%;
    background: var(--st-primary);
    border-radius: 2px;
    transition: width 150ms linear;
  }

  .b-progress-thumb {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 12px;
    height: 12px;
    background: #fff;
    border-radius: 50%;
    opacity: 0;
    transition: opacity 150ms ease, transform 150ms ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  }

  &:hover .b-progress-thumb {
    opacity: 1;
  }
}

/* 右侧：音量 + 操作按钮 */
.bottom-right {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 280px;
  justify-content: flex-end;
  flex-shrink: 0;
}

.b-volume-slider {
  width: 80px;

  .b-volume-track {
    width: 100%;
    height: 4px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
    cursor: pointer;

    .b-volume-filled {
      height: 100%;
      background: var(--st-primary);
      border-radius: 2px;
      transition: width 150ms ease;
    }
  }
}

/* 底部操作按钮组 */
.bottom-action-btns {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
  padding-left: 12px;
  border-left: 1px solid rgba(255, 255, 255, 0.15);
}

.heart-icon {
  font-size: 18px;
  line-height: 1;
  color: rgba(255, 255, 255, 0.7);
  transition: all 200ms ease;

  &.active {
    color: #ff4757;
    animation: heart-beat 300ms ease;
  }
}

@keyframes heart-beat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.b-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  cursor: pointer;
  transition: all 200ms ease;
  font-size: 12px;
  backdrop-filter: blur(8px);

  &:hover {
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
  }

  &.active {
    color: var(--st-primary);
    background: rgba(94, 92, 230, 0.15);
  }

  span {
    font-size: 11px;
    font-weight: 500;
  }
}

/* 播放列表弹层 */
.playlist-popup {
  position: absolute;
  bottom: calc(72px + 8px);
  right: 24px;
  width: 360px;
  max-height: 380px;
  background: rgba(30, 30, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 100;
  backdrop-filter: blur(16px);

  .playlist-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    font-size: 14px;
    font-weight: 600;
    color: #fff;

    .clear-btn {
      border: none;
      background: transparent;
      color: var(--st-primary);
      font-size: 13px;
      cursor: pointer;

      &:hover {
        color: var(--st-primary-hover, #7a78eb);
      }
    }
  }

  .playlist-items {
    overflow-y: auto;
    max-height: 320px;
  }

  .playlist-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 16px;
    cursor: pointer;
    transition: background 150ms ease;

    &:hover {
      background: rgba(255, 255, 255, 0.08);

      .item-remove {
        opacity: 1;
      }
    }

    &.active {
      background: rgba(94, 92, 230, 0.15);

      .item-name {
        color: var(--st-primary);
        font-weight: 600;
      }
    }

    .item-index {
      width: 20px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.4);
      text-align: center;
      font-feature-settings: 'tnum';
    }

    .item-name {
      flex: 1;
      font-size: 13px;
      color: #fff;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-singer {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.5);
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-remove {
      opacity: 0;
      border: none;
      background: transparent;
      color: rgba(255, 255, 255, 0.4);
      cursor: pointer;
      font-size: 14px;
      transition: opacity 200ms ease, color 150ms ease;

      &:hover {
        color: var(--st-primary);
      }
    }
  }

  .empty-tip {
    padding: 32px;
    text-align: center;
    color: rgba(255, 255, 255, 0.4);
    font-size: 14px;
  }
}

/* 弹层入场动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity 200ms ease, transform 200ms ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

/* 淡入淡出动画 */
.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 300ms ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ========== 移动端底部控件 ========== */
.mobile-bottom {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
  width: calc(100% - 120px);
  max-width: 400px;
}

/* ========== 评论抽屉 ========== */
.comment-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
    background: #fff;
  }
}

.comment-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.comment-header {
  display: flex;
  align-items: center;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.comment-count {
  margin-left: 12px;
  font-size: 13px;
  color: #999;
}

.comment-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #f5f5f5;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms;

  &:hover {
    background: #e8e8e8;
    color: #333;
  }
}

.comment-input-row {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f5f5;

  .el-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      border-radius: 20px;
      background: #f5f5f5;
      box-shadow: none;
    }
  }
}

.send-btn {
  padding: 0 20px;
  height: 36px;
  border-radius: 18px;
  border: none;
  background: var(--st-primary);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 200ms;

  &:hover {
    opacity: 0.9;
  }
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
}

.comment-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.comment-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  word-break: break-word;
}

.comment-time {
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

/* ========== 响应式 ========== */
@media (max-width: 767px) {
  .desktop-layout {
    display: none;
  }

  .desktop-bottom {
    display: none;
  }

  .mobile-layout {
    display: flex;
  }

  .side-actions {
    right: 12px;
    gap: 16px;
  }

  .side-btn {
    width: 48px;
    padding: 8px 6px;

    .el-icon {
      font-size: 24px !important;
    }
  }
}

@media (min-width: 768px) {
  .mobile-layout {
    display: none;
  }

  .mobile-bottom {
    display: none;
  }
}

@media (max-width: 1024px) {
  .desktop-layout {
    padding: 90px 40px 160px 40px;
    gap: 48px;
  }

  .left-panel {
    min-width: 320px;
  }

  .vinyl-wrapper {
    width: 320px;
    height: 320px;
  }

  .vinyl-cover {
    width: 200px;
    height: 200px;
  }

  .song-name {
    font-size: 36px;
  }

  .lyrics-container {
    height: 280px;
  }
}
</style>