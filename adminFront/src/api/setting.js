import request from './request'

// 设置页 API
// 对接后端 Mod_setting

// 修改个人信息
// PUT /api/setting/{userId}
export function updateUserInfo(userId, data) {
  return request({
    url: `/setting/${userId}`,
    method: 'put',
    data
  })
}

// 修改密码
// PUT /api/setting/{userId}/password
export function updatePassword(userId, data) {
  return request({
    url: `/setting/${userId}/password`,
    method: 'put',
    data
  })
}

// 修改头像
// POST /api/setting/{userId}/avatar
export function updateAvatar(userId, data) {
  return request({
    url: `/setting/${userId}/avatar`,
    method: 'post',
    data
  })
}