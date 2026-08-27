const { getUserInfo, isLogin, setUserInfo } = require('../../utils/auth')
const { updateUserInfoApi, updatePasswordApi, updateAvatarApi } = require('../../api/setting')
const { uploadImage } = require('../../utils/upload')
const { resolveUrl } = require('../../utils/url')

Page({
  data: {
    userInfo: null,
    avatar: '',
    username: '',
    email: '',
    phone: '',
    about: '',
    oldPassword: '',
    newPassword: '',
    saving: false,
    savingPwd: false
  },

  onShow() {
    if (!isLogin()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.init()
  },

  init() {
    const u = getUserInfo()
    this.setData({
      userInfo: u,
      avatar: u ? resolveUrl(u.imageUrl) : '',
      username: (u && u.username) || '',
      email: (u && u.email) || '',
      phone: (u && u.phone) || '',
      about: (u && u.about) || ''
    })
  },
  onField(e) {
    this.setData({ [e.currentTarget.dataset.field]: e.detail.value })
  },

  onSaveInfo() {
    const { userInfo, username, email, phone, about } = this.data
    if (!userInfo) return
    this.setData({ saving: true })
    updateUserInfoApi(userInfo.userId, { username, email, phone, about })
      .then(() => {
        setUserInfo({ ...userInfo, username })
        this.setData({ userInfo: { ...userInfo, username } })
        wx.showToast({ title: '已保存', icon: 'none' })
      })
      .catch(() => {})
      .finally(() => this.setData({ saving: false }))
  },

  onChangeAvatar() {
    if (!this.data.userInfo) return
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (r) => {
        wx.showLoading({ title: '上传中' })
        uploadImage(r.tempFilePaths[0])
          .then((url) => {
            this._avatarUrl = url
            return updateAvatarApi(this.data.userInfo.userId, { imageUrl: url })
          })
          .then(() => {
            const u = { ...this.data.userInfo, imageUrl: this._avatarUrl }
            setUserInfo(u)
            this.setData({ userInfo: u })
            wx.hideLoading()
            wx.showToast({ title: '头像已更新', icon: 'none' })
          })
          .catch(() => wx.hideLoading())
      }
    })
  },

  onSavePassword() {
    const { userInfo, oldPassword, newPassword } = this.data
    if (!userInfo) return
    if (!oldPassword || !newPassword) {
      wx.showToast({ title: '请填写新旧密码', icon: 'none' })
      return
    }
    this.setData({ savingPwd: true })
    updatePasswordApi(userInfo.userId, { oldPassword, newPassword })
      .then(() => {
        wx.showToast({ title: '密码已修改', icon: 'none' })
        this.setData({ oldPassword: '', newPassword: '' })
      })
      .catch(() => {})
      .finally(() => this.setData({ savingPwd: false }))
  }
})
