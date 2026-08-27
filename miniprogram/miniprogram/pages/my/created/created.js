const { myCreatedSongListApi, createSongListApi } = require('../../api/songList')
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
    myCreatedSongListApi()
      .then((r) => this.setData({ lists: decorateLists(r.data || []) }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onCreate() {
    wx.showModal({
      title: '新建歌单',
      editable: true,
      placeholderText: '请输入歌单名称',
      success: (r) => {
        if (r.confirm && r.content) {
          const name = (r.content || '').trim()
          if (!name) return
          createSongListApi({ name })
            .then(() => {
              wx.showToast({ title: '创建成功', icon: 'none' })
              this.load()
            })
            .catch(() => {})
        }
      }
    })
  },

  onPlaylistTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/songlist-detail/songlist-detail?id=' + id })
  }
})
