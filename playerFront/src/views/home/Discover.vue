<template>
  <div class="discover-page">
    <!-- 骨架屏（加载中） -->
    <div v-if="loading" class="skeleton-section">
      <div class="skeleton-banner"></div>
      <div class="skeleton-card-row" v-for="n in 3" :key="n">
        <div class="skeleton-title"></div>
        <div class="skeleton-cards">
          <div class="skeleton-card" v-for="m in 6" :key="m"></div>
        </div>
      </div>
    </div>

    <!-- 内容区 -->
    <template v-else>
      <!-- Banner -->
      <section class="banner-section">
        <div class="banner-card">
          <div class="banner-text">
            <span class="banner-tag">MusicDreamer</span>
            <h2 class="banner-title">让音乐流动</h2>
            <p class="banner-desc">发现属于你的声音，遇见每一个心动旋律</p>
            <button class="banner-btn" @click="playRecommend">
              <el-icon><VideoPlay /></el-icon>
              <span>立即试听</span>
            </button>
          </div>
          <div class="banner-visual">
            <div class="visual-circle circle-1"></div>
            <div class="visual-circle circle-2"></div>
            <div class="visual-circle circle-3"></div>
            <el-icon class="visual-icon"><Headset /></el-icon>
          </div>
        </div>
      </section>

      <!-- 推荐歌曲 -->
      <section class="content-section" v-if="recommendSongs.length">
        <div class="section-header">
          <h3 class="section-title">推荐歌曲</h3>
          <router-link to="/rank" class="section-more">查看更多 →</router-link>
        </div>
        <div class="song-grid">
          <div
            v-for="song in recommendSongs"
            :key="song.musicId"
            class="song-card"
            @click="playSong(song)"
          >
            <div class="song-cover">
              <img
                v-if="song.imageUrl"
                :src="song.imageUrl"
                :alt="song.musicName"
                @error="handleImgError"
              />
              <el-icon v-else class="cover-fallback"><Headset /></el-icon>
              <div class="play-overlay">
                <div class="play-btn">
                  <el-icon><VideoPlay /></el-icon>
                </div>
              </div>
            </div>
            <div class="song-info">
              <div class="song-name" :title="song.musicName">{{ song.musicName }}</div>
              <div class="song-singer">{{ song.singerName }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 排行榜 -->
      <section class="content-section" v-if="rankSongs.length">
        <div class="section-header">
          <h3 class="section-title">热门排行</h3>
          <router-link to="/rank" class="section-more">完整榜单 →</router-link>
        </div>
        <div class="rank-list">
          <div
            v-for="(song, idx) in rankSongs"
            :key="song.musicId"
            class="rank-item"
            :class="{ top: idx < 3 }"
            @click="playSong(song)"
          >
            <span class="rank-num">{{ idx + 1 }}</span>
            <div class="rank-cover">
              <img
                v-if="song.imageUrl"
                :src="song.imageUrl"
                :alt="song.musicName"
                @error="handleImgError"
              />
              <el-icon v-else><Headset /></el-icon>
            </div>
            <div class="rank-info">
              <span class="rank-name" :title="song.musicName">{{ song.musicName }}</span>
              <span class="rank-singer">{{ song.singerName }}</span>
            </div>
            <span class="rank-plays">{{ formatPlays(song.listenNumb) }}</span>
          </div>
        </div>
      </section>

      <!-- 推荐歌单 -->
      <section class="content-section" v-if="songLists.length">
        <div class="section-header">
          <h3 class="section-title">精选歌单</h3>
          <router-link to="/songlist" class="section-more">更多歌单 →</router-link>
        </div>
        <div class="playlist-grid">
          <router-link
            v-for="list in songLists"
            :key="list.id"
            :to="`/songlist/${list.id}`"
            class="playlist-card"
          >
            <div class="playlist-cover">
              <img
                v-if="list.pic"
                :src="list.pic"
                :alt="list.name"
                @error="handleImgError"
              />
              <el-icon v-else><Files /></el-icon>
            </div>
            <div class="playlist-name" :title="list.name">{{ list.name }}</div>
          </router-link>
        </div>
      </section>

      <!-- 推荐歌手 -->
      <section class="content-section" v-if="recommendArtists.length">
        <div class="section-header">
          <h3 class="section-title">推荐歌手</h3>
          <router-link to="/singer" class="section-more">全部歌手 →</router-link>
        </div>
        <div class="artist-grid">
          <div
            v-for="artist in recommendArtists"
            :key="artist.id"
            class="artist-card"
            @click="goSinger(artist.id)"
          >
            <div class="artist-avatar">
              <img
                v-if="artist.imageUrl"
                :src="artist.imageUrl"
                :alt="artist.username"
                @error="handleImgError"
              />
              <el-icon v-else><Microphone /></el-icon>
            </div>
            <div class="artist-name" :title="artist.username">{{ artist.username }}</div>
            <div class="artist-stat">{{ artist.songCount || 0 }} 首歌曲</div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { VideoPlay, Headset, Files, Microphone } from '@element-plus/icons-vue'
import { recommendSongsApi, rankSongsApi, recommendArtistsApi } from '@/api/music'
import { publicSongListApi } from '@/api/songList'
import { usePlayerStore } from '@/store/player'
import { useUserStore } from '@/store/user'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const loading = ref(true)
const recommendSongs = ref([])
const rankSongs = ref([])
const songLists = ref([])
const recommendArtists = ref([])

// 播放歌曲
function playSong(song) {
  playerStore.playSong(song)
}

// 跳转歌手详情
function goSinger(artistId) {
  router.push(`/singer/${artistId}`)
}

// 播放推荐列表
function playRecommend() {
  if (recommendSongs.value.length) {
    playerStore.setPlaylist(recommendSongs.value, 0)
    ElMessage.success('开始播放推荐歌曲')
  } else {
    ElMessage.info('暂无推荐歌曲')
  }
}

// 格式化播放次数
function formatPlays(num) {
  if (!num) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toString()
}

// 图片加载失败
function handleImgError(e) {
  e.target.style.display = 'none'
}

// 加载首页数据
async function loadData() {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    const [songsRes, rankRes, listsRes, artistsRes] = await Promise.all([
      recommendSongsApi({ userId, limit: 6 }),
      rankSongsApi(5),
      publicSongListApi(),
      recommendArtistsApi(6)
    ])
    recommendSongs.value = songsRes.data || []
    rankSongs.value = rankRes.data || []
    songLists.value = listsRes.data || []
    recommendArtists.value = artistsRes.data || []
  } catch (e) {
    console.error('加载首页数据失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.discover-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* === 骨架屏（animation.md 第三章）=== */
.skeleton-section {
  .skeleton-banner {
    height: 200px;
    border-radius: var(--rounded-lg);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
  .skeleton-card-row {
    margin-top: 40px;
  }
  .skeleton-title {
    width: 120px;
    height: 24px;
    border-radius: var(--rounded-sm);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
  .skeleton-cards {
    display: flex;
    gap: 16px;
    margin-top: 16px;
  }
  .skeleton-card {
    flex: 1;
    height: 200px;
    border-radius: var(--rounded-lg);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === Banner === */
.banner-section {
  margin-bottom: 40px;
}

.banner-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 200px;
  padding: 0 48px;
  border-radius: var(--rounded-lg);
  background: linear-gradient(135deg, var(--st-primary) 0%, var(--st-primary-soft) 100%);
  overflow: hidden;
  position: relative;
}

.banner-text {
  position: relative;
  z-index: 1;

  .banner-tag {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--rounded-pill);
    font-size: 12px;
    color: #fff;
    margin-bottom: 12px;
  }

  .banner-title {
    font-size: 36px;
    font-weight: 300;
    color: #fff;
    letter-spacing: -0.8px;
    margin-bottom: 8px;
  }

  .banner-desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 20px;
  }
}

.banner-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  border-radius: var(--rounded-pill);
  background: #fff;
  color: var(--st-primary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.banner-visual {
  position: relative;
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;

  .visual-circle {
    position: absolute;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.2);
  animation: visualPulse 3s infinite ease-in-out;
  }
  .circle-1 {
    width: 180px;
    height: 180px;
    animation-delay: 0s;
  }
  .circle-2 {
    width: 140px;
    height: 140px;
    animation-delay: 0.5s;
  }
  .circle-3 {
    width: 100px;
    height: 100px;
    animation-delay: 1s;
  }
  .visual-icon {
    font-size: 48px;
    color: rgba(255, 255, 255, 0.9);
    z-index: 1;
  }
}

@keyframes visualPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

/* === 内容区通用 === */
.content-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.2px;
  }

  .section-more {
    font-size: 13px;
    color: var(--st-ink-mute);
    transition: color 150ms ease;

    &:hover {
      color: var(--st-primary);
    }
  }
}

/* === 推荐歌曲网格 === */
.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.song-card {
  cursor: pointer;

  .song-cover {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-md);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 250ms ease;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-fallback {
      font-size: 32px;
      color: var(--st-ink-mute);
    }

    /* 悬停上浮 + 播放按钮渐显（animation.md 第二章）*/
    .play-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 300ms ease;

      .play-btn {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background: var(--st-primary);
        color: #fff;
        font-size: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transform: scale(0.8);
        transition: all 300ms ease;
        box-shadow: 0 4px 16px rgba(94, 92, 230, 0.4);
      }
    }
  }

  &:hover .song-cover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  &:hover .play-overlay {
    background: rgba(0, 0, 0, 0.3);

    .play-btn {
      opacity: 1;
      transform: scale(1);
    }
  }

  .song-info {
    margin-top: 10px;

    .song-name {
      font-size: 14px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .song-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
      margin-top: 2px;
    }
  }
}

/* === 排行榜列表 === */
.rank-list {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);
  }

  &.top .rank-num {
    color: var(--st-primary);
    font-weight: 600;
  }

  .rank-num {
    width: 28px;
    font-size: 16px;
    font-weight: 500;
    color: var(--st-ink-mute);
    text-align: center;
    font-feature-settings: 'tnum';
  }

  .rank-cover {
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

  .rank-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .rank-name {
      font-size: 14px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .rank-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
    }
  }

  .rank-plays {
    font-size: 13px;
    color: var(--st-ink-mute);
    font-feature-settings: 'tnum';
  }
}

/* === 歌单网格 === */
.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.playlist-card {
  cursor: pointer;

  .playlist-cover {
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-md);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 250ms ease;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 32px;
      color: var(--st-ink-mute);
    }
  }

  &:hover .playlist-cover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .playlist-name {
    margin-top: 10px;
    font-size: 14px;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
  }
}

/* === 歌手网格 === */
.artist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 20px;
}

.artist-card {
  text-align: center;
  cursor: pointer;

  .artist-avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    margin: 0 auto;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 250ms ease;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 36px;
      color: var(--st-ink-mute);
    }
  }

  &:hover .artist-avatar {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .artist-name {
    margin-top: 10px;
    font-size: 14px;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .artist-stat {
    font-size: 12px;
    color: var(--st-ink-mute);
    margin-top: 2px;
  }
}
</style>
