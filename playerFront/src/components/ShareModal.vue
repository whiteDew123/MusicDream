<template>
  <transition name="share-fade">
    <div v-if="visible" class="share-modal" @click.self="close">
      <div class="share-content">
        <div class="share-header">
          <h3 class="share-title">分享到</h3>
          <button class="close-btn" @click="close">
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>

        <div class="share-body">
          <!-- 分享链接 -->
          <div class="share-link-row">
            <el-input
              v-model="shareUrl"
              readonly
              placeholder="分享链接"
            >
              <template #append>
                <button
                  class="copy-btn"
                  @click="handleCopy"
                  :disabled="!shareUrl"
                >
                  复制链接
                </button>
              </template>
            </el-input>
          </div>

          <!-- 分享渠道 -->
          <div class="share-channels">
            <div class="channel-item" @click="handleShare('wechat')">
              <div class="channel-icon wechat">
                <el-icon :size="24"><ChatLineRound /></el-icon>
              </div>
              <span class="channel-name">微信</span>
            </div>
            <div class="channel-item" @click="handleShare('weibo')">
              <div class="channel-icon weibo">
                <el-icon :size="24"><Reading /></el-icon>
              </div>
              <span class="channel-name">微博</span>
            </div>
            <div class="channel-item" @click="handleShare('qq')">
              <div class="channel-icon qq">
                <el-icon :size="24"><Service /></el-icon>
              </div>
              <span class="channel-name">QQ</span>
            </div>
            <div class="channel-item" @click="handleShare('qr')">
              <div class="channel-icon qr">
                <el-icon :size="24"><Connection /></el-icon>
              </div>
              <span class="channel-name">二维码</span>
            </div>
          </div>

          <!-- 二维码展示 -->
          <div v-if="showQr" class="qr-section">
            <div class="qr-code">
              <img :src="qrCodeUrl" alt="QR Code" />
            </div>
            <p class="qr-tip">扫描二维码在移动端查看</p>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, ChatLineRound, Reading, Service, Connection } from '@element-plus/icons-vue'
import { shareSongApi } from '@/api/interaction'

const props = defineProps({
  visible: Boolean,
  song: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:visible'])

const showQr = ref(false)

const shareUrl = computed(() => {
  if (typeof window === 'undefined' || !props.song?.musicId) return ''
  return `${window.location.origin}/song/${props.song.musicId}`
})

const qrCodeUrl = computed(() => {
  if (!shareUrl.value) return ''
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(shareUrl.value)}`
})

function close() {
  emit('update:visible', false)
  showQr.value = false
}

async function handleCopy() {
  if (!shareUrl.value) return
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (e) {
    // 降级方案
    const input = document.createElement('input')
    input.value = shareUrl.value
    document.body.appendChild(input)
    input.select()
    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制到剪贴板')
    } catch {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(input)
  }
}

async function handleShare(channel) {
  if (!props.song?.musicId) return
  try {
    await shareSongApi(props.song.musicId)
  } catch (e) {
    // 静默失败，分享行为仍可继续
  }

  const title = encodeURIComponent(props.song?.musicName || '好歌推荐')
  const url = encodeURIComponent(shareUrl.value)

  switch (channel) {
    case 'wechat':
      ElMessage.info('请在微信中粘贴链接分享')
      handleCopy()
      break
    case 'weibo': {
      const weiboUrl = `https://service.weibo.com/share/share.php?title=${title}&url=${url}`
      window.open(weiboUrl, '_blank', 'width=600,height=500')
      break
    }
    case 'qq': {
      const qqUrl = `https://connect.qq.com/widget/shareqq/index.html?title=${title}&url=${url}`
      window.open(qqUrl, '_blank', 'width=600,height=500')
      break
    }
    case 'qr':
      showQr.value = true
      break
  }
}

// 关闭时重置状态
watch(
  () => props.visible,
  (val) => {
    if (!val) {
      showQr.value = false
    }
  }
)
</script>

<style scoped lang="scss">
.share-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.share-content {
  width: 100%;
  max-width: 400px;
  background: var(--st-canvas);
  border-radius: var(--rounded-lg);
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
}

.share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--st-hairline);
}

.share-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--st-ink);
}

.close-btn {
  border: none;
  background: var(--st-input-bg);
  color: var(--st-ink-mute);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms ease;

  &:hover {
    background: var(--st-hairline);
    color: var(--st-ink);
  }
}

.share-body {
  padding: 20px;
}

.share-link-row {
  margin-bottom: 24px;

  :deep(.el-input-group__append) {
    background: var(--st-primary);
    color: #fff;
    border: none;
    border-radius: 0 var(--rounded-sm) var(--rounded-sm) 0;
    padding: 0 16px;
  }

  .copy-btn {
    background: transparent;
    color: #fff;
    border: none;
    font-size: 14px;
    cursor: pointer;
    padding: 8px 0;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.share-channels {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.channel-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 200ms ease;

  &:hover {
    transform: scale(1.08);

    .channel-icon {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.channel-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: box-shadow 200ms ease;

  &.wechat {
    background: #07c160;
  }
  &.weibo {
    background: #e6162d;
  }
  &.qq {
    background: #12b7f5;
  }
  &.qr {
    background: var(--st-primary);
  }
}

.channel-name {
  font-size: 13px;
  color: var(--st-ink-secondary);
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  border-top: 1px dashed var(--st-hairline);
  margin-top: 8px;
}

.qr-code {
  width: 160px;
  height: 160px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-sm);
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
  }
}

.qr-tip {
  font-size: 13px;
  color: var(--st-ink-mute);
  margin-top: 12px;
}

/* 动画 */
.share-fade-enter-active,
.share-fade-leave-active {
  transition: opacity 250ms ease;

  .share-content {
    transition: transform 250ms ease, opacity 250ms ease;
  }
}

.share-fade-enter-from,
.share-fade-leave-to {
  opacity: 0;

  .share-content {
    transform: scale(0.9) translateY(20px);
    opacity: 0;
  }
}
</style>
