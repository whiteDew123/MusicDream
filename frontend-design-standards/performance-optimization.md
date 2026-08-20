# 前端性能优化补充（v1.0）

> 本文档是 `design-spotify.md`、`design-linear.md`、`animation.md` 的**性能补充**，不修改原有设计规范，仅追加优化策略。

---

## 1. 图片优化

> 设计规范中专辑封面是核心视觉，必须优化加载策略。

### 必须做的

- `<img>` 标签统一加 `loading="lazy"`，视口外图片不请求
- 封面图提供 WebP 格式，体积比 JPEG 小约 30%
- 使用 `srcset` 响应式图片，移动端加载小尺寸，桌面端加载大尺寸

```html
<img
  src="cover.webp"
  srcset="cover-200.webp 200w, cover-400.webp 400w"
  sizes="(max-width: 768px) 200px, 400px"
  loading="lazy"
  alt="专辑封面"
/>
```

### 效果

首页 20 张封面从 10-40MB 降到 2MB 以内，首屏加载时间减半。

---

## 2. 字体加载

> Spotify 和 Linear 设计系统都依赖自定义字体，必须消除白屏等待。

### 必须做的

- CSS 中所有 `@font-face` 加 `font-display: swap`（先用系统字体，到了再替换）
- 在 HTML `<head>` 中预加载字体文件

```css
@font-face {
  font-family: 'SpotifyMixUI';
  src: url('/fonts/SpotifyMixUI.woff2') format('woff2');
  font-display: swap;
}
```

```html
<link rel="preload" href="/fonts/SpotifyMixUI.woff2" as="font" crossorigin />
```

### 效果

页面打开立刻看到文字，不会出现 2-3 秒的空白期。

---

## 3. 阴影性能

> Spotify 设计规范中大量使用 `box-shadow`，在暗色背景下需注意。

### 原则

- 暗色背景（`#121212`）上，`box-shadow` 的模糊半径 > 16px 时肉眼几乎不可见，但 GPU 计算量很大
- 大量元素（如歌单列表 50+ 个卡片）不要每个都加阴影

### 怎么做

- 列表项用 `border` 或 `outline` 替代阴影
- 阴影只用于少数浮层（弹窗、下拉菜单、悬浮卡片）
- 弹窗级阴影：`blur: 16px` 足够，不需要 24px

```css
/* 用 border 替代阴影（列表项） */
.list-item {
  border: 1px solid #333;
  /* 而非 box-shadow: rgba(0,0,0,0.5) 0px 8px 24px; */
}

/* 阴影仅用于浮层 */
.modal {
  box-shadow: rgba(0,0,0,0.5) 0px 4px 16px;
}
```

### 效果

滚动长列表时帧率稳定在 60fps，不会出现掉帧。

---

## 4. 虚拟滚动

> 歌单列表、搜索结果等可能超过 50 条的场景，必须使用虚拟滚动。

### 什么时候用

| 列表长度 | 方案 |
|----------|------|
| < 50 条 | 正常渲染 |
| ≥ 50 条 | 虚拟滚动 |

### 技术选型

- Vue 3 推荐 `vue-virtual-scroller` 或 `@tanstack/vue-virtual`
- 仅渲染视口内可见的 10-20 条，其余 DOM 不创建

### 效果

| 指标 | 不用虚拟滚动 | 用虚拟滚动 |
|------|:---:|:---:|
| DOM 节点数 | 200+ | ~20 |
| 内存占用 | ~50MB | ~5MB |
| 首屏渲染 | 卡顿 | 秒开 |

---

## 5. 动画性能

> 作为 `animation.md` 的补充，不加新规则，只强调现有规范中的关键约束。

### 必须遵守

- 动画**仅使用 `transform` 和 `opacity`**，禁止 `left`/`top`/`width`/`height`
- 列表交错入场（`animation.md` 第二章）中，若列表 > 30 条，取消交错效果，统一淡入
- 骨架屏（`animation.md` 第三章）的脉冲动画使用 `animation` 而非 JS `setInterval`

### 什么时候跳过动画

- 用户开启 `prefers-reduced-motion` 时，所有动画禁用
- 列表长度 > 30 条时，跳过交错入场，改为统一淡入

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 6. 构建优化（补充）

> 以下为 Vue 3 + Vite 项目构建时的通用优化，非设计规范内容。

- 路由懒加载：`() => import('./views/SongDetail.vue')`
- 第三方库按需引入（Element Plus 用 `unplugin-vue-components`）
- 生产构建开启 gzip/brotli 压缩
- 静态资源使用 CDN 或强缓存

---

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2025-08-19 | 初始版本，覆盖图片、字体、阴影、虚拟滚动、动画性能、构建优化 |