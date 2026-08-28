<template>
  <div class="rank-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><TrophyBase /></el-icon>
        音乐排行
      </h2>
      <p class="page-desc">{{ activeTab === 'play' ? '最热歌曲榜单，按播放量排名' : '最新歌曲榜单，按发布时间排名' }}</p>
    </div>

    <!-- Tab 切换 -->
    <div class="rank-tabs">
      <div class="tab-item" :class="{ active: activeTab === 'play' }" @click="switchTab('play')">
        <el-icon><VideoPlay /></el-icon>
        播放量榜
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'time' }" @click="switchTab('time')">
        <el-icon><Clock /></el-icon>
        新歌榜
      </div>
      <div class="tab-indicator" :style="indicatorStyle"></div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-list">
      <div class="skeleton-item" v-for="n in 10" :key="n"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="rankSongs.length === 0" class="empty-state">
      <el-icon class="empty-icon"><TrophyBase /></el-icon>
      <p>暂无榜单数据</p>
    </div>

    <!-- 榜单列表 -->
    <div v-else class="rank-table">
      <div class="table-header">
        <span class="col-rank">排名</span>
        <span class="col-song">歌曲</span>
        <span class="col-singer">歌手</span>
        <span class="col-plays" v-if="activeTab === 'play'">播放量</span>
        <span class="col-plays" v-else>发布时间</span>
        <span class="col-action"></span>
      </div>
      <div
        v-for="(song, idx) in rankSongs"
        :key="song.musicId"
        class="table-row"
        :class="{ top: idx < 3 }"
        @dblclick="playSong(song)"
      >
        <span class="col-rank rank-num">
          <span class="rank-badge" v-if="idx < 3" :class="`rank-${idx + 1}`">{{ idx + 1 }}</span>
          <span v-else>{{ idx + 1 }}</span>
        </span>
        <div class="col-song">
          <div class="song-cover">
            <img v-if="song.imageUrl" :src="song.imageUrl" :alt="song.musicName" @error="handleImgError" />
            <el-icon v-else><Headset /></el-icon>
          </div>
          <span class="song-name" :title="song.musicName">{{ song.musicName }}</span>
        </div>
        <span class="col-singer singer-name" :title="song.singerName">{{ song.singerName }}</span>
        <span class="col-plays" v-if="activeTab === 'play'">{{ formatPlays(song.listenNumb) }}</span>
        <span class="col-plays" v-else>{{ formatTime(song.createTime) }}</span>
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

    <!-- 播放全部按钮 -->
    <div v-if="rankSongs.length" class="play-all-bar">
      <button class="play-all-btn" @click="playAll">
        <el-icon><VideoPlay /></el-icon>
        播放全部
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { TrophyBase, Headset, VideoPlay, Plus, Clock } from '@element-plus/icons-vue'
import { rankSongsApi, rankSongsByTimeApi } from '@/api/music'
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()

const loading = ref(true)
const rankSongs = ref([])
const activeTab = ref('play')

const indicatorStyle = computed(() => {
  return activeTab.value === 'play'
    ? { transform: 'translateX(0)' }
    : { transform: 'translateX(100%)' }
})

function switchTab(tab) {
  activeTab.value = tab
  loadData()
}

function playSong(song) {
  playerStore.playSong(song)
}

function addToPlaylist(song) {
  playerStore.addToPlaylist(song)
}

function playAll() {
  if (rankSongs.value.length) {
    playerStore.setPlaylist(rankSongs.value, 0)
  }
}

function formatPlays(num) {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toString()
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function handleImgError(e) {
  e.target.style.display = 'none'
}

async function loadData() {
  loading.value = true
  try {
    const api = activeTab.value === 'play' ? rankSongsApi : rankSongsByTimeApi
    const res = await api(100)
    rankSongs.value = res.data || []
  } catch (e) {
    console.error('加载排行数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.rank-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    color: var(--st-ink);
  }

  .page-desc {
    font-size: 14px;
    color: var(--st-ink-mute);
    margin-top: 4px;
  }
}

/* Tab 切换 */
.rank-tabs {
  position: relative;
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-md);
  overflow: hidden;
  width: fit-content;

  .tab-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 500;
    color: var(--st-ink-mute);
    cursor: pointer;
    transition: color 200ms ease;
    user-select: none;

    &:hover {
      color: var(--st-ink);
    }

    &.active {
      color: var(--st-primary);
    }
  }

  .tab-indicator {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 50%;
    height: 2px;
    background: var(--st-primary);
    transition: transform 250ms cubic-bezier(0.4, 0, 0.2, 1);
  }
}

/* 骨架屏 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.skeleton-item {
  height: 56px;
  border-radius: var(--rounded-md);
  background: var(--st-skeleton);
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

  .empty-icon {
    font-size: 48px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }
}

/* 榜单表格 */
.rank-table {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 1fr 160px 100px 80px;
  align-items: center;
  padding: 10px 20px;
  border-bottom: 1px solid var(--st-hairline);
  font-size: 12px;
  color: var(--st-ink-mute);
  font-weight: 500;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 160px 100px 80px;
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

  &.top .rank-num {
    color: var(--st-primary);
  }
}

.col-rank {
  text-align: center;
  font-feature-settings: 'tnum';
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 700;
  color: #fff;

  &.rank-1 { background: linear-gradient(135deg, #ff6b6b, #ee5a24); }
  &.rank-2 { background: linear-gradient(135deg, #ffa502, #ff6348); }
  &.rank-3 { background: linear-gradient(135deg, #ffd32a, #ffa502); }
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

.col-singer {
  font-size: 13px;
  color: var(--st-ink-mute);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-plays {
  font-size: 13px;
  color: var(--st-ink-mute);
  font-feature-settings: 'tnum';
  text-align: right;
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

/* 播放全部 */
.play-all-bar {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.play-all-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 32px;
  border: none;
  border-radius: var(--rounded-pill);
  background: var(--st-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    background: var(--st-primary-hover);
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.3);
  }
}
</style>