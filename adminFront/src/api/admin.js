import request from './request'

// 管理员模块 API
// 对接 music_gateway 路由：/api/admin/**

// 分页查询用户列表
// GET /api/admin/pageUser/{pn}/{size}?keyword=
export function pageUser(pn, size, keyword) {
  const params = {}
  if (keyword) params.keyword = keyword
  return request({
    url: `/admin/pageUser/${pn}/${size}`,
    method: 'get',
    params
  })
}

// 冻结用户
// POST /api/admin/freezeUser?id=
export function freezeUser(id) {
  return request({
    url: '/admin/freezeUser',
    method: 'post',
    params: { id }
  })
}

// 解冻用户
// POST /api/admin/unfreezeUser?id=
export function unfreezeUser(id) {
  return request({
    url: '/admin/unfreezeUser',
    method: 'post',
    params: { id }
  })
}

// 分页查询音乐列表
// GET /api/admin/pageMusic/{pn}/{size}?keyword=
export function pageMusic(pn, size, keyword) {
  const params = {}
  if (keyword) params.keyword = keyword
  return request({
    url: `/admin/pageMusic/${pn}/${size}`,
    method: 'get',
    params
  })
}

// 冻结音乐
// POST /api/admin/freezeMusic?id=
export function freezeMusic(id) {
  return request({
    url: '/admin/freezeMusic',
    method: 'post',
    params: { id }
  })
}

// 解冻音乐
// POST /api/admin/unfreezeMusic?id=
export function unfreezeMusic(id) {
  return request({
    url: '/admin/unfreezeMusic',
    method: 'post',
    params: { id }
  })
}

// 分页查询日志
// GET /api/admin/pageLog/{pn}/{size}?keyword=
export function pageLog(pn, size, keyword) {
  const params = {}
  if (keyword) params.keyword = keyword
  return request({
    url: `/admin/pageLog/${pn}/${size}`,
    method: 'get',
    params
  })
}

// 获取仪表盘监控数据（统计信息）
// GET /api/admin/getMonitorData
export function getMonitorData() {
  return request({
    url: '/admin/getMonitorData',
    method: 'get'
  })
}

// 获取热门音乐 TOP N
// GET /api/admin/getTopMusic?limit=5
export function getTopMusic(limit = 5) {
  return request({
    url: '/admin/getTopMusic',
    method: 'get',
    params: { limit }
  })
}