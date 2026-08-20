import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 播放器状态管理
// - 维护播放列表、当前歌曲、播放进度、音量、播放模式
// - 通过 HTML5 Audio API 控制实际音频播放
// - animation.md 第一章：播放/暂停弹性缩放、进度条拖拽、歌曲切换滑入滑出
export const usePlayerStore = defineStore('player', () => {
  // ===== 音频元素 =====
  const audio = ref(typeof window !== 'undefined' ? new Audio() : null)
  if (audio.value) {
    audio.value.preload = 'auto'
  }

  // ===== 播放列表 =====
  const playlist = ref([])
  const currentIndex = ref(-1)

  // ===== 播放状态 =====
  const playing = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.7)
  const muted = ref(false)

  // 播放模式：0-顺序播放 1-单曲循环 2-随机播放
  const playMode = ref(0)
  const playModeLabels = ['顺序播放', '单曲循环', '随机播放']

  // ===== 当前歌曲（计算属性）=====
  const currentSong = computed(() => {
    return currentIndex.value >= 0 ? playlist.value[currentIndex.value] : null
  })

  // ===== 播放控制 =====

  // 设置播放列表并播放指定索引
  function setPlaylist(list, index = 0) {
    playlist.value = list
    currentIndex.value = index
    loadAndPlay()
  }

  // 添加单首歌到播放列表（已存在则跳到该歌）
  function playSong(song) {
    const idx = playlist.value.findIndex((s) => s.musicId === song.musicId)
    if (idx >= 0) {
      currentIndex.value = idx
    } else {
      playlist.value.push(song)
      currentIndex.value = playlist.value.length - 1
    }
    loadAndPlay()
  }

  // 添加到播放列表末尾（不立即播放）
  function addToPlaylist(song) {
    const idx = playlist.value.findIndex((s) => s.musicId === song.musicId)
    if (idx < 0) {
      playlist.value.push(song)
    }
  }

  // 加载并播放当前歌曲
  function loadAndPlay() {
    const song = currentSong.value
    if (!song || !audio.value) return
    audio.value.src = song.musicUrl || ''
    audio.value
      .play()
      .then(() => {
        playing.value = true
      })
      .catch(() => {
        playing.value = false
      })
  }

  // 播放/暂停切换
  function togglePlay() {
    if (!audio.value || !currentSong.value) return
    if (playing.value) {
      audio.value.pause()
      playing.value = false
    } else {
      audio.value
        .play()
        .then(() => {
          playing.value = true
        })
        .catch(() => {
          playing.value = false
        })
    }
  }

  // 上一首
  function playPrev() {
    if (playlist.value.length === 0) return
    if (playMode.value === 2) {
      // 随机
      currentIndex.value = Math.floor(Math.random() * playlist.value.length)
    } else {
      currentIndex.value =
        (currentIndex.value - 1 + playlist.value.length) % playlist.value.length
    }
    loadAndPlay()
  }

  // 下一首
  function playNext() {
    if (playlist.value.length === 0) return
    if (playMode.value === 2) {
      // 随机
      currentIndex.value = Math.floor(Math.random() * playlist.value.length)
    } else {
      currentIndex.value = (currentIndex.value + 1) % playlist.value.length
    }
    loadAndPlay()
  }

  // 切换播放模式
  function togglePlayMode() {
    playMode.value = (playMode.value + 1) % 3
  }

  // 跳转到指定时间（拖拽进度条）
  function seekTo(time) {
    if (audio.value) {
      audio.value.currentTime = time
      currentTime.value = time
    }
  }

  // 设置音量
  function setVolume(vol) {
    volume.value = vol
    if (audio.value) {
      audio.value.volume = vol
      muted.value = vol === 0
    }
  }

  // 静音切换
  function toggleMute() {
    if (audio.value) {
      muted.value = !muted.value
      audio.value.muted = muted.value
    }
  }

  // 从播放列表移除
  function removeFromPlaylist(index) {
    playlist.value.splice(index, 1)
    if (index < currentIndex.value) {
      currentIndex.value--
    } else if (index === currentIndex.value) {
      if (playlist.value.length === 0) {
        currentIndex.value = -1
        playing.value = false
        audio.value && (audio.value.src = '')
      } else {
        currentIndex.value = currentIndex.value % playlist.value.length
        loadAndPlay()
      }
    }
  }

  // 清空播放列表
  function clearPlaylist() {
    playlist.value = []
    currentIndex.value = -1
    playing.value = false
    if (audio.value) {
      audio.value.pause()
      audio.value.src = ''
    }
  }

  // ===== 音频事件绑定（在 App 或 Layout 中调用）=====
  function initAudioEvents() {
    if (!audio.value) return
    audio.value.addEventListener('timeupdate', () => {
      currentTime.value = audio.value.currentTime
    })
    audio.value.addEventListener('loadedmetadata', () => {
      duration.value = audio.value.duration
    })
    audio.value.addEventListener('ended', () => {
      if (playMode.value === 1) {
        // 单曲循环
        loadAndPlay()
      } else {
        playNext()
      }
    })
    audio.value.addEventListener('play', () => {
      playing.value = true
    })
    audio.value.addEventListener('pause', () => {
      playing.value = false
    })
  }

  return {
    audio,
    playlist,
    currentIndex,
    playing,
    currentTime,
    duration,
    volume,
    muted,
    playMode,
    playModeLabels,
    currentSong,
    setPlaylist,
    playSong,
    addToPlaylist,
    loadAndPlay,
    togglePlay,
    playPrev,
    playNext,
    togglePlayMode,
    seekTo,
    setVolume,
    toggleMute,
    removeFromPlaylist,
    clearPlaylist,
    initAudioEvents
  }
})
