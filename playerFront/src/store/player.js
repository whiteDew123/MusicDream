import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

// 创建不经过 /api 前缀的 axios 实例，用于加载静态资源（如歌词文件）
const resourceAxios = axios.create({
  timeout: 10000
})

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

  // ===== 歌词 =====
  // 解析后的歌词数组：[{ time: 秒数, text: '歌词文本' }]
  const lyrics = ref([])
  // 当前高亮歌词行索引
  const currentLyricIndex = ref(-1)
  // 桌面字幕开关
  const subtitleEnabled = ref(false)

  // ===== 当前歌曲（计算属性）=====
  const currentSong = computed(() => {
    return currentIndex.value >= 0 ? playlist.value[currentIndex.value] : null
  })

  // ===== 歌词解析 =====
  // 解析 LRC 格式歌词，返回 [{ time: 秒数, text: '歌词' }] 按时间升序
  function parseLyrics(lrcString) {
    if (!lrcString || typeof lrcString !== 'string') return []
    const lines = lrcString.split(/\r?\n/)
    const result = []
    // 匹配 [mm:ss.xx] 或 [mm:ss] 时间标签
    const timeReg = /\[(\d{1,2}):(\d{1,2})(?:\.(\d{1,3}))?\]/g
    for (const line of lines) {
      let match
      const times = []
      while ((match = timeReg.exec(line)) !== null) {
        const min = parseInt(match[1])
        const sec = parseInt(match[2])
        const ms = match[3] ? parseInt(match[3].padEnd(3, '0')) : 0
        times.push(min * 60 + sec + ms / 1000)
      }
      // 提取歌词文本（去掉所有时间标签）
      const text = line.replace(timeReg, '').trim()
      // 跳过空行和元数据标签（ti/ar/al/by/offset 等）
      if (times.length === 0) continue
      for (const t of times) {
        result.push({ time: t, text })
      }
    }
    result.sort((a, b) => a.time - b.time)
    return result
  }

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
  async function loadAndPlay() {
    const song = currentSong.value
    if (!song || !audio.value) return
    audio.value.src = song.musicUrl || ''
    // 解析歌词（支持直接文本和文件路径两种格式）
    await loadLyrics(song.lyric)
    currentLyricIndex.value = -1
    audio.value
      .play()
      .then(() => {
        playing.value = true
      })
      .catch(() => {
        playing.value = false
      })
  }

  // 加载歌词：支持直接 LRC 文本或文件路径
  async function loadLyrics(lyricData) {
    if (!lyricData) {
      lyrics.value = []
      return
    }
    // 如果是文件路径（以 / 或 http 开头），则通过 HTTP 获取内容
    if (typeof lyricData === 'string' && (lyricData.startsWith('/') || lyricData.startsWith('http'))) {
      try {
        const res = await resourceAxios.get(lyricData, { responseType: 'text' })
        lyrics.value = parseLyrics(res.data)
      } catch (e) {
        console.warn('歌词文件加载失败:', e)
        lyrics.value = []
      }
    } else {
      // 直接是 LRC 文本
      lyrics.value = parseLyrics(lyricData)
    }
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
      // 追踪当前歌词行
      const t = currentTime.value
      let idx = -1
      for (let i = 0; i < lyrics.value.length; i++) {
        if (lyrics.value[i].time <= t) {
          idx = i
        } else {
          break
        }
      }
      currentLyricIndex.value = idx
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
    lyrics,
    currentLyricIndex,
    subtitleEnabled,
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
    initAudioEvents,
    parseLyrics
  }
})