<template>
  <div class="created-page">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Files /></el-icon>
        <h2>我创建的歌单</h2>
      </div>
    </header>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 6" :key="n">
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
        <p class="empty-text">还没有创建歌单</p>
        <p class="empty-sub">去歌单广场创建你的第一个歌单，收纳喜欢的歌曲</p>
        <router-link to="/songlist" class="empty-btn">去创建歌单</router-link>
      </div>

      <!-- 歌单卡片网格 -->
      <div v-else class="list-grid">
        <div
          v-for="list in lists"
          :key="list.id"
          class="list-card"
          @click="openList(list)"
        >
          <div class="card-cover">
            <img
              v-if="list.pic && !errorCovers.has(list.id)"
              :src="list.pic"
              :alt="list.name"
              @error="handleCoverError(list.id)"
            />
            <el-icon v-else class="cover-fallback"><Files /></el-icon>
            <button
              class="delete-btn"
              title="删除歌单"
              @click.stop="confirmDelete(list)"
            >
              <el-icon><Delete /></el-icon>
            </button>
          </div>
          <div class="card-info">
            <div class="card-name" :title="list.name">{{ list.name }}</div>
            <div class="card-meta">
              <span v-if="list.style" class="meta-style" :title="list.style">{{ list.style }}</span>
              <span class="meta-author">{{ list.username || '我' }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Files, Delete } from '@element-plus/icons-vue'
import { myCreatedSongListApi, deleteSongListApi } from '@/api/songList'

const router = useRouter()

const loading = ref(true)
const lists = ref([])
// 封面加载失败的歌单 id 集合
const errorCovers = reactive(new Set())

// 加载我创建的歌单
// 注：响应拦截器在 code !== 200 时已自动提示并 reject，故此处成功即 code 200
async function loadData() {
  loading.value = true
  try {
    const res = await myCreatedSongListApi()
    lists.value = res.data || []
  } catch (e) {
    console.error('加载我创建的歌单失败:', e)
    lists.value = []
  } finally {
    loading.value = false
  }
}

// 打开歌单详情
function openList(list) {
  router.push(`/songlist/${list.id}`)
}

// 删除确认
function confirmDelete(list) {
  ElMessageBox.confirm(
    `确定要删除歌单「${list.name}」吗？删除后不可恢复。`,
    '删除歌单',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => doDelete(list))
    .catch(() => {})
}

// 执行删除
async function doDelete(list) {
  try {
    await deleteSongListApi(list.id)
    lists.value = lists.value.filter((l) => l.id !== list.id)
    errorCovers.delete(list.id)
    ElMessage.success('歌单已删除')
  } catch (e) {
    // 拦截器已统一提示错误，此处仅记录日志
    console.error('删除歌单失败:', e)
  }
}

// 封面加载失败
function handleCoverError(id) {
  errorCovers.add(id)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.created-page {
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

    .delete-btn {
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
        background: #f56c6c;
      }
    }
  }

  &:hover .card-cover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  &:hover .delete-btn {
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

    .card-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 4px;
      font-size: 12px;
      color: var(--st-ink-mute);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .meta-style {
        max-width: 90px;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .meta-author {
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
