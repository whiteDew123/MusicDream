/**
 * 图片上传工具（头像等）
 * - 走网关 /api/upload/image，multipart file
 * - 返回上传后的 fileUrl（相对路径，前端加域名前缀或直接使用）
 */
const { BASE_URL, UPLOAD_TIMEOUT } = require('../config/index')
const { getToken } = require('./auth')

function uploadImage(filePath) {
  const token = getToken()
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + '/upload/image',
      filePath,
      name: 'file',
      timeout: UPLOAD_TIMEOUT,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            resolve(body.data.fileUrl)
          } else {
            wx.showToast({ title: body.message || '上传失败', icon: 'none' })
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

module.exports = { uploadImage }
