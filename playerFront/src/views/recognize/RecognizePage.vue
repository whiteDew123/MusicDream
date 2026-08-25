<template>
  <div class="recognize-page">
    <!-- 标题 -->
    <div class="page-header">
      <p class="eyebrow">RECOGNIZE · 听歌识曲</p>
      <h2>听歌识曲</h2>
      <p class="sub">直接捕获浏览器标签页音频，无需扬声器也能识别歌曲。</p>
    </div>

    <!-- 音频源切换 -->
    <div class="source-switch">
      <button
        class="source-option"
        :class="{ active: recorder.captureMode.value === 'system' }"
        :disabled="recorder.isRecording.value"
        @click="setSource('system')"
      >
        <el-icon><Monitor /></el-icon>
        <span>系统音频</span>
      </button>
      <button
        class="source-option"
        :class="{ active: recorder.captureMode.value === 'mic' }"
        :disabled="recorder.isRecording.value"
        @click="setSource('mic')"
      >
        <el-icon><Microphone /></el-icon>
        <span>麦克风</span>
      </button>
    </div>

    <!-- 系统音频提示 -->
    <p v-if="recorder.captureMode.value === 'system' && status === 'idle'" class="source-hint">
      点击下方按钮 → 选择播放歌曲的标签页 → 点击"分享"即可开始识别
    </p>

    <!-- 录音按钮 -->
    <div class="recorder-area">
      <button
        class="record-btn"
        :class="{ recording: recorder.isRecording.value }"
        :disabled="status === 'loading'"
        @click="handleRecordClick"
      >
        <span class="record-icon">
          <el-icon v-if="!recorder.isRecording.value"><Microphone /></el-icon>
          <el-icon v-else><VideoPause /></el-icon>
        </span>
      </button>

      <p class="status-text">
        <template v-if="status === 'idle'">点击开始听歌识曲</template>
        <template v-else-if="status === 'recording'">正在录音 {{ recorder.duration.value }}s / 最长 15s</template>
        <template v-else-if="status === 'loading'">正在识别中...</template>
        <template v-else-if="status === 'noresult'">{{ noResultText }}</template>
        <template v-else-if="status === 'error'">{{ errorMessage }}</template>
        <template v-else-if="status === 'result'">识别成功 🎉</template>
      </p>
    </div>

    <!-- 识别结果 -->
    <transition name="slide-up">
      <div v-if="status === 'result' && song" class="result-card">
        <div class="result-cover">
          <img v-if="song.imageUrl" :src="song.imageUrl" alt="封面" />
          <div v-else class="cover-placeholder">
            <el-icon><Headset /></el-icon>
          </div>
        </div>
        <div class="result-info">
          <h3>{{ song.musicName }}</h3>
          <p>{{ song.singerName || '未知歌手' }}</p>
          <div class="result-actions">
            <el-button type="primary" round @click="playSong">播放</el-button>
            <el-button round @click="reset">重新识别</el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 错误 / 无结果操作 -->
    <div v-if="status === 'noresult' || status === 'error'" class="error-actions">
      <el-button type="primary" round @click="reset">重新识别</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone, VideoPause, Headset, Monitor } from '@element-plus/icons-vue'
import { useAudioRecorder } from '@/composables/useAudioRecorder'
import { recognizeApi } from '@/api/recognize'
import { songDetailApi } from '@/api/music'
import { usePlayerStore } from '@/store/player'

const recorder = useAudioRecorder()
const playerStore = usePlayerStore()
const status = ref('idle')
const song = ref(null)
const errorMessage = ref('')

const noResultText = computed(() =>
  recorder.captureMode.value === 'system'
    ? '未识别到歌曲，请确认已勾选“分享音频”，或切换到麦克风模式'
    : '未识别到歌曲，请靠近音源重试'
)

function setSource(mode) {
  if (recorder.isRecording.value) return
  recorder.captureMode.value = mode
}

async function handleRecordClick() {
  if (status.value === 'loading') return

  if (!recorder.isRecording.value) {
    const isSystem = recorder.captureMode.value === 'system'
    if (isSystem) {
      await recorder.startSystem(15)
    } else {
      await recorder.start(15)
    }
    if (recorder.isRecording.value) {
      status.value = 'recording'
    } else if (recorder.error.value) {
      status.value = 'error'
      errorMessage.value = recorder.error.value
    }
    return
  }

  // 停止并识别
  const blob = await recorder.stop()
  if (!blob) return

  if (recorder.duration.value < 3) {
    status.value = 'error'
    errorMessage.value = '录音太短，请至少录制 3 秒'
    return
  }

  status.value = 'loading'
  try {
    const res = await recognizeApi(blob, recorder.duration.value)
    const data = res.data || {}
    if (data.success && data.musicId) {
      const detailRes = await songDetailApi(data.musicId)
      song.value = detailRes.data || {}
      status.value = 'result'
    } else {
      status.value = 'noresult'
    }
  } catch (e) {
    status.value = 'error'
    errorMessage.value = e.message || '识别失败，请重试'
  }
}

function reset() {
  recorder.reset()
  status.value = 'idle'
  song.value = null
  errorMessage.value = ''
}

function playSong() {
  if (!song.value) return
  playerStore.playSong(song.value)
  ElMessage.success('已加入播放并开始播放')
}
</script>

<style scoped lang="scss">
.recognize-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
  min-height: 100%;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;

  .eyebrow {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    color: var(--st-primary);
    margin-bottom: 8px;
  }

  h2 {
    font-size: 32px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.5px;
    margin-bottom: 8px;
  }

  .sub {
    font-size: 14px;
    color: var(--st-ink-mute);
  }
}

.source-switch {
  display: flex;
  gap: 0;
  border: 1px solid var(--st-hairline);
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
}

.source-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: var(--st-ink-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: background 200ms ease, color 200ms ease;

  &:not(:last-child) {
    border-right: 1px solid var(--st-hairline);
  }

  &:hover:not(:disabled) {
    background: var(--st-canvas-hover);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  &.active {
    background: var(--st-primary);
    color: #fff;
  }
}

.source-hint {
  font-size: 13px;
  color: var(--st-ink-mute);
  margin-bottom: 16px;
  text-align: center;
}

.recorder-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.record-btn {
  width: 160px;
  height: 160px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--st-primary) 0%, var(--st-primary-soft) 100%);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(94, 92, 230, 0.3);
  transition: transform 250ms ease, box-shadow 250ms ease;
  position: relative;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 24px rgba(94, 92, 230, 0.4);
  }

  &.recording {
    animation: pulse 1500ms ease-in-out infinite;

    &::after {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 50%;
      border: 2px solid rgba(94, 92, 230, 0.5);
      animation: ring 1500ms ease-out infinite;
    }
  }

  .record-icon {
    font-size: 48px;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

@keyframes ring {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.6); opacity: 0; }
}

.status-text {
  font-size: 14px;
  color: var(--st-ink-secondary);
  min-height: 20px;
}

.result-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-top: 32px;
  max-width: 520px;
  width: 100%;
}

.result-cover {
  width: 96px;
  height: 96px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--st-canvas-hover);
    color: var(--st-ink-mute);
    font-size: 32px;
  }
}

.result-info {
  flex: 1;

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--st-ink);
    margin-bottom: 4px;
  }

  p {
    font-size: 14px;
    color: var(--st-ink-mute);
    margin-bottom: 12px;
  }

  .result-actions {
    display: flex;
    gap: 8px;
  }
}

.error-actions {
  margin-top: 24px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 300ms ease-out;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>