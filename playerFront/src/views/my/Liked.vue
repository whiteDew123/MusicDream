<template>
  <div class="liked-page">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><StarFilled /></el-icon>
        <h2>我喜欢的音乐</h2>
      </div>
    </header>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-wrap">
      <div class="skeleton-summary"></div>
      <div class="skeleton-table">
        <div class="skeleton-row" v-for="n in 8" :key="n"></div>
      </div>
    </div>

    <!-- 内容区 -->
    <template v-else>
      <!-- 空状态 -->
      <div v-if="!songs.length" class="empty-state">
        <el-icon class="empty-icon"><StarFilled /></el-icon>
        <p class="empty-text">还没有收藏的歌曲</p>
        <p class="empty-sub">去发现页找到喜欢的歌曲，点击收藏即可加入这里</p>
        <router-link to="/discover" class="empty-btn">去发现音乐</router-link>
      </div>

      <!-- 列表区 -->
      <template v-else>
        <!-- 顶部摘要 -->
        <div class="summary-bar">
          <div class="summary-meta">
            <span class="meta-count">共 <b>{{ songs.length }}</b> 首歌曲</span>
          </div>
          <div class="summary-actions">
            <button class="action-btn primary" @click="playAll">
              <el-icon><VideoPlay /></el-icon>
              <span>播放全部</span>
            </button>
            <button class="action-btn" @click="addAll">
              <el-icon><Plus /></el-icon>
              <span>加入列表</span>
            </button>
          </div>
        </div>

        <!-- 歌曲表格 -->
        <div class="song-table">
          <!-- 表头 -->
          <div class="table-head">
            <span class="col-index">#</span>
            <span class="col-cover">封面</span>
            <span class="col-name">歌曲</span>
            <span class="col-singer">歌手</span>
            <span class="col-time">时长</span>
            <span class="col-ops">操作</span>
          </div>
          <!-- 表体 -->
          <div
            v-for="(song, idx) in songs"
            :key="song.musicId"
            class="table-row"
            @dblclick="playSong(song)"
          >
            <span class="col-index">{{ idx + 1 }}</span>
            <div class="col-cover">
              <img
                v-if="song.imageUrl && !errorCovers.has(song.musicId)"
                :src="song.imageUrl"
                :alt="song.musicName"
                @error="handleCoverError(song.musicId)"
              />
              <el-icon v-else class="cover-fallback"><Headset /></el-icon>
            </div>
            <span class="col-name" :title="song.musicName">{{ song.musicName }}</span>
            <span class="col-singer" :title="song.singerName">{{ song.singerName }}</span>
            <span class="col-time">{{ formatDuration(song.timelength) }}</span>
            <div class="col-ops">
              <button class="op-btn" title="播放" @click="playSong(song)">
                <el-icon><VideoPlay /></el-icon>
              </button>
              <button class="op-btn" title="加入播放列表" @click="addOne(song)">
                <el-icon><Plus /></el-icon>
              </button>
              <button
                class="op-btn unlike"
                title="取消收藏"
                @click="removeLiked(song)"
              >
                <el-icon><StarFilled /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { StarFilled, VideoPlay, Plus, Headset } from '@element-plus/icons-vue'
import { likedMusicApi, removeLikedMusicApi } from '@/api/like'
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()

const loading = ref(true)
const songs = ref([])
// 封面加载失败的 musicId 集合
const errorCovers = reactive(new Set())

// 加载收藏歌曲
// 注：响应拦截器在 code !== 200 时已自动提示并 reject，故此处成功即 code 200
async function loadData() {
  loading.value = true
  try {
    const res = await likedMusicApi()
    songs.value = res.data || []
  } catch (e) {
    console.error('加载我喜欢的音乐失败:', e)
    songs.value = []
  } finally {
    loading.value = false
  }
}

// 时长格式化 → mm:ss
function formatDuration(t) {
  if (t === null || t === undefined || t === '') return '--:--'
  let sec = Number(t)
  if (isNaN(sec)) return '--:--'
  // 数值过大时按毫秒处理
  if (sec > 3600) sec = sec / 1000
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 播放单首
function playSong(song) {
  playerStore.playSong(song)
}

// 加入播放列表（不立即播放）
function addOne(song) {
  playerStore.addToPlaylist(song)
  ElMessage.success(`已加入播放列表：${song.musicName}`)
}

// 播放全部
function playAll() {
  if (!songs.value.length) return
  playerStore.setPlaylist(songs.value, 0)
  ElMessage.success('开始播放全部收藏歌曲')
}

// 加入全部到播放列表
function addAll() {
  if (!songs.value.length) return
  songs.value.forEach((song) => playerStore.addToPlaylist(song))
  ElMessage.success(`已加入 ${songs.value.length} 首歌曲到播放列表`)
}

// 取消收藏
async function removeLiked(song) {
  try {
    await removeLikedMusicApi(song.musicId)
    songs.value = songs.value.filter((s) => s.musicId !== song.musicId)
    errorCovers.delete(song.musicId)
    ElMessage.success('已取消收藏')
  } catch (e) {
    // 拦截器已统一提示错误，此处仅记录日志
    console.error('取消收藏失败:', e)
  }
}

// 封面加载失败
function handleCoverError(musicId) {
  errorCovers.add(musicId)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.liked-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* === 页头 === */
.page-header {
  margin-bottom: 24px;

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;

    .title-icon {
      font-size: 26px;
      color: var(--st-primary);
    }

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: var(--st-ink);
      letter-spacing: -0.2px;
    }
  }
}

/* === 骨架屏 === */
.skeleton-wrap {
  .skeleton-summary {
    height: 56px;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    margin-bottom: 16px;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
  .skeleton-table {
    background: var(--st-canvas);
    border: 1px solid var(--st-hairline);
    border-radius: var(--rounded-lg);
    overflow: hidden;
  }
  .skeleton-row {
    height: 56px;
    border-bottom: 1px solid var(--st-hairline);
    animation: skeletonPulse 1.2s infinite ease-in-out;
    &:last-child {
      border-bottom: none;
    }
  }
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === 空状态 === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  text-align: center;

  .empty-icon {
    font-size: 64px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 18px;
    color: var(--st-ink);
    margin-bottom: 6px;
  }

  .empty-sub {
    font-size: 13px;
    color: var(--st-ink-mute);
    margin-bottom: 20px;
  }

  .empty-btn {
    padding: 8px 20px;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
    color: #fff;
    font-size: 14px;
    transition: background 200ms ease;

    &:hover {
      background: var(--st-primary-hover);
    }
  }
}

/* === 顶部摘要 === */
.summary-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .summary-meta .meta-count {
    font-size: 14px;
    color: var(--st-ink-mute);

    b {
      color: var(--st-ink);
      font-weight: 600;
    }
  }

  .summary-actions {
    display: flex;
    gap: 10px;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  color: var(--st-ink);
  font-size: 13px;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    border-color: var(--st-primary);
    color: var(--st-primary);
  }

  &.primary {
    background: var(--st-primary);
    border-color: var(--st-primary);
    color: #fff;

    &:hover {
      background: var(--st-primary-hover);
      border-color: var(--st-primary-hover);
      color: #fff;
    }
  }
}

/* === 歌曲表格 === */
.song-table {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 50px 56px 1fr 160px 70px 140px;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
}

.table-head {
  font-size: 12px;
  color: var(--st-ink-mute);
  border-bottom: 1px solid var(--st-hairline);
  background: var(--st-canvas-soft);
}

.table-row {
  cursor: pointer;
  transition: background 150ms ease;
  border-bottom: 1px solid var(--st-hairline);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--st-canvas-hover);

    .col-ops .op-btn {
      opacity: 1;
    }
  }

  .col-index {
    font-size: 13px;
    color: var(--st-ink-mute);
    text-align: center;
    font-feature-settings: 'tnum';
  }

  .col-cover {
    width: 44px;
    height: 44px;
    border-radius: var(--rounded-sm);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-fallback {
      font-size: 18px;
      color: var(--st-ink-mute);
    }
  }

  .col-name {
    font-size: 14px;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
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
  }

  .col-ops {
    display: flex;
    align-items: center;
    gap: 6px;

    .op-btn {
      width: 30px;
      height: 30px;
      border: none;
      background: transparent;
      color: var(--st-ink-mute);
      cursor: pointer;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      opacity: 0;
      transition: all 200ms ease;

      &:hover {
        background: var(--st-input-bg);
        color: var(--st-primary);
      }

      &.unlike {
        color: var(--st-primary);
        opacity: 1;

        &:hover {
          color: var(--st-primary-hover);
        }
      }
    }
  }
}
</style>
