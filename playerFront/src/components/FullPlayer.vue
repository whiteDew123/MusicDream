<template>
  <div class="full-player" ref="playerRef">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <button class="back-btn" @click.stop="$emit('close')">
        <el-icon :size="24"><ArrowDown /></el-icon>
      </button>
      <div class="top-center" v-if="currentSong">
        <span class="top-label">正在播放</span>
        <span class="top-title">{{ currentSong.musicName }}</span>
      </div>
      <div class="top-actions">
        <button class="mode-btn" @click.stop="playerStore.togglePlayMode()">
          <el-icon :size="20">
            <Sort v-if="playerStore.playMode === 0" />
            <Refresh v-else-if="playerStore.playMode === 1" />
            <Switch v-else />
          </el-icon>
        </button>
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
              <div class="lyrics-container">
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
            <div class="mobile-lyrics">
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

    <!-- ========== 右侧边栏（抖音风格）========== -->
    <div class="side-actions" v-if="currentSong">
      <button
        class="side-btn"
        :class="{ active: stats.liked }"
        @click.stop="handleLike"
      >
        <el-icon :size="28"><StarFilled v-if="stats.liked" /><Star /></el-icon>
        <span v-if="stats.likesCount > 0">{{ formatCount(stats.likesCount) }}</span>
      </button>
      <button
        class="side-btn"
        @click.stop="showComment = true"
      >
        <el-icon :size="28"><ChatDotRound /></el-icon>
        <span v-if="stats.commentCount > 0">{{ formatCount(stats.commentCount) }}</span>
      </button>
      <button
        class="side-btn"
        :class="{ active: favorited }"
        @click.stop="handleFavorite"
      >
        <el-icon :size="28"><StarFilled v-if="favorited" /><Star /></el-icon>
        <span>{{ favorited ? '已收藏' : '收藏' }}</span>
      </button>
      <button
        class="side-btn"
        @click.stop="handleShare"
      >
        <el-icon :size="28"><Promotion /></el-icon>
        <span v-if="stats.shareCount > 0">{{ formatCount(stats.shareCount) }}</span>
      </button>
    </div>

    <!-- 桌面端底部播放控件 -->
    <div class="desktop-bottom" v-if="isDesktop && currentSong">
      <div class="player-controls">
        <button class="ctrl-btn" @click.stop="playerStore.playPrev()">
          <el-icon :size="28"><CaretLeft /></el-icon>
        </button>
        <button class="play-btn" @click.stop="playerStore.togglePlay()">
          <el-icon :size="32">
            <VideoPause v-if="playerStore.playing" />
            <VideoPlay v-else />
          </el-icon>
        </button>
        <button class="ctrl-btn" @click.stop="playerStore.playNext()">
          <el-icon :size="28"><CaretRight /></el-icon>
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
            <div
              class="progress-thumb"
              :style="{ left: progressPercent + '%' }"
            ></div>
          </div>
        </div>
        <span class="time-label">{{ formatTime(playerStore.duration) }}</span>
      </div>
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
  Close
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

// 可见歌词窗口（当前行前后各 2 行，共 5 行）
const visibleLyrics = computed(() => {
  const lyrics = playerStore.lyrics
  const idx = playerStore.currentLyricIndex
  if (!lyrics.length || idx < 0) return []

  const HALF = 2
  const start = Math.max(0, idx - HALF)
  const end = Math.min(lyrics.length, idx + HALF + 1)

  const result = []
  for (let i = start; i < end; i++) {
    result.push({
      ...lyrics[i],
      index: i,
      offset: i - idx
    })
  }
  return result
})

// 桌面端歌词样式
function getLyricStyle(line) {
  const absOffset = Math.abs(line.offset)
  return {
    transform: `translateY(${line.offset * LYRIC_LINE_HEIGHT}px)`,
    opacity: Math.max(0.2, 1 - absOffset * 0.35),
    fontSize: line.offset === 0 ? '32px' : `${28 - absOffset * 5}px`,
    fontWeight: line.offset === 0 ? 700 : 400
  }
}

// 移动端歌词样式
function getMobileLyricStyle(line) {
  const absOffset = Math.abs(line.offset)
  return {
    transform: `translateY(${line.offset * 38}px)`,
    opacity: Math.max(0.2, 1 - absOffset * 0.35),
    fontSize: line.offset === 0 ? '26px' : `${22 - absOffset * 3}px`,
    fontWeight: line.offset === 0 ? 700 : 400
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

/* === 顶部栏 === */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
  pointer-events: none;
}

.back-btn,
.mode-btn {
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
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 20px;
  padding: 20px 24px;
  backdrop-filter: blur(8px);
  mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.3) 12%,
    rgba(0, 0, 0, 1) 30%,
    rgba(0, 0, 0, 1) 70%,
    rgba(0, 0, 0, 0.3) 88%,
    transparent 100%
  );
  -webkit-mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 0.3) 12%,
    rgba(0, 0, 0, 1) 30%,
    rgba(0, 0, 0, 1) 70%,
    rgba(0, 0, 0, 0.3) 88%,
    transparent 100%
  );
}

.lyric-line {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.45);
  transition: transform 400ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 400ms ease,
    font-size 300ms ease,
    color 300ms ease;
  padding: 0 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: 0.02em;

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

/* ========== 桌面端底部控件 ========== */
.desktop-bottom {
  position: absolute;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 16px;
  width: 60%;
  max-width: 600px;
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