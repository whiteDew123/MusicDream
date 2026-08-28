/**
 * 听歌识曲 API（对齐 playerFront/src/api/recognize.js）— 二期功能，仅接口预置
 * 网关路由：POST /api/recognize（multipart/form-data，字段 file）
 * 注意：小程序 RecorderManager 录制 mp3/aac，需与后端确认格式兼容（Web 端传的是 WAV）。
 */
const { BASE_URL, UPLOAD_TIMEOUT } = require('../config/index')
const { getToken } = require('../utils/auth')

// 上传录音文件进行识曲
function recognizeApi(filePath) {
  const token = getToken()
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + '/recognize',
      filePath,
      name: 'file',
      timeout: UPLOAD_TIMEOUT,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) resolve(body)
          else {
            wx.showToast({ title: body.message || '识别失败', icon: 'none' })
            reject(new Error(body.message || 'Error'))
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        wx.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = { recognizeApi }
