const { likedSongListApi } = require('../../api/like')
const { isLogin } = require('../../utils/auth')
const { decorateLists } = require('../../utils/url')

Page({
  data: {
    loading: true,
    lists: []
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
    likedSongListApi()
      .then((r) => this.setData({ lists: decorateLists(r.data || []) }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onPlaylistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/songlist-detail/songlist-detail?id=' + id })
  }
})
