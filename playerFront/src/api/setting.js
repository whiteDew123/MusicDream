import request from './request'

// 对接 music_gateway 路由：
// /api/setting/** → Mod_setting SettingController
// 所有接口均需登录，网关解析 JWT 后通过 X-User-Id 头透传用户ID。
// 后端从 X-User-Id 头获取当前用户，URL 路径不携带 userId。

// 获取当前登录用户信息（完整资料）
// GET /api/setting/userInfo
export function getUserInfoApi() {
  return request({
    url: '/setting/userInfo',
    method: 'get'
  })
}

// 修改用户资料（email / phone / about / username）
// PUT /api/setting/userInfo
// body: { email, phone, about, username }
export function updateUserInfoApi(data) {
  return request({
    url: '/setting/userInfo',
    method: 'put',
    data
  })
}

// 修改密码
// PUT /api/setting/password
// body: { oldPassword, newPassword }
export function updatePasswordApi(data) {
  return request({
    url: '/setting/password',
    method: 'put',
    data
  })
}

// 修改头像
// POST /api/setting/avatar
// body: { imageUrl }
export function updateAvatarApi(data) {
  return request({
    url: '/setting/avatar',
    method: 'post',
    data
  })
}