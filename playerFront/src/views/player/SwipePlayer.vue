<template>
  <div class="swipe-player" ref="playerRef" :data-theme="theme">
    <!-- 返回按钮 -->
    <div class="top-bar">
      <button class="back-btn" @click="goBack">
        <el-icon :size="24"><ArrowLeft /></el-icon>
      </button>
      <span class="top-title" v-if="currentSong">{{ currentSong.musicName }}</span>
    </div>

    <!-- 卡片堆叠区域 -->
    <div class="card-stack" ref="stackRef">
      <!-- 上一首（底部） -->
      <div
        v-if="prevSong"
        class="song-card prev"
        :style="prevCardStyle"
      >
        <div class="card-bg">
          <img v-if="prevSong.imageUrl" :src="prevSong.imageUrl" alt="" />
          <div class="card-overlay"></div>
        </div>
      </div>

      <!-- 当前歌曲（中间，可拖动） -->
      <div
        v-if="currentSong"
        class="song-card current"
        :style="currentCardStyle"
        @touchstart="onTouchStart"
        @touchmove="onTouchMove"
        @touchend="onTouchEnd"
        @mousedown="onMouseDown"
      >
        <div class="card-bg">
          <img
            v-if="currentSong.imageUrl"
            :src="currentSong.imageUrl"
            :alt="currentSong.musicName"
          />
          <div class="card-overlay"></div>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <!-- 左侧：歌曲信息 -->
          <div class="info-area">
            <h1 class="song-name">{{ currentSong.musicName }}</h1>
            <p class="singer-name" @click="goSinger">{{ currentSong.singerName }}</p>
            <div class="action-buttons">
              <button class="act-btn" @click="togglePlay">
                <el-icon :size="22">
                  <VideoPause v-if="playerStore.playing" />
                  <VideoPlay v-else />
                </el-icon>
                <span>{{ playerStore.playing ? '暂停' : '播放' }}</span>
              </button>
            </div>
          </div>

          <!-- 底部播放信息 -->
          <div class="bottom-area">
            <div class="progress-info">
              <span class="time-label">{{ formatTime(playerStore.currentTime) }}</span>
              <div class="mini-progress">
                <div
                  class="mini-progress-fill"
                  :style="{ width: progressPercent + '%' }"
                ></div>
              </div>
              <span class="time-label">{{ formatTime(playerStore.duration) }}</span>
            </div>
            <p class="lyric-hint" v-if="playerStore.lyrics.length > 0">
              {{ currentLyricText }}
            </p>
          </div>
        </div>
      </div>

      <!-- 下一首（顶部） -->
      <div
        v-if="nextSong"
        class="song-card next"
        :style="nextCardStyle"
      >
        <div class="card-bg">
          <img v-if="nextSong.imageUrl" :src="nextSong.imageUrl" alt="" />
          <div class="card-overlay"></div>
        </div>
      </div>
    </div>

    <!-- 右侧操作栏 -->
    <SongActions
      v-if="currentSong"
      :song="currentSong"
      @open-comment="showComment = true"
      @open-share="showShare = true"
      class="right-actions"
    />

    <!-- 评论抽屉 -->
    <CommentDrawer
      v-model:visible="showComment"
      :song-id="currentSong?.musicId"
      @comment-count="onCommentCountUpdate"
    />

    <!-- 分享弹层 -->
    <ShareModal
      v-model:visible="showShare"
      :song="currentSong"
    />

    <!-- 边缘渐隐 -->
    <div class="edge-fade top"></div>
    <div class="edge-fade bottom"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import SongActions from '@/components/SongActions.vue'
import CommentDrawer from '@/components/CommentDrawer.vue'
import ShareModal from '@/components/ShareModal.vue'
import { usePlayerStore } from '@/store/player'
import { recommendSongsApi } from '@/api/music'
import { useUserStore } from '@/store/user'
import { useTheme } from '@/utils/theme'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()
const { theme } = useTheme()

const playerRef = ref(null)
const stackRef = ref(null)

// 歌曲列表
const songList = ref([])
const currentIdx = ref(0)

// 拖动状态
const dragY = ref(0)
const dragStartY = ref(0)
const dragging = ref(false)
const dragStartTime = ref(0)

// 面板状态
const showComment = ref(false)
const showShare = ref(false)

// 计算属性
const currentSong = computed(() => songList.value[currentIdx.value] || null)
const prevSong = computed(() => songList.value[currentIdx.value - 1] || null)
const nextSong = computed(() => songList.value[currentIdx.value + 1] || null)

const progressPercent = computed(() => {
  if (!playerStore.duration) return 0
  return (playerStore.currentTime / playerStore.duration) * 100
})

const currentLyricText = computed(() => {
  const idx = playerStore.currentLyricIndex
  if (idx >= 0 && playerStore.lyrics[idx]) {
    return playerStore.lyrics[idx].text
  }
  return ''
})

// 卡片样式
const currentCardStyle = computed(() => {
  let translateY = dragY.value
  let scale = 1
  let opacity = 1
  const threshold = window.innerHeight * 0.3

  if (translateY < 0) {
    // 向上拖动
    const ratio = Math.min(Math.abs(translateY) / threshold, 1)
    scale = 1 - ratio * 0.1
    opacity = 1 - ratio * 0.4
  } else if (translateY > 0) {
    // 向下拖动
    const ratio = Math.min(translateY / threshold, 1)
    scale = 1 - ratio * 0.08
    opacity = 1 - ratio * 0.3
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity
  }
})

const prevCardStyle = computed(() => {
  let translateY = -window.innerHeight * 0.15
  let scale = 0.85
  let opacity = 0.6

  if (dragY.value < 0) {
    const ratio = Math.min(Math.abs(dragY.value) / (window.innerHeight * 0.3), 1)
    translateY = -window.innerHeight * 0.15 + ratio * window.innerHeight * 0.55
    scale = 0.85 + ratio * 0.15
    opacity = 0.6 + ratio * 0.4
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity,
    zIndex: 1
  }
})

const nextCardStyle = computed(() => {
  let translateY = window.innerHeight * 0.15
  let scale = 0.85
  let opacity = 0.6

  if (dragY.value > 0) {
    const ratio = Math.min(dragY.value / (window.innerHeight * 0.3), 1)
    translateY = window.innerHeight * 0.15 - ratio * window.innerHeight * 0.55
    scale = 0.85 + ratio * 0.15
    opacity = 0.6 + ratio * 0.4
  }

  return {
    transform: `translateY(${translateY}px) scale(${scale})`,
    opacity,
    zIndex: 1
  }
})

// 格式化时间
function formatTime(sec) {
  if (!sec || isNaN(sec)) return '00:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 加载推荐歌曲
async function loadSongs() {
  try {
    const res = await recommendSongsApi({
      userId: userStore.userInfo?.userId,
      limit: 20
    })
    songList.value = res.data || []
    if (songList.value.length > 0) {
      currentIdx.value = 0
      playCurrentSong()
    }
  } catch (e) {
    console.error('加载歌曲列表失败:', e)
    ElMessage.error('加载歌曲失败')
  }
}

// 播放当前歌曲
function playCurrentSong() {
  if (currentSong.value) {
    playerStore.setPlaylist(songList.value, currentIdx.value)
  }
}

// 播放/暂停
function togglePlay() {
  playerStore.togglePlay()
}

// 切换歌曲
function switchSong(direction) {
  const newIdx = currentIdx.value + direction
  if (newIdx < 0 || newIdx >= songList.value.length) return

  currentIdx.value = newIdx
  playCurrentSong()

  // 重置拖动状态
  dragY.value = 0
}

// 返回
function goBack() {
  // 停止当前播放但保留状态
  router.back()
}

// 跳转歌手页
function goSinger() {
  if (currentSong.value?.singerId) {
    router.push(`/singer/${currentSong.value.singerId}`)
  }
}

// 评论数更新
function onCommentCountUpdate() {
  // 可以触发 SongActions 刷新
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
  const currentY = e.touches[0].clientY
  dragY.value = currentY - dragStartY.value
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
  const threshold = window.innerHeight * 0.25
  const velocity = Math.abs(dragY.value) / (Date.now() - dragStartTime.value) * 1000

  // 快速滑动（fling）或超过阈值则切换
  if (dragY.value < -threshold || velocity > 1500) {
    switchSong(1) // 下一首
  } else if (dragY.value > threshold || velocity > 1500) {
    switchSong(-1) // 上一首
  } else {
    // 回弹
    dragY.value = 0
  }
}

// 初始化
onMounted(async () => {
  await loadSongs()
  playerStore.initAudioEvents()
})

onBeforeUnmount(() => {
  // 离开页面时暂停播放（保留状态，用户返回可继续）
  playerStore.audio?.pause()
})
</script>

<style scoped lang="scss">
.swipe-player {
  position: relative;
  width: 100%;
  height: 100vh;
  height: 100dvh;
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
  padding: 16px 20px;
  gap: 12px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
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
  font-size: 16px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* === 卡片堆叠 === */
.card-stack {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.song-card {
  position: absolute;
  inset: 0;
  will-change: transform, opacity;
  transition: transform 350ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 350ms cubic-bezier(0.22, 1, 0.36, 1);
}

.song-card.current {
  z-index: 10;
}

.song-card.prev,
.song-card.next {
  pointer-events: none;
}

.card-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .card-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0.4) 0%,
      transparent 30%,
      transparent 60%,
      rgba(0, 0, 0, 0.7) 100%
    );
  }
}

/* === 卡片内容 === */
.card-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 100px 80px 120px 60px;
  pointer-events: none;
}

.info-area {
  pointer-events: auto;
}

.song-name {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  margin-bottom: 12px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
}

.singer-name {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 24px;
  cursor: pointer;
  display: inline-block;
  transition: color 200ms ease;

  &:hover {
    color: var(--st-primary);
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.act-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border: none;
  border-radius: var(--rounded-pill);
  background: var(--st-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms ease;
  box-shadow: 0 4px 20px rgba(94, 92, 230, 0.4);

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 24px rgba(94, 92, 230, 0.5);
  }

  &:active {
    transform: scale(0.95);
  }
}

.bottom-area {
  pointer-events: auto;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.time-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-feature-settings: 'tnum';
  min-width: 42px;
}

.mini-progress {
  flex: 1;
  height: 3px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: var(--st-primary);
  border-radius: 2px;
  transition: width 150ms linear;
}

.lyric-hint {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 500px;
}

/* === 右侧操作栏 === */
.right-actions {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 50;
}

/* === 边缘渐隐 === */
.edge-fade {
  position: absolute;
  left: 0;
  right: 0;
  height: 40px;
  z-index: 5;
  pointer-events: none;

  &.top {
    top: 0;
    background: linear-gradient(180deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
  }

  &.bottom {
    bottom: 0;
    background: linear-gradient(0deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
  }
}

/* === 响应式适配 === */
@media (max-width: 768px) {
  .card-content {
    padding: 80px 30px 100px 20px;
  }

  .song-name {
    font-size: 32px;
  }

  .singer-name {
    font-size: 16px;
  }

  .right-actions {
    right: 8px;
  }

  .act-btn {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>
