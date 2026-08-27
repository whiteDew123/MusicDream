/**
 * 歌单 API（对齐 playerFront/src/api/songList.js）
 * 网关路由：/api/songList/** → Mod_songList SongListController
 */
const request = require('../utils/request')

// ===== 公开（白名单，无需登录）=====

// 公开歌单列表
function publicSongListApi() {
  return request({ url: '/songList/public/list', method: 'GET' })
}

// 歌单详情
function songListDetailApi(id) {
  return request({ url: '/songList/public/detail/' + id, method: 'GET' })
}

// 歌单内歌曲列表
function songListSongsApi(listId) {
  return request({ url: '/songList/public/songs/' + listId, method: 'GET' })
}

// ===== 需登录 =====

// 我创建的歌单
function myCreatedSongListApi() {
  return request({ url: '/songList/my', method: 'GET' })
}

// 创建歌单
function createSongListApi(data) {
  return request({ url: '/songList', method: 'POST', data })
}

// 修改歌单
function updateSongListApi(data) {
  return request({ url: '/songList', method: 'PUT', data })
}

// 删除歌单
function deleteSongListApi(id) {
  return request({ url: '/songList/' + id, method: 'DELETE' })
}

// 添加歌曲到歌单
function addMusicToSongListApi(data) {
  return request({ url: '/songList/music', method: 'POST', data })
}

// 从歌单移除歌曲
function removeMusicFromSongListApi(data) {
  return request({ url: '/songList/music', method: 'DELETE', data })
}

// 收藏/取消收藏歌单（toggle）
function toggleLikeSongListApi(id) {
  return request({ url: '/songList/like/' + id, method: 'POST' })
}

module.exports = {
  publicSongListApi,
  songListDetailApi,
  songListSongsApi,
  myCreatedSongListApi,
  createSongListApi,
  updateSongListApi,
  deleteSongListApi,
  addMusicToSongListApi,
  removeMusicFromSongListApi,
  toggleLikeSongListApi
}
