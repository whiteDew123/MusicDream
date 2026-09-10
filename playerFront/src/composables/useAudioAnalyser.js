// 音频可视化分析器（模块级单例）
// - 全站唯一接入 Web Audio API 的入口：AudioContext / AnalyserNode 跨组件共享
// - 接入后音频被"截流"到分析链中，必须 connect(destination) 接回扬声器，否则全站静音
// - createMediaElementSource 对同一 audio 元素只能调用一次，重复调用抛 InvalidStateError

// 模块级状态：不能放在导出函数内部（否则每次调用重建，破坏单例语义）
let audioContext = null
let analyserNode = null
let sourceNode = null
let boundElement = null // 已接入分析链的 audio 元素（幂等判断依据）

export function useAudioAnalyser() {
  // 懒加载创建 AudioContext：首次播放才创建，规避浏览器自动播放策略对空闲上下文的限制
  function ensureContext() {
    if (audioContext) return true
    const Ctor = window.AudioContext || window.webkitAudioContext
    if (!Ctor) return false
    audioContext = new Ctor()
    analyserNode = audioContext.createAnalyser()
    analyserNode.fftSize = 512 // 256 个频率桶，前 40 桶覆盖人耳敏感的低频段
    analyserNode.smoothingTimeConstant = 0.8 // 内置平滑，避免柱条逐帧跳变刺眼
    analyserNode.connect(audioContext.destination) // 链路末端：Analyser → 扬声器
    return true
  }

  // 绑定音频元素（幂等：同一元素重复调用直接复用，切歌无需重新绑定）
  function attach(audioEl) {
    if (!audioEl || !ensureContext()) return false
    if (boundElement === audioEl) return true
    try {
      sourceNode = audioContext.createMediaElementSource(audioEl)
      // 此处"剪断"了 audio 元素默认直连扬声器的通路，必须重新接回（上面已 connect destination）
      sourceNode.connect(analyserNode)
      boundElement = audioEl
    } catch (e) {
      console.warn('音频分析器绑定失败:', e)
      return false
    }
    return true
  }

  // 激活 AudioContext：浏览器初始将其置于 suspended，必须在用户手势上下文（即播放点击）中恢复
  function resume() {
    if (audioContext && audioContext.state === 'suspended') {
      audioContext.resume().catch(() => {})
    }
  }

  // 读取频域数据（每帧调用；attach 前返回 null，调用方需兜底）
  function getFrequencyData() {
    if (!analyserNode) return null
    const data = new Uint8Array(analyserNode.frequencyBinCount)
    analyserNode.getByteFrequencyData(data)
    return data
  }

  // 读取时域波形数据（第三期波形/瀑布类渲染器使用）
  function getTimeDomainData() {
    if (!analyserNode) return null
    const data = new Uint8Array(analyserNode.fftSize)
    analyserNode.getByteTimeDomainData(data)
    return data
  }

  return { attach, resume, getFrequencyData, getTimeDomainData }
}