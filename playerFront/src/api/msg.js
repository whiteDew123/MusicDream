import request from './request'

// 对接 music_gateway 路由：
// /api/msg/** → Mod_msg MsgController
// 查询类接口需登录

// 我的消息列表
// GET /api/msg/my
export function myMessagesApi() {
  return request({
    url: '/msg/my',
    method: 'get'
  })
}

// 未读消息数
// GET /api/msg/unread-count
export function unreadCountApi() {
  return request({
    url: '/msg/unread-count',
    method: 'get'
  })
}

// 标记单条消息为已读
// PUT /api/msg/read/{id}
export function markReadApi(id) {
  return request({
    url: `/msg/read/${id}`,
    method: 'put'
  })
}

// 全部标记为已读
// PUT /api/msg/read-all
export function markAllReadApi() {
  return request({
    url: '/msg/read-all',
    method: 'put'
  })
}
