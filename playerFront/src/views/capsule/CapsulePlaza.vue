<template>
  <div class="capsule-plaza">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Clock /></el-icon>
        时空胶囊广场
      </h2>
      <p class="page-desc">在这里遇见来自时空彼端的音乐与心意，每一颗公开的胶囊都是一段被铭记的记忆</p>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 9" :key="n">
        <div class="skeleton-cover"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
        <div class="skeleton-line shorter"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="capsules.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Clock /></el-icon>
      <p>广场上还没有公开的胶囊</p>
    </div>

    <!-- 胶囊网格 -->
    <div v-else class="card-grid">
      <div
        v-for="(item, idx) in capsules"
        :key="item.id"
        class="capsule-card"
        :style="{ animationDelay: `${Math.min(idx, 10) * 50}ms` }"
        @click="goDetail(item.id)"
      >
        <div class="card-cover">
          <img
            v-if="item.imageUrl && !imgErrors[item.id]"
            :src="item.imageUrl"
            :alt="item.musicName"
            loading="lazy"
            @error="handleImgError(item.id)"
          />
          <el-icon v-else class="cover-fallback"><Headset /></el-icon>
        </div>
        <div class="card-body">
          <div class="card-title" :title="item.musicName">{{ item.musicName }}</div>
          <div class="card-singer" :title="item.singerName">{{ item.singerName }}</div>
          <div class="card-sender">
            <el-icon><User /></el-icon>
            <span>{{ item.senderName || '匿名使者' }}</span>
          </div>
          <div v-if="item.message" class="card-message">{{ item.message }}</div>
          <div class="card-footer">
            <button
              class="like-btn"
              :class="{ liked: item.liked, bouncing: likeBouncing[item.id] }"
              @click.stop="toggleLike(item)"
            >
              <el-icon><StarFilled v-if="item.liked" /><Star v-else /></el-icon>
              <span>{{ item.likeCount }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 自定义 Toast：滑入 350ms，停留 3 秒 -->
    <Transition name="toast-slide">
      <div v-if="toast.visible" class="custom-toast" :class="toast.type">
        <el-icon><Check v-if="toast.type === 'success'" /><Warning v-else /></el-icon>
        <span>{{ toast.message }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Clock,
  User,
  Headset,
  Star,
  StarFilled,
  Check,
  Warning
} from '@element-plus/icons-vue'
import { getPlazaListApi, toggleCapsuleLikeApi } from '@/api/capsule'

const router = useRouter()

// 加载状态与胶囊列表
const loading = ref(true)
const capsules = ref([])
// 封面加载失败的胶囊 id → 显示占位图标
const imgErrors = reactive({})
// 点赞弹跳动画中的胶囊 id
const likeBouncing = reactive({})

// 自定义 Toast 状态
const toast = reactive({ visible: false, message: '', type: 'success' })
let toastTimer = null

function showToast(message, type = 'success') {
  toast.message = message
  toast.type = type
  toast.visible = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.visible = false
  }, 3000)
}

// 跳转到胶囊详情
function goDetail(id) {
  router.push(`/capsule/${id}`)
}

// 封面加载失败：标记该胶囊，显示占位图标
function handleImgError(id) {
  imgErrors[id] = true
}

// 加载广场列表
async function loadData() {
  loading.value = true
  try {
    const res = await getPlazaListApi()
    capsules.value = res.data || []
  } catch (e) {
    console.error('加载广场列表失败:', e)
    showToast('加载失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 切换点赞
async function toggleLike(item) {
  try {
    const res = await toggleCapsuleLikeApi(item.id)
    // res.data 为布尔值表示点赞后状态；无明确返回则按当前态取反
    const liked = typeof res.data === 'boolean' ? res.data : !item.liked
    item.liked = liked
    item.likeCount = liked ? item.likeCount + 1 : item.likeCount - 1
    if (liked) {
      likeBouncing[item.id] = true
      setTimeout(() => {
        likeBouncing[item.id] = false
      }, 400)
      showToast('点赞成功')
    } else {
      showToast('已取消点赞')
    }
  } catch (e) {
    console.error('点赞失败:', e)
    showToast('操作失败，请重试', 'error')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.capsule-plaza {
  max-width: 1200px;
  margin: 0 auto;
  animation: pageFadeIn 250ms ease-out;
}

@keyframes pageFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* === 页面头部 === */
.page-header {
  margin-bottom: 28px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 26px;
    font-weight: 600;
    color: var(--st-ink);

    .el-icon {
      color: var(--st-primary);
    }
  }

  .page-desc {
    font-size: 14px;
    font-weight: 300;
    color: var(--st-ink-mute);
    margin-top: 6px;
  }
}

/* === 骨架屏 === */
.skeleton-grid,
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;

  @media (max-width: 900px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  background: var(--st-canvas);
  border-radius: var(--rounded-lg);
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .skeleton-cover {
    width: 100%;
    aspect-ratio: 16 / 9;
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

    &.shorter {
      width: 40%;
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
    font-size: 56px;
    color: var(--st-primary-subdued);
    margin-bottom: 16px;
  }
}

/* === 胶囊卡片 === */
.capsule-card {
  background: var(--st-canvas);
  border-radius: var(--rounded-lg);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 250ms ease, box-shadow 250ms ease;
  animation: cardEntrance 300ms ease-out both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .card-cover {
    width: 100%;
    aspect-ratio: 16 / 9;
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
      font-size: 48px;
      color: var(--st-ink-mute);
    }
  }

  .card-body {
    padding: 14px 16px 16px;
  }

  .card-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .card-singer {
    font-size: 13px;
    font-weight: 300;
    color: var(--st-ink-mute);
    margin-top: 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .card-sender {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 8px;
    font-size: 12px;
    color: var(--st-ink-mute);

    .el-icon {
      font-size: 12px;
      flex-shrink: 0;
    }
  }

  .card-message {
    font-size: 13px;
    font-weight: 300;
    color: var(--st-ink-secondary);
    margin-top: 8px;
    line-height: 1.5;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .card-footer {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }
}

@keyframes cardEntrance {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* === 点赞按钮 === */
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  color: var(--st-ink-mute);
  font-size: 13px;
  font-weight: 300;
  cursor: pointer;
  transition: color 200ms ease, border-color 200ms ease, background 200ms ease;

  &:hover {
    color: var(--st-primary);
    border-color: var(--st-primary);
  }

  &.liked {
    color: var(--st-primary);
    border-color: var(--st-primary);
    background: rgba(94, 92, 230, 0.08);
  }

  &.bouncing .el-icon {
    animation: likeBounce 400ms ease;
  }
}

@keyframes likeBounce {
  0% { transform: scale(0.6); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* === 自定义 Toast === */
.custom-toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--rounded-pill);
  font-size: 14px;
  font-weight: 300;
  color: #fff;
  background: var(--st-ink);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);

  &.error {
    background: #d93025;
  }
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: opacity 350ms ease-out, transform 350ms ease-out;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -16px);
}
</style>
