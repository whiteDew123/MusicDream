/**
 * 歌曲 / 歌手 API（对齐 playerFront/src/api/music.js）
 * 网关路由：/api/music/** → Mod_recommend MusicController
 *           /api/singer/** → Mod_recommend ArtistController
 */
const request = require('../utils/request')

// 推荐歌曲
// GET /music/recommend/songs?userId=&limit=
function recommendSongsApi(params) {
  return request({ url: '/music/recommend/songs', method: 'GET', data: params })
}

// 歌曲排行
// GET /music/rank?limit=
function rankSongsApi(limit = 10) {
  return request({ url: '/music/rank', method: 'GET', data: { limit } })
}

// 歌曲搜索
// GET /music/search?keyword=&page=&size=
function searchSongsApi(params) {
  return request({ url: '/music/search', method: 'GET', data: params })
}

// 歌曲详情
// GET /music/{musicId}
function songDetailApi(musicId) {
  return request({ url: '/music/' + musicId, method: 'GET' })
}

// 推荐歌手
// GET /singer/recommend/artists?limit=
function recommendArtistsApi(limit = 10) {
  return request({ url: '/singer/recommend/artists', method: 'GET', data: { limit } })
}

// 歌手详情
// GET /singer/detail/{artistId}
function artistDetailApi(artistId) {
  return request({ url: '/singer/detail/' + artistId, method: 'GET' })
}

module.exports = {
  recommendSongsApi,
  rankSongsApi,
  searchSongsApi,
  songDetailApi,
  recommendArtistsApi,
  artistDetailApi
}
