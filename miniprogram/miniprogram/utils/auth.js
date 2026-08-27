/**
 * 登录态本地存储工具
 * 使用 wx storage 持久化 JWT 与用户信息，与 Web 端 playerFront 的 auth.js 语义对齐。
 */
const { TOKEN_KEY, USER_KEY } = require('../config/index')

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

function getUserInfo() {
  const raw = wx.getStorageSync(USER_KEY)
  return raw ? raw : null
}

function setUserInfo(user) {
  wx.setStorageSync(USER_KEY, user)
}

// 清空登录态
function clearAuth() {
  wx.removeStorageSync(TOKEN_KEY)
  wx.removeStorageSync(USER_KEY)
}

// 是否已登录
function isLogin() {
  return !!getToken()
}

module.exports = {
  getToken,
  setToken,
  getUserInfo,
  setUserInfo,
  clearAuth,
  isLogin
}
