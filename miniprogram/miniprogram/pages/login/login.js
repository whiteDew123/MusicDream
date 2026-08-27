const { wxLoginApi, loginApi } = require('../../api/auth')
const { setToken, setUserInfo, isLogin } = require('../../utils/auth')

Page({
  data: {
    account: '',
    password: '',
    loading: false
  },

  onLoad(options) {
    this.redirect = (options && options.redirect) || ''
    if (isLogin()) this.toHome()
  },

  // 微信一键登录
  onWxLogin() {
    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          return
        }
        wx.showLoading({ title: '登录中' })
        wxLoginApi({ code: res.code })
          .then((r) => {
            wx.hideLoading()
            this.applyLogin(r.data)
          })
          .catch(() => {
            wx.hideLoading()
            wx.showToast({ title: '微信登录暂不可用，请用账号密码登录', icon: 'none' })
          })
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  },

  // 账号密码登录
  onLogin() {
    const { account, password } = this.data
    if (!account || !password) {
      wx.showToast({ title: '请输入账号和密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    loginApi({ account, password })
      .then((r) => {
        this.applyLogin(r.data)
      })
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },

  applyLogin(data) {
    if (!data) {
      wx.showToast({ title: '登录失败', icon: 'none' })
      return
    }
    setToken(data.token)
    setUserInfo({
      userId: data.userId,
      username: data.username,
      role: data.role,
      imageUrl: data.imageUrl
    })
    wx.showToast({ title: '登录成功', icon: 'success' })
    this.toHome()
  },

  toHome() {
    wx.switchTab({ url: '/pages/discovery/discovery' })
  },

  onAccount(e) {
    this.setData({ account: e.detail.value })
  },
  onPassword(e) {
    this.setData({ password: e.detail.value })
  },
  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
