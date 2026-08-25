<template>
  <div class="song-actions">
    <!-- 点赞 -->
    <div class="action-item" @click="handleLike">
      <div class="action-icon" :class="{ liked }">
        <el-icon :size="28"><StarFilled v-if="liked" /><Star /></el-icon>
      </div>
      <span class="action-count">{{ formatCount(likeCount) }}</span>
    </div>

    <!-- 评论 -->
    <div class="action-item" @click="$emit('open-comment')">
      <div class="action-icon">
        <el-icon :size="28"><ChatDotRound /></el-icon>
      </div>
      <span class="action-count">{{ formatCount(commentCount) }}</span>
    </div>

    <!-- 收藏 -->
    <div class="action-item" @click="handleFavorite">
      <div class="action-icon" :class="{ favorited }">
        <el-icon :size="28"><StarFilled v-if="favorited" /><Star /></el-icon>
      </div>
      <span class="action-count">{{ favorited ? '已收藏' : '收藏' }}</span>
    </div>

    <!-- 分享 -->
    <div class="action-item" @click="handleShare">
      <div class="action-icon">
        <el-icon :size="28"><Promotion /></el-icon>
      </div>
      <span class="action-count">{{ formatCount(shareCount) }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Star,
  StarFilled,
  ChatDotRound,
  Promotion
} from '@element-plus/icons-vue'
import { getMusicStatsApi, toggleLikeApi, shareSongApi } from '@/api/interaction'
import { addLikedMusicApi, removeLikedMusicApi } from '@/api/like'

const props = defineProps({
  song: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['open-comment', 'open-share'])

const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const commentCount = ref(0)
const shareCount = ref(0)

function formatCount(n) {
  if (!n && n !== 0) return ''
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

// 加载交互状态（AAA：统一接口，1 次请求获取全部数据）
async function loadStats() {
  if (!props.song?.musicId) return
  try {
    const res = await getMusicStatsApi(props.song.musicId)
    liked.value = res.data?.liked ?? false
    likeCount.value = res.data?.likesCount ?? 0
    commentCount.value = res.data?.commentCount ?? 0
    shareCount.value = res.data?.shareCount ?? 0
  } catch (e) {
    // 404 或网络异常时保持默认值
  }
}

// 点赞
async function handleLike() {
  if (!props.song?.musicId) return
  try {
    const res = await toggleLikeApi(props.song.musicId)
    liked.value = res.data?.liked ?? !liked.value
    likeCount.value = res.data?.likesCount ?? (liked.value ? likeCount.value + 1 : likeCount.value - 1)
    ElMessage.success(liked.value ? '点赞成功' : '已取消点赞')
  } catch (e) {
    // request.js 已统一弹窗
  }
}

// 收藏
async function handleFavorite() {
  if (!props.song?.musicId) return
  try {
    if (favorited.value) {
      await removeLikedMusicApi(props.song.musicId)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addLikedMusicApi(props.song.musicId)
      favorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    // request.js 已统一弹窗
  }
}

// 分享
async function handleShare() {
  if (!props.song?.musicId) return
  try {
    const res = await shareSongApi(props.song.musicId)
    if (res.data?.shareCount != null) {
      shareCount.value = res.data.shareCount
    }
    emit('open-share', res.data?.shareUrl)
  } catch (e) {
    emit('open-share')
  }
}

// 歌曲切换时刷新数据
watch(
  () => props.song?.musicId,
  () => {
    if (props.song?.musicId) {
      loadStats()
    }
  }
)

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.song-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
  padding-right: 8px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 200ms ease;

  &:hover {
    transform: scale(1.1);

    .action-icon {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
    }
  }

  &:active {
    transform: scale(0.9);
  }
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 250ms ease;

  &.liked {
    color: var(--st-primary);
    background: rgba(94, 92, 230, 0.25);
    border-color: var(--st-primary);
  }

  &.favorited {
    color: #f5a623;
    background: rgba(245, 166, 35, 0.25);
    border-color: rgba(245, 166, 35, 0.6);
  }
}

.action-count {
  font-size: 13px;
  color: #fff;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
</style>