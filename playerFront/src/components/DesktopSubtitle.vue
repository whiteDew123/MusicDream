<template>
  <div
    class="desktop-subtitle"
    :style="subtitleStyle"
    @mousedown="startDrag"
  >
    <!-- 虚化封面背景 -->
    <div class="subtitle-bg" v-if="currentSong?.imageUrl">
      <img :src="currentSong.imageUrl" alt="" />
    </div>

    <div class="subtitle-content">
      <!-- 歌曲信息 -->
      <div class="subtitle-header">
        <div class="subtitle-song">{{ currentSong?.musicName }}</div>
        <div class="subtitle-singer">{{ currentSong?.singerName || '未知歌手' }}</div>
      </div>

      <!-- 多行歌词 -->
      <div class="subtitle-lyrics">
        <div
          v-for="line in visibleLyrics"
          :key="line.index"
          class="lyric-line"
          :class="{
            active: line.offset === 0,
            'fade-top': line.offset < 0,
            'fade-bottom': line.offset > 0
          }"
          :style="getLyricStyle(line)"
        >
          {{ line.text || '♪' }}
        </div>
        <div v-if="!playerStore.lyrics.length" class="no-lyrics">
          暂无歌词
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { usePlayerStore } from '@/store/player'

const playerStore = usePlayerStore()

const currentSong = computed(() => playerStore.currentSong)

// 可见歌词窗口（当前行前后各 2 行，共 5 行）
const visibleLyrics = computed(() => {
  const lyrics = playerStore.lyrics
  const idx = playerStore.currentLyricIndex
  if (!lyrics.length || idx < 0) return []

  const HALF = 2
  const start = Math.max(0, idx - HALF)
  const end = Math.min(lyrics.length, idx + HALF + 1)

  const result = []
  for (let i = start; i < end; i++) {
    result.push({
      ...lyrics[i],
      index: i,
      offset: i - idx
    })
  }
  return result
})

// 歌词样式
function getLyricStyle(line) {
  const isActive = line.offset === 0
  return {
    opacity: isActive ? 1 : (0.35 - Math.abs(line.offset) * 0.1),
    fontSize: isActive ? '20px' : '16px',
    fontWeight: isActive ? 600 : 400,
    transform: `scale(${isActive ? 1 : 0.95})`
  }
}

// 字幕位置（持久化到 localStorage）
const position = ref({ x: 100, y: 100 })

// 加载保存的位置
function loadPosition() {
  try {
    const saved = localStorage.getItem('subtitle-position')
    if (saved) {
      position.value = JSON.parse(saved)
    }
  } catch (e) {
    // 忽略错误
  }
}

// 保存位置
function savePosition() {
  try {
    localStorage.setItem('subtitle-position', JSON.stringify(position.value))
  } catch (e) {
    // 忽略错误
  }
}

const subtitleStyle = computed(() => ({
  left: position.value.x + 'px',
  top: position.value.y + 'px'
}))

// 拖动逻辑
const dragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const posStart = ref({ x: 0, y: 0 })

function startDrag(e) {
  dragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY }
  posStart.value = { ...position.value }

  const onMove = (ev) => {
    if (!dragging.value) return
    position.value.x = posStart.value.x + (ev.clientX - dragStart.value.x)
    position.value.y = posStart.value.y + (ev.clientY - dragStart.value.y)
  }

  const onUp = () => {
    dragging.value = false
    savePosition()
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

onMounted(() => {
  loadPosition()
})
</script>

<style scoped lang="scss">
.desktop-subtitle {
  position: fixed;
  z-index: 9999;
  cursor: move;
  user-select: none;
  -webkit-user-select: none;
  width: 320px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.5);
  transition: box-shadow 200ms ease;

  &:hover {
    box-shadow: 0 16px 56px rgba(0, 0, 0, 0.6);
  }
}

/* 虚化封面背景 */
.subtitle-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    filter: blur(40px) saturate(1.4) brightness(0.5);
    transform: scale(1.2);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      rgba(0, 0, 0, 0.3) 0%,
      rgba(0, 0, 0, 0.5) 50%,
      rgba(0, 0, 0, 0.7) 100%
    );
  }
}

.subtitle-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  padding: 20px 24px;
  gap: 16px;
  pointer-events: none;
}

/* 歌曲信息 */
.subtitle-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.subtitle-song {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.subtitle-singer {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 多行歌词 */
.subtitle-lyrics {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-height: 120px;
  justify-content: center;
}

.lyric-line {
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  transition: all 300ms cubic-bezier(0.22, 1, 0.36, 1);
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.6);
  line-height: 1.5;
  word-break: break-word;

  &.active {
    color: #fff;
    text-shadow:
      0 2px 20px rgba(94, 92, 230, 0.6),
      0 0 40px rgba(94, 92, 230, 0.3),
      0 2px 8px rgba(0, 0, 0, 0.8);
  }

  &.fade-top,
  &.fade-bottom {
    color: rgba(255, 255, 255, 0.5);
  }
}

.no-lyrics {
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
  text-align: center;
  padding: 20px 0;
}
</style>