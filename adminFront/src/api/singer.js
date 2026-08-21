import request from './request'

// 歌手仪表盘 API
// 对接后端 Mod_singer

// 获取歌手仪表盘数据
// GET /api/singer/dashboard?singerId=xxx
export function getSingerDashboard(singerId) {
  return request({
    url: '/singer/dashboard',
    method: 'get',
    params: { singerId }
  })
}

// 获取歌手信息
// GET /api/singer/info/{singerId}
export function getSingerInfo(singerId) {
  return request({
    url: `/singer/info/${singerId}`,
    method: 'get'
  })
}