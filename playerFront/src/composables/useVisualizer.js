// 可视化调度层（模块级单例）
// 职责：rAF 循环 / 渲染器生命周期 / 页面隐藏暂停 / 模式持久化 / 封面主色提取 / 渲染锚点透传
// 对渲染器只认"注册表约定接口"，不感知具体预设实现（开闭原则）
import { useAudioAnalyser } from './useAudioAnalyser'
import { MODE_LIST, DEFAULT_MODE, getRenderer } from '@/utils/visualizer/renderers'

const STORAGE_KEY = 'visualizer:mode'
const FALLBACK_COLOR = '#5e5ce6'
const analyser = useAudioAnalyser()

// 模块级状态：跨组件共享（与 useAudioAnalyser 同一设计）
let canvas2d = null
let size = { w: 0, h: 0 }
let rendererDef = null // 当前渲染器定义（注册表单例）
let renderer = null // 当前渲染器实例（Object.create 隔离实例状态）
let rendererId = 'off'
let color = FALLBACK_COLOR
let anchor = null // 渲染锚点（如黑胶中心）：null = 渲染器自决（缺省屏幕中心）
let playingNow = false
let running = false
let rafId = 0
let lastTs = 0
// 系统"减弱动态效果"：只影响默认模式（首次无持久化记录时默认关），用户手动选择即视为显式开启
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

// 十六进制颜色归一化：非法值回退主题紫，防止向 canvas 写入无效色值
function normalizeColor(hex) {
  if (/^#[0-9a-fA-F]{6}$/.test(hex)) return hex
  if (/^#[0-9a-fA-F]{3}$/.test(hex)) {
    return '#' + hex.slice(1).split('').map((c) => c + c).join('')
  }
  return FALLBACK_COLOR
}

// 旧存档键迁移：三期"流星"中途重设计为"星网"（id meteor → constellation），避免老用户存储的旧 id 失效
const LEGACY_MODE_MAP = { meteor: 'constellation' }

// 读取初始模式（本地持久化优先；无记录且系统开启减弱动效时默认关闭）
export function getInitialMode() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const mapped = LEGACY_MODE_MAP[stored] || stored
      if (MODE_LIST.some((m) => m.id === mapped)) return mapped
    }
  } catch (e) {
    // localStorage 不可用（隐私模式等）时静默回退
  }
  return reducedMotion ? 'off' : DEFAULT_MODE
}

export function useVisualizer() {
  // 渲染器 init 参数收口：新增作用域参数（如锚点）只改此处，避免各调用点遗漏
  function initOpts() {
    return { ...size, color, anchor }
  }

  // ---- 画布绑定 ----
  function attach(canvas) {
    const ctx = canvas.getContext('2d')
    const dpr = window.devicePixelRatio || 1
    const rect = canvas.getBoundingClientRect()
    canvas.width = rect.width * dpr
    canvas.height = rect.height * dpr
    ctx.scale(dpr, dpr) // 此后均以 CSS 像素坐标绘制，高分屏不发虚
    canvas2d = ctx
    size = { w: rect.width, h: rect.height }
    if (renderer) {
      renderer.init(canvas2d, initOpts())
    }
    window.addEventListener('resize', handleResize)
    document.addEventListener('visibilitychange', handleVisibility)
  }

  function detach() {
    stopLoop()
    window.removeEventListener('resize', handleResize)
    document.removeEventListener('visibilitychange', handleVisibility)
    canvas2d = null
  }

  // 窗口尺寸变化：重建画布物理像素与渲染器尺寸（rendrer.resize 由 init 覆盖实现）
  function handleResize() {
    if (!canvas2d) return
    const canvas = canvas2d.canvas
    const dpr = window.devicePixelRatio || 1
    const rect = canvas.getBoundingClientRect()
    canvas.width = rect.width * dpr
    canvas.height = rect.height * dpr
    canvas2d.setTransform(dpr, 0, 0, dpr, 0, 0)
    size = { w: rect.width, h: rect.height }
    if (renderer) {
      renderer.init(canvas2d, initOpts())
    }
  }

  // ---- 模式 ----
  function setMode(id) {
    if (!MODE_LIST.some((m) => m.id === id)) return
    rendererId = id
    try {
      localStorage.setItem(STORAGE_KEY, id)
    } catch (e) {
      // 持久化失败不影响本次运行
    }
    if (id === 'off') {
      stopLoop()
      renderer = null
      if (canvas2d) canvas2d.clearRect(0, 0, size.w, size.h)
      return
    }
    rendererDef = getRenderer(id)
    // Object.create：实例状态（相位、涟漪等）与注册表单例隔离
    renderer = Object.create(rendererDef)
    if (canvas2d) {
      renderer.init(canvas2d, initOpts())
    }
    if (playingNow) startLoop()
  }

  function setColor(hex) {
    color = normalizeColor(hex)
    // 颜色变化 = 重建渲染器配色（渲染器自身无 setColor 约定，保持接口最小化）
    if (renderer && canvas2d) {
      renderer.init(canvas2d, initOpts())
    }
  }

  // 渲染锚点（三期）：如黑胶中心，由组件测量后透传；非法值回退 null（渲染器缺省屏幕中心）
  function setAnchor(pos) {
    anchor = pos && typeof pos.x === 'number' && typeof pos.y === 'number' ? { x: pos.x, y: pos.y } : null
    if (renderer && canvas2d) {
      renderer.init(canvas2d, initOpts())
    }
  }

  // ---- 播放联动 ----
  function setPlaying(playing) {
    playingNow = playing
    if (playing) startLoop()
    else stopLoop()
  }

  // 页面切后台：rAF 虽被浏览器自动暂停，显式停止以免恢复时状态错乱；切回前台且仍在播放则续跑
  function handleVisibility() {
    if (document.hidden) {
      stopLoop()
    } else if (playingNow && rendererId !== 'off') {
      startLoop()
    }
  }

  function startLoop() {
    if (running || !canvas2d || rendererId === 'off') return
    running = true
    lastTs = performance.now()
    rafId = requestAnimationFrame(tick)
  }

  function stopLoop() {
    running = false
    cancelAnimationFrame(rafId)
  }

  function tick(ts) {
    if (!running) return
    const dt = Math.min(ts - lastTs, 100) // 上限防切页恢复时的大步进跳变
    lastTs = ts
    const freq = analyser.getFrequencyData()
    const time = analyser.getTimeDomainData()
    // freq 为 null 说明音频从未播放（分析器未挂载）：跳过绘制，画布保持透明
    if (renderer && freq) {
      renderer.render(canvas2d, { freq, time, dt })
    }
    rafId = requestAnimationFrame(tick)
  }

  return { attach, detach, setMode, setColor, setAnchor, setPlaying }
}

// ---- 封面主色提取 ----
// 8×8 缩小采样后取均值并放大亮度：得到比原图更鲜明的氛围色（纯背景易观感贫瘠）
// 跨域图片会被 canvas 污染导致 getImageData 抛错：catch 后返回 null，由调用方回退默认色
export function extractCoverColor(imageUrl) {
  return new Promise((resolve) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      try {
        const c = document.createElement('canvas')
        c.width = 8
        c.height = 8
        const ctx = c.getContext('2d')
        ctx.drawImage(img, 0, 0, 8, 8)
        const { data } = ctx.getImageData(0, 0, 8, 8)
        let r = 0,
          g = 0,
          b = 0
        for (let i = 0; i < data.length; i += 4) {
          r += data[i]
          g += data[i + 1]
          b += data[i + 2]
        }
        const n = data.length / 4
        r /= n
        g /= n
        b /= n
        // 亮度放大：最大通道拉伸到 235，过暗封面也能贡献可见的氛围色
        const mx = Math.max(r, g, b) || 1
        const k = Math.min(235 / mx, 2)
        const toHex = (v) => Math.round(Math.min(v * k, 255)).toString(16).padStart(2, '0')
        resolve(`#${toHex(r)}${toHex(g)}${toHex(b)}`)
      } catch (e) {
        resolve(null)
      }
    }
    img.onerror = () => resolve(null)
    img.src = imageUrl
  })
}