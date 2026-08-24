<template>
  <div class="songlist-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Files /></el-icon>
        歌单广场
      </h2>
      <p class="page-desc">精选公开歌单，发现属于你的音乐世界</p>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 12" :key="n">
        <div class="skeleton-cover"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="songLists.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Files /></el-icon>
      <p>暂无公开歌单</p>
    </div>

    <!-- 歌单网格 -->
    <div v-else class="list-grid">
      <div
        v-for="list in songLists"
        :key="list.id"
        class="list-card"
        @click="goDetail(list.id)"
      >
        <div class="list-cover">
          <img
            v-if="list.pic && !imgErrors[list.id]"
            :src="list.pic"
            :alt="list.name"
            @error="handleImgError(list.id)"
          />
          <el-icon v-else class="cover-fallback"><Files /></el-icon>
          <span v-if="list.style" class="style-tag">{{ list.style }}</span>
        </div>
        <div class="list-info">
          <div class="list-name" :title="list.name">{{ list.name }}</div>
          <div class="list-creator" :title="list.username">
            <el-icon><User /></el-icon>
            <span>{{ list.username || '未知创建者' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Files, User } from '@element-plus/icons-vue'
import { publicSongListApi } from '@/api/songList'

const router = useRouter()

// 加载状态与歌单列表
const loading = ref(true)
const songLists = ref([])
// 记录封面加载失败的歌单 id，用于切换到占位图标
const imgErrors = reactive({})

// 跳转到歌单详情页
function goDetail(id) {
  router.push(`/songlist/${id}`)
}

// 封面加载失败：标记该歌单，显示 Files 占位图标
function handleImgError(id) {
  imgErrors[id] = true
}

// 加载公开歌单列表
async function loadData() {
  loading.value = true
  try {
    const res = await publicSongListApi()
    songLists.value = res.data || []
  } catch (e) {
    console.error('加载歌单列表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.songlist-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* === 页面头部 === */
.page-header {
  margin-bottom: 24px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    color: var(--st-ink);

    .el-icon {
      color: var(--st-primary);
    }
  }

  .page-desc {
    font-size: 14px;
    color: var(--st-ink-mute);
    margin-top: 4px;
  }
}

/* === 骨架屏 === */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.skeleton-card {
  .skeleton-cover {
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }

  .skeleton-line {
    height: 14px;
    margin-top: 10px;
    border-radius: var(--rounded-sm);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;

    &.short {
      width: 60%;
      height: 12px;
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
  padding: 80px 0;
  color: var(--st-ink-mute);

  .empty-icon {
    font-size: 48px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }
}

/* === 歌单网格 === */
.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.list-card {
  cursor: pointer;
  transition: transform 200ms ease;

  &:hover {
    transform: translateY(-4px);
  }

  .list-cover {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 1px 3px rgba(0, 55, 112, 0.08);
    transition: box-shadow 250ms ease;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-fallback {
      font-size: 36px;
      color: var(--st-ink-mute);
    }

    .style-tag {
      position: absolute;
      top: 8px;
      left: 8px;
      padding: 2px 8px;
      border-radius: var(--rounded-pill);
      background: rgba(94, 92, 230, 0.85);
      color: #fff;
      font-size: 12px;
      line-height: 1.4;
      backdrop-filter: blur(4px);
    }
  }

  &:hover .list-cover {
    box-shadow: 0 8px 24px rgba(0, 55, 112, 0.12);
  }

  .list-info {
    margin-top: 10px;

    .list-name {
      font-size: 14px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.4;
    }

    .list-creator {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 6px;
      font-size: 12px;
      color: var(--st-ink-mute);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .el-icon {
        font-size: 12px;
        flex-shrink: 0;
      }
    }
  }
}
</style>
