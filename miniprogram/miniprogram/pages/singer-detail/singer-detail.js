const { artistDetailApi } = require('../../api/music')
const { decorateSongs, resolveUrl } = require('../../utils/url')

Page({
  data: {
    loading: true,
    artist: null,
    songs: []
  },

  onLoad(options) {
    this.artistId = options.id
    this.load()
  },

  load() {
    if (!this.artistId) return
    this.setData({ loading: true })
    artistDetailApi(this.artistId)
      .then((r) => {
        const a = r.data || {}
        a.imageUrl = resolveUrl(a.imageUrl)
        this.setData({ artist: a, songs: decorateSongs(a.songs || []) })
        wx.setNavigationBarTitle({ title: a.username || '歌手详情' })
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onShareAppMessage() {
    const a = this.data.artist
    return {
      title: a ? a.username + ' 的歌曲' : 'MusicDreamer',
      path: '/pages/discovery/discovery'
    }
  }
})
