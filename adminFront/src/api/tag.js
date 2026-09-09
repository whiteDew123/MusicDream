import request from './request'

// 标签字典 API
// 管理接口（管理员）：对接后端 Mod_admin → 网关路由 /api/admin/tag/**
// 动态字典接口（登录用户）：对接后端 Mod_singer → 网关路由 /api/singer/tag/**

// 分页查询标签列表（含使用频次）
// GET /api/admin/tag/page/{pn}/{size}
export function pageTags(pn, size, keyword) {
  return request({
    url: `/admin/tag/page/${pn}/${size}`,
    method: 'get',
    params: { keyword }
  })
}

// 标签使用频次统计（按次数降序）
// GET /api/admin/tag/stats
export function statsTags() {
  return request({
    url: '/admin/tag/stats',
    method: 'get'
  })
}

// 新增标签（默认启用）
// POST /api/admin/tag/add
export function addTag(code, name) {
  return request({
    url: '/admin/tag/add',
    method: 'post',
    params: { code, name }
  })
}

// 编辑标签
// POST /api/admin/tag/update
export function updateTag(tagId, code, name) {
  return request({
    url: '/admin/tag/update',
    method: 'post',
    params: { tagId, code, name }
  })
}

// 启用/禁用标签
// POST /api/admin/tag/status
export function updateTagStatus(tagId, status) {
  return request({
    url: '/admin/tag/status',
    method: 'post',
    params: { tagId, status }
  })
}

// 删除标签（被歌曲引用时后端返回失败提示）
// POST /api/admin/tag/delete
export function deleteTag(tagId) {
  return request({
    url: '/admin/tag/delete',
    method: 'post',
    params: { tagId }
  })
}

// 启用中的标签列表（供标签选择器动态加载，需登录）
// GET /api/singer/tag/enabled
export function getEnabledTags() {
  return request({
    url: '/singer/tag/enabled',
    method: 'get'
  })
}