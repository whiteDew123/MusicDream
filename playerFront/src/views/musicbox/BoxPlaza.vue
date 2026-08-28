<template>
  <div class="box-plaza">
    <!-- 顶部标题 -->
    <div class="plaza-header">
      <h2 class="plaza-title">🎁 盲盒广场</h2>
      <p class="plaza-desc">探索未知音乐，遇见品味相同的TA</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-button
        :type="activeFilter === 'latest' ? 'primary' : ''"
        @click="activeFilter = 'latest'; loadBoxes()"
      >
        最新
      </el-button>
      <el-button
        :type="activeFilter === 'hot' ? 'primary' : ''"
        @click="activeFilter = 'hot'; loadBoxes()"
      >
        🔥 热门
      </el-button>
      <el-button
        :type="activeFilter === 'random' ? 'primary' : ''"
        @click="activeFilter = 'random'; loadBoxes()"
      >
        🎲 随机
      </el-button>

      <el-divider direction="vertical" />

      <!-- 心情标签筛选 -->
      <el-select
        v-model="activeTag"
        placeholder="心情标签"
        clearable
        style="width: 140px"
        @change="loadBoxes"
      >
        <el-option label="🌙 深夜" value="深夜" />
        <el-option label="☕ 治愈" value="治愈" />
        <el-option label="🎸 摇滚" value="摇滚" />
        <el-option label="💔 失恋" value="失恋" />
        <el-option label="🌈 开心" value="开心" />
        <el-option label="🌧️ 雨天" value="雨天" />
        <el-option label="📚 学习" value="学习" />
        <el-option label="🏃 运动" value="运动" />
      </el-select>

      <div class="create-btn">
        <el-button type="primary" @click="router.push('/musicbox/create')">
          <el-icon><Plus /></el-icon>
          创建盲盒
        </el-button>
      </div>
    </div>

    <!-- 盲盒卡片列表 -->
    <div v-loading="loading" class="box-grid">
      <div
        v-for="box in boxList"
        :key="box.id"
        class="box-card"
        @click="goToDetail(box.id)"
      >
        <!-- 模糊封面 -->
        <div class="box-cover">
          <img v-if="box.coverUrl" :src="box.coverUrl" alt="cover" />
          <div v-else class="cover-placeholder">
            <el-icon><Headset /></el-icon>
          </div>
          <div class="cover-blur"></div>
          <div class="cover-overlay">
            <span class="open-hint">点击开启</span>
          </div>
        </div>

        <!-- 卡片信息 -->
        <div class="box-info">
          <h3 class="box-title">{{ box.title }}</h3>
          <div class="box-tags">
            <el-tag size="small" type="info">{{ box.moodTag }}</el-tag>
            <span class="song-count">{{ box.songCount }}首歌</span>
          </div>
          <div class="box-stats">
            <span class="stat-item">
              <el-icon><View /></el-icon>
              {{ box.openCount }}
            </span>
            <span class="stat-item" @click.stop="handleLike(box)">
              <el-icon :class="{ liked: box.isLiked }">
                <component :is="box.isLiked ? 'StarFilled' : 'Star'" />
              </el-icon>
              {{ box.likeCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && boxList.length === 0"
      description="暂无盲盒，快来创建第一个吧"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Headset,
  View,
  Star,
  StarFilled
} from '@element-plus/icons-vue'
import {
  getPlazaListApi,
  getPlazaListByTagApi,
  getHotBoxesApi,
  getRandomBoxesApi,
  toggleLikeApi
} from '@/api/musicbox'

const router = useRouter()
const loading = ref(false)
const boxList = ref([])
const activeFilter = ref('latest')
const activeTag = ref('')

// 加载盲盒列表
async function loadBoxes() {
  loading.value = true
  try {
    let res
    if (activeFilter.value === 'hot') {
      res = await getHotBoxesApi({ limit: 30 })
    } else if (activeFilter.value === 'random') {
      res = await getRandomBoxesApi({ limit: 20 })
    } else if (activeTag.value) {
      res = await getPlazaListByTagApi({ tag: activeTag.value, page: 1, size: 30 })
    } else {
      res = await getPlazaListApi({ page: 1, size: 30 })
    }
    boxList.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
async function handleLike(box) {
  try {
    await toggleLikeApi(box.id)
    box.isLiked = !box.isLiked
    box.likeCount += box.isLiked ? 1 : -1
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 跳转详情
function goToDetail(boxId) {
  router.push(`/Musicbox/${boxId}`)
}

onMounted(() => {
  loadBoxes()
})
</script>

<style scoped lang="scss">
.box-plaza {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部标题 */
.plaza-header {
  text-align: center;
  margin-bottom: 32px;

  .plaza-title {
    font-size: 32px;
    font-weight: 700;
    color: var(--st-ink);
    margin: 0 0 8px;
  }

  .plaza-desc {
    font-size: 14px;
    color: var(--st-ink-secondary);
    margin: 0;
  }
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .create-btn {
    margin-left: auto;
  }
}

/* 盲盒卡片网格 */
.box-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

/* 盲盒卡片 */
.box-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 200ms ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(94, 92, 230, 0.15);
  }
}

/* 模糊封面 */
.box-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    filter: blur(8px) brightness(0.8);
    transition: filter 200ms ease;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      font-size: 48px;
      color: rgba(255, 255, 255, 0.6);
    }
  }

  .cover-blur {
    position: absolute;
    inset: 0;
    backdrop-filter: blur(12px);
    background: rgba(0, 0, 0, 0.1);
  }

  .cover-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.2);
    opacity: 0;
    transition: opacity 200ms ease;

    .open-hint {
      padding: 8px 20px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 20px;
      font-size: 14px;
      font-weight: 500;
      color: var(--st-primary);
    }
  }

  &:hover .cover-overlay {
    opacity: 1;
  }
}

/* 卡片信息 */
.box-info {
  padding: 12px;

  .box-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--st-ink);
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .box-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .song-count {
      font-size: 12px;
      color: var(--st-ink-secondary);
    }
  }

  .box-stats {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--st-ink-secondary);
      cursor: pointer;
      transition: color 150ms ease;

      .el-icon {
        font-size: 14px;
      }

      &.liked,
      &:hover {
        color: #f56c6c;

        .el-icon {
          color: #f56c6c;
        }
      }
    }
  }
}
</style>