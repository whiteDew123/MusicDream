/**
 * 播放器全局单例（核心）
 * - 基于 wx.createInnerAudioContext() 的全局唯一播放实例，供所有页面共用
 * - 维护播放列表、当前歌曲、播放进度、音量、播放模式、歌词
 * - 通过简单事件订阅（on/off）让页面与 MiniPlayer 实时刷新
 * - 对齐 Web 端 playerFront/src/store/player.js 的语义：parseLyrics、三模式、切换逻辑
 */
const { PLAY_MODE, DEFAULT_VOLUME } = require('../config/index')
const { resolveUrl } = require('./url')

class Player {
  constructor() {
    this.audio = wx.createInnerAudioContext()
    this.audio.obeyMuteSwitch = false
    this.audio.autoplay = false
    this.audio.preload = 'auto'

    this.playlist = []
    this.currentIndex = -1
    this.playing = false
    this.currentTime = 0
    this.duration = 0
    this.volume = DEFAULT_VOLUME
    this.muted = false
    this.playMode = PLAY_MODE.SEQUENCE
    this.lyrics = []
    this.currentLyricIndex = -1

    this._listeners = []

    if (this.audio) {
      this.audio.volume = this.volume
      this._bindEvents()
    }
  }

  // ===== 订阅 =====
  on(cb) {
    if (typeof cb === 'function' && this._listeners.indexOf(cb) === -1) {
      this._listeners.push(cb)
    }
  }
  off(cb) {
    const i = this._listeners.indexOf(cb)
    if (i > -1) this._listeners.splice(i, 1)
  }
  _notify() {
    const snapshot = this.getSnapshot()
    this._listeners.forEach((cb) => cb(snapshot))
  }

  getSnapshot() {
    return {
      song: this.currentSong,
      playlist: this.playlist,
      currentIndex: this.currentIndex,
      playing: this.playing,
      currentTime: this.currentTime,
      duration: this.duration,
      volume: this.volume,
      muted: this.muted,
      playMode: this.playMode,
      lyrics: this.lyrics,
      currentLyricIndex: this.currentLyricIndex
    }
  }

  get currentSong() {
    return this.currentIndex >= 0 ? this.playlist[this.currentIndex] : null
  }

  // ===== 歌词解析（移植 Web）=====
  parseLyrics(lrcString) {
    if (!lrcString || typeof lrcString !== 'string') return []
    const lines = lrcString.split(/\r?\n/)
    const result = []
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
      const text = line.replace(timeReg, '').trim()
      if (times.length === 0) continue
      for (const t of times) {
        result.push({ time: t, text })
      }
    }
    result.sort((a, b) => a.time - b.time)
    return result
  }

  // ===== 播放控制 =====
  addToPlaylist(song) {
    const idx = this.playlist.findIndex((s) => s.musicId === song.musicId)
    if (idx < 0) this.playlist.push(song)
  }

  // 播单首（已存在则跳转到该歌）
  playSong(song) {
    if (!song) return
    const idx = this.playlist.findIndex((s) => s.musicId === song.musicId)
    if (idx >= 0) {
      this.currentIndex = idx
    } else {
      this.playlist.push(song)
      this.currentIndex = this.playlist.length - 1
    }
    this.loadAndPlay()
  }

  setPlaylist(list, index = 0) {
    this.playlist = list || []
    this.currentIndex = index >= 0 && index < this.playlist.length ? index : 0
    this.loadAndPlay()
  }

  async loadAndPlay() {
    const song = this.currentSong
    if (!song || !this.audio) return
    this.audio.src = resolveUrl(song.musicUrl) || ''
    await this.loadLyrics(song.lyric)
    this.currentLyricIndex = -1
    this._safePlay()
    this._notify()
  }

  loadLyrics(lyricData) {
    this.lyrics = []
    if (!lyricData) return Promise.resolve()
    // 仅当 lyric 是"路径/URL"时才归一化并拉取；否则视为 LRC 文本直接解析
    const isPath =
      typeof lyricData === 'string' &&
      (lyricData.charAt(0) === '/' || /^https?:\/\//.test(lyricData))
    if (isPath) {
      const resolved = resolveUrl(lyricData)
      return new Promise((resolve) => {
        wx.request({
          url: resolved,
          success: (res) => {
            this.lyrics = this.parseLyrics(res.data)
            this._notify()
            resolve()
          },
          fail: () => resolve()
        })
      })
    }
    this.lyrics = this.parseLyrics(lyricData)
    this._notify()
    return Promise.resolve()
  }

  togglePlay() {
    const song = this.currentSong
    if (!this.audio || !song) return
    if (this.playing) {
      this.audio.pause()
    } else {
      this._safePlay()
    }
  }

  playPrev() {
    if (this.playlist.length === 0) return
    if (this.playMode === PLAY_MODE.RANDOM) {
      this.currentIndex = Math.floor(Math.random() * this.playlist.length)
    } else {
      this.currentIndex = (this.currentIndex - 1 + this.playlist.length) % this.playlist.length
    }
    this.loadAndPlay()
  }

  playNext() {
    if (this.playlist.length === 0) return
    if (this.playMode === PLAY_MODE.RANDOM) {
      this.currentIndex = Math.floor(Math.random() * this.playlist.length)
    } else {
      this.currentIndex = (this.currentIndex + 1) % this.playlist.length
    }
    this.loadAndPlay()
  }

  // 点击指定索引播放
  playAt(index) {
    if (!this.playlist[index]) return
    this.currentIndex = index
    this.loadAndPlay()
  }

  togglePlayMode() {
    this.playMode = (this.playMode + 1) % 3
    this._notify()
  }

  seekTo(time) {
    if (this.audio) {
      this.audio.seek(time)
      this.currentTime = time
      this._notify()
    }
  }

  setVolume(vol) {
    this.volume = vol
    if (this.audio) this.audio.volume = vol
    this.muted = vol === 0
    this._notify()
  }

  toggleMute() {
    this.muted = !this.muted
    if (this.audio) this.audio.muted = this.muted
    this._notify()
  }

  // 当前播放索引（列表里是否高亮）
  isCurrent(musicId) {
    const song = this.currentSong
    return !!song && song.musicId === musicId
  }

  clearPlaylist() {
    this.playlist = []
    this.currentIndex = -1
    this.playing = false
    this.lyrics = []
    if (this.audio) {
      this.audio.stop()
      this.audio.src = ''
    }
    this._notify()
  }

  // ===== 音频事件 =====
  _safePlay() {
    const p = this.audio && this.audio.play()
    if (p && typeof p.catch === 'function') {
      p.catch(() => {})
    }
  }

  _bindEvents() {
    this.audio.onTimeUpdate(() => {
      this.currentTime = this.audio.currentTime || 0
      this._trackLyric()
      this._notify()
    })
    this.audio.onCanplay(() => {
      this.duration = this.audio.duration || 0
      this._notify()
    })
    this.audio.onPlay(() => {
      this.playing = true
      this._notify()
    })
    this.audio.onPause(() => {
      this.playing = false
      this._notify()
    })
    this.audio.onEnded(() => {
      if (this.playMode === PLAY_MODE.SINGLE) {
        this.audio.seek(0)
        this._safePlay()
      } else {
        this.playNext()
      }
    })
    this.audio.onError(() => {
      this.playing = false
      this._notify()
    })
  }

  _trackLyric() {
    const t = this.currentTime
    let idx = -1
    for (let i = 0; i < this.lyrics.length; i++) {
      if (this.lyrics[i].time <= t) idx = i
      else break
    }
    this.currentLyricIndex = idx
  }
}

// 单例（挂到全局，页面用 getApp().player 访问）
let _instance = null
function getPlayer() {
  if (!_instance) {
    _instance = new Player()
  }
  return _instance
}

module.exports = { Player, getPlayer }
