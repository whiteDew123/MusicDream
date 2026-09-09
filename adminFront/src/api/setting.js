import request from './request'

// 设置中心模块 API
// 对接后端 Mod_setting → 网关路由 /api/setting/**

// 获取当前用户信息（带 imageUrl / email / phone / about）
// GET /api/setting/userInfo
export function getUserInfo() {
  return request({
    url: '/setting/userInfo',
    method: 'get'
  })
}

// 更新用户信息（username / email / phone / about）
// PUT /api/setting/userInfo
export function updateUserInfo(userId, data) {
  return request({
    url: '/setting/userInfo',
    method: 'put',
    data: { userId, ...data }
  })
}

// 更新用户头像
// POST /api/setting/avatar
// data: { imageUrl: '上传后返回的url' }
export function updateAvatar(userId, data) {
  return request({
    url: '/setting/avatar',
    method: 'post',
    data: { userId, ...data }
  })
}

// 修改密码
// PUT /api/setting/password
export function updatePassword(userId, { oldPassword, newPassword }) {
  return request({
    url: '/setting/password',
    method: 'put',
    data: { userId, oldPassword, newPassword }
  })
}