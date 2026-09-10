// 渲染器注册表（开闭原则核心）
// - 新增可视化预设：新建一个渲染器文件 + 在 rendererRegistry 登记一行
// - 核心调度（useVisualizer）与本目录之外的所有代码零修改
//
// 渲染器策略接口约定（所有预设必须实现）：
// - id / name：注册标识与模式面板显示名
// - init(ctx, {w, h, color, anchor})：创建或重建（color 为 #rrggbb 十六进制字符串；anchor 为可选锚点 {x,y}，缺省屏幕中心）
// - render(ctx, {freq, time, dt})：每帧绘制；freq=频域 Uint8Array，time=时域 Uint8Array，dt=距上帧毫秒
// - destroy()：释放（当前无外部资源，保留为扩展点）
// 颜色约定：渲染器内用 ctx.globalAlpha + ctx.fillStyle/strokeStyle 直接着色
import bars from './bars'
import pulse from './pulse'
import waterfall from './waterfall'
import nebula from './nebula'
import constellation from './constellation'
import vinylOrbit from './vinylOrbit'

// 注册表：id 同时作为 localStorage 持久化键
export const rendererRegistry = { bars, pulse, waterfall, nebula, constellation, vinylOrbit }

// 模式面板选项列表（off 为关闭档，不是渲染器，由调度器特殊处理）
export const MODE_LIST = [
  { id: 'off', name: '关' },
  { id: 'bars', name: '频谱' },
  { id: 'pulse', name: '脉冲' },
  { id: 'waterfall', name: '瀑布' },
  { id: 'nebula', name: '星云' },
  { id: 'constellation', name: '星网' },
  { id: 'vinylOrbit', name: '唱片光效' }
]

export const DEFAULT_MODE = 'bars'

// 按 id 取渲染器定义；未知 id（如 off 或已删除预设的持久化旧值）回退默认，防止空渲染
export function getRenderer(id) {
  if (rendererRegistry[id]) return rendererRegistry[id]
  if (rendererRegistry[DEFAULT_MODE]) return rendererRegistry[DEFAULT_MODE]
  return null
}