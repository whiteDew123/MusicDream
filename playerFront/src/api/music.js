import request from './request'

// 对接 music_gateway 路由：
// /api/music/**    → Mod_recommend MusicController
// /api/songList/** → Mod_songList SongListController
//
// 注：网关 StripPrefix=1 会去掉 /api 前缀，baseURL 已含 /api

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

// 歌曲排行
// GET /api/music/rank?limit=
export function rankSongsApi(limit = 10) {
  return request({
    url: '/music/rank',
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
// GET /api/music/recommend/artists?limit=
export function recommendArtistsApi(limit = 10) {
  return request({
    url: '/music/recommend/artists',
    method: 'get',
    params: { limit }
  })
}

// 歌手详情
// GET /api/artists/{artistId}
export function artistDetailApi(artistId) {
  return request({
    url: `/artists/${artistId}`,
    method: 'get'
  })
}

// ===== 歌单相关 =====

// 公开歌单列表
// GET /api/songList/public/list
export function publicSongListApi() {
  return request({
    url: '/songList/public/list',
    method: 'get'
  })
}

// 歌单详情
// GET /api/songList/public/detail/{id}
export function songListDetailApi(id) {
  return request({
    url: `/songList/public/detail/${id}`,
    method: 'get'
  })
}
