/**
 * 全局入口
 * - 把播放器单例挂到 globalData.player，页面用 getApp().player 访问
 * - 启动统一走"登录判断"：无 token 的入口由各 tab 页 onShow 时校验并跳登录
 */
const { getPlayer } = require('./utils/player')

App({
  globalData: {
    player: null,
    brandPrimary: '#5e5ce6'
  },

  onLaunch() {
    this.globalData.player = getPlayer()
  }
})
