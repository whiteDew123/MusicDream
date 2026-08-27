/**
 * 静态资源 URL 归一化
 * - 后端 music_url / image_url / lyric 多为相对路径（/uploads、/music、/img、/lyric）
 * - 小程序无 Vite 代理，需前缀 ASSET_BASE_URL 成完整 URL
 */
const { ASSET_BASE_URL } = require('../config/index')

function resolveUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//i.test(path) || path.indexOf('//') === 0) return path
  const base = ASSET_BASE_URL
  return base + (path.charAt(0) === '/' ? '' : '/') + path
}

// 装饰歌曲数组：imageUrl/musicUrl/lyric/pic 归一化
function decorateSongs(list) {
  return (list || []).map((s) =>
    s
      ? {
          ...s,
          imageUrl: resolveUrl(s.imageUrl),
          musicUrl: resolveUrl(s.musicUrl),
          lyric: resolveUrl(s.lyric),
          pic: resolveUrl(s.pic)
        }
      : s
  )
}

// 装饰歌单数组
function decorateLists(list) {
  return (list || []).map((l) =>
    l ? { ...l, pic: resolveUrl(l.pic), listPic: resolveUrl(l.listPic) } : l
  )
}

// 装饰歌手数组
function decorateArtists(list) {
  return (list || []).map((a) => (a ? { ...a, imageUrl: resolveUrl(a.imageUrl) } : a))
}

module.exports = {
  resolveUrl,
  decorateSongs,
  decorateLists,
  decorateArtists
}
