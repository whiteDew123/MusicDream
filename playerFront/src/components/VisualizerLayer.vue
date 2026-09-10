<template>
  <!-- 音频可视化层（二期）：全屏 Canvas 氛围层，不拦截任何交互 -->
  <canvas ref="canvasRef" class="visualizer-canvas" aria-hidden="true"></canvas>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useVisualizer } from '@/composables/useVisualizer'

const props = defineProps({
  // 可视化模式 id（off/bars/pulse/wave...），由父组件负责持久化前展示态
  mode: { type: String, required: true },
  // 主题色（#rrggbb），随封面主色变化
  color: { type: String, default: '#5e5ce6' },
  // 是否正在播放（联动启停绘制循环）
  playing: { type: Boolean, default: false },
  // 渲染锚点（{x, y}，相对画布左上角的 CSS 像素坐标）：脉冲/轨道光效类渲染器以此为圆心；null = 屏幕中心
  anchor: { type: Object, default: null }
})

const visualizer = useVisualizer()
const canvasRef = ref(null)

onMounted(() => {
  // attach 必须先于 setMode：renderer.init 依赖已就绪的 2D 上下文
  visualizer.attach(canvasRef.value)
  visualizer.setMode(props.mode)
  visualizer.setColor(props.color)
  visualizer.setAnchor(props.anchor)
  visualizer.setPlaying(props.playing)
})

watch(
  () => props.mode,
  (m) => visualizer.setMode(m)
)

watch(
  () => props.color,
  (c) => visualizer.setColor(c)
)

watch(
  () => props.playing,
  (p) => visualizer.setPlaying(p)
)

watch(
  () => props.anchor,
  (a) => visualizer.setAnchor(a),
  { deep: true }
)

onBeforeUnmount(() => visualizer.detach())
</script>

<style scoped>
.visualizer-canvas {
  position: absolute;
  inset: 0;
  width: 100% !important;
  height: 100% !important;
  z-index: 5; /* 背景模糊(0)之上、卡片堆叠(10)之下 */
  pointer-events: none; /* 纯氛围层：点击穿透到卡片与控制器 */
}
</style>