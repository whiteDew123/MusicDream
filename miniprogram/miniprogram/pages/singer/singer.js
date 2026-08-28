const { recommendArtistsApi } = require('../../api/music')
const { decorateArtists } = require('../../utils/url')

Page({
  data: {
    loading: true,
    artists: []
  },

  onLoad() {
    this.load()
  },

  load() {
    this.setData({ loading: true })
    recommendArtistsApi(100)
      .then((r) => this.setData({ artists: decorateArtists(r.data || []) }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onArtistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/singer-detail/singer-detail?id=' + id })
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 歌手', path: '/pages/singer/singer' }
  }
})
