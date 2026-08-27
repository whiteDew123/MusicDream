/**
 * 收藏 API（对齐 playerFront/src/api/like.js）
 * 网关路由：/api/like/** → Mod_like LikeController（均需登录）
 */
const request = require('../utils/request')

// 我喜欢的音乐列表
function likedMusicApi() {
  return request({ url: '/like/music', method: 'GET' })
}

// 添加收藏歌曲
function addLikedMusicApi(musicId) {
  return request({ url: '/like/music/' + musicId, method: 'POST' })
}

// 移除收藏歌曲
function removeLikedMusicApi(musicId) {
  return request({ url: '/like/music/' + musicId, method: 'DELETE' })
}

// 我收藏的歌单列表
function likedSongListApi() {
  return request({ url: '/like/list', method: 'GET' })
}

// 添加收藏歌单
function addLikedSongListApi(listId) {
  return request({ url: '/like/list/' + listId, method: 'POST' })
}

// 移除收藏歌单
function removeLikedSongListApi(listId) {
  return request({ url: '/like/list/' + listId, method: 'DELETE' })
}

module.exports = {
  likedMusicApi,
  addLikedMusicApi,
  removeLikedMusicApi,
  likedSongListApi,
  addLikedSongListApi,
  removeLikedSongListApi
}
