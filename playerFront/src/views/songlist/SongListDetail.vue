<template>
  <div class="songlist-detail">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回歌单广场</span>
    </button>

    <!-- 顶部信息区骨架屏 -->
    <div v-if="metaLoading" class="header-skeleton">
      <div class="skeleton-cover"></div>
      <div class="skeleton-meta">
        <div class="skeleton-line lg"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <!-- 顶部信息区 -->
    <header v-else-if="detail" class="detail-header">
      <div class="detail-cover">
        <img
          v-if="detail.pic && !coverError"
          :src="detail.pic"
          :alt="detail.name"
          @error="coverError = true"
        />
        <el-icon v-else class="cover-fallback"><Files /></el-icon>
      </div>

      <div class="detail-meta">
        <div class="meta-top">
          <h1 class="detail-name" :title="detail.name">{{ detail.name }}</h1>
          <span v-if="detail.style" class="style-tag">{{ detail.style }}</span>
        </div>

        <div class="meta-creator">
          <el-icon><User /></el-icon>
          <span>{{ detail.username || '未知创建者' }}</span>
        </div>

        <p v-if="detail.introduction" class="meta-intro">{{ detail.introduction }}</p>

        <div class="meta-actions">
          <button
            class="play-all-btn"
            :disabled="songsLoading || songs.length === 0"
            @click="playAll"
          >
            <el-icon><VideoPlay /></el-icon>
            <span>播放全部</span>
          </button>
          <button
            class="like-btn"
            :class="{ liked: isLiked }"
            :disabled="likeLoading"
            @click="toggleLike"
          >
            <el-icon><StarFilled v-if="isLiked" /><Star v-else /></el-icon>
            <span>{{ isLiked ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 歌曲列表标题 -->
    <div class="songs-section-title">
      <span>歌曲列表</span>
      <span v-if="songs.length" class="songs-count">共 {{ songs.length }} 首</span>
    </div>

    <!-- 歌曲列表骨架屏 -->
    <div v-if="songsLoading" class="skeleton-list">
      <div class="skeleton-item" v-for="n in 8" :key="n"></div>
    </div>

    <!-- 歌曲空状态 -->
    <div v-else-if="songs.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Headset /></el-icon>
      <p>该歌单暂无歌曲</p>
    </div>

    <!-- 歌曲表格 -->
    <div v-else class="song-table">
      <div class="table-header">
        <span class="col-index">#</span>
        <span class="col-song">歌曲</span>
        <span class="col-singer">歌手</span>
        <span class="col-time">时长</span>
        <span class="col-action"></span>
      </div>
      <div
        v-for="(song, idx) in songs"
        :key="song.musicId"
        class="table-row"
        @dblclick="playSong(song)"
      >
        <span class="col-index">
          <span class="row-num">{{ idx + 1 }}</span>
          <el-icon class="row-play-icon"><VideoPlay /></el-icon>
        </span>
        <div class="col-song">
          <div class="song-cover">
            <img
              v-if="song.imageUrl"
              :src="song.imageUrl"
              :alt="song.musicName"
              @error="handleImgError"
            />
            <el-icon v-else><Headset /></el-icon>
          </div>
          <span class="song-name" :title="song.musicName">{{ song.musicName }}</span>
        </div>
        <span class="col-singer singer-name" :title="song.singerName">{{ song.singerName }}</span>
        <span class="col-time">{{ formatDuration(song.timelength) }}</span>
        <span class="col-action">
          <button class="icon-btn" @click.stop="playSong(song)" title="播放">
            <el-icon><VideoPlay /></el-icon>
          </button>
          <button class="icon-btn" @click.stop="addToPlaylist(song)" title="加入列表">
            <el-icon><Plus /></el-icon>
          </button>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Files,
  User,
  VideoPlay,
  Plus,
  Star,
  StarFilled,
  Headset
} from '@element-plus/icons-vue'
import {
  songListDetailApi,
  songListSongsApi,
  toggleLikeSongListApi
} from '@/api/songList'
import { usePlayerStore } from '@/store/player'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()

// 歌单 ID（从路由参数获取）
const listId = route.params.id

// 歌单元数据（metadata）
const metaLoading = ref(true)
const detail = ref(null)
const coverError = ref(false)

// 歌单内歌曲列表
const songsLoading = ref(true)
const songs = ref([])

// 收藏状态
const isLiked = ref(false)
const likeLoading = ref(false)

// 返回歌单广场
function goBack() {
  router.push('/songlist')
}

// 时长格式化：秒 → mm:ss
function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return '--:--'
  const sec = Math.floor(Number(seconds))
  if (isNaN(sec) || sec < 0) return '--:--'
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 播放量格式化：万 / 亿
function formatPlays(num) {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toString()
}

// 列表内封面加载失败：隐藏 img
function handleImgError(e) {
  e.target.style.display = 'none'
}

// 播放单首歌曲
function playSong(song) {
  playerStore.playSong(song)
}

// 加入播放列表（不立即播放）
function addToPlaylist(song) {
  playerStore.addToPlaylist(song)
  ElMessage.success('已加入播放列表')
}

// 播放全部：转换字段后设置播放列表
// 说明：songListSongsApi 返回字段与 playSong 所需字段一致
//       （musicId/musicName/musicUrl/imageUrl/singerName/timelength/listenNumb），
//       这里做一次字段映射，剔除 listId 等非歌曲字段
function playAll() {
  if (songs.value.length === 0) return
  const list = songs.value.map((s) => ({
    musicId: s.musicId,
    musicName: s.musicName,
    musicUrl: s.musicUrl,
    imageUrl: s.imageUrl,
    lyric: s.lyric,
    singerName: s.singerName,
    timelength: s.timelength,
    listenNumb: s.listenNumb
  }))
  playerStore.setPlaylist(list, 0)
  ElMessage.success('开始播放全部')
}

// 切换收藏状态
async function toggleLike() {
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await toggleLikeSongListApi(listId)
    // data=true 表示已收藏，false 表示已取消
    isLiked.value = !!res.data
    ElMessage.success(isLiked.value ? '已收藏歌单' : '已取消收藏')
  } catch (e) {
    console.error('切换收藏失败:', e)
    ElMessage.error('操作失败，请重试')
  } finally {
    likeLoading.value = false
  }
}

// 加载歌单元数据
async function loadDetail() {
  metaLoading.value = true
  try {
    const res = await songListDetailApi(listId)
    detail.value = res.data || null
    // 同步初始收藏状态
    isLiked.value = !!(detail.value && detail.value.isLike)
  } catch (e) {
    console.error('加载歌单详情失败:', e)
  } finally {
    metaLoading.value = false
  }
}

// 加载歌单内歌曲
async function loadSongs() {
  songsLoading.value = true
  try {
    const res = await songListSongsApi(listId)
    songs.value = res.data || []
  } catch (e) {
    console.error('加载歌单歌曲失败:', e)
  } finally {
    songsLoading.value = false
  }
}

onMounted(() => {
  loadDetail()
  loadSongs()
})
</script>

<style scoped lang="scss">
.songlist-detail {
  max-width: 1000px;
  margin: 0 auto;
}

/* === 返回按钮 === */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  margin-bottom: 20px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  color: var(--st-ink-mute);
  font-size: 13px;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    color: var(--st-primary);
    border-color: var(--st-primary);
    background: rgba(94, 92, 230, 0.06);
  }
}

/* === 顶部信息区骨架屏 === */
.header-skeleton {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);

  .skeleton-cover {
    width: 200px;
    height: 200px;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
    flex-shrink: 0;
  }

  .skeleton-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-top: 8px;

    .skeleton-line {
      height: 16px;
      width: 70%;
      border-radius: var(--rounded-sm);
      background: #e8ecf0;
      animation: skeletonPulse 1.2s infinite ease-in-out;

      &.lg {
        height: 28px;
        width: 50%;
      }

      &.short {
        width: 35%;
      }
    }
  }
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === 顶部信息区 === */
.detail-header {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: 0 1px 3px rgba(0, 55, 112, 0.08);
}

.detail-cover {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--st-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-fallback {
    font-size: 56px;
    color: var(--st-ink-mute);
  }
}

.detail-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;

  .meta-top {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .detail-name {
    font-size: 26px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.3px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .style-tag {
    flex-shrink: 0;
    padding: 3px 12px;
    border-radius: var(--rounded-pill);
    background: var(--st-primary-subdued);
    color: var(--st-brand-dark);
    font-size: 12px;
    line-height: 1.4;
  }

  .meta-creator {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 12px;
    font-size: 14px;
    color: var(--st-ink-mute);

    .el-icon {
      font-size: 14px;
    }
  }

  .meta-intro {
    margin-top: 12px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--st-ink-mute);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }

  .meta-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
  }
}

/* === 操作按钮 === */
.play-all-btn,
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 22px;
  border-radius: var(--rounded-pill);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms ease;
  border: 1px solid transparent;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.play-all-btn {
  background: var(--st-primary);
  color: #fff;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover);
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.3);
  }
}

.like-btn {
  background: var(--st-canvas);
  border-color: var(--st-hairline);
  color: var(--st-ink-mute);

  &:hover:not(:disabled) {
    color: var(--st-primary);
    border-color: var(--st-primary);
    background: rgba(94, 92, 230, 0.06);
  }

  &.liked {
    color: var(--st-primary);
    border-color: var(--st-primary);
    background: rgba(94, 92, 230, 0.1);

    .el-icon {
      color: var(--st-primary);
    }
  }
}

/* === 歌曲列表区 === */
.songs-section-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 32px 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--st-ink);

  .songs-count {
    font-size: 12px;
    font-weight: 400;
    color: var(--st-ink-mute);
  }
}

/* === 歌曲列表骨架屏 === */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-item {
  height: 56px;
  border-radius: var(--rounded-md);
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
}

/* === 空状态 === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: var(--st-ink-mute);

  .empty-icon {
    font-size: 48px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }
}

/* === 歌曲表格 === */
.song-table {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 1fr 160px 80px 100px;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid var(--st-hairline);
  font-size: 12px;
  color: var(--st-ink-mute);
  font-weight: 500;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 160px 80px 100px;
  align-items: center;
  padding: 8px 20px;
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);

    .col-action .icon-btn {
      opacity: 1;
    }

    .row-num {
      display: none;
    }

    .row-play-icon {
      display: inline-flex;
    }
  }
}

.col-index {
  text-align: center;
  font-feature-settings: 'tnum';
  display: flex;
  align-items: center;
  justify-content: center;

  .row-num {
    font-size: 14px;
    color: var(--st-ink-mute);
  }

  .row-play-icon {
    display: none;
    color: var(--st-primary);
    font-size: 16px;
  }
}

.col-song {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  min-width: 0;

  .song-cover {
    width: 40px;
    height: 40px;
    border-radius: var(--rounded-sm);
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
      font-size: 18px;
      color: var(--st-ink-mute);
    }
  }

  .song-name {
    font-size: 14px;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.col-singer {
  font-size: 13px;
  color: var(--st-ink-mute);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-time {
  font-size: 13px;
  color: var(--st-ink-mute);
  font-feature-settings: 'tnum';
  text-align: center;
}

.col-action {
  display: flex;
  gap: 8px;
  justify-content: flex-end;

  .icon-btn {
    border: none;
    background: transparent;
    color: var(--st-ink-mute);
    cursor: pointer;
    font-size: 16px;
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    opacity: 0;
    transition: all 150ms ease;

    &:hover {
      color: var(--st-primary);
      background: rgba(94, 92, 230, 0.1);
    }
  }
}
</style>
