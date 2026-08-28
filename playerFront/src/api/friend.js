import request from './request'

// 对接 music_gateway 路由：
// /api/friend/** → Mod_friend FriendController
// 网关 StripPrefix=1 会去掉 /api 前缀，baseURL 已含 /api
// 所有接口需登录（网关鉴权后经 X-User-Id 头透传用户）

// ===== 搜索用户 =====

// 搜索用户（排除已好友）
// GET /api/friend/search?keyword=xxx
export function searchUsersApi(keyword) {
  return request({ url: '/friend/search', method: 'get', params: { keyword } })
}

// ===== 好友请求 =====

// 发送好友请求
// POST /api/friend/request  body: { receiverId, message }
export function sendFriendRequestApi(data) {
  return request({ url: '/friend/request', method: 'post', data })
}

// 获取收到的好友请求
// GET /api/friend/request/received
export function getReceivedRequestsApi() {
  return request({ url: '/friend/request/received', method: 'get' })
}

// 获取发送的好友请求
// GET /api/friend/request/sent
export function getSentRequestsApi() {
  return request({ url: '/friend/request/sent', method: 'get' })
}

// 接受好友请求
// PUT /api/friend/request/accept/{id}
export function acceptFriendRequestApi(id) {
  return request({ url: `/friend/request/accept/${id}`, method: 'put' })
}

// 拒绝好友请求
// PUT /api/friend/request/reject/{id}
export function rejectFriendRequestApi(id) {
  return request({ url: `/friend/request/reject/${id}`, method: 'put' })
}

// ===== 好友管理 =====

// 获取好友列表
// GET /api/friend/list
export function getFriendListApi() {
  return request({ url: '/friend/list', method: 'get' })
}

// 删除好友
// DELETE /api/friend/{friendId}
export function deleteFriendApi(friendId) {
  return request({ url: `/friend/${friendId}`, method: 'delete' })
}