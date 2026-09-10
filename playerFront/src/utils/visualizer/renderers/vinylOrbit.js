// 预设 7：唱片轨道光效（三期新增）
// 围绕黑胶中心的多层光环：环段亮度由对应频段驱动，三环异步旋转塑造"轨道槽"质感
export default {
  id: 'vinylOrbit',
  name: '唱片光效',

  init(ctx, { w, h, color, anchor }) {
    this.w = w
    this.h = h
    this.color = color
    this.cx = anchor && typeof anchor.x === 'number' ? anchor.x : w / 2
    this.cy = anchor && typeof anchor.y === 'number' ? anchor.y : h / 2
    this.baseR = Math.min(w, h) * 0.3 // 光环基准半径（黑胶外圈之外）
    this.rot = 0
    // 频段分段：低/中/高频各自成环，环径、旋转方向各异
    this.bands = [
      { from: 1, to: 10, r0: 1.0, r1: 1.16, spin: 1 },
      { from: 10, to: 48, r0: 1.22, r1: 1.4, spin: -0.8 },
      { from: 48, to: 110, r0: 1.46, r1: 1.64, spin: 0.6 }
    ]
  },

  render(ctx, { freq, dt }) {
    const { w, h, color, cx, cy } = this
    ctx.clearRect(0, 0, w, h)
    this.rot += dt * 0.0004

    const SEG = 40
    ctx.strokeStyle = color
    ctx.lineCap = 'round'
    for (const band of this.bands) {
      // 频段整体能量：调制该环基础亮度（段落静音时整环变暗）
      let energy = 0
      for (let i = band.from; i <= band.to; i++) energy += freq[i]
      energy /= (band.to - band.from + 1) * 255

      for (let s = 0; s < SEG; s++) {
        const bin = band.from + Math.floor((s / SEG) * (band.to - band.from))
        const v = freq[bin] / 255
        const ang = this.rot * band.spin + (s / SEG) * Math.PI * 2
        // 相邻段半径错位：模拟唱片轨道槽的凹凸质感
        const r = this.baseR * (band.r0 + (band.r1 - band.r0) * (s % 2))
        ctx.globalAlpha = Math.min(1, 0.04 + v * 0.55 + energy * 0.2)
        ctx.lineWidth = 2 + energy * 2
        ctx.beginPath()
        ctx.arc(cx, cy, r, ang, ang + (Math.PI * 2) / SEG - 0.015)
        ctx.stroke()
      }
    }
    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h, anchor }) {
    this.w = w
    this.h = h
    this.baseR = Math.min(w, h) * 0.3
    this.cx = anchor && typeof anchor.x === 'number' ? anchor.x : w / 2
    this.cy = anchor && typeof anchor.y === 'number' ? anchor.y : h / 2
  },

  destroy() {}
}