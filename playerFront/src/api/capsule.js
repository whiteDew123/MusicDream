/**
 * 时空胶囊 API
 *
 * 前端 baseURL=/api → Vite proxy → 网关 9000 → StripPrefix=1 → Mod_capsule /capsule/**
 */
import request from './request'

/**
 * 创建胶囊
 * @param {Object} data - { receiverId, musicId, message, unlockTime, isPublic }
 */
export function createCapsuleApi(data) {
  return request({
    url: '/capsule',
    method: 'POST',
    data
  })
}

/**
 * 我创建的胶囊
 */
export function getMyCapsulesApi() {
  return request({
    url: '/capsule/my',
    method: 'GET'
  })
}

/**
 * 写给我的胶囊
 */
export function getReceivedCapsulesApi() {
  return request({
    url: '/capsule/received',
    method: 'GET'
  })
}

/**
 * 胶囊详情（封印状态隐藏留言）
 * @param {Number} id - 胶囊 ID
 */
export function getCapsuleDetailApi(id) {
  return request({
    url: `/capsule/${id}`,
    method: 'GET'
  })
}

/**
 * 时空广场（公开，无需登录）
 * @param {Number} size - 数量，默认 20
 */
export function getPlazaListApi(size = 20) {
  return request({
    url: '/capsule/plaza',
    method: 'GET',
    params: { size }
  })
}

/**
 * 点赞/取消点赞
 * @param {Number} id - 胶囊 ID
 */
export function toggleCapsuleLikeApi(id) {
  return request({
    url: `/capsule/like/${id}`,
    method: 'POST'
  })
}

/**
 * 设为公开
 * @param {Number} id - 胶囊 ID
 */
export function makeCapsulePublicApi(id) {
  return request({
    url: `/capsule/public/${id}`,
    method: 'PUT'
  })
}

/**
 * 删除胶囊
 * @param {Number} id - 胶囊 ID
 */
export function deleteCapsuleApi(id) {
  return request({
    url: `/capsule/${id}`,
    method: 'DELETE'
  })
}

/**
 * 搜索歌曲（复用推荐服务搜索接口）
 * @param {String} keyword - 搜索关键词
 */
export function searchMusicApi(keyword) {
  return request({
    url: '/music/search',
    method: 'GET',
    params: { keyword, page: 1, size: 10 }
  })
}
