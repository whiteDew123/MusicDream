<template>
  <div class="rank-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><TrophyBase /></el-icon>
        音乐排行
      </h2>
      <p class="page-desc">最热歌曲榜单，按播放量排名</p>
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
        <span class="col-plays">播放量</span>
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
        <span class="col-plays">{{ formatPlays(song.listenNumb) }}</span>
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
import { ref, onMounted } from 'vue'
import { TrophyBase, Headset, VideoPlay, Plus } from '@element-plus/icons-vue'
import { rankSongsApi } from '@/api/music'
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()

const loading = ref(true)
const rankSongs = ref([])

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

function handleImgError(e) {
  e.target.style.display = 'none'
}

async function loadData() {
  loading.value = true
  try {
    const res = await rankSongsApi(100)
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

/* 骨架屏 */
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
