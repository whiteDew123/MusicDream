// 预设 6：星网（三期二改，替代流星）
// 星点恒常可见并随频谱脉动闪烁；频谱分 8 段做尖峰检测，某段能量突增时对应星区闪现连线网（随机 4 星全连接）
export default {
  id: 'constellation',
  name: '星网',

  init(ctx, { w, h, color, quality }) {
    this.w = w
    this.h = h
    this.color = color
    this.t = 0
    this.stars = []
    // 性能档（三期批次B）：低档减半星点数，连线触发阈值收紧
    this.low = quality === 'low'
    const COUNT = this.low ? 60 : 110
    for (let i = 0; i < COUNT; i++) {
      this.stars.push({
        nx: 0.02 + Math.random() * 0.96, // 归一化坐标：resize 免重建
        ny: 0.03 + Math.random() * 0.8,
        size: Math.random() * 1.8 + 0.7,
        phase: Math.random() * Math.PI * 2,
        white: Math.random() < 0.1 // 10% 白星，增加层次
      })
    }
    // 8 个频段区（按 x 分箱）：尖峰时从对应区随机取 4 颗连线
    this.zones = Array.from({ length: 8 }, () => [])
    for (const s of this.stars) {
      const z = Math.min(7, Math.floor(s.nx * 8))
      this.zones[z].push(s)
    }
    this.links = [] // { a, b, life, maxLife }（a/b 为星对象）
    this.rolls = new Array(8).fill(0.2) // 各频段滚动能量基线
  },

  render(ctx, { freq, dt }) {
    const { w, h, color } = this
    ctx.clearRect(0, 0, w, h)
    this.t += dt

    // 低频总能量：整体脉动调制
    let bass = 0
    for (let i = 1; i <= 8; i++) bass += freq[i]
    bass /= 8 * 255

    // 8 段能量（桶 1~192 对数分箱，偏重低频可见性）
    const en = new Array(8)
    for (let z = 0; z < 8; z++) {
      const b0 = Math.floor(Math.pow((z + 1) / 8, 1.6) * 192)
      const b1 = Math.floor(Math.pow((z + 1.3) / 8, 1.6) * 192)
      let s = 0
      for (let i = b0; i <= b1; i++) s += freq[i]
      en[z] = s / (b1 - b0 + 1) / 255
    }

    // 尖峰检测：区能量突超滚动基线 → 对应星区连网（连线池上限防刷屏；低档收紧阈值）
    const spikeDelta = this.low ? 0.2 : 0.14
    const spikeFloor = this.low ? 0.6 : 0.55
    const linkCap = this.low ? 18 : 30
    for (let z = 0; z < 8; z++) {
      this.rolls[z] = this.rolls[z] * 0.94 + en[z] * 0.06
      if (en[z] > this.rolls[z] + spikeDelta && en[z] > spikeFloor && this.links.length < linkCap) {
        const zone = this.zones[z]
        if (zone.length >= 4) {
          const pick = [...zone].sort(() => Math.random() - 0.5).slice(0, 4)
          const life = 260 + Math.random() * 240
          for (let i = 0; i < 4; i++) {
            for (let j = i + 1; j < 4; j++) {
              this.links.push({ a: pick[i], b: pick[j], life: 0, maxLife: life })
            }
          }
        }
      }
    }

    // 星点：个体相位闪烁 × 总能量脉动（低音越强整体越亮、越大），保证任何时刻可见
    const boost = 0.6 + bass * 0.9
    for (const s of this.stars) {
      const tw = 0.55 + 0.45 * Math.sin(this.t * 0.003 + s.phase)
      ctx.globalAlpha = Math.min(1, (0.16 + tw * 0.5) * boost)
      ctx.fillStyle = s.white ? '#ffffff' : color
      const sz = s.size * (0.8 + bass * 0.5)
      ctx.fillRect(s.nx * w, s.ny * h, sz, sz)
    }

    // 连线：前 80% 生命周期全亮，随后线性淡出
    ctx.strokeStyle = color
    ctx.lineWidth = 1.2
    for (const l of this.links) {
      l.life += dt
      const fade = 1 - l.life / l.maxLife
      const a = fade < 0.8 ? fade / 0.8 : 1
      ctx.globalAlpha = a * 0.65
      ctx.beginPath()
      ctx.moveTo(l.a.nx * w, l.a.ny * h)
      ctx.lineTo(l.b.nx * w, l.b.ny * h)
      ctx.stroke()
    }
    this.links = this.links.filter((l) => l.life < l.maxLife)
    ctx.globalAlpha = 1
  },

  resize(ctx, { w, h }) {
    this.w = w
    this.h = h
  },

  destroy() {}
}