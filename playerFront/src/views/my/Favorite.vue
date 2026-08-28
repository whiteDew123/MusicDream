<template>
  <div class="favorite-page">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Files /></el-icon>
        <h2>收藏的歌单</h2>
      </div>
    </header>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 10" :key="n">
        <div class="skeleton-cover"></div>
        <div class="skeleton-line short"></div>
        <div class="skeleton-line long"></div>
      </div>
    </div>

    <!-- 内容区 -->
    <template v-else>
      <!-- 空状态 -->
      <div v-if="!lists.length" class="empty-state">
        <el-icon class="empty-icon"><Files /></el-icon>
        <p class="empty-text">还没有收藏的歌单</p>
        <p class="empty-sub">去发现页或歌单广场找到喜欢的歌单，点击收藏即可加入这里</p>
        <router-link to="/songlist" class="empty-btn">去逛歌单广场</router-link>
      </div>

      <!-- 歌单卡片网格 -->
      <div v-else class="list-grid">
        <div
          v-for="list in lists"
          :key="list.listId"
          class="list-card"
          @click="openList(list)"
        >
          <div class="card-cover">
            <img
              v-if="list.listPic && !errorCovers.has(list.listId)"
              :src="list.listPic"
              :alt="list.listName"
              @error="handleCoverError(list.listId)"
            />
            <el-icon v-else class="cover-fallback"><Files /></el-icon>
            <button
              class="unlike-btn"
              title="取消收藏"
              @click.stop="removeFavorite(list)"
            >
              <el-icon><StarFilled /></el-icon>
            </button>
          </div>
          <div class="card-info">
            <div class="card-name" :title="list.listName">{{ list.listName }}</div>
            <div class="card-creator" :title="list.username">by {{ list.username || '未知' }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Files, StarFilled } from '@element-plus/icons-vue'
import { likedSongListApi, removeLikedSongListApi } from '@/api/like'

const router = useRouter()

const loading = ref(true)
const lists = ref([])
// 封面加载失败的 listId 集合
const errorCovers = reactive(new Set())

// 加载收藏歌单
// 注：响应拦截器在 code !== 200 时已自动提示并 reject，故此处成功即 code 200
async function loadData() {
  loading.value = true
  try {
    const res = await likedSongListApi()
    lists.value = res.data || []
  } catch (e) {
    console.error('加载收藏歌单失败:', e)
    lists.value = []
  } finally {
    loading.value = false
  }
}

// 打开歌单详情
function openList(list) {
  router.push(`/songlist/${list.listId}`)
}

// 取消收藏
async function removeFavorite(list) {
  try {
    await removeLikedSongListApi(list.listId)
    lists.value = lists.value.filter((l) => l.listId !== list.listId)
    errorCovers.delete(list.listId)
    ElMessage.success('已取消收藏')
  } catch (e) {
    // 拦截器已统一提示错误，此处仅记录日志
    console.error('取消收藏失败:', e)
  }
}

// 封面加载失败
function handleCoverError(listId) {
  errorCovers.add(listId)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.favorite-page {
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
    background: var(--st-skeleton);
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
  .skeleton-line {
    height: 14px;
    border-radius: var(--rounded-sm);
    background: var(--st-skeleton);
    margin-top: 10px;
    animation: skeletonPulse 1.2s infinite ease-in-out;
    &.short {
      width: 60%;
    }
    &.long {
      width: 40%;
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

/* === 歌单卡片网格 === */
.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.list-card {
  cursor: pointer;

  .card-cover {
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
      font-size: 36px;
      color: var(--st-ink-mute);
    }

    .unlike-btn {
      position: absolute;
      top: 8px;
      right: 8px;
      width: 30px;
      height: 30px;
      border: none;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.45);
      color: #fff;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      opacity: 0;
      transition: all 200ms ease;

      &:hover {
        background: var(--st-primary);
      }
    }
  }

  &:hover .card-cover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  &:hover .unlike-btn {
    opacity: 1;
  }

  .card-info {
    margin-top: 10px;

    .card-name {
      font-size: 14px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.4;
    }

    .card-creator {
      font-size: 12px;
      color: var(--st-ink-mute);
      margin-top: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
