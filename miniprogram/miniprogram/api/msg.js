/**
 * 消息 API（对齐 playerFront/src/api/msg.js）
 * 网关路由：/api/msg/** → Mod_msg MsgController（查询需登录）
 */
const request = require('../utils/request')

// 我的消息列表
function myMessagesApi() {
  return request({ url: '/msg/my', method: 'GET' })
}

// 未读消息数
function unreadCountApi() {
  return request({ url: '/msg/unread-count', method: 'GET' })
}

// 标记单条已读
function markReadApi(id) {
  return request({ url: '/msg/read/' + id, method: 'PUT' })
}

// 全部标记已读
function markAllReadApi() {
  return request({ url: '/msg/read-all', method: 'PUT' })
}

module.exports = {
  myMessagesApi,
  unreadCountApi,
  markReadApi,
  markAllReadApi
}
