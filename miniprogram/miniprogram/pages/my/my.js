const { getUserInfo, isLogin, clearAuth } = require('../../utils/auth')
const { unreadCountApi } = require('../../api/msg')
const { resolveUrl } = require('../../utils/url')

Page({
  data: {
    login: false,
    userInfo: null,
    avatar: '',
    unread: 0
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    const login = isLogin()
    const u = login ? getUserInfo() : null
    this.setData({ login, userInfo: u, avatar: u ? resolveUrl(u.imageUrl) : '' })
    if (login) this.loadUnread()
  },

  loadUnread() {
    unreadCountApi()
      .then((r) => this.setData({ unread: (r.data && r.data.count) || r.data || 0 }))
      .catch(() => {})
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },
  goLiked() {
    wx.navigateTo({ url: '/pages/my/liked/liked' })
  },
  goFavorite() {
    wx.navigateTo({ url: '/pages/my/favorite/favorite' })
  },
  goCreated() {
    wx.navigateTo({ url: '/pages/my/created/created' })
  },
  goNotify() {
    wx.navigateTo({ url: '/pages/notify/notify' })
  },
  goSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' })
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (r) => {
        if (r.confirm) {
          clearAuth()
          this.refresh()
          wx.showToast({ title: '已退出', icon: 'none' })
        }
      }
    })
  },

  onShareAppMessage() {
    return { title: 'MusicDreamer · 我的', path: '/pages/my/my' }
  }
})
