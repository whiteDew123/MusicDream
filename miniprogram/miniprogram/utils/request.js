/**
 * 网络层封装（唯一出口）
 * - 所有业务 API 必须经由本文件，禁止页面内直接 wx.request
 * - baseURL 取 config.BASE_URL，自动注入 Authorization: Bearer <token>
 * - 统一处理 Result{code, message, data}：code===200 时 resolve 整个 Result 对象（与 Web 端一致，页面用 res.data）
 * - code===401 时清登录态并跳转登录页
 * - silent: true 可静默失败（不弹出 toast）
 */
const { BASE_URL, REQUEST_TIMEOUT } = require('../config/index')
const { getToken, clearAuth } = require('./auth')

let redirectingLogin = false

function toLogin() {
  if (redirectingLogin) return
  redirectingLogin = true
  clearAuth()
  wx.navigateTo({
    url: '/pages/login/login?redirect=1',
    complete: () => {
      setTimeout(() => { redirectingLogin = false }, 1000)
    }
  })
}

function request(options) {
  const {
    url,
    method = 'GET',
    data,
    header = {},
    silent = false,
    timeout = REQUEST_TIMEOUT
  } = options

  const token = getToken()

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      timeout,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      success: (res) => {
        const body = res.data

        // HTTP 层错误
        if (res.statusCode !== 200) {
          if (res.statusCode === 401) {
            toLogin()
          } else if (!silent && res.statusCode !== 404) {
            wx.showToast({ title: (body && body.message) || '网络异常', icon: 'none' })
          }
          return reject(new Error((body && body.message) || 'HTTP ' + res.statusCode))
        }

        // 后端统一 Result 结构
        if (body && typeof body === 'object' && typeof body.code !== 'undefined') {
          if (body.code === 200) {
            return resolve(body)
          }
          if (body.code === 401) {
            toLogin()
          } else if (!silent) {
            wx.showToast({ title: body.message || '请求失败', icon: 'none' })
          }
          return reject(new Error(body.message || 'Error'))
        }

        // 未包 Result，直接返回
        return resolve(body)
      },
      fail: (err) => {
        if (!silent) {
          wx.showToast({ title: '网络异常', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

module.exports = request
