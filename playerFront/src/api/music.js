import request from './request'

// 对接 music_gateway 路由：
// /api/music/**   → Mod_recommend MusicController
// /api/singer/**  → Mod_recommend ArtistController
//
// 注：网关 StripPrefix=1 会去掉 /api 前缀，baseURL 已含 /api
// 歌单相关接口见 songList.js，收藏相关见 like.js

// ===== 歌曲相关 =====

// 推荐歌曲
// GET /api/music/recommend/songs?userId=&limit=
export function recommendSongsApi(params) {
  return request({
    url: '/music/recommend/songs',
    method: 'get',
    params
  })
}

// 歌曲排行（按播放量）
// GET /api/music/rank?limit=
export function rankSongsApi(limit = 10) {
  return request({
    url: '/music/rank',
    method: 'get',
    params: { limit }
  })
}

// 歌曲排行（按发布时间）
// GET /api/music/rank/play-time?limit=
export function rankSongsByTimeApi(limit = 10) {
  return request({
    url: '/music/rank/play-time',
    method: 'get',
    params: { limit }
  })
}

// 歌曲搜索
// GET /api/music/search?keyword=&page=&size=
export function searchSongsApi(params) {
  return request({
    url: '/music/search',
    method: 'get',
    params
  })
}

// 歌曲详情
// GET /api/music/{musicId}
export function songDetailApi(musicId) {
  return request({
    url: `/music/${musicId}`,
    method: 'get'
  })
}

// ===== 歌手相关 =====

// 推荐歌手
// GET /api/singer/recommend/artists?limit=
export function recommendArtistsApi(limit = 10) {
  return request({
    url: '/singer/recommend/artists',
    method: 'get',
    params: { limit }
  })
}

// 歌手详情
// GET /api/singer/detail/{artistId}
export function artistDetailApi(artistId) {
  return request({
    url: `/singer/detail/${artistId}`,
    method: 'get'
  })
}