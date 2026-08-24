import request from './request'

// 对接 music_gateway 路由：
// /api/setting/** → Mod_setting SettingController
// 所有接口均需登录

// 修改用户信息
// PUT /api/setting/{userId}
// body: { username, email, phone, about }
export function updateUserInfoApi(userId, data) {
  return request({
    url: `/setting/${userId}`,
    method: 'put',
    data
  })
}

// 修改密码
// PUT /api/setting/{userId}/password
// body: { oldPassword, newPassword }
export function updatePasswordApi(userId, data) {
  return request({
    url: `/setting/${userId}/password`,
    method: 'put',
    data
  })
}

// 修改头像
// POST /api/setting/{userId}/avatar
// body: { imageUrl }
export function updateAvatarApi(userId, data) {
  return request({
    url: `/setting/${userId}/avatar`,
    method: 'post',
    data
  })
}
