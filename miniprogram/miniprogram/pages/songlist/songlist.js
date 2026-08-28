const { publicSongListApi } = require('../../api/songList')
const { decorateLists } = require('../../utils/url')

Page({
  data: {
    loading: true,
    songLists: []
  },

  onShow() {
    this.load()
  },

  load() {
    this.setData({ loading: true })
    publicSongListApi()
      .then((r) => this.setData({ songLists: decorateLists(r.data || []) }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onPlaylistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/songlist-detail/songlist-detail?id=' + id })
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 歌单', path: '/pages/songlist/songlist' }
  }
})
