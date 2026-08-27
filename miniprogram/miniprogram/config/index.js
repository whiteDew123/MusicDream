/**
 * 全局配置
 * - BASE_URL：后端网关地址。开发期用 localhost + 开发者工具勾选"不校验合法域名"；
 *   上线必须换成 HTTPS 合法域名（request / uploadFile / downloadFile 三类都要在白名单）。
 * - 所有常量集中在此，禁止在业务代码里写死。
 */
module.exports = {
  // 开发环境（本机网关 9000）。生产替换为 https://your-domain.com/api
  BASE_URL: 'http://localhost:9000/api',

  // 静态资源服务地址（音乐/封面/歌词，由 Mod_upload 提供，相对路径 /uploads、/music、/img、/lyric 前缀到此处）
  // 开发：本机 8012；生产：公开的静态资源域名（需加入 downloadFile/request 合法域名）
  ASSET_BASE_URL: 'http://localhost:8012',

  // 登录态 storage 键
  TOKEN_KEY: 'MD_TOKEN',
  USER_KEY: 'MD_USER_INFO',

  // 分页
  PAGE_SIZE: 10,

  // 播放器默认音量 0~1
  DEFAULT_VOLUME: 0.7,

  // 播放模式
  PLAY_MODE: {
    SEQUENCE: 0, // 顺序
    SINGLE: 1,   // 单曲循环
    RANDOM: 2    // 随机
  },

  // 分享渠道（对齐 Web 端 channel）
  SHARE_CHANNEL: 'wechat',

  // 请求超时（ms）
  REQUEST_TIMEOUT: 10000,
  UPLOAD_TIMEOUT: 15000
}
