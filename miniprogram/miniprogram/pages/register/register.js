const { registerApi, sendEmailCodeApi } = require('../../api/auth')

Page({
  data: {
    username: '',
    email: '',
    password: '',
    emailCode: '',
    loading: false,
    sending: false,
    countdown: 0
  },

  onSendCode() {
    const { email } = this.data
    if (!email) {
      wx.showToast({ title: '请输入邮箱', icon: 'none' })
      return
    }
    if (this.data.countdown > 0) return
    this.setData({ sending: true })
    sendEmailCodeApi(email)
      .then(() => {
        this.setData({ sending: false, countdown: 60 })
        wx.showToast({ title: '验证码已发送', icon: 'none' })
        this.startCountdown()
      })
      .catch(() => this.setData({ sending: false }))
  },

  startCountdown() {
    this._timer = setInterval(() => {
      const c = this.data.countdown - 1
      if (c <= 0) {
        clearInterval(this._timer)
        this.setData({ countdown: 0 })
      } else {
        this.setData({ countdown: c })
      }
    }, 1000)
  },

  onRegister() {
    const { username, email, password, emailCode } = this.data
    if (!username || !email || !password || !emailCode) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    registerApi({ username, password, email, emailCode })
      .then(() => {
        this.setData({ loading: false })
        wx.showToast({ title: '注册成功，请登录', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 800)
      })
      .catch(() => this.setData({ loading: false }))
  },

  onField(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
  },

  onUnload() {
    if (this._timer) clearInterval(this._timer)
  }
})
