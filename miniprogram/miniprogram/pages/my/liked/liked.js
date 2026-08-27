const { likedMusicApi } = require('../../api/like')
const { isLogin } = require('../../utils/auth')

Page({
  data: {
    loading: true,
    songs: []
  },

  onShow() {
    if (!isLogin()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.load()
  },

  load() {
    this.setData({ loading: true })
    likedMusicApi()
      .then((r) => this.setData({ songs: r.data || [] }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  }
})
