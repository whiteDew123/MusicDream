import request from './request'

// 管理中心模块 API
// 对接后端 Mod_singer → 网关路由 /api/singer/**

// 发布歌曲（歌手权限）
// POST /api/singer/addMusic
export function addMusic(data) {
  return request({
    url: '/singer/addMusic',
    method: 'post',
    data
  })
}

// 获取歌曲列表（管理端/歌手端）
// GET /api/singer/songs
export function getMusicList(params) {
  return request({
    url: '/singer/songs',
    method: 'get',
    params
  })
}

// 删除歌曲（管理员权限）
// DELETE /api/singer/songs/{id}
export function deleteMusic(id) {
  return request({
    url: `/singer/songs/${id}`,
    method: 'delete'
  })
}

// 更新歌曲状态（管理员权限：锁定/解锁）
// PUT /api/singer/songs/{id}/status
export function updateMusicStatus(id, activation) {
  return request({
    url: `/singer/songs/${id}/status`,
    method: 'put',
    data: { activation }
  })
}
