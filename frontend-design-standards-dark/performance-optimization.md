# 前端性能优化补充 · 暗色版（v1.0）

> 本文档是 `design-spotify.md`、`animation.md` 的**性能补充**，不修改原有设计规范，仅追加暗色模式下的优化策略。
> 通用优化（路由懒加载、构建优化等）见 `../frontend-design-standards/performance-optimization.md`，本文档仅覆盖暗色专属差异。

---

## 1. 图片优化

> 暗色模式下专辑封面是核心视觉色彩来源，加载策略与亮色一致，但需注意暗色背景下的占位处理。

### 必须做的

- `<img>` 标签统一加 `loading="lazy"`，视口外图片不请求
- 封面图提供 WebP 格式，体积比 JPEG 小约 30%
- 使用 `srcset` 响应式图片，移动端加载小尺寸，桌面端加载大尺寸
- 暗色模式下图片占位区使用 `#1f1f1f` 背景（而非亮色的 `#f0f4f8`），与暗色卡片融为一体

```html
<img
  src="cover.webp"
  srcset="cover-200.webp 200w, cover-400.webp 400w"
  sizes="(max-width: 768px) 200px, 400px"
  loading="lazy"
  alt="专辑封面"
  style="background: #1f1f1f;"
/>
```

```css
/* 暗色图片占位容器 */
.album-cover-placeholder {
  background: #1f1f1f;
  aspect-ratio: 1;
  border-radius: 6px;
}
```

### 效果

暗色背景下占位区域与卡片背景颜色一致，图片加载前不会出现白色闪烁。

## 2. 字体加载

> 暗色系统使用 SpotifyMixUI / CircularSp 字体族，与亮色系统的 Inter 策略一致。

### 必须做的

- CSS 中所有 `@font-face` 加 `font-display: swap`（先用系统字体，到了再替换）
- 在 HTML `<head>` 中预加载字体文件
- 暗色字体栈与亮色隔离，通过 `[data-theme="dark"]` 选择器切换

```css
/* 暗色系统字体栈 */
[data-theme="dark"] body {
  font-family: 'CircularSp', 'SpotifyMixUI', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
}

@font-face {
  font-family: 'CircularSp';
  src: url('/fonts/CircularSp.woff2') format('woff2');
  font-display: swap;
}
```

```html
<!-- 按需预加载：仅当用户偏好暗色或手动切换后加载 -->
<link rel="preload" href="/fonts/CircularSp.woff2" as="font" crossorigin media="(prefers-color-scheme: dark)" />
```

### 效果

暗色字体不会在亮色模式下被下载，减少不必要的网络开销。

## 3. 阴影性能

> 暗色设计规范（Spotify）中大量使用重阴影（`rgba(0,0,0,0.5)`），在深色背景下需注意性能。

### 原则

- 暗色背景下，阴影**必须重**才能可见（0.3–0.5 透明度 + 16–24px blur），但重阴影的 GPU 开销更大
- 大量元素（如歌单列表 50+ 个卡片）不要每个都加阴影，仅悬浮态才加
- 利用暗色背景的天然层次感，用**背景色变化**替代部分阴影

### 怎么做

- 列表项默认无阴影，靠背景色 `#121212` / `#181818` / `#1f1f1f` 之间的微妙差异区分层次
- 阴影仅用于少数浮层（弹窗、下拉菜单、悬浮卡片）
- 暗色系统推荐阴影参数：

| 层级   | 阴影值                               | 使用场景      |
| :----- | :----------------------------------- | :------------ |
| 轻阴影 | `rgba(0,0,0,0.3) 0px 8px 8px`       | 默认卡片悬浮  |
| 中阴影 | `rgba(0,0,0,0.5) 0px 8px 24px`      | 弹窗/下拉菜单 |
| 重阴影 | `rgba(0,0,0,0.6) 0px 8px 32px`      | 模态框        |

```css
/* 暗色：用背景色变化替代阴影（列表项） */
.list-item {
  background: #121212;
  /* 不用 box-shadow */
}

.list-item:hover {
  background: #1f1f1f;
  /* 悬浮时背景变亮即产生层次感，无需阴影 */
}

/* 阴影仅用于真正的浮层 */
.modal {
  box-shadow: rgba(0,0,0,0.6) 0px 8px 32px;
}

.card:hover {
  background: #1f1f1f;
  box-shadow: rgba(0,0,0,0.5) 0px 8px 24px;
  transition: background 0.25s ease, box-shadow 0.25s ease;
}
```

### 效果

默认状态下卡片无阴影，仅靠背景色差区分层次，GPU 负担最小；悬浮时才叠加阴影，滚动长列表时帧率稳定在 60fps。

## 4. 虚拟滚动

> 暗色与亮色策略一致，无额外差异。

| 列表长度 | 方案     |
| :------- | :------- |
| < 50 条  | 正常渲染 |
| ≥ 50 条  | 虚拟滚动 |

### 技术选型

- Vue 3 推荐 `vue-virtual-scroller` 或 `@tanstack/vue-virtual`
- 仅渲染视口内可见的 10-20 条，其余 DOM 不创建

### 暗色注意

- 虚拟滚动的容器背景色需与列表项背景一致（`#121212`），避免滚动时出现色差闪烁
- 预渲染的占位骨架构使用 `#1f1f1f` 背景

## 5. 动画性能

> 作为 `animation.md` 的补充，不加新规则，只强调暗色模式下的关键约束。

### 必须遵守

- 动画**仅使用 `transform` 和 `opacity`**，禁止 `left`/`top`/`width`/`height`
- 列表交错入场（交互协议 第二章）中，若列表 > 30 条，取消交错效果，统一淡入
- 骨架屏（交互协议 第三章）的脉冲动画：暗色下透明度在 0.4 ↔ 0.8 之间循环（亮色为 0.5 ↔ 1）

### 什么时候跳过动画

- 用户开启 `prefers-reduced-motion` 时，所有动画禁用
- 列表长度 > 30 条时，跳过交错入场，改为统一淡入
- 系统处于省电模式时（`prefers-reduced-data`），减少动画帧率

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}

/* 暗色骨架屏脉冲 */
[data-theme="dark"] .skeleton {
  background: #1f1f1f;
  animation: skeletonPulseDark 1.2s ease-in-out infinite;
}

@keyframes skeletonPulseDark {
  0%, 100% { opacity: 0.4; }
  50%      { opacity: 0.8; }
}
```

## 6. 主题切换性能

> 暗色与亮色之间的切换必须在瞬间完成，不能出现闪烁或重排。

### 原则

- 使用 CSS 变量（`[data-theme="dark"]`）切换，而非替换整个样式表
- 所有颜色值统一通过 `var(--xxx)` 引用，切换时只改变量值，不改 DOM 结构
- 禁止在 JS 中动态计算颜色后写入内联样式

### 主题切换过渡

```css
/* 全局颜色过渡：让主题切换有平滑感，但时间要短，避免拖沓 */
* {
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

/* 动画和 transform 不受主题切换影响，排除在外 */
* {
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, transform 0s, opacity 0s;
}
```

### 切换策略

```javascript
// 主题切换：仅改 data-theme 属性，CSS 变量自动接管
function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);
}

// 初始化：优先用户手动选择，其次系统偏好，默认亮色
function initTheme() {
  const saved = localStorage.getItem('theme');
  if (saved) {
    setTheme(saved);
  } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    setTheme('dark');
  }
}
```

### 效果

主题切换在 200ms 内完成，无闪烁、无重排、无额外网络请求。

## 7. 构建优化（补充）

> 暗色与亮色共享构建配置，仅需额外注意：

- 暗色字体文件（CircularSp）体积较大，建议使用 `subset` 工具裁剪字符集
- 暗色背景下的图片（如专辑封面）无需特殊处理，但 SVG 图标需确保在暗色背景下可见（`fill="currentColor"`）
- 生产构建开启 gzip/brotli 压缩

## 版本历史

| 版本 | 日期       | 变更                                                         |
| :--- | :--------- | :----------------------------------------------------------- |
| v1.0 | 2026-08-27 | 初始暗色性能优化版本；基于 Spotify 暗色设计系统，覆盖图片、字体、阴影、动画、主题切换性能 |