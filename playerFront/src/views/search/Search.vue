<template>
  <div class="search-page">
    <div class="search-header">
      <h2 class="search-title">
        <el-icon><Search /></el-icon>
        搜索结果
        <span v-if="keyword" class="search-keyword">"{{ keyword }}"</span>
      </h2>
    </div>

    <div v-if="loading" class="search-loading">
      <el-icon class="loading-icon is-loading"><Loading /></el-icon>
      <span>搜索中…</span>
    </div>

    <div v-else-if="results.length === 0" class="search-empty">
      <el-icon class="empty-icon"><Search /></el-icon>
      <p v-if="keyword">没有找到 "{{ keyword }}" 相关的歌曲</p>
      <p v-else>输入关键词开始搜索</p>
    </div>

    <div v-else class="search-results">
      <div
        v-for="(song, idx) in results"
        :key="song.musicId"
        class="result-item"
        @click="playSong(song)"
      >
        <span class="result-num">{{ idx + 1 }}</span>
        <div class="result-cover">
          <img
            v-if="song.imageUrl"
            :src="song.imageUrl"
            :alt="song.musicName"
          />
          <el-icon v-else><Headset /></el-icon>
        </div>
        <div class="result-info">
          <span class="result-name">{{ song.musicName }}</span>
          <span class="result-singer">{{ song.singerName }}</span>
        </div>
        <span class="result-time">{{ formatTime(song.timelength) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Loading, Headset } from '@element-plus/icons-vue'
import { searchSongsApi } from '@/api/music'
import { usePlayerStore } from '@/store/player'

const route = useRoute()
const playerStore = usePlayerStore()

const keyword = ref('')
const loading = ref(false)
const results = ref([])

function playSong(song) {
  playerStore.playSong(song)
}

function formatTime(seconds) {
  if (!seconds) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

async function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res = await searchSongsApi({ keyword: keyword.value, page: 1, size: 20 })
    results.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = val || ''
    if (keyword.value) doSearch()
  }
)

onMounted(() => {
  keyword.value = route.query.keyword || ''
  if (keyword.value) doSearch()
})
</script>

<style scoped lang="scss">
.search-page {
  max-width: 1000px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 24px;

  .search-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 22px;
    font-weight: 600;
    color: var(--st-ink);

    .search-keyword {
      font-size: 16px;
      font-weight: 400;
      color: var(--st-primary);
    }
  }
}

.search-loading,
.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--st-ink-mute);
  font-size: 14px;

  .loading-icon,
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    color: var(--st-primary-subdued);
  }

  .loading-icon.is-loading {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-results {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  overflow: hidden;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background 150ms ease;

  &:hover {
    background: var(--st-canvas-hover);
  }

  .result-num {
    width: 28px;
    font-size: 14px;
    color: var(--st-ink-mute);
    text-align: center;
    font-feature-settings: 'tnum';
  }

  .result-cover {
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

  .result-info {
    flex: 1;
    display: flex;
    flex-direction: column;

    .result-name {
      font-size: 14px;
      color: var(--st-ink);
    }

    .result-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
    }
  }

  .result-time {
    font-size: 13px;
    color: var(--st-ink-mute);
    font-feature-settings: 'tnum';
  }
}
</style>
