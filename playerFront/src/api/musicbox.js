import request from '@/api/request'

// ======================== 创建盲盒 ========================

/**
 * 创建盲盒
 */
export function createMusicBoxApi(data) {
  return request.post('/musicbox/create', data)
}

// ======================== 盲盒广场 ========================

/**
 * 盲盒广场列表（最新）
 */
export function getPlazaListApi(params) {
  return request.get('/musicbox/plaza', { params })
}

/**
 * 按标签筛选盲盒广场
 */
export function getPlazaListByTagApi(params) {
  return request.get('/musicbox/plaza/tag', { params })
}

/**
 * 热门排行
 */
export function getHotBoxesApi(params) {
  return request.get('/musicbox/plaza/hot', { params })
}

/**
 * 随机推荐
 */
export function getRandomBoxesApi(params) {
  return request.get('/musicbox/plaza/random', { params })
}

// ======================== 盲盒互动 ========================

/**
 * 获取盲盒详情（不记录开启次数）
 */
export function getBoxDetailApi(boxId) {
  return request.get(`/musicbox/${boxId}`)
}

/**
 * 开启盲盒
 */
export function openBoxApi(boxId) {
  return request.post(`/musicbox/${boxId}/open`)
}

/**
 * 点赞/取消点赞盲盒
 */
export function toggleLikeApi(boxId) {
  return request.post(`/musicbox/${boxId}/like`)
}

// ======================== 我的盲盒 ========================

/**
 * 我创建的盲盒
 */
export function getMyBoxesApi() {
  return request.get('/musicbox/my')
}

/**
 * 我开启过的盲盒
 */
export function getOpenedBoxesApi() {
  return request.get('/musicbox/opened')
}

/**
 * 我点赞过的盲盒
 */
export function getLikedBoxesApi() {
  return request.get('/musicbox/liked')
}

/**
 * 删除盲盒
 */
export function deleteBoxApi(boxId) {
  return request.delete(`/musicbox/${boxId}`)
}

// ======================== 盲盒交友 ========================

/**
 * 发送盲盒交友请求
 */
export function sendBoxFriendRequestApi(boxId, data) {
  return request.post(`/musicbox/${boxId}/friend-request`, data)
}

/**
 * 查询收到的盲盒交友请求
 */
export function getReceivedBoxRequestsApi() {
  return request.get('/musicbox/friend-request/received')
}

/**
 * 接受盲盒交友请求
 */
export function acceptBoxRequestApi(requestId) {
  return request.put(`/musicbox/friend-request/accept/${requestId}`)
}

/**
 * 拒绝盲盒交友请求
 */
export function rejectBoxRequestApi(requestId) {
  return request.put(`/musicbox/friend-request/reject/${requestId}`)
}