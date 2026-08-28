<template>
  <div class="my-box">
    <div class="my-box-header">
      <h2 class="my-box-title">🎁 我的盲盒</h2>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" @tab-change="loadBoxes" class="my-box-tabs">
      <el-tab-pane label="我创建的" name="created" />
      <el-tab-pane label="我开启的" name="opened" />
      <el-tab-pane label="我点赞的" name="liked" />
    </el-tabs>

    <!-- 盲盒列表 -->
    <div v-loading="loading" class="box-grid">
      <div
        v-for="box in boxList"
        :key="box.id"
        class="box-card"
        @click="goToDetail(box.id)"
      >
        <!-- 封面 -->
        <div class="box-cover">
          <img v-if="box.coverUrl" :src="box.coverUrl" alt="cover" />
          <div v-else class="cover-placeholder">
            <el-icon><Headset /></el-icon>
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
            <span class="stat-item">
              <el-icon><StarFilled /></el-icon>
              {{ box.likeCount }}
            </span>
          </div>
        </div>

        <!-- 操作按钮（仅我创建的显示） -->
        <div v-if="activeTab === 'created'" class="box-actions" @click.stop>
          <el-button size="small" type="danger" @click="handleDelete(box)">
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && boxList.length === 0"
      :description="emptyText"
    >
      <el-button v-if="activeTab === 'created'" type="primary" @click="router.push('/musicbox/create')">
        创建盲盒
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Headset,
  View,
  StarFilled
} from '@element-plus/icons-vue'
import {
  getMyBoxesApi,
  getOpenedBoxesApi,
  getLikedBoxesApi,
  deleteBoxApi
} from '@/api/musicbox'

const router = useRouter()
const loading = ref(false)
const boxList = ref([])
const activeTab = ref('created')

const emptyText = computed(() => {
  switch (activeTab.value) {
    case 'created': return '还没有创建盲盒，快去创建吧'
    case 'opened': return '还没有开启过盲盒'
    case 'liked': return '还没有点赞过盲盒'
    default: return '暂无数据'
  }
})

// 加载盲盒列表
async function loadBoxes() {
  loading.value = true
  try {
    let res
    switch (activeTab.value) {
      case 'created':
        res = await getMyBoxesApi()
        break
      case 'opened':
        res = await getOpenedBoxesApi()
        break
      case 'liked':
        res = await getLikedBoxesApi()
        break
    }
    boxList.value = res.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 删除盲盒
function handleDelete(box) {
  ElMessageBox.confirm(
    `确定要删除盲盒"${box.title}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBoxApi(box.id)
      ElMessage.success('已删除')
      loadBoxes()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
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
.my-box {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.my-box-header {
  margin-bottom: 24px;

  .my-box-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--st-ink);
    margin: 0;
  }
}

.my-box-tabs {
  margin-bottom: 24px;
}

.box-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.box-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 200ms ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(94, 92, 230, 0.15);

    .box-actions {
      opacity: 1;
    }
  }
}

.box-cover {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
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
}

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

      .el-icon {
        font-size: 14px;
      }
    }
  }
}

.box-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 150ms ease;
}
</style>