# 前端性能优化补充（v2.0）

> 本文档是 `design-stripe.md`、`design-webflow.md`、`animation.md` 的**性能补充**，不修改原有设计规范，仅追加优化策略。

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

## 2. 字体加载

> Stripe 和 Webflow 设计系统都依赖现代无衬线字体（Inter / SF Pro），必须消除白屏等待。

### 必须做的

- CSS 中所有 `@font-face` 加 `font-display: swap`（先用系统字体，到了再替换）
- 在 HTML `<head>` 中预加载字体文件
- 优先使用系统字体栈作为 fallback，减少字体下载阻塞

```css
/* 亮色系统推荐字体栈 */
body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
}

@font-face {
  font-family: 'Inter';
  src: url('/fonts/Inter.woff2') format('woff2');
  font-display: swap;
}
```

```html
<link rel="preload" href="/fonts/Inter.woff2" as="font" crossorigin />
```



### 效果

页面打开立刻看到文字，不会出现 2-3 秒的空白期。

## 3. 阴影性能

> 亮色设计规范（Stripe + Webflow）中大量使用 `box-shadow`，在浅色背景下需注意性能。

### 原则

- 亮色背景（`#f6f9fc`）上，阴影比暗色背景更明显，**不需要大模糊半径**即可产生层次感
- `blur: 8px–12px` 足以表现层次，`blur > 16px` 在亮色背景上会造成过度柔化，且增加 GPU 负担
- 大量元素（如歌单列表 50+ 个卡片）不要每个都加阴影

### 怎么做

- 列表项用 `border` 或 `outline` 替代阴影
- 阴影只用于少数浮层（弹窗、下拉菜单、悬浮卡片）
- 亮色系统推荐阴影参数：

| 层级   | 阴影值                        | 使用场景      |
| :----- | :---------------------------- | :------------ |
| 轻阴影 | `0 2px 8px rgba(0,0,0,0.06)`  | 默认卡片      |
| 中阴影 | `0 4px 20px rgba(0,0,0,0.08)` | 卡片悬浮态    |
| 重阴影 | `0 4px 24px rgba(0,0,0,0.10)` | 弹窗/下拉菜单 |

```css
/* 用 border 替代阴影（列表项） */
.list-item {
  border: 1px solid #e5e7eb;
  /* 而非 box-shadow: 0 4px 12px rgba(0,0,0,0.08); */
}

/* 阴影仅用于浮层和卡片悬浮态 */
.modal {
  box-shadow: 0 4px 24px rgba(0,0,0,0.10);
}

.card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  transition: box-shadow 0.25s ease;
}
```



### 效果

滚动长列表时帧率稳定在 60fps，不会出现掉帧；阴影在亮色背景上清晰可见但不刺眼。

## 4. 虚拟滚动

> 歌单列表、搜索结果等可能超过 50 条的场景，必须使用虚拟滚动。

### 什么时候用

| 列表长度 | 方案     |
| :------- | :------- |
| < 50 条  | 正常渲染 |
| ≥ 50 条  | 虚拟滚动 |

### 技术选型

- Vue 3 推荐 `vue-virtual-scroller` 或 `@tanstack/vue-virtual`
- 仅渲染视口内可见的 10-20 条，其余 DOM 不创建

### 效果

| 指标       | 不用虚拟滚动 | 用虚拟滚动 |
| :--------- | :----------- | :--------- |
| DOM 节点数 | 200+         | ~20        |
| 内存占用   | ~50MB        | ~5MB       |
| 首屏渲染   | 卡顿         | 秒开       |

## 5. 动画性能

> 作为 `交互协议.md` 的补充，不加新规则，只强调现有规范中的关键约束。

### 必须遵守

- 动画**仅使用 `transform` 和 `opacity`**，禁止 `left`/`top`/`width`/`height`
- 列表交错入场（交互协议 第二章）中，若列表 > 30 条，取消交错效果，统一淡入
- 骨架屏（交互协议 第三章）的脉冲动画使用 CSS `animation` 而非 JS `setInterval`

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



## 6. 构建优化（补充）

> 以下为 Vue 3 + Vite 项目构建时的通用优化，非设计规范内容。

- 路由懒加载：`() => import('./views/SongDetail.vue')`
- 第三方库按需引入（Element Plus 用 `unplugin-vue-components`）
- 生产构建开启 gzip/brotli 压缩
- 静态资源使用 CDN 或强缓存

## 版本历史

| 版本 | 日期       | 变更                                                         |
| :--- | :--------- | :----------------------------------------------------------- |
| v2.0 | 2026-08-20 | 设计源从 Spotify/Linear 切换为 Stripe/Webflow；同步更新字体、阴影策略及文档引用 |
| v1.0 | 2025-08-19 | 初始版本，覆盖图片、字体、阴影、虚拟滚动、动画性能、构建优化 |

|      |      |      |
|------|------|------|
|      |      |      |