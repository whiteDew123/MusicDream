/**
 * 歌曲标签预设常量
 *
 * 结构：按类别分组。
 * - category：展示标题（中文）
 * - code：存储前缀（类别码）
 * - tags：该类别下的标签名称数组
 *
 * 存储格式约定：`code:名称`，多个标签以逗号分隔，如 "genre:流行,mood:欢快"。
 * 标签选择器优先从服务端字典动态加载（GET /singer/tag/enabled），
 * 本常量仅作为接口失败时的降级兜底。
 */
export const PRESET_TAGS = [
  {
    category: '风格',
    code: 'genre',
    tags: ['流行', '摇滚', '电子', '民谣', 'R&B', '嘻哈',
           '爵士', '古典', '轻音乐', '古风', '国风', '说唱',
           '雷鬼', '灵魂乐', '另类/独立']
  },
  {
    category: '情绪',
    code: 'mood',
    tags: ['欢快', '悲伤', '治愈', '激情', '慵懒', '伤感', '甜蜜', '励志']
  }
]

/**
 * 类别码 → 中文类别名映射（未知类别码回退显示 code 本身）
 */
export const CODE_CATEGORY_MAP = {
  genre: '风格',
  mood: '情绪',
  lang: '语言',
  era: '年代',
  scene: '场景',
  inst: '乐器'
}

/**
 * 标签管理页可作为类别的选项（含预留类别）
 */
export const TAG_CODE_OPTIONS = [
  { code: 'genre', label: '风格' },
  { code: 'mood', label: '情绪' },
  { code: 'lang', label: '语言' },
  { code: 'era', label: '年代' },
  { code: 'scene', label: '场景' },
  { code: 'inst', label: '乐器' }
]

/**
 * 将服务端返回的扁平标签列表 [{ code, name }] 聚合为分组结构
 *
 * @param {Array<{code:string, name:string}>} flat 扁平标签列表
 * @returns {Array<{category:string, code:string, tags:string[]}>} 分组结构
 */
export function buildGroups(flat) {
  const map = new Map()
  for (const item of flat || []) {
    if (!item || !item.code) continue
    const code = item.code
    if (!map.has(code)) {
      map.set(code, { category: CODE_CATEGORY_MAP[code] || code, code, tags: [] })
    }
    if (item.name) map.get(code).tags.push(item.name)
  }
  return [...map.values()]
}

/**
 * 依据标签名称查找其所属类别码（无前缀旧数据回显时使用）
 *
 * @param {string} name 标签名称
 * @param {Array<{code:string, tags:string[]}>} [groups] 分组结构，默认 PRESET_TAGS
 * @returns {string|null} 匹配到的类别码（如 "genre"），未匹配返回 null
 */
export function resolveTagCode(name, groups = PRESET_TAGS) {
  for (const group of groups || []) {
    if (group.tags.includes(name)) {
      return group.code
    }
  }
  return null
}