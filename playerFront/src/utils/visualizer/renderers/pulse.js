// 预设 2：圆形脉冲（中心涟漪 + 旋转频谱环）
// 三期扩展：支持可选渲染锚点（如黑胶中心），缺省屏幕中心
export default {
  id: 'pulse',
  name: '脉冲',

  init(ctx, { w, h, color, anchor }) {
    this.w = w
    this.h = h
    this.color = color
    this.anchor = anchor && typeof anchor.x === 'number' && typeof anchor.y === 'number' ? anchor : null
    this.baseR = Math.min(w, h) * 0.12 // 内圈半径：随屏幕尺寸缩放
    this.ripples = [] // 鼓点触发的扩散涟漪
    this.phase = 0 // 频谱环旋转相位
    this.lastBass = 0
  },

  render(ctx, { freq, dt }) {
    const { w, h, color, baseR } = this
    ctx.clearRect(0, 0, w, h)
    // 锚点优先：黑胶中心；未提供则回退屏幕中心
    const cx = this.anchor ? this.anchor.x : w / 2
    const cy = this.anchor ? this.anchor.y : h / 2

    // 低频能量均值（桶 1~8）：检测鼓点脉冲
    let bass = 0
    for (let i = 1; i <= 8; i++) bass += freq[i]
    bass /= 8 * 255

    // 能量突增视为节奏点：生成一圈涟漪（限长防堆积）
    if (bass > this.lastBass + 0.12 && bass > 0.5) {
      this.ripples.push({ r: baseR, alpha: 0.45 })
      if (this.ripples.length > 10) this.ripples.shift()
    }
    this.lastBass = bass

    // 中心发光圆：低频越强越亮
    ctx.globalAlpha = 0.08 + bass * 0.25
    ctx.fillStyle = color
    ctx.beginPath()
    ctx.arc(cx, cy, baseR * 0.9, 0, Math.PI * 2)
    ctx.fill()

    // 涟漪：半径随时间扩散、透明度衰减，alpha 过低时回收
    ctx.lineWidth = 1.5
    for (const rp of this.ripples) {
      rp.r += dt * 0.06
      rp.alpha *= 0.97
      ctx.globalAlpha = rp.alpha
      ctx.strokeStyle = color
      ctx.beginPath()
      ctx.arc(cx, cy, rp.r, 0, Math.PI * 2)
      ctx.stroke()
    }
    this.ripples = this.ripples.filter((rp) => rp.alpha > 0.02)

    // 旋转频谱环：48 根柱自内向外辐射，缓慢旋转
    const N = 48
    this.phase += dt * 0.0005
    const maxR = Math.min(w, h) * 0.34
    ctx.lineWidth = 2
    for (let i = 0; i < N; i++) {
      const bin = Math.floor((i / N) * 96)
      const v = Math.min(freq[bin] / 255, 1)
      const len = baseR + Math.pow(v, 1.2) * (maxR - baseR)
      const ang = (i / N) * Math.PI * 2 + this.phase
      const x1 = cx + Math.cos(ang) * baseR
      const y1 = cy + Math.sin(ang) * baseR
      const x2 = cx + Math.cos(ang) * len
      const y2 = cy + Math.sin(ang) * len
      ctx.globalAlpha = 0.15 + v * 0.45
      ctx.strokeStyle = color
      ctx.beginPath()
      ctx.moveTo(x1, y1)
      ctx.lineTo(x2, y2)
      ctx.stroke()
    }

    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h, anchor }) {
    this.w = w
    this.h = h
    this.anchor = anchor && typeof anchor.x === 'number' && typeof anchor.y === 'number' ? anchor : null
    this.baseR = Math.min(w, h) * 0.12
  },

  destroy() {}
}