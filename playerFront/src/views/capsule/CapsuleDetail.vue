<template>
  <div class="capsule-detail">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回广场</span>
    </button>

    <!-- 骨架屏 -->
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton-left">
        <div class="skeleton-cover"></div>
        <div class="skeleton-line lg"></div>
        <div class="skeleton-line short"></div>
        <div class="skeleton-btn"></div>
      </div>
      <div class="skeleton-right">
        <div class="skeleton-block"></div>
      </div>
    </div>

    <!-- 详情内容 -->
    <div v-else-if="detail" class="detail-card">
      <!-- 上部：左封面 + 右状态/留言 -->
      <div class="detail-top">
        <!-- 左侧 -->
        <div class="detail-left">
          <div class="detail-cover">
            <img
              v-if="detail.imageUrl && !coverError"
              :src="detail.imageUrl"
              :alt="detail.musicName"
              loading="lazy"
              @error="coverError = true"
            />
            <el-icon v-else class="cover-fallback"><Headset /></el-icon>
          </div>
          <div class="song-meta">
            <h1 class="song-name" :title="detail.musicName">{{ detail.musicName }}</h1>
            <div class="song-singer" :title="detail.singerName">{{ detail.singerName }}</div>
          </div>
          <button class="play-btn" @click="playSong">
            <el-icon><VideoPlay /></el-icon>
            <span>播放歌曲</span>
          </button>
        </div>

        <!-- 右侧 -->
        <div class="detail-right">
          <!-- 封印状态 -->
          <div v-if="detail.status === 0" class="sealed-state">
            <div class="sealed-head">
              <el-icon class="lock-icon"><Lock /></el-icon>
              <div class="sealed-text">
                <div class="sealed-label">距离解锁还有</div>
                <div class="countdown">{{ detail.countdown || '—' }}</div>
              </div>
            </div>
            <div class="message-box sealed">
              <div class="blurred-placeholder">
                这是一段被封印的留言，待时光流转、约定之时，方能揭晓其中承载的心意与旋律。
              </div>
              <div class="frosted-mask">
                <el-icon><Lock /></el-icon>
                <span>封印中</span>
              </div>
            </div>
          </div>

          <!-- 已解锁 / 已公开状态 -->
          <div v-else class="unlocked-state">
            <div class="status-badge">
              <el-icon><Unlock /></el-icon>
              <span>{{ detail.status === 2 ? '已公开' : '已解锁' }}</span>
            </div>
            <div class="message-box">
              <div class="message-label">来自时空彼端的留言</div>
              <p class="message-content">{{ detail.message || '（无留言内容）' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部：发送者 + 时间 + 点赞 -->
      <div class="detail-footer">
        <div class="footer-meta">
          <div class="meta-item">
            <el-icon><User /></el-icon>
            <span class="meta-label">发送者</span>
            <span class="meta-value">{{ detail.senderName || '匿名使者' }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span class="meta-label">创建时间</span>
            <span class="meta-value">{{ formatTime(detail.createTime) }}</span>
          </div>
          <div v-if="detail.unlockTime" class="meta-item">
            <el-icon><Unlock /></el-icon>
            <span class="meta-label">解锁时间</span>
            <span class="meta-value">{{ formatTime(detail.unlockTime) }}</span>
          </div>
        </div>
        <button
          class="like-btn"
          :class="{ liked: detail.liked, bouncing: likeBouncing }"
          @click="toggleLike"
        >
          <el-icon><StarFilled v-if="detail.liked" /><Star v-else /></el-icon>
          <span>{{ detail.likeCount }}</span>
        </button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-icon class="empty-icon"><Warning /></el-icon>
      <p>未找到该胶囊</p>
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
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  User,
  Headset,
  VideoPlay,
  Lock,
  Unlock,
  Star,
  StarFilled,
  Clock,
  Check,
  Warning
} from '@element-plus/icons-vue'
import { getCapsuleDetailApi, toggleCapsuleLikeApi } from '@/api/capsule'
import { usePlayerStore } from '@/store/player'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()

// 胶囊 ID（来自路由参数）
const capsuleId = route.params.id

// 加载状态与详情
const loading = ref(true)
const detail = ref(null)
const coverError = ref(false)
// 点赞弹跳动画
const likeBouncing = ref(false)

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

// 返回广场
function goBack() {
  router.push('/capsule')
}

// 时间格式化：兼容时间戳或字符串
function formatTime(t) {
  if (!t) return '—'
  const d = typeof t === 'number' ? new Date(t) : new Date(t)
  if (isNaN(d.getTime())) return String(t)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

// 播放歌曲（复用播放器 store）
function playSong() {
  if (!detail.value || !detail.value.musicUrl) {
    showToast('暂无可用音频', 'error')
    return
  }
  playerStore.playSong({
    musicId: detail.value.musicId,
    musicName: detail.value.musicName,
    musicUrl: detail.value.musicUrl,
    imageUrl: detail.value.imageUrl,
    singerName: detail.value.singerName,
    timelength: detail.value.timelength
  })
  showToast('开始播放')
}

// 加载详情
async function loadDetail() {
  loading.value = true
  try {
    const res = await getCapsuleDetailApi(capsuleId)
    detail.value = res.data || null
  } catch (e) {
    console.error('加载胶囊详情失败:', e)
    showToast('加载失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 切换点赞
async function toggleLike() {
  if (!detail.value) return
  try {
    const res = await toggleCapsuleLikeApi(capsuleId)
    const liked = typeof res.data === 'boolean' ? res.data : !detail.value.liked
    detail.value.liked = liked
    detail.value.likeCount = liked
      ? detail.value.likeCount + 1
      : detail.value.likeCount - 1
    if (liked) {
      likeBouncing.value = true
      setTimeout(() => {
        likeBouncing.value = false
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
  loadDetail()
})
</script>

<style scoped lang="scss">
.capsule-detail {
  max-width: 1000px;
  margin: 0 auto;
  animation: pageFadeIn 250ms ease-out;
}

@keyframes pageFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* === 返回按钮 === */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  margin-bottom: 20px;
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
    background: rgba(94, 92, 230, 0.06);
  }
}

/* === 骨架屏 === */
.detail-skeleton {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  padding: 24px;
  background: var(--st-canvas);
  border-radius: var(--rounded-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }

  .skeleton-cover {
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }

  .skeleton-line {
    height: 16px;
    margin-top: 12px;
    border-radius: var(--rounded-sm);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;

    &.lg {
      height: 26px;
      width: 70%;
    }

    &.short {
      width: 40%;
      height: 14px;
    }
  }

  .skeleton-btn {
    height: 40px;
    width: 140px;
    margin-top: 20px;
    border-radius: var(--rounded-pill);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }

  .skeleton-block {
    height: 100%;
    min-height: 200px;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
  }
}

@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === 详情卡片 === */
.detail-card {
  background: var(--st-canvas);
  border-radius: var(--rounded-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.detail-top {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 32px;
  padding: 32px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 24px;
  }
}

/* === 左侧 === */
.detail-left {
  display: flex;
  flex-direction: column;

  .detail-cover {
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-lg);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-fallback {
      font-size: 64px;
      color: var(--st-ink-mute);
    }
  }

  .song-meta {
    margin-top: 16px;

    .song-name {
      font-size: 22px;
      font-weight: 600;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .song-singer {
      font-size: 14px;
      font-weight: 300;
      color: var(--st-ink-mute);
      margin-top: 4px;
    }
  }
}

.play-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  padding: 10px 24px;
  border: none;
  border-radius: var(--rounded-pill);
  background: var(--st-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 200ms ease, box-shadow 200ms ease;

  &:hover {
    background: var(--st-primary-hover);
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.3);
  }
}

/* === 右侧 === */
.detail-right {
  display: flex;
  flex-direction: column;
}

/* === 封印状态 === */
.sealed-state {
  .sealed-head {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    border-radius: var(--rounded-lg);
    background: linear-gradient(135deg, rgba(94, 92, 230, 0.06), rgba(94, 92, 230, 0.02));
    border: 1px solid rgba(94, 92, 230, 0.15);

    .lock-icon {
      font-size: 40px;
      color: var(--st-primary);
      flex-shrink: 0;
    }

    .sealed-text {
      .sealed-label {
        font-size: 13px;
        font-weight: 300;
        color: var(--st-ink-mute);
      }

      .countdown {
        font-size: 20px;
        font-weight: 500;
        color: var(--st-primary);
        margin-top: 4px;
      }
    }
  }
}

/* === 留言框 === */
.message-box {
  margin-top: 20px;
  border-radius: var(--rounded-lg);
  position: relative;
  overflow: hidden;

  &.sealed {
    .blurred-placeholder {
      padding: 24px;
      font-size: 14px;
      font-weight: 300;
      line-height: 1.8;
      color: var(--st-ink-secondary);
      user-select: none;
      background: var(--st-canvas-soft);
    }

    .frosted-mask {
      position: absolute;
      inset: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: rgba(246, 249, 252, 0.6);
      backdrop-filter: blur(8px);
      -webkit-backdrop-filter: blur(8px);

      .el-icon {
        font-size: 28px;
        color: var(--st-primary);
      }

      span {
        font-size: 13px;
        font-weight: 300;
        color: var(--st-ink-mute);
        letter-spacing: 2px;
      }
    }
  }
}

.message-label {
  font-size: 12px;
  font-weight: 300;
  color: var(--st-ink-mute);
  margin-bottom: 8px;
}

.message-content {
  padding: 20px;
  font-size: 15px;
  font-weight: 300;
  line-height: 1.8;
  color: var(--st-ink-secondary);
  background: var(--st-canvas-soft);
  border-radius: var(--rounded-lg);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: var(--rounded-pill);
  background: rgba(94, 92, 230, 0.1);
  color: var(--st-primary);
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 16px;
}

/* === 底部 === */
.detail-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 32px;
  border-top: 1px solid var(--st-hairline);
  background: var(--st-canvas-soft);
  flex-wrap: wrap;

  .footer-meta {
    display: flex;
    gap: 24px;
    flex-wrap: wrap;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 300;
    color: var(--st-ink-mute);

    .el-icon {
      font-size: 14px;
    }

    .meta-value {
      color: var(--st-ink-secondary);
    }
  }
}

/* === 点赞按钮 === */
.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  color: var(--st-ink-mute);
  font-size: 14px;
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
