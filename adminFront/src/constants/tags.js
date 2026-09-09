/**
 * 歌曲标签预设常量
 *
 * 结构：按类别分组。
 * - category：展示标题（中文）
 * - code：存储前缀（类别码）
 * - tags：该类别下的标签名称数组
 *
 * 存储格式约定：`code:名称`，多个标签以逗号分隔，如 "genre:流行,mood:欢快"。
 * 后续扩展类别时，仅需在此数组追加一组配置，选择器即可自动渲染新分组。
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
 * 依据标签名称查找其所属类别码（无前缀旧数据回显时使用）
 *
 * @param {string} name 标签名称
 * @returns {string|null} 匹配到的类别码（如 "genre"），未匹配返回 null
 */
export function resolveTagCode(name) {
  for (const group of PRESET_TAGS) {
    if (group.tags.includes(name)) {
      return group.code
    }
  }
  return null
}