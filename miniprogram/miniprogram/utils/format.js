/**
 * 通用格式化工具
 */

// 播放量格式化：12345 -> 1.2万，>=1亿 用亿
function formatPlays(num) {
  if (num === null || num === undefined) return '0'
  const n = Number(num)
  if (isNaN(n)) return '0'
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}

// 时长格式化：秒 -> mm:ss 或 mm:ss 不足一位补零
function formatDuration(sec) {
  if (sec === null || sec === undefined || isNaN(Number(sec))) return '00:00'
  const s = Math.max(0, Math.floor(Number(sec)))
  const min = Math.floor(s / 60)
  const secs = s % 60
  const pad = (v) => (v < 10 ? '0' + v : '' + v)
  return pad(min) + ':' + pad(secs)
}

module.exports = {
  formatPlays,
  formatDuration
}
