const { myMessagesApi, markAllReadApi } = require('../../api/msg')
const { isLogin } = require('../../utils/auth')

Page({
  data: {
    loading: true,
    msgs: []
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
    myMessagesApi()
      .then((r) => this.setData({ msgs: r.data || [] }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  onReadAll() {
    markAllReadApi()
      .then(() => {
        wx.showToast({ title: '已全部已读', icon: 'none' })
        const msgs = this.data.msgs.map((m) => ({ ...m, isread: 1 }))
        this.setData({ msgs })
      })
      .catch(() => {})
  }
})
