<template>
  <div class="singer-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Microphone /></el-icon>
        歌手
      </h2>
      <p class="page-desc">热门歌手，发现你喜欢的声音</p>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 12" :key="n">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="artists.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Microphone /></el-icon>
      <p>暂无歌手数据</p>
    </div>

    <!-- 歌手卡片网格 -->
    <div v-else class="singer-grid">
      <div
        v-for="artist in artists"
        :key="artist.id"
        class="singer-card"
        @click="goDetail(artist.id)"
      >
        <div class="avatar-wrap">
          <img
            v-if="artist.imageUrl && !avatarErrors[artist.id]"
            :src="artist.imageUrl"
            :alt="artist.username"
            class="singer-avatar"
            @error="handleAvatarError(artist.id)"
          />
          <div v-else class="avatar-placeholder fallback-avatar" :style="getAvatarFallbackStyle(artist.username)">
            <span class="avatar-initial">{{ getAvatarInitial(artist.username) }}</span>
          </div>
        </div>
        <div class="singer-name" :title="artist.username">{{ artist.username }}</div>
        <div class="singer-count">歌曲 {{ artist.songCount || 0 }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Microphone } from '@element-plus/icons-vue'
import { recommendArtistsApi } from '@/api/music'

const router = useRouter()

const loading = ref(true)
const artists = ref([])
// 头像加载失败标记，按 artistId 记录
const avatarErrors = reactive({})

const SINGER_GRADIENTS = [
  'linear-gradient(135deg, #5e5ce6, #9b8cff)',
  'linear-gradient(135deg, #ff6b6b, #ff8e53)',
  'linear-gradient(135deg, #11998e, #38ef7d)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #30cfd0, #330867)',
  'linear-gradient(135deg, #a8edea, #fed6e3)'
]

function hashColor(name) {
  let h = 0
  for (let i = 0; i < (name?.length || 0); i++) {
    h = (h * 31 + name.charCodeAt(i)) >>> 0
  }
  return SINGER_GRADIENTS[h % SINGER_GRADIENTS.length]
}

function getAvatarFallbackStyle(name) {
  return { background: hashColor(name) }
}

function getAvatarInitial(name) {
  if (!name) return '♪'
  const trimmed = name.trim()
  const enMatch = trimmed.match(/^([A-Za-z])/)
  if (enMatch) return enMatch[1].toUpperCase()
  return trimmed[0] || '♪'
}

// 跳转歌手详情
function goDetail(artistId) {
  router.push(`/singer/${artistId}`)
}

// 头像加载失败：标记后显示 Microphone 占位图标
function handleAvatarError(artistId) {
  avatarErrors[artistId] = true
}

// 时长格式化 seconds -> mm:ss
function formatDuration(seconds) {
  if (seconds === null || seconds === undefined || isNaN(seconds)) return '00:00'
  const sec = Math.floor(Number(seconds))
  const mm = Math.floor(sec / 60).toString().padStart(2, '0')
  const ss = (sec % 60).toString().padStart(2, '0')
  return `${mm}:${ss}`
}

// 加载推荐歌手列表
async function loadData() {
  loading.value = true
  try {
    const res = await recommendArtistsApi(30)
    artists.value = res.data || []
  } catch (e) {
    console.error('加载歌手列表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.singer-page {
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
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}
.skeleton-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}
.skeleton-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: var(--st-skeleton);
  animation: skeletonPulse 1.2s infinite ease-in-out;
}
.skeleton-line {
  width: 100px;
  height: 14px;
  margin-top: 12px;
  border-radius: var(--rounded-sm);
  background: var(--st-skeleton);
  animation: skeletonPulse 1.2s infinite ease-in-out;

  &.short {
    width: 60px;
    height: 12px;
    margin-top: 8px;
  }
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

/* 歌手卡片网格 */
.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
}

.singer-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
  border-radius: var(--rounded-lg);
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);

    .avatar-wrap {
      box-shadow: 0 6px 18px rgba(94, 92, 230, 0.18);
    }
  }

  .avatar-wrap {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: box-shadow 200ms ease;

    .singer-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .fallback-avatar {
      border-radius: 50%;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      .avatar-initial {
        font-size: 32px;
        font-weight: 700;
        text-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
      }
    }
  }

  .singer-name {
    margin-top: 14px;
    font-size: 15px;
    font-weight: 500;
    color: var(--st-ink);
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .singer-count {
    margin-top: 4px;
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}
</style>
