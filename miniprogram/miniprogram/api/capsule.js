/**
 * 时空胶囊 API（对齐 playerFront/src/api/capsule.js）— 二期功能，仅接口预置
 * 网关路由：/api/capsule/** → Mod_capsule /capsule/**
 */
const request = require('../utils/request')

// 创建胶囊
function createCapsuleApi(data) {
  return request({ url: '/capsule', method: 'POST', data })
}

// 我创建的胶囊
function getMyCapsulesApi() {
  return request({ url: '/capsule/my', method: 'GET' })
}

// 写给我的胶囊
function getReceivedCapsulesApi() {
  return request({ url: '/capsule/received', method: 'GET' })
}

// 胶囊详情（封印状态隐藏留言）
function getCapsuleDetailApi(id) {
  return request({ url: '/capsule/' + id, method: 'GET' })
}

// 时空广场（公开）
function getPlazaListApi(size = 20) {
  return request({ url: '/capsule/plaza', method: 'GET', data: { size } })
}

// 点赞/取消点赞
function toggleCapsuleLikeApi(id) {
  return request({ url: '/capsule/like/' + id, method: 'POST' })
}

// 设为公开
function makeCapsulePublicApi(id) {
  return request({ url: '/capsule/public/' + id, method: 'PUT' })
}

// 删除胶囊
function deleteCapsuleApi(id) {
  return request({ url: '/capsule/' + id, method: 'DELETE' })
}

module.exports = {
  createCapsuleApi,
  getMyCapsulesApi,
  getReceivedCapsulesApi,
  getCapsuleDetailApi,
  getPlazaListApi,
  toggleCapsuleLikeApi,
  makeCapsulePublicApi,
  deleteCapsuleApi
}
