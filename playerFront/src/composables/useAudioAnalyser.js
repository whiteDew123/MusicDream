// 音频可视化分析器（模块级单例）
// - 全站唯一接入 Web Audio API 的入口：AudioContext 全局共享，每个音频元素独立 AnalyserNode 分析链
// - 接入后音频被"截流"到分析链中，必须 connect(destination) 接回扬声器，否则全站静音
// - createMediaElementSource 对同一 audio 元素只能调用一次，重复调用抛 InvalidStateError
// - 多源设计（三期批次B）：主播放器（player store）与播放室（roomAudio）切换时各自独立分析链；
//   无参取数默认最近 attach 的活跃源，存量调用方（PlayerBar / useVisualizer）零改动

// 模块级状态：不能放在导出函数内部（否则每次调用重建，破坏单例语义）
let audioContext = null
let analysers = new Map() // audio 元素 → { source, analyser }
let activeElement = null // 活跃源：最近 attach 的元素（无参取数的默认目标）

export function useAudioAnalyser() {
  // 懒加载创建 AudioContext：首次播放才创建，规避浏览器自动播放策略对空闲上下文的限制
  function ensureContext() {
    if (audioContext) return true
    const Ctor = window.AudioContext || window.webkitAudioContext
    if (!Ctor) return false
    audioContext = new Ctor()
    return true
  }

  // 绑定音频元素（幂等：同一元素重复调用直接复用，切歌无需重新绑定）
  function attach(audioEl) {
    if (!audioEl || !ensureContext()) return false
    if (analysers.has(audioEl)) {
      activeElement = audioEl
      return true
    }
    try {
      const source = audioContext.createMediaElementSource(audioEl)
      // 此处"剪断"了 audio 元素默认直连扬声器的通路，必须重新接回
      const analyser = audioContext.createAnalyser()
      analyser.fftSize = 512 // 256 个频率桶，前 40 桶覆盖人耳敏感的低频段
      analyser.smoothingTimeConstant = 0.8 // 内置平滑，避免柱条逐帧跳变刺眼
      source.connect(analyser)
      analyser.connect(audioContext.destination) // 链路末端：Analyser → 扬声器
      analysers.set(audioEl, { source, analyser })
      activeElement = audioEl
    } catch (e) {
      console.warn('音频分析器绑定失败:', e)
      return false
    }
    return true
  }

  // 解绑音频元素（播放室页卸载时释放）：断开后元素恢复默认直连扬声器路由，引用释放可被 GC
  function detach(audioEl) {
    const entry = analysers.get(audioEl)
    if (!entry) return false
    try {
      entry.source.disconnect()
      entry.analyser.disconnect()
    } catch (e) {
      // 已断开的链重复 disconnect 会抛错，忽略
    }
    analysers.delete(audioEl)
    if (activeElement === audioEl) activeElement = null
    return true
  }

  // 激活 AudioContext：浏览器初始将其置于 suspended，必须在用户手势上下文（即播放点击）中恢复
  function resume() {
    if (audioContext && audioContext.state === 'suspended') {
      audioContext.resume().catch(() => {})
    }
  }

  // 活跃源解析：优先最近 attach 元素；已失效时回退链路中任意一条（如离开播放室后回到主播放场景）
  function getActiveAnalyser() {
    if (activeElement) {
      const entry = analysers.get(activeElement)
      if (entry) return entry.analyser
    }
    const first = analysers.values().next()
    return first.done ? null : first.value.analyser
  }

  // 读取频域数据（每帧调用；attach 前返回 null，调用方需兜底）
  function getFrequencyData() {
    const analyser = getActiveAnalyser()
    if (!analyser) return null
    const data = new Uint8Array(analyser.frequencyBinCount)
    analyser.getByteFrequencyData(data)
    return data
  }

  // 读取时域波形数据（波形/瀑布类渲染器使用）
  function getTimeDomainData() {
    const analyser = getActiveAnalyser()
    if (!analyser) return null
    const data = new Uint8Array(analyser.fftSize)
    analyser.getByteTimeDomainData(data)
    return data
  }

  return { attach, detach, resume, getFrequencyData, getTimeDomainData }
}