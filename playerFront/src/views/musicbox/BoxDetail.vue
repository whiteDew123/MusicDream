<template>
  <div class="box-detail" v-loading="loading">
    <!-- 盲盒详情 -->
    <div v-if="boxDetail" class="box-content">
      <!-- 盲盒头部 -->
      <div class="box-header">
        <div class="box-cover-large">
          <img v-if="boxDetail.coverUrl" :src="boxDetail.coverUrl" alt="cover" />
          <div v-else class="cover-placeholder">
            <el-icon><Headset /></el-icon>
          </div>
        </div>
        <div class="box-info-large">
          <h1 class="box-title-large">{{ boxDetail.title }}</h1>
          <el-tag size="large">{{ boxDetail.moodTag }}</el-tag>
          <div class="box-stats-large">
            <span><el-icon><View /></el-icon> {{ boxDetail.openCount }} 次开启</span>
            <span @click="handleLike" class="like-btn">
              <el-icon :class="{ liked: boxDetail.isLiked }">
                <component :is="boxDetail.isLiked ? 'StarFilled' : 'Star'" />
              </el-icon>
              {{ boxDetail.likeCount }} 点赞
            </span>
          </div>
          <div class="box-actions">
            <el-button type="primary" size="large" @click="handleOpen" :disabled="isOpened">
              {{ isOpened ? '已开启' : '🎁 开启盲盒' }}
            </el-button>
            <el-button size="large" @click="showFriendRequestDialog = true">
              💝 想认识TA
            </el-button>
          </div>
        </div>
      </div>

      <!-- 创建者留言（开启后可见） -->
      <div v-if="isOpened && boxDetail.message" class="creator-message">
        <div class="message-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>创建者留言</span>
        </div>
        <p class="message-content">{{ boxDetail.message }}</p>
      </div>

      <!-- 歌曲列表 -->
      <div class="song-list-section">
        <h2 class="section-title">🎵 歌曲列表</h2>
        <div class="song-list">
          <div
            v-for="(song, index) in boxDetail.songs"
            :key="song.songId"
            class="song-item"
            @click="playSong(song)"
          >
            <span class="song-index">{{ index + 1 }}</span>
            <img v-if="song.coverUrl" class="song-cover" :src="song.coverUrl" alt="cover" />
            <div v-else class="song-cover-placeholder">
              <el-icon><Headset /></el-icon>
            </div>
            <div class="song-info">
              <span class="song-name">{{ song.songName }}</span>
              <span class="song-singer">{{ song.singerName }}</span>
            </div>
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && !boxDetail" description="盲盒不存在或已删除" />

    <!-- 交友请求弹窗 -->
    <el-dialog
      v-model="showFriendRequestDialog"
      title="想认识TA"
      width="400px"
    >
      <div class="friend-request-form">
        <p class="request-hint">
          开启盲盒后，如果你觉得品味相投，可以发送交友请求。<br>
          对方同意后，你们将成为好友。
        </p>
        <el-input
          v-model="friendRequestMessage"
          type="textarea"
          :rows="3"
          placeholder="写一段话，让对方更了解你（可选）"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="showFriendRequestDialog = false">取消</el-button>
        <el-button type="primary" :loading="sendingRequest" @click="handleSendFriendRequest">
          发送请求
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Headset,
  View,
  Star,
  StarFilled,
  ChatDotRound,
  VideoPlay
} from '@element-plus/icons-vue'
import {
  getBoxDetailApi,
  openBoxApi,
  toggleLikeApi,
  sendBoxFriendRequestApi
} from '@/api/musicbox'
import { usePlayerStore } from '@/store/player'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()

const loading = ref(false)
const boxDetail = ref(null)
const isOpened = ref(false)
const showFriendRequestDialog = ref(false)
const friendRequestMessage = ref('')
const sendingRequest = ref(false)

// 加载盲盒基本信息
async function loadBoxDetail() {
  loading.value = true
  try {
    const res = await getBoxDetailApi(route.params.id)
    boxDetail.value = res.data
    if (boxDetail.value) {
      isOpened.value = boxDetail.value.isOpened || false
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 开启盲盒
async function handleOpen() {
  loading.value = true
  try {
    const res = await openBoxApi(route.params.id)
    boxDetail.value = res.data
    isOpened.value = true
    ElMessage.success('盲盒已开启')
  } catch (error) {
    ElMessage.error(error.message || '开启失败')
  } finally {
    loading.value = false
  }
}

// 点赞
async function handleLike() {
  try {
    await toggleLikeApi(route.params.id)
    boxDetail.value.isLiked = !boxDetail.value.isLiked
    boxDetail.value.likeCount += boxDetail.value.isLiked ? 1 : -1
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 播放歌曲
function playSong(song) {
  playerStore.playSong({
    id: song.songId,
    name: song.songName,
    singer: song.singerName,
    cover: song.coverUrl
  })
}

// 发送交友请求
async function handleSendFriendRequest() {
  sendingRequest.value = true
  try {
    await sendBoxFriendRequestApi(route.params.id, {
      receiverId: boxDetail.value.userId,
      message: friendRequestMessage.value
    })
    ElMessage.success('交友请求已发送')
    showFriendRequestDialog.value = false
    friendRequestMessage.value = ''
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sendingRequest.value = false
  }
}

onMounted(() => {
  loadBoxDetail()
})
</script>

<style scoped lang="scss">
.box-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
}

.box-content {
  .box-header {
    display: flex;
    gap: 32px;
    margin-bottom: 32px;

    .box-cover-large {
      width: 280px;
      height: 280px;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

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
          font-size: 64px;
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }

    .box-info-large {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 16px;

      .box-title-large {
        font-size: 28px;
        font-weight: 700;
        color: var(--st-ink);
        margin: 0;
      }

      .box-stats-large {
        display: flex;
        gap: 24px;
        font-size: 14px;
        color: var(--st-ink-secondary);

        .like-btn {
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 4px;
          transition: color 150ms ease;

          .el-icon {
            font-size: 16px;
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

      .box-actions {
        display: flex;
        gap: 12px;
      }
    }
  }

  /* 创建者留言 */
  .creator-message {
    background: linear-gradient(135deg, rgba(94, 92, 230, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    border-left: 4px solid var(--st-primary);
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 32px;

    .message-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      color: var(--st-primary);
      margin-bottom: 12px;

      .el-icon {
        font-size: 18px;
      }
    }

    .message-content {
      font-size: 15px;
      color: var(--st-ink);
      line-height: 1.6;
      margin: 0;
    }
  }

  /* 歌曲列表 */
  .song-list-section {
    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--st-ink);
      margin: 0 0 16px;
    }

    .song-list {
      background: #ffffff;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .song-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      cursor: pointer;
      transition: background 150ms ease;
      border-bottom: 1px solid var(--st-hairline);

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background: var(--st-primary-subdued);

        .play-icon {
          opacity: 1;
        }
      }

      .song-index {
        width: 24px;
        font-size: 14px;
        color: var(--st-ink-secondary);
        text-align: center;
      }

      .song-cover {
        width: 48px;
        height: 48px;
        border-radius: 6px;
        object-fit: cover;
      }

      .song-cover-placeholder {
        width: 48px;
        height: 48px;
        border-radius: 6px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;

        .el-icon {
          font-size: 20px;
          color: rgba(255, 255, 255, 0.6);
        }
      }

      .song-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .song-name {
          font-size: 15px;
          color: var(--st-ink);
          font-weight: 500;
        }

        .song-singer {
          font-size: 13px;
          color: var(--st-ink-secondary);
        }
      }

      .play-icon {
        font-size: 20px;
        color: var(--st-primary);
        opacity: 0;
        transition: opacity 150ms ease;
      }
    }
  }
}

.friend-request-form {
  .request-hint {
    font-size: 14px;
    color: var(--st-ink-secondary);
    line-height: 1.6;
    margin-bottom: 16px;
  }
}
</style>