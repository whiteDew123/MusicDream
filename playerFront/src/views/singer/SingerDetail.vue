<template>
  <div class="singer-detail">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      返回歌手列表
    </button>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-detail">
      <div class="skeleton-header">
        <div class="skeleton-big-avatar"></div>
        <div class="skeleton-meta">
          <div class="skeleton-line lg"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
        </div>
      </div>
      <div class="skeleton-list">
        <div class="skeleton-item" v-for="n in 8" :key="n"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!artist" class="empty-state">
      <el-icon class="empty-icon"><Microphone /></el-icon>
      <p>未找到歌手信息</p>
    </div>

    <template v-else>
      <!-- 歌手头部信息 -->
      <div class="artist-header">
        <div class="big-avatar-wrap">
          <img
            v-if="artist.imageUrl && !avatarError"
            :src="artist.imageUrl"
            :alt="artist.username"
            class="big-avatar"
            @error="avatarError = true"
          />
          <el-icon v-else class="big-avatar-placeholder"><Microphone /></el-icon>
        </div>
        <div class="artist-meta">
          <h2 class="artist-name">{{ artist.username }}</h2>
          <p class="artist-desc" v-if="artist.about">{{ artist.about }}</p>
          <p class="artist-desc muted" v-else>暂无简介</p>
          <div class="artist-stats">
            <span class="stat">歌曲 {{ artist.songCount || (songs.length) || 0 }}</span>
            <button class="play-all-btn" v-if="songs.length" @click="playAll">
              <el-icon><VideoPlay /></el-icon>
              播放全部
            </button>
          </div>
        </div>
      </div>

      <!-- 歌曲列表 -->
      <div v-if="songs.length === 0" class="empty-state small">
        <el-icon class="empty-icon"><Headset /></el-icon>
        <p>该歌手暂无歌曲</p>
      </div>

      <div v-else class="song-table">
        <div class="table-header">
          <span class="col-index">序号</span>
          <span class="col-song">歌曲</span>
          <span class="col-duration">时长</span>
          <span class="col-action"></span>
        </div>
        <div
          v-for="(song, idx) in songs"
          :key="song.musicId"
          class="table-row"
          @dblclick="playSong(song)"
        >
          <span class="col-index index-num">{{ idx + 1 }}</span>
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
          <span class="col-duration">{{ formatDuration(song.timelength) }}</span>
          <span class="col-action">
            <button class="play-icon-btn" @click.stop="playSong(song)" title="播放">
              <el-icon><VideoPlay /></el-icon>
            </button>
            <button class="add-icon-btn" @click.stop="addToPlaylist(song)" title="加入列表">
              <el-icon><Plus /></el-icon>
            </button>
          </span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Microphone, Headset, VideoPlay, Plus } from '@element-plus/icons-vue'
import { artistDetailApi } from '@/api/music'
import { usePlayerStore } from '@/store/player'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()

const loading = ref(true)
const artist = ref(null)
// 头像加载失败标记
const avatarError = ref(false)

// 歌曲列表
const songs = computed(() => (artist.value && artist.value.songs) || [])

// 播放单首
function playSong(song) {
  playerStore.playSong(song)
}

// 加入播放列表
function addToPlaylist(song) {
  playerStore.addToPlaylist(song)
}

// 播放全部
function playAll() {
  if (songs.value.length) {
    playerStore.setPlaylist(songs.value, 0)
  }
}

// 返回歌手列表
function goBack() {
  router.push('/singer')
}

// 时长格式化 seconds -> mm:ss
function formatDuration(seconds) {
  if (seconds === null || seconds === undefined || isNaN(seconds)) return '00:00'
  const sec = Math.floor(Number(seconds))
  const mm = Math.floor(sec / 60).toString().padStart(2, '0')
  const ss = (sec % 60).toString().padStart(2, '0')
  return `${mm}:${ss}`
}

// 歌曲封面加载失败：隐藏 img
function handleImgError(e) {
  e.target.style.display = 'none'
}

// 加载歌手详情
async function loadData() {
  loading.value = true
  avatarError.value = false
  try {
    const id = route.params.id
    const res = await artistDetailApi(id)
    artist.value = res.data || null
  } catch (e) {
    console.error('加载歌手详情失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.singer-detail {
  max-width: 1000px;
  margin: 0 auto;
}

/* 返回按钮 */
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
  transition: all 150ms ease;

  &:hover {
    color: var(--st-primary);
    border-color: var(--st-primary);
  }
}

/* 骨架屏 */
.skeleton-header {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 32px;
}
.skeleton-big-avatar {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
  flex-shrink: 0;
}
.skeleton-meta {
  flex: 1;
}
.skeleton-line {
  height: 16px;
  border-radius: var(--rounded-sm);
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
  margin-bottom: 12px;

  &.lg { width: 200px; height: 28px; }
  &.short { width: 120px; }
}
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
@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: var(--st-ink-mute);

  &.small { padding: 48px 0; }

  .empty-icon {
    font-size: 48px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }
}

/* 歌手头部信息 */
.artist-header {
  display: flex;
  gap: 28px;
  align-items: center;
  padding: 24px;
  margin-bottom: 28px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
}

.big-avatar-wrap {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--st-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .big-avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .big-avatar-placeholder {
    font-size: 60px;
    color: var(--st-primary-subdued);
  }
}

.artist-meta {
  flex: 1;
  min-width: 0;

  .artist-name {
    font-size: 26px;
    font-weight: 600;
    color: var(--st-ink);
    margin-bottom: 12px;
  }

  .artist-desc {
    font-size: 14px;
    line-height: 1.6;
    color: var(--st-ink-mute);
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;

    &.muted { color: var(--st-primary-subdued); }
  }

  .artist-stats {
    display: flex;
    align-items: center;
    gap: 20px;

    .stat {
      font-size: 13px;
      color: var(--st-ink-mute);
    }
  }
}

.play-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 22px;
  border: none;
  border-radius: var(--rounded-pill);
  background: var(--st-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    background: var(--st-primary-hover);
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.3);
  }
}

/* 歌曲表格 */
.song-table {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 1fr 80px 100px;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid var(--st-hairline);
  font-size: 12px;
  color: var(--st-ink-mute);
  font-weight: 500;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 80px 100px;
  align-items: center;
  padding: 8px 20px;
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);

    .col-action button {
      opacity: 1;
    }
  }
}

.col-index {
  text-align: center;
  font-size: 13px;
  color: var(--st-ink-mute);
  font-feature-settings: 'tnum';
}

.col-song {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;

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

    img { width: 100%; height: 100%; object-fit: cover; }
    .el-icon { font-size: 18px; color: var(--st-ink-mute); }
  }

  .song-name {
    font-size: 14px;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.col-duration {
  font-size: 13px;
  color: var(--st-ink-mute);
  font-feature-settings: 'tnum';
}

.col-action {
  display: flex;
  gap: 8px;
  justify-content: flex-end;

  button {
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
