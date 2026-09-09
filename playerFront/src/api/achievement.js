import request from './request'

// 对接 music_gateway 路由：
// /api/achievement/** → Mod_achievement AchievementController
// 所有接口均需登录

// 成就墙：全部规则 + 当前用户进度/解锁状态
// GET /api/achievement/wall
export function achievementWallApi() {
  return request({
    url: '/achievement/wall',
    method: 'get'
  })
}

// 用户等级信息：等级、积分、距下一级进度、打卡信息
// GET /api/achievement/level
export function levelInfoApi() {
  return request({
    url: '/achievement/level',
    method: 'get'
  })
}

// 每日打卡
// POST /api/achievement/checkin
export function checkinApi() {
  return request({
    url: '/achievement/checkin',
    method: 'post'
  })
}

// 触发判定：播放/收藏事件回调（silent 不弹错误提示）
// POST /api/achievement/trigger?action=PLAY&musicId=123
export function triggerAchievementApi(action, musicId) {
  return request({
    url: '/achievement/trigger',
    method: 'post',
    params: { action, musicId },
    silent: true
  })
}
