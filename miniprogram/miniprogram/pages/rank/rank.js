const { rankSongsApi } = require('../../api/music')

Page({
  data: {
    loading: true,
    songs: []
  },

  onLoad() {
    this.load()
  },

  load() {
    this.setData({ loading: true })
    rankSongsApi(50)
      .then((r) => this.setData({ songs: r.data || [] }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 排行榜', path: '/pages/rank/rank' }
  }
})
