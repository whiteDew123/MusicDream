import request from './request'

// 歌曲管理相关 API
// 管理员歌曲分页 / 冻结 / 解冻走 Mod_admin
// 歌曲修改 / 删除 / 歌手分页走 Mod_singer

/**
 * 管理员分页查询歌曲
 * GET /api/admin/pageMusic/{pn}/{size}?keyword=xxx
 */
export function getAdminMusicPageApi(pn, size, keyword) {
  return request({
    url: `/admin/pageMusic/${pn}/${size}`,
    method: 'get',
    params: { keyword }
  })
}

/**
 * 管理员冻结歌曲
 * POST /api/admin/freezeMusic?id=xxx
 */
export function freezeMusicApi(id) {
  return request({
    url: '/admin/freezeMusic',
    method: 'post',
    params: { id }
  })
}

/**
 * 管理员解冻歌曲
 * POST /api/admin/unfreezeMusic?id=xxx
 */
export function unfreezeMusicApi(id) {
  return request({
    url: '/admin/unfreezeMusic',
    method: 'post',
    params: { id }
  })
}

/**
 * 歌手/管理员分页查询歌曲
 * GET /api/singer/songs?page=&size=
 * 歌手登录时后端会强制只返回自己的歌曲
 */
export function getSingerMusicPageApi(page, size) {
  return request({
    url: '/singer/songs',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 修改歌曲
 * PUT /api/singer/songs/{musicId}
 */
export function updateMusicApi(musicId, data) {
  return request({
    url: `/singer/songs/${musicId}`,
    method: 'put',
    data
  })
}

/**
 * 冻结歌曲（下架，activation=2）
 * POST /api/singer/songs/{musicId}/freeze
 */
export function withdrawMusicApi(musicId) {
  return request({
    url: `/singer/songs/${musicId}/freeze`,
    method: 'post'
  })
}

/**
 * 硬删除歌曲（物理删除）
 * DELETE /api/singer/songs/{musicId}/hard
 */
export function deleteMusicHardApi(musicId) {
  return request({
    url: `/singer/songs/${musicId}/hard`,
    method: 'delete'
  })
}

/**
 * 管理员硬删除歌曲
 * POST /api/admin/deleteMusic?id=xxx
 */
export function adminDeleteMusicApi(id) {
  return request({
    url: '/admin/deleteMusic',
    method: 'post',
    params: { id }
  })
}

/**
 * 重新上架歌曲（解冻，activation=0）
 * POST /api/singer/songs/{musicId}/unfreeze
 */
export function relaunchMusicApi(musicId) {
  return request({
    url: `/singer/songs/${musicId}/unfreeze`,
    method: 'post'
  })
}

/**
 * 上传文件（音乐/封面/歌词）
 * POST /api/upload/music | /api/upload/image | /api/upload/lrc
 */
export function uploadFileApi(file, type) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/upload/${type}`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 发布歌曲（管理端歌手专用）
 * POST /api/singer/songs
 */
export function publishMusicApi(data) {
  return request({
    url: '/singer/songs',
    method: 'post',
    data
  })
}

/**
 * 管理员分页查询待审核歌曲
 * GET /api/admin/pagePendingMusic/{pn}/{size}
 */
export function getPendingMusicPageApi(pn, size) {
  return request({
    url: `/admin/pagePendingMusic/${pn}/${size}`,
    method: 'get'
  })
}

/**
 * 管理员审核通过歌曲
 * POST /api/admin/approveMusic?id=xxx
 */
export function approveMusicApi(id) {
  return request({
    url: '/admin/approveMusic',
    method: 'post',
    params: { id }
  })
}

/**
 * 管理员驳回歌曲
 * POST /api/admin/rejectMusic?id=xxx
 */
export function rejectMusicApi(id, remark) {
  return request({
    url: '/admin/rejectMusic',
    method: 'post',
    params: { id, remark }
  })
}