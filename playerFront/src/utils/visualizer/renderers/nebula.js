// 预设 5：粒子星云（三期新增）
// 粒子环绕锚点做轨道漂移 + 闪烁，低频爆发驱动径向膨胀与增亮
export default {
  id: 'nebula',
  name: '星云',

  init(ctx, { w, h, color, anchor }) {
    this.w = w
    this.h = h
    this.color = color
    this.cx = anchor && typeof anchor.x === 'number' ? anchor.x : w / 2
    this.cy = anchor && typeof anchor.y === 'number' ? anchor.y : h / 2
    this.minDim = Math.min(w, h)
    this.t = 0
    this.particles = []
    const COUNT = 56
    for (let i = 0; i < COUNT; i++) {
      this.particles.push({
        ang: Math.random() * Math.PI * 2,
        r: Math.pow(Math.random(), 0.65) * this.minDim * 0.42,
        speed: (Math.random() * 0.5 + 0.08) * (Math.random() < 0.5 ? -1 : 1),
        size: Math.random() * 1.8 + 0.6,
        tw: Math.random() * Math.PI * 2 // 闪烁相位
      })
    }
  },

  render(ctx, { freq, dt }) {
    const { w, h, color, cx, cy } = this
    ctx.clearRect(0, 0, w, h)
    this.t += dt

    // 低频能量（桶 1~8）→ 爆发系数
    let bass = 0
    for (let i = 1; i <= 8; i++) bass += freq[i]
    bass /= 8 * 255
    const burst = Math.max(0, (bass - 0.45) * 0.8)

    // 中心光晕：低频越强越亮、越大
    ctx.globalAlpha = 0.05 + bass * 0.22
    ctx.fillStyle = color
    ctx.beginPath()
    ctx.arc(cx, cy, this.minDim * (0.1 + burst * 0.08), 0, Math.PI * 2)
    ctx.fill()

    // 粒子：轨道推进 + 爆发时视觉半径放大 + 正弦闪烁
    for (const p of this.particles) {
      p.ang += p.speed * dt * 0.00035
      const px = cx + Math.cos(p.ang) * p.r
      const py = cy + Math.sin(p.ang) * p.r
      const flicker = 0.6 + 0.4 * Math.sin(this.t * 0.004 + p.tw)
      ctx.globalAlpha = Math.min(1, (0.08 + (p.size / 2.4) * 0.4 + burst * 0.45) * flicker)
      ctx.beginPath()
      ctx.arc(px, py, p.size * (1 + burst * 0.9), 0, Math.PI * 2)
      ctx.fill()
    }
    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h, anchor }) {
    this.w = w
    this.h = h
    this.minDim = Math.min(w, h)
    this.cx = anchor && typeof anchor.x === 'number' ? anchor.x : w / 2
    this.cy = anchor && typeof anchor.y === 'number' ? anchor.y : h / 2
    // 窗口变小后钳制轨道半径，避免粒子飘出画布
    const cap = this.minDim * 0.5
    for (const p of this.particles) {
      if (p.r > cap) p.r = cap
    }
  },

  destroy() {}
}