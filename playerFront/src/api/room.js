import request from './request'

// 对接 music_gateway 路由：
// /api/room/** → Mod_room RoomController
// 网关 StripPrefix=1 会去掉 /api 前缀，baseURL 已含 /api
// 除 /room/invite/** 外需登录（网关鉴权后经 X-User-Id 头透传用户）

// ===== 房间 =====

// 创建房间
// POST /api/room/create  body: { name, maxMembers, isPublic, inviteExpireHours, playMode }
export function createRoomApi(data) {
  return request({ url: '/room/create', method: 'post', data })
}

// 房间详情
// GET /api/room/{id}
export function roomDetailApi(id) {
  return request({ url: `/room/${id}`, method: 'get' })
}

// 房间列表
// GET /api/room/list
export function roomListApi() {
  return request({ url: '/room/list', method: 'get' })
}

// 修改房间（仅房主）
// PUT /api/room/{id}  body: { id, name, maxMembers, isPublic, inviteExpireHours, cover, playMode }
export function updateRoomApi(id, data) {
  return request({ url: `/room/${id}`, method: 'put', data })
}

// 关闭房间（仅房主）
// DELETE /api/room/{id}
export function closeRoomApi(id) {
  return request({ url: `/room/${id}`, method: 'delete' })
}

// 加入房间
// POST /api/room/{id}/join
export function joinRoomApi(id) {
  return request({ url: `/room/${id}/join`, method: 'post' })
}

// 离开房间
// POST /api/room/{id}/leave
export function leaveRoomApi(id) {
  return request({ url: `/room/${id}/leave`, method: 'post' })
}

// 转让房主（仅房主）
// POST /api/room/{id}/transfer  body: { userId }
export function transferRoomApi(id, data) {
  return request({ url: `/room/${id}/transfer`, method: 'post', data })
}

// 移出成员（仅房主）
// DELETE /api/room/{id}/kick/{userId}
export function kickRoomApi(id, userId) {
  return request({ url: `/room/${id}/kick/${userId}`, method: 'delete' })
}

// 通过邀请码查询房间（公开）
// GET /api/room/invite/{code}
export function inviteRoomApi(code) {
  return request({ url: `/room/invite/${code}`, method: 'get' })
}

// ===== 歌单 =====

// 查询房间歌单
// GET /api/room/{id}/playlist
export function roomPlaylistApi(id) {
  return request({ url: `/room/${id}/playlist`, method: 'get' })
}

// 添加歌曲到歌单
// POST /api/room/{id}/playlist/add  body: { musicId }
export function addRoomPlaylistApi(id, data) {
  return request({ url: `/room/${id}/playlist/add`, method: 'post', data })
}

// 从歌单移除歌曲
// DELETE /api/room/{id}/playlist/{musicId}
export function removeRoomPlaylistApi(id, musicId) {
  return request({ url: `/room/${id}/playlist/${musicId}`, method: 'delete' })
}

// 歌单排序
// PUT /api/room/{id}/playlist/sort  body: { items: [{ musicId, sortOrder }] }
export function sortRoomPlaylistApi(id, data) {
  return request({ url: `/room/${id}/playlist/sort`, method: 'put', data })
}

// ===== 消息 =====

// 查询房间消息
// GET /api/room/{id}/messages?after={seq}
export function roomMessagesApi(id, after) {
  return request({
    url: `/room/${id}/messages`,
    method: 'get',
    params: after ? { after } : {}
  })
}

// ===== 切歌投票 =====

// 发起切歌投票
// POST /api/room/{id}/skip-vote  body: { musicId }
export function skipVoteApi(id, data) {
  return request({ url: `/room/${id}/skip-vote`, method: 'post', data })
}

// 附议切歌投票
// POST /api/room/{id}/skip-vote/agree  body: { musicId }
export function agreeVoteApi(id, data) {
  return request({ url: `/room/${id}/skip-vote/agree`, method: 'post', data })
}
