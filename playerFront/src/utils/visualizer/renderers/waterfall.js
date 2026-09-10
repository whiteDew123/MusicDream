// 预设 4：频谱瀑布（三期新增）
// 热图带状：每帧采样一行频谱，新行插入顶部、旧行随时间下沉淡出
export default {
  id: 'waterfall',
  name: '瀑布',

  init(ctx, { w, h, color, quality }) {
    this.w = w
    this.h = h
    this.color = color
    this.rows = [] // n 行频谱（rows[0] 最新）
    // 性能档（三期批次B）：低档削减每行柱数与历史行数（行数减半、柱数减 1/3）
    this.quality = quality || 'high'
    this.N = this.quality === 'low' ? 32 : 48
    this.rowH = Math.max(3, Math.round(h / (this.quality === 'low' ? 45 : 90))) // 高档约 90 行历史（1.5 秒）
    this.maxRows = Math.max(10, Math.floor(h / this.rowH))
  },

  render(ctx, { freq }) {
    const { w, h, color } = this
    ctx.clearRect(0, 0, w, h)

    // 行采样：对数分布偏重低频段（音色可听区），仅取前 96 桶
    const N = this.N
    const row = new Array(N)
    for (let i = 0; i < N; i++) {
      const bin = Math.min(95, Math.floor(Math.pow(i / (N - 1), 1.4) * 95))
      row[i] = Math.pow(freq[bin] / 255, 0.85)
    }
    this.rows.unshift(row)
    if (this.rows.length > this.maxRows) this.rows.pop()

    const bw = w / N
    const slot = h / this.maxRows
    ctx.fillStyle = color
    for (let r = 0; r < this.rows.length; r++) {
      const line = this.rows[r]
      // 新行最亮、旧行渐暗，形成时间下沉的沉积感
      const fade = Math.max(0.15, 1 - (r / this.maxRows) * 0.85)
      const y = r * slot
      for (let i = 0; i < N; i++) {
        ctx.globalAlpha = Math.min(1, (0.05 + line[i] * 0.95) * fade)
        ctx.fillRect(i * bw, y, Math.max(1, bw - 1), slot)
      }
    }
    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h }) {
    this.w = w
    this.h = h
    this.rowH = Math.max(3, Math.round(h / (this.quality === 'low' ? 45 : 90)))
    this.maxRows = Math.max(10, Math.floor(h / this.rowH))
    if (this.rows.length > this.maxRows) this.rows.length = this.maxRows
  },

  destroy() {}
}