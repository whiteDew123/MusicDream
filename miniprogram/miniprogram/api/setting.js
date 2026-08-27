/**
 * 用户设置 API（对齐 playerFront/src/api/setting.js）
 * 网关路由：/api/setting/** → Mod_setting SettingController（需登录）
 */
const request = require('../utils/request')

// 修改用户信息
function updateUserInfoApi(userId, data) {
  return request({ url: '/setting/' + userId, method: 'PUT', data })
}

// 修改密码
function updatePasswordApi(userId, data) {
  return request({ url: '/setting/' + userId + '/password', method: 'PUT', data })
}

// 修改头像
function updateAvatarApi(userId, data) {
  return request({ url: '/setting/' + userId + '/avatar', method: 'POST', data })
}

module.exports = {
  updateUserInfoApi,
  updatePasswordApi,
  updateAvatarApi
}
