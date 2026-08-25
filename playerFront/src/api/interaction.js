import request from './request'

// 播放器交互 API：点赞、评论、分享
// 对应后端：Mod_like / Mod_msg
// 网关路由：/api/like/** → Mod_like, /api/comment/** → Mod_msg, /api/share/** → Mod_msg

// ===== 统一统计接口（AAA 方案：一次请求获取全部交互数据）=====

// 统一查询歌曲交互统计：点赞状态 + 点赞数 + 评论数 + 分享数
// GET /api/like/song/{musicId}/stats
export function getMusicStatsApi(musicId) {
  return request({
    url: `/like/song/${musicId}/stats`,
    method: 'get'
  })
}

// ===== 点赞接口 =====

// 点赞/取消点赞（切换状态）
// POST /api/like/song/{musicId}
export function toggleLikeApi(musicId) {
  return request({
    url: `/like/song/${musicId}`,
    method: 'post'
  })
}

// 查询点赞状态 + 点赞数
// GET /api/like/song/{musicId}/status
export function getLikeStatusApi(musicId) {
  return request({
    url: `/like/song/${musicId}/status`,
    method: 'get'
  })
}

// ===== 评论接口 =====

// 分页查询歌曲评论列表
// GET /api/comment/music/{musicId}?pn=1&size=20
export function commentListApi(musicId, pn = 1, size = 20) {
  return request({
    url: `/comment/music/${musicId}`,
    method: 'get',
    params: { pn, size }
  })
}

// 发表评论
// POST /api/comment/music/{musicId}
export function createCommentApi(musicId, data) {
  return request({
    url: `/comment/music/${musicId}`,
    method: 'post',
    data
  })
}

// 删除评论
// DELETE /api/comment/{id}
export function deleteCommentApi(id) {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

// 查询评论数
// GET /api/comment/music/{musicId}/count
export function commentCountApi(musicId) {
  return request({
    url: `/comment/music/${musicId}/count`,
    method: 'get'
  })
}

// ===== 分享接口 =====

// 记录分享行为（返回分享链接 + 最新分享数）
// POST /api/share/music/{musicId}?channel=link
export function shareSongApi(musicId, channel = 'link') {
  return request({
    url: `/share/music/${musicId}`,
    method: 'post',
    params: { channel }
  })
}

// 查询歌曲分享数
// GET /api/share/music/{musicId}/count
export function shareCountApi(musicId) {
  return request({
    url: `/share/music/${musicId}/count`,
    method: 'get'
  })
}