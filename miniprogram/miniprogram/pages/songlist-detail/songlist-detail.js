const { songListDetailApi, songListSongsApi, toggleLikeSongListApi } = require('../../api/songList')
const { getPlayer } = require('../../utils/player')
const { decorateSongs, resolveUrl } = require('../../utils/url')

Page({
  data: {
    loading: true,
    list: null,
    songs: [],
    liked: false
  },

  onLoad(options) {
    this.id = options.id
    this.load()
  },

  load() {
    if (!this.id) return
    this.setData({ loading: true })
    Promise.all([songListDetailApi(this.id), songListSongsApi(this.id)])
      .then(([d, s]) => {
        const l = d.data || {}
        l.pic = resolveUrl(l.pic)
        this.setData({ list: l, songs: decorateSongs(s.data || []), liked: l.isLike === 1 })
        wx.setNavigationBarTitle({ title: l.name || '歌单' })
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onPlayAll() {
    if (this.data.songs.length) {
      getPlayer().setPlaylist(this.data.songs, 0)
      wx.showToast({ title: '开始播放', icon: 'none' })
    } else {
      wx.showToast({ title: '歌单暂无歌曲', icon: 'none' })
    }
  },

  onToggleLike() {
    if (!this.id) return
    toggleLikeSongListApi(this.id)
      .then((r) => {
        this.setData({ liked: !!r.data })
        wx.showToast({ title: r.data ? '已收藏' : '已取消收藏', icon: 'none' })
      })
      .catch(() => {})
  },

  onShareAppMessage() {
    const l = this.data.list
    return { title: l ? l.name : 'MusicDreamer 歌单', path: '/pages/discovery/discovery' }
  }
})
