// 预设 3：线性波形（时域数据的三层流动曲线）
export default {
  id: 'wave',
  name: '波形',

  init(ctx, { w, h, color }) {
    this.w = w
    this.h = h
    this.color = color
    this.offset = 0 // 波形流动偏移量
  },

  render(ctx, { time, dt }) {
    const { w, h, color } = this
    ctx.clearRect(0, 0, w, h)
    const len = time.length
    // 偏移随时间推进，波形向左流动
    this.offset = (this.offset + dt * 0.08) % len

    // 三层波形：振幅/透明度递减、流速递增，制造前后纵深
    const layers = [
      { amp: h * 0.3, alpha: 0.55, lw: 2, speed: 1 },
      { amp: h * 0.2, alpha: 0.35, lw: 1.5, speed: 1.6 },
      { amp: h * 0.12, alpha: 0.2, lw: 1, speed: 2.4 }
    ]

    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    for (const layer of layers) {
      ctx.beginPath()
      for (let x = 0; x <= w; x += 14) {
        // 按 x 采样时域数据（128 为中位静音值），分层变速制造错位
        const idx = Math.floor(x * layer.speed + this.offset) % len
        const v = (time[idx] - 128) / 128
        const y = h / 2 + v * layer.amp
        if (x === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      }
      ctx.globalAlpha = layer.alpha
      ctx.strokeStyle = color
      ctx.lineWidth = layer.lw
      ctx.stroke()
    }

    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h }) {
    this.w = w
    this.h = h
  },

  destroy() {}
}