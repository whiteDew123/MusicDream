<template>
  <footer class="player-bar">
    <!-- 左侧：当前歌曲信息 -->
    <div class="song-info">
      <div class="cover" :class="{ clickable: currentSong }" @click="toggleLyrics">
        <img
          v-if="currentSong?.imageUrl"
          :src="currentSong.imageUrl"
          :alt="currentSong.musicName"
        />
        <el-icon v-else class="cover-placeholder"><Headset /></el-icon>
      </div>
      <div class="song-text" v-if="currentSong">
        <div class="song-name">{{ currentSong.musicName }}</div>
        <div class="song-singer">{{ currentSong.singerName }}</div>
      </div>
      <div class="song-empty" v-else>暂无播放</div>
    </div>

    <!-- 中间：播放控制 + 进度条 -->
    <div class="player-center">
      <div class="controls">
        <button
          class="ctrl-btn mode-btn"
          :title="playerStore.playModeLabels[playerStore.playMode]"
          @click="playerStore.togglePlayMode()"
        >
          <el-icon v-if="playerStore.playMode === 0"><Sort /></el-icon>
          <el-icon v-else-if="playerStore.playMode === 1"><Refresh /></el-icon>
          <el-icon v-else><Switch /></el-icon>
        </button>
        <button class="ctrl-btn" :disabled="!currentSong" @click="playerStore.playPrev()">
          <el-icon><CaretLeft /></el-icon>
        </button>
        <button
          class="ctrl-btn play-btn"
          :disabled="!currentSong"
          @click="playerStore.togglePlay()"
        >
          <el-icon v-if="!playerStore.playing"><VideoPlay /></el-icon>
          <el-icon v-else><VideoPause /></el-icon>
        </button>
        <button class="ctrl-btn" :disabled="!currentSong" @click="playerStore.playNext()">
          <el-icon><CaretRight /></el-icon>
        </button>
        <button class="ctrl-btn list-btn" @click="showPlaylist = !showPlaylist">
          <el-icon><Tickets /></el-icon>
        </button>
      </div>

      <!-- 进度条 -->
      <div class="progress-wrap">
        <span class="time current">{{ formatTime(playerStore.currentTime) }}</span>
        <div
          class="progress-bar"
          ref="progressBarRef"
          @mousedown="handleProgressMouseDown"
        >
          <div class="progress-track">
            <div class="progress-filled" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div
            class="progress-thumb"
            :style="{ left: progressPercent + '%' }"
          ></div>
        </div>
        <span class="time total">{{ formatTime(playerStore.duration) }}</span>
      </div>
    </div>

    <!-- 右侧：音量 -->
    <div class="player-right">
      <button class="ctrl-btn" @click="playerStore.toggleMute()">
        <el-icon v-if="playerStore.muted || playerStore.volume === 0"><MuteNotification /></el-icon>
        <el-icon v-else><Microphone /></el-icon>
      </button>
      <div class="volume-slider">
        <div
          class="volume-track"
          ref="volumeBarRef"
          @mousedown="handleVolumeMouseDown"
        >
          <div class="volume-filled" :style="{ width: volumePercent + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- 播放列表弹层 -->
    <transition name="slide-up">
      <div v-if="showPlaylist" class="playlist-popup">
        <div class="playlist-header">
          <span>播放列表（{{ playerStore.playlist.length }}）</span>
          <button class="clear-btn" @click="playerStore.clearPlaylist()">清空</button>
        </div>
        <div class="playlist-items">
          <div
            v-for="(song, idx) in playerStore.playlist"
            :key="song.musicId"
            class="playlist-item"
            :class="{ active: idx === playerStore.currentIndex }"
            @click="playerStore.setPlaylist(playerStore.playlist, idx)"
          >
            <span class="item-index">{{ idx + 1 }}</span>
            <span class="item-name">{{ song.musicName }}</span>
            <span class="item-singer">{{ song.singerName }}</span>
            <button
              class="item-remove"
              @click.stop="playerStore.removeFromPlaylist(idx)"
            >
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div v-if="playerStore.playlist.length === 0" class="empty-tip">
            播放列表为空
          </div>
        </div>
      </div>
    </transition>

    <!-- 全屏播放器面板（抖音风格） -->
    <FullPlayer v-if="showLyrics" @close="toggleLyrics" />
  </footer>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Headset,
  Sort,
  Refresh,
  Switch,
  CaretLeft,
  CaretRight,
  VideoPlay,
  VideoPause,
  Tickets,
  MuteNotification,
  Microphone,
  Close
} from '@element-plus/icons-vue'
import { usePlayerStore } from '@/store/player'
import FullPlayer from '@/components/FullPlayer.vue'

const playerStore = usePlayerStore()

const showPlaylist = ref(false)
const showLyrics = ref(false)
const progressBarRef = ref(null)
const volumeBarRef = ref(null)

// 切换全屏播放器面板
function toggleLyrics() {
  if (!currentSong.value) return
  showLyrics.value = !showLyrics.value
}

const currentSong = computed(() => playerStore.currentSong)

const progressPercent = computed(() => {
  if (!playerStore.duration) return 0
  return (playerStore.currentTime / playerStore.duration) * 100
})

const volumePercent = computed(() => {
  return (playerStore.muted ? 0 : playerStore.volume) * 100
})

// 格式化时间 mm:ss
function formatTime(sec) {
  if (!sec || isNaN(sec)) return '00:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 进度条拖拽（animation.md 第一章：拖拽时滑块圆点放大 1.3 倍）
function handleProgressMouseDown(e) {
  if (!playerStore.duration) return
  const bar = progressBarRef.value
  const updateProgress = (ev) => {
    const rect = bar.getBoundingClientRect()
    const percent = Math.min(Math.max((ev.clientX - rect.left) / rect.width, 0), 1)
    playerStore.seekTo(percent * playerStore.duration)
  }
  updateProgress(e)
  const onMove = (ev) => updateProgress(ev)
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

// 音量拖拽
function handleVolumeMouseDown(e) {
  const bar = volumeBarRef.value
  const updateVolume = (ev) => {
    const rect = bar.getBoundingClientRect()
    const percent = Math.min(Math.max((ev.clientX - rect.left) / rect.width, 0), 1)
    playerStore.setVolume(percent)
  }
  updateVolume(e)
  const onMove = (ev) => updateVolume(ev)
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}
</script>

<style scoped lang="scss">
/* 播放栏：深色背景维持"舞台感"（animation.md 特别说明）*/
.player-bar {
  position: relative;
  height: var(--playerbar-height);
  background: var(--player-bg);
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-xl);
  gap: var(--spacing-xl);
  flex-shrink: 0;
}

/* === 左侧：歌曲信息 === */
.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 260px;
  flex-shrink: 0;

  .cover {
    width: 48px;
    height: 48px;
    border-radius: var(--rounded-sm);
    overflow: hidden;
    background: var(--player-bg-soft);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      font-size: 22px;
      color: var(--player-text-mute);
    }

    &.clickable {
      cursor: pointer;
      transition: box-shadow 200ms ease;

      &:hover {
        box-shadow: 0 0 0 2px var(--st-primary);
      }
    }
  }

  .song-text {
    overflow: hidden;

    .song-name {
      font-size: 14px;
      color: var(--player-text);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .song-singer {
      font-size: 12px;
      color: var(--player-text-mute);
      margin-top: 2px;
    }
  }

  .song-empty {
    font-size: 14px;
    color: var(--player-text-mute);
  }
}

/* === 中间：控制 + 进度 === */
.player-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  max-width: 600px;
  margin: 0 auto;
}

.controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ctrl-btn {
  border: none;
  background: transparent;
  color: var(--player-text-mute);
  cursor: pointer;
  font-size: 16px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 200ms ease;

  &:hover:not(:disabled) {
    color: var(--player-text);
    background: rgba(255, 255, 255, 0.1);
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

/* 播放/暂停按钮：弹性缩放动画（animation.md 第一章）*/
.play-btn {
  width: 40px !important;
  height: 40px !important;
  background: var(--st-primary) !important;
  color: #fff !important;
  font-size: 20px !important;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover) !important;
    transform: scale(1.1);
  }

  &:active:not(:disabled) {
    transform: scale(0.85);
  }
}

/* 进度条 */
.progress-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;

  .time {
    font-size: 11px;
    color: var(--player-text-mute);
    font-feature-settings: 'tnum';
    min-width: 36px;
    text-align: center;
  }
}

.progress-bar {
  flex: 1;
  height: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;

  .progress-track {
    width: 100%;
    height: 4px;
    background: var(--player-bar);
    border-radius: 2px;
    overflow: hidden;
  }

  .progress-filled {
    height: 100%;
    background: var(--st-primary);
    border-radius: 2px;
    transition: width 150ms linear;
  }

  .progress-thumb {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 12px;
    height: 12px;
    background: #fff;
    border-radius: 50%;
    opacity: 0;
    transition: opacity 150ms ease, transform 150ms ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  }

  &:hover .progress-thumb {
    opacity: 1;
  }
}

/* === 右侧：音量 === */
.player-right {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 160px;
  justify-content: flex-end;
  flex-shrink: 0;
}

.volume-slider {
  width: 80px;

  .volume-track {
    width: 100%;
    height: 4px;
    background: var(--player-bar);
    border-radius: 2px;
    cursor: pointer;

    .volume-filled {
      height: 100%;
      background: var(--st-primary);
      border-radius: 2px;
      transition: width 150ms ease;
    }
  }
}

/* === 播放列表弹层 === */
.playlist-popup {
  position: absolute;
  bottom: calc(var(--playerbar-height) + 8px);
  right: 24px;
  width: 380px;
  max-height: 400px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 100;

  .playlist-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid var(--st-hairline);
    font-size: 14px;
    font-weight: 600;
    color: var(--st-ink);

    .clear-btn {
      border: none;
      background: transparent;
      color: var(--st-primary);
      font-size: 13px;
      cursor: pointer;

      &:hover {
        color: var(--st-primary-hover);
      }
    }
  }

  .playlist-items {
    overflow-y: auto;
    max-height: 340px;
  }

  .playlist-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 16px;
    cursor: pointer;
    transition: background 150ms ease;

    &:hover {
      background: var(--st-canvas-hover);

      .item-remove {
        opacity: 1;
      }
    }

    &.active {
      background: rgba(94, 92, 230, 0.08);
      color: var(--st-primary);

      .item-name {
        color: var(--st-primary);
        font-weight: 600;
      }
    }

    .item-index {
      width: 20px;
      font-size: 12px;
      color: var(--st-ink-mute);
      text-align: center;
      font-feature-settings: 'tnum';
    }

    .item-name {
      flex: 1;
      font-size: 13px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
      max-width: 80px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-remove {
      opacity: 0;
      border: none;
      background: transparent;
      color: var(--st-ink-mute);
      cursor: pointer;
      font-size: 14px;
      transition: opacity 200ms ease, color 150ms ease;

      &:hover {
        color: var(--st-primary);
      }
    }
  }

  .empty-tip {
    padding: 32px;
    text-align: center;
    color: var(--st-ink-mute);
    font-size: 14px;
  }
}

/* 弹层入场动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity 200ms ease, transform 200ms ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(12px);
}


</style>
