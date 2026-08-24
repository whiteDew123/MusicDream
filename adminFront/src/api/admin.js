import request from './request'

// 仪表盘数据 API
// 对接后端 Mod_admin

// 获取监控数据（用户数、歌曲数等）
// GET /api/admin/monitor
export function getMonitorData() {
  return request({
    url: '/admin/monitor',
    method: 'get'
  })
}

// 获取热门歌曲排行
// GET /api/admin/topMusic?limit=5
export function getTopMusic(limit) {
  return request({
    url: '/admin/topMusic',
    method: 'get',
    params: { limit }
  })
}

// 获取最近 N 天新增趋势
// GET /api/admin/trend?days=7
export function getTrend(days) {
  return request({
    url: '/admin/trend',
    method: 'get',
    params: { days }
  })
}

// 分页获取操作日志
// GET /api/admin/pageLog/{pn}/{size}?keyword=xxx
export function pageLog(pn, size, keyword) {
  return request({
    url: `/admin/pageLog/${pn}/${size}`,
    method: 'get',
    params: { keyword }
  })
}

// 管理员分页查询用户
// GET /api/admin/pageUser/{pn}/{size}?keyword=xxx
export function getUserPageApi(pn, size, keyword) {
  return request({
    url: `/admin/pageUser/${pn}/${size}`,
    method: 'get',
    params: { keyword }
  })
}

// 管理员冻结用户
// POST /api/admin/freezeUser?id=xxx
export function freezeUserApi(id) {
  return request({
    url: '/admin/freezeUser',
    method: 'post',
    params: { id }
  })
}

// 管理员解冻用户
// POST /api/admin/unfreezeUser?id=xxx
export function unfreezeUserApi(id) {
  return request({
    url: '/admin/unfreezeUser',
    method: 'post',
    params: { id }
  })
}