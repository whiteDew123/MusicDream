<template>
  <div class="my-capsules-page">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><MagicStick /></el-icon>
        <h2>我的胶囊</h2>
      </div>
      <button class="create-btn" @click="goCreate">
        <el-icon><Plus /></el-icon>
        <span>创建胶囊</span>
      </button>
    </header>

    <!-- 双栏 Tab -->
    <el-tabs v-model="activeTab" class="capsule-tabs" @tab-change="onTabChange">
      <el-tab-pane label="我创建的" name="created">
        <!-- 骨架屏 -->
        <div v-if="loading" class="capsule-grid">
          <div v-for="n in 4" :key="n" class="skeleton-card">
            <div class="skeleton-cover"></div>
            <div class="skeleton-body">
              <div class="skeleton-line short"></div>
              <div class="skeleton-line long"></div>
              <div class="skeleton-line mid"></div>
            </div>
          </div>
        </div>

        <template v-else>
          <!-- 空状态 -->
          <div v-if="!createdList.length" class="empty-state">
            <el-icon class="empty-icon"><MagicStick /></el-icon>
            <p class="empty-text">还没有创建过胶囊</p>
            <p class="empty-sub">封存一首歌、一句话，留给未来的自己</p>
            <button class="empty-btn" @click="goCreate">去创建胶囊</button>
          </div>

          <!-- 卡片列表 -->
          <div v-else class="capsule-grid">
            <div
              v-for="(cap, idx) in createdList"
              :key="cap.id"
              class="capsule-card"
              :style="{ animationDelay: idx * 50 + 'ms' }"
            >
              <div class="cap-card-inner">
                <!-- 左侧封面 -->
                <div class="cap-cover">
                  <img
                    v-if="cap.imageUrl"
                    :src="cap.imageUrl"
                    :alt="cap.musicName"
                    loading="lazy"
                  />
                  <el-icon v-else class="cover-fallback"><Star /></el-icon>
                  <div v-if="isSealed(cap)" class="cover-lock">
                    <el-icon><Lock /></el-icon>
                  </div>
                </div>
                <!-- 右侧信息 -->
                <div class="cap-info">
                  <div class="cap-top">
                    <div class="cap-title-row">
                      <span class="cap-song">{{ cap.musicName }}</span>
                      <span class="cap-tag" :class="tagClass(cap.status)">
                        {{ statusText(cap.status) }}
                      </span>
                    </div>
                    <span class="cap-singer">{{ cap.singerName }}</span>
                  </div>

                  <!-- 留言 / 倒计时 -->
                  <div v-if="isSealed(cap)" class="cap-message blurred">
                    封印中的留言，等待时间解锁……
                  </div>
                  <div v-else class="cap-message">{{ cap.message || '（无留言）' }}</div>

                  <!-- 元信息行 -->
                  <div class="cap-meta">
                    <span v-if="isSealed(cap)" class="meta-countdown">
                      <el-icon class="meta-icon"><Lock /></el-icon>
                      {{ cap.countdown || '等待解锁' }}
                    </span>
                    <span v-else class="meta-time">解锁于 {{ formatTime(cap.unlockTime) }}</span>
                    <span class="meta-dur">{{ formatDuration(cap.timelength) }}</span>
                  </div>

                  <!-- 操作行 -->
                  <div class="cap-actions">
                    <button
                      v-if="!isSealed(cap)"
                      class="act-btn play"
                      title="播放"
                      @click="playMusic(cap)"
                    >
                      <el-icon><VideoPlay /></el-icon>播放
                    </button>
                    <button
                      v-if="!isSealed(cap) && !isPublic(cap) && roleSender"
                      class="act-btn public"
                      title="设为公开"
                      @click="makePublic(cap)"
                    >
                      设为公开
                    </button>
                    <button
                      class="act-btn like"
                      :class="{ liked: cap.liked, bounce: bouncingIds.has(cap.id) }"
                      :title="cap.liked ? '取消点赞' : '点赞'"
                      @click="toggleLike(cap)"
                    >
                      <el-icon class="like-icon"><Star /></el-icon>
                      <span class="like-count">{{ cap.likeCount || 0 }}</span>
                    </button>
                    <button
                      v-if="roleSender"
                      class="act-btn delete"
                      title="删除"
                      @click="confirmDelete(cap)"
                    >
                      <el-icon><Delete /></el-icon>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-tab-pane>

      <el-tab-pane label="写给我的" name="received">
        <!-- 骨架屏 -->
        <div v-if="loading" class="capsule-grid">
          <div v-for="n in 4" :key="n" class="skeleton-card">
            <div class="skeleton-cover"></div>
            <div class="skeleton-body">
              <div class="skeleton-line short"></div>
              <div class="skeleton-line long"></div>
              <div class="skeleton-line mid"></div>
            </div>
          </div>
        </div>

        <template v-else>
          <!-- 空状态 -->
          <div v-if="!receivedList.length" class="empty-state">
            <el-icon class="empty-icon"><MagicStick /></el-icon>
            <p class="empty-text">还没有人给你写胶囊</p>
            <p class="empty-sub">胶囊送达后将出现在这里</p>
          </div>

          <!-- 卡片列表 -->
          <div v-else class="capsule-grid">
            <div
              v-for="(cap, idx) in receivedList"
              :key="cap.id"
              class="capsule-card"
              :style="{ animationDelay: idx * 50 + 'ms' }"
            >
              <div class="cap-card-inner">
                <!-- 左侧封面 -->
                <div class="cap-cover">
                  <img
                    v-if="cap.imageUrl"
                    :src="cap.imageUrl"
                    :alt="cap.musicName"
                    loading="lazy"
                  />
                  <el-icon v-else class="cover-fallback"><Star /></el-icon>
                  <div v-if="isSealed(cap)" class="cover-lock">
                    <el-icon><Lock /></el-icon>
                  </div>
                </div>
                <!-- 右侧信息 -->
                <div class="cap-info">
                  <div class="cap-top">
                    <div class="cap-title-row">
                      <span class="cap-song">{{ cap.musicName }}</span>
                      <span class="cap-tag" :class="tagClass(cap.status)">
                        {{ statusText(cap.status) }}
                      </span>
                    </div>
                    <span class="cap-singer">{{ cap.singerName }}</span>
                  </div>

                  <!-- 留言 / 倒计时 -->
                  <div v-if="isSealed(cap)" class="cap-message blurred">
                    封印中的留言，等待时间解锁……
                  </div>
                  <div v-else class="cap-message">{{ cap.message || '（无留言）' }}</div>

                  <!-- 元信息行 -->
                  <div class="cap-meta">
                    <span v-if="isSealed(cap)" class="meta-countdown">
                      <el-icon class="meta-icon"><Lock /></el-icon>
                      {{ cap.countdown || '等待解锁' }}
                    </span>
                    <span v-else class="meta-time">解锁于 {{ formatTime(cap.unlockTime) }}</span>
                    <span class="meta-dur">{{ formatDuration(cap.timelength) }}</span>
                  </div>

                  <!-- 操作行 -->
                  <div class="cap-actions">
                    <button
                      v-if="!isSealed(cap)"
                      class="act-btn play"
                      title="播放"
                      @click="playMusic(cap)"
                    >
                      <el-icon><VideoPlay /></el-icon>播放
                    </button>
                    <button
                      class="act-btn like"
                      :class="{ liked: cap.liked, bounce: bouncingIds.has(cap.id) }"
                      :title="cap.liked ? '取消点赞' : '点赞'"
                      @click="toggleLike(cap)"
                    >
                      <el-icon class="like-icon"><Star /></el-icon>
                      <span class="like-count">{{ cap.likeCount || 0 }}</span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Plus, Lock, VideoPlay, Delete, Star } from '@element-plus/icons-vue'
import {
  getMyCapsulesApi,
  getReceivedCapsulesApi,
  deleteCapsuleApi,
  makeCapsulePublicApi,
  toggleCapsuleLikeApi
} from '@/api/capsule'
import { usePlayerStore } from '@/store/player'

const router = useRouter()
const playerStore = usePlayerStore()

const activeTab = ref('created')
const loading = ref(true)
const createdList = ref([])
const receivedList = ref([])
// 点赞弹跳中 id 集合
const bouncingIds = reactive(new Set())

// 当前是否为"我创建的"视角（控制删除 / 设为公开按钮可见性）
const roleSender = computed(() => activeTab.value === 'created')

// 状态判断
function isSealed(cap) {
  return cap.status === 0
}
function isPublic(cap) {
  return cap.status === 2
}

function statusText(status) {
  return { 0: '封印中', 1: '已解锁', 2: '已公开' }[status] || '未知'
}

function tagClass(status) {
  return (
    { 0: 'tag-sealed', 1: 'tag-unlocked', 2: 'tag-public' }[status] || 'tag-sealed'
  )
}

// 时间格式化
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

function formatDuration(seconds) {
  if (!seconds) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'created') {
      const res = await getMyCapsulesApi()
      createdList.value = res.data || []
    } else {
      const res = await getReceivedCapsulesApi()
      receivedList.value = res.data || []
    }
  } catch (e) {
    console.error('加载胶囊列表失败:', e)
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  loadData()
}

// 跳转创建
function goCreate() {
  router.push('/my/capsule/create')
}

// 播放音乐
function playMusic(cap) {
  playerStore.playSong({
    musicId: cap.musicId,
    musicName: cap.musicName,
    musicUrl: cap.musicUrl,
    imageUrl: cap.imageUrl,
    singerName: cap.singerName,
    timelength: cap.timelength
  })
}

// 点赞（弹跳）
async function toggleLike(cap) {
  // 触发弹跳动画
  bouncingIds.add(cap.id)
  setTimeout(() => bouncingIds.delete(cap.id), 400)
  // 乐观更新
  cap.liked = !cap.liked
  cap.likeCount = (cap.likeCount || 0) + (cap.liked ? 1 : -1)
  try {
    await toggleCapsuleLikeApi(cap.id)
  } catch (e) {
    // 回滚
    cap.liked = !cap.liked
    cap.likeCount = (cap.likeCount || 0) + (cap.liked ? 1 : -1)
  }
}

// 设为公开
async function makePublic(cap) {
  try {
    await ElMessageBox.confirm('设为公开后，胶囊将出现在时空广场，是否继续？', '设为公开', {
      confirmButtonText: '设为公开',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await makeCapsulePublicApi(cap.id)
    cap.status = 2
    cap.isPublic = true
    ElMessage.success('已设为公开')
  } catch (e) {
    // 拦截器已提示
  }
}

// 删除确认
function confirmDelete(cap) {
  ElMessageBox.confirm(
    `确定要删除胶囊「${cap.musicName}」吗？删除后不可恢复。`,
    '删除胶囊',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => doDelete(cap))
    .catch(() => {})
}

async function doDelete(cap) {
  try {
    await deleteCapsuleApi(cap.id)
    createdList.value = createdList.value.filter((c) => c.id !== cap.id)
    receivedList.value = receivedList.value.filter((c) => c.id !== cap.id)
    ElMessage.success('胶囊已删除')
  } catch (e) {
    console.error('删除胶囊失败:', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.my-capsules-page {
  max-width: 1100px;
  margin: 0 auto;
  animation: pageFadeIn 250ms ease-out;
}

@keyframes pageFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* === 页头 === */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

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

  .create-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 9px 20px;
    border: none;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: all 200ms ease;

    &:hover {
      background: var(--st-primary-hover);
      transform: translateY(-1px);
      box-shadow: 0 6px 18px rgba(94, 92, 230, 0.28);
    }
    &:active {
      transform: translateY(0);
    }
  }
}

/* === Tab === */
.capsule-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: var(--st-hairline);
  }
  :deep(.el-tabs__item) {
    font-size: 15px;
    color: var(--st-ink-mute);
    font-weight: 400;
    height: 44px;

    &.is-active {
      color: var(--st-primary);
      font-weight: 600;
    }
    &:hover {
      color: var(--st-primary);
    }
  }
  :deep(.el-tabs__active-bar) {
    background: var(--st-primary);
    height: 3px;
    border-radius: var(--rounded-pill);
  }
}

/* === 卡片网格 === */
.capsule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(440px, 1fr));
  gap: 18px;
}

.capsule-card {
  animation: cardEnter 300ms ease-out both;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* === 卡片内部 === */
.cap-card-inner {
  display: flex;
  gap: 16px;
  padding: 16px;
  height: 100%;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 250ms ease, box-shadow 250ms ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
  }
}

.cap-cover {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: var(--rounded-md);
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

  .cover-fallback {
    font-size: 32px;
    color: var(--st-ink-mute);
  }

  .cover-lock {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(13, 37, 61, 0.45);
    color: #fff;
    font-size: 26px;
    backdrop-filter: blur(2px);
  }
}

.cap-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.cap-top {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cap-title-row {
  display: flex;
  align-items: center;
  gap: 8px;

  .cap-song {
    font-size: 15px;
    font-weight: 600;
    color: var(--st-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.cap-tag {
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: var(--rounded-pill);
  font-size: 11px;
  font-weight: 500;

  &.tag-sealed {
    background: rgba(100, 116, 139, 0.12);
    color: #64748d;
  }
  &.tag-unlocked {
    background: rgba(94, 92, 230, 0.12);
    color: var(--st-primary);
  }
  &.tag-public {
    background: rgba(16, 185, 129, 0.12);
    color: #10b981;
  }
}

.cap-singer {
  font-size: 12px;
  color: var(--st-ink-mute);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cap-message {
  font-size: 13px;
  line-height: 1.6;
  color: var(--st-ink-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;

  &.blurred {
    color: transparent;
    text-shadow: 0 0 8px rgba(13, 37, 61, 0.35);
    user-select: none;
  }
}

.cap-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--st-ink-mute);

  .meta-countdown {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #64748b;

    .meta-icon {
      font-size: 13px;
    }
  }

  .meta-dur {
    font-feature-settings: 'tnum';
  }
}

.cap-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  color: var(--st-ink-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    background: var(--st-canvas-hover);
    border-color: var(--st-primary-subdued);
  }

  &.play {
    color: var(--st-primary);
    border-color: var(--st-primary-subdued);

    &:hover {
      background: rgba(94, 92, 230, 0.08);
    }
  }

  &.public:hover {
    color: #10b981;
    border-color: rgba(16, 185, 129, 0.4);
  }

  &.like {
    .like-icon {
      font-size: 14px;
      transition: color 200ms ease;
    }

    &.liked .like-icon {
      color: #f59e0b;
    }

    &.bounce .like-icon {
      animation: likeBounce 400ms ease;
    }
  }

  &.delete {
    padding: 5px 8px;
    color: var(--st-ink-mute);

    &:hover {
      color: #f56c6c;
      border-color: rgba(245, 108, 108, 0.4);
      background: rgba(245, 108, 108, 0.06);
    }
  }
}

@keyframes likeBounce {
  0% {
    transform: scale(0.6);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

/* === 骨架屏 === */
.skeleton-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-sm);

  .skeleton-cover {
    width: 96px;
    height: 96px;
    border-radius: var(--rounded-md);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;
    flex-shrink: 0;
  }

  .skeleton-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-top: 6px;
  }

  .skeleton-line {
    height: 12px;
    border-radius: var(--rounded-xs);
    background: #e8ecf0;
    animation: skeletonPulse 1.2s infinite ease-in-out;

    &.short {
      width: 40%;
    }
    &.mid {
      width: 60%;
    }
    &.long {
      width: 80%;
    }
  }
}

@keyframes skeletonPulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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
    padding: 9px 24px;
    border: none;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: background 200ms ease, transform 150ms ease;

    &:hover {
      background: var(--st-primary-hover);
      transform: translateY(-1px);
    }
  }
}

/* === 响应式 === */
@media (max-width: 540px) {
  .capsule-grid {
    grid-template-columns: 1fr;
  }
}
</style>
