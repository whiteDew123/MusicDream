/**
 * 点赞 / 互动统计 API（对齐 playerFront/src/api/interaction.js）
 * 网关路由：/api/like/** → Mod_like, /api/comment/** → Mod_msg, /api/share/** → Mod_msg
 */
const request = require('../utils/request')

// ===== 统一统计（一次请求拿全部交互数据）=====
// GET /like/song/{musicId}/stats
function getMusicStatsApi(musicId) {
  return request({ url: '/like/song/' + musicId + '/stats', method: 'GET' })
}

// ===== 点赞 =====
// POST /like/song/{musicId}  toggle
function toggleLikeApi(musicId) {
  return request({ url: '/like/song/' + musicId, method: 'POST' })
}

// GET /like/song/{musicId}/status
function getLikeStatusApi(musicId) {
  return request({ url: '/like/song/' + musicId + '/status', method: 'GET' })
}

// ===== 评论 =====
// GET /comment/music/{musicId}?pn=&size=
function commentListApi(musicId, pn = 1, size = 20) {
  return request({ url: '/comment/music/' + musicId, method: 'GET', data: { pn, size } })
}

// POST /comment/music/{musicId}
function createCommentApi(musicId, data) {
  return request({ url: '/comment/music/' + musicId, method: 'POST', data })
}

// DELETE /comment/{id}
function deleteCommentApi(id) {
  return request({ url: '/comment/' + id, method: 'DELETE' })
}

// GET /comment/music/{musicId}/count
function commentCountApi(musicId) {
  return request({ url: '/comment/music/' + musicId + '/count', method: 'GET' })
}

// ===== 分享 =====
// POST /share/music/{musicId}?channel=
function shareSongApi(musicId, channel = 'wechat') {
  return request({ url: '/share/music/' + musicId, method: 'POST', data: { channel } })
}

// GET /share/music/{musicId}/count
function shareCountApi(musicId) {
  return request({ url: '/share/music/' + musicId + '/count', method: 'GET' })
}

module.exports = {
  getMusicStatsApi,
  toggleLikeApi,
  getLikeStatusApi,
  commentListApi,
  createCommentApi,
  deleteCommentApi,
  commentCountApi,
  shareSongApi,
  shareCountApi
}
