import axios from 'axios'

/**
 * LRC 歌词工具
 * - parseLrc：把 LRC 文本解析为 [{ time: 秒, text: '歌词' }]（按时间升序）
 * - fetchLrc：通过 URL 拉取 LRC 文本
 */

/**
 * 解析 LRC 歌词
 * @param {string} lrcString LRC 文本
 * @returns {{time:number,text:string}[]}
 */
export function parseLrc(lrcString) {
  if (!lrcString || typeof lrcString !== 'string') return []
  const lines = lrcString.split(/\r?\n/)
  const result = []
  // 匹配 [mm:ss.xx] 或 [mm:ss] 时间标签（可能一行多个时间标签）
  const timeReg = /\[(\d{1,2}):(\d{1,2})(?:\.(\d{1,3}))?\]/g
  for (const line of lines) {
    let match
    const times = []
    while ((match = timeReg.exec(line)) !== null) {
      const min = parseInt(match[1])
      const sec = parseInt(match[2])
      const ms = match[3] ? parseInt(match[3].padEnd(3, '0')) : 0
      times.push(min * 60 + sec + ms / 1000)
    }
    const text = line.replace(timeReg, '').trim()
    // 跳过空行与元数据标签（ti/ar/al/by/offset 等）
    if (times.length === 0) continue
    for (const t of times) {
      result.push({ time: t, text })
    }
  }
  result.sort((a, b) => a.time - b.time)
  return result
}

/**
 * 通过 URL 拉取 LRC 文本（支持相对路径，走静态资源代理）
 * @param {string} url 歌词文件 URL
 * @returns {Promise<string>} 歌词文本
 */
export async function fetchLrc(url) {
  if (!url) return ''
  const res = await axios.get(url, { responseType: 'text', timeout: 10000 })
  return res.data || ''
}
