import request from './request'

// 对接 music_gateway 路由：
// /api/like/** → Mod_like LikeController
// 所有接口均需登录

// ===== 收藏歌曲 =====

// 我喜欢的音乐列表
// GET /api/like/music
export function likedMusicApi() {
  return request({
    url: '/like/music',
    method: 'get'
  })
}

// 添加收藏歌曲
// POST /api/like/music/{musicId}
export function addLikedMusicApi(musicId) {
  return request({
    url: `/like/music/${musicId}`,
    method: 'post'
  })
}

// 移除收藏歌曲
// DELETE /api/like/music/{musicId}
export function removeLikedMusicApi(musicId) {
  return request({
    url: `/like/music/${musicId}`,
    method: 'delete'
  })
}

// ===== 收藏歌单 =====

// 我收藏的歌单列表
// GET /api/like/list
export function likedSongListApi() {
  return request({
    url: '/like/list',
    method: 'get'
  })
}

// 添加收藏歌单
// POST /api/like/list/{listId}
export function addLikedSongListApi(listId) {
  return request({
    url: `/like/list/${listId}`,
    method: 'post'
  })
}

// 移除收藏歌单
// DELETE /api/like/list/{listId}
export function removeLikedSongListApi(listId) {
  return request({
    url: `/like/list/${listId}`,
    method: 'delete'
  })
}
