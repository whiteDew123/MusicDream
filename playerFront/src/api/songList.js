import request from './request'

// 对接 music_gateway 路由：
// /api/songList/** → Mod_songList SongListController
// 网关 StripPrefix=1 会去掉 /api 前缀，baseURL 已含 /api

// ===== 公开接口（白名单，无需登录）=====

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

// 歌单内歌曲列表
// GET /api/songList/public/songs/{listId}
export function songListSongsApi(listId) {
  return request({
    url: `/songList/public/songs/${listId}`,
    method: 'get'
  })
}

// ===== 需登录接口 =====

// 我创建的歌单
// GET /api/songList/my
export function myCreatedSongListApi() {
  return request({
    url: '/songList/my',
    method: 'get'
  })
}

// 创建歌单
// POST /api/songList
// body: { name, introduction, style, pic }
export function createSongListApi(data) {
  return request({
    url: '/songList',
    method: 'post',
    data
  })
}

// 修改歌单
// PUT /api/songList
// body: { id, name, introduction, style, pic }
export function updateSongListApi(data) {
  return request({
    url: '/songList',
    method: 'put',
    data
  })
}

// 删除歌单
// DELETE /api/songList/{id}
export function deleteSongListApi(id) {
  return request({
    url: `/songList/${id}`,
    method: 'delete'
  })
}

// 添加歌曲到歌单
// POST /api/songList/music
// body: { listId, musicId }
export function addMusicToSongListApi(data) {
  return request({
    url: '/songList/music',
    method: 'post',
    data
  })
}

// 从歌单移除歌曲
// DELETE /api/songList/music
// body: { listId, musicId }
export function removeMusicFromSongListApi(data) {
  return request({
    url: '/songList/music',
    method: 'delete',
    data
  })
}

// 收藏 / 取消收藏歌单（toggle）
// POST /api/songList/like/{id}
// 返回 data=true 表示收藏，false 表示取消
export function toggleLikeSongListApi(id) {
  return request({
    url: `/songList/like/${id}`,
    method: 'post'
  })
}
