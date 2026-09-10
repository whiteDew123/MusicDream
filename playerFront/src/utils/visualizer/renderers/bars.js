// 预设 1：条形频谱（中心镜像渐变光柱）
export default {
  id: 'bars',
  name: '频谱',

  init(ctx, { w, h, color }) {
    this.w = w
    this.h = h
    this.color = color
  },

  render(ctx, { freq }) {
    const { w, h, color } = this
    ctx.clearRect(0, 0, w, h)
    const count = 64
    const barW = w / count
    const bw = Math.max(2, barW * 0.7) // 柱宽 70%，留缝形成节奏感
    const cy = h / 2 // 镜像中心线

    for (let i = 0; i < count; i++) {
      // 仅映射前 128 个低频桶：高频桶能量低，映射全范围会使柱条整体低平
      const bin = Math.floor((i / count) * 128)
      const v = Math.min(freq[bin] / 255, 1)
      const barH = Math.pow(v, 1.4) * h * 0.4 // 单侧最大 40% 屏高
      const x = i * barW + (barW - bw) / 2

      const alpha = 0.2 + v * 0.55
      // 主柱：从中心向上生长
      ctx.globalAlpha = alpha
      ctx.fillStyle = color
      ctx.beginPath()
      ctx.roundRect(x, cy - barH, bw, Math.max(barH, 1.5), 1.5)
      ctx.fill()
      // 镜像柱：向下生长，透明度约 45% 形成水面倒影感，中心留 2px 暗缝
      ctx.globalAlpha = alpha * 0.45
      ctx.beginPath()
      ctx.roundRect(x, cy + 2, bw, Math.max(barH, 1.5), 1.5)
      ctx.fill()
    }
    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h }) {
    this.w = w
    this.h = h
  },

  destroy() {}
}