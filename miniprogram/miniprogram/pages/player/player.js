const { getPlayer } = require('../../utils/player')
const { formatDuration } = require('../../utils/format')
const { toggleCollect, addToSongList } = require('../../utils/songActions')
const { likedMusicApi } = require('../../api/like')
const { resolveUrl } = require('../../utils/url')

Page({
  data: {
    song: null,
    cover: '',
    playing: false,
    playMode: 0,
    playModeLabels: ['顺序', '单曲', '随机'],
    lyrics: [],
    currentLyricIndex: -1,
    view: 'album',
    progress: 0,
    sliderValue: 0,
    seekIng: false,
    currentTimeText: '00:00',
    durationText: '00:00',
    liked: false
  },

  onLoad() {
    this._handler = (snap) => this.onChange(snap)
    getPlayer().on(this._handler)
    this.onChange(getPlayer().getSnapshot())
  },

  onUnload() {
    if (this._handler) getPlayer().off(this._handler)
  },

  onChange(snap) {
    const duration = snap.duration || (snap.song && snap.song.timelength) || 0
    const progress = duration ? Math.min(100, (snap.currentTime / duration) * 100) : 0
    this.setData({
      song: snap.song,
      cover: snap.song ? resolveUrl(snap.song.imageUrl) : '',
      playing: snap.playing,
      playMode: snap.playMode,
      lyrics: snap.lyrics,
      currentLyricIndex: snap.currentLyricIndex,
      progress,
      sliderValue: snap.currentTime,
      currentTimeText: formatDuration(snap.currentTime),
      durationText: formatDuration(duration)
    })
  },

  // 歌词/封面切换
  onToggleView() {
    this.setData({ view: this.data.view === 'album' ? 'lyric' : 'album' })
  },

  // 进度条
  onSliderChanging(e) {
    const dur = this.data.song ? (this.data.song.timelength || 0) : 0
    const value = e.detail.value
    this.setData({
      seekIng: true,
      sliderValue: value,
      currentTimeText: formatDuration(value),
      progress: dur ? Math.min(100, (value / dur) * 100) : 0
    })
  },
  onSliderChange(e) {
    getPlayer().seekTo(e.detail.value)
    this.setData({ seekIng: false })
  },

  onModeTap() {
    getPlayer().togglePlayMode()
  },
  onPrev() {
    getPlayer().playPrev()
  },
  onTogglePlay() {
    getPlayer().togglePlay()
  },
  onNext() {
    getPlayer().playNext()
  },

  // 收藏
  onCollect() {
    if (!this.data.song) return
    toggleCollect(this.data.song).then((liked) => {
      if (typeof liked === 'boolean') this.setData({ liked })
    })
  },

  refreshLiked() {
    const song = this.data.song
    if (!song) {
      this.setData({ liked: false })
      return
    }
    likedMusicApi()
      .then((r) => {
        this.setData({ liked: (r.data || []).some((x) => x.musicId === song.musicId) })
      })
      .catch(() => {})
  },

  onComment() {
    if (!this.data.song) return
    wx.navigateTo({ url: '/pages/song-detail/song-detail?musicId=' + this.data.song.musicId })
  },
  onAddToSongList() {
    if (!this.data.song) return
    addToSongList(this.data.song)
  },
  onGoDetail() {
    if (!this.data.song) return
    wx.navigateTo({ url: '/pages/song-detail/song-detail?musicId=' + this.data.song.musicId })
  },
  onShareAppMessage() {
    const song = this.data.song
    return {
      title: song ? song.musicName + ' - ' + song.singerName : 'MusicDreamer',
      path: '/pages/discovery/discovery'
    }
  }
})
