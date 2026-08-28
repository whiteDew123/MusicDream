# 前端设计标准 · 暗色模式

> 适用范围：用户可选暗色主题，或跟随系统偏好自动切换。管理端也可支持暗色（独立于应用端）。

---

## 📁 文档索引

| 文档 | 用途 |
| :--- | :--- |
| `Design-standards-dark.md`（本文档） | 路由总纲、设计源指派、全局 CSS 变量 |
| `design-spotify.md` | 播放端暗色设计源（颜色、字体、组件、布局） |
| `animation.md` | 暗色交互协议（播放器、内容浏览、反馈、全局基础交互） |
| `performance-optimization.md` | 暗色性能优化补充（图片、字体、阴影、动画、主题切换） |

---

## 🎵 播放端（面向用户 · 暗色）

**设计源**：`design-spotify.md`

**核心视觉基因**：
- 深色画布（`#121212` 基底）
- Spotify Green（`#1ed760`）作为唯一品牌强调色
- 内容优先，专辑封面为主要色彩来源
- 沉浸式播放体验，UI 退居幕后

**使用指令**：
> “按照暗色模式（Spotify 风格）生成[页面名称]。”


## 🛠️ 管理端

> 保持现状


## 🔐 登录/注册页 · 暗色

​		保持现状，后续优化


## 🎨 暗色全局变量

```css
[data-theme="dark"] {
  /* 品牌色 */
  --brand-primary: #1ed760;
  --brand-primary-hover: #1fdf64;
  --brand-accent: #4353ff;

  /* 基底 */
  --color-bg-page: #121212;
  --color-bg-card: #181818;
  --color-bg-surface: #1f1f1f;

  /* 文字 */
  --color-text-primary: #ffffff;
  --color-text-secondary: #b3b3b3;
  --color-text-muted: #6b7280;

  /* 边框与分割 */
  --color-border: #333333;
  --color-divider: #2a2a2a;

  /* 阴影（暗色用重阴影） */
  --shadow-card: rgba(0,0,0,0.3) 0px 8px 8px;
  --shadow-elevated: rgba(0,0,0,0.5) 0px 8px 24px;
  --shadow-modal: rgba(0,0,0,0.6) 0px 8px 32px;
}
```

---

### 统一变量文件：`design-tokens.css`

```css
/* ============================================
   design-tokens.css
   亮色与暗色变量分离，通过 data-theme 切换
   ============================================ */

/* ---------- 亮色（默认） ---------- */
:root,
[data-theme="light"] {
  /* 品牌色 */
  --brand-primary: #5e5ce6;
  --brand-primary-hover: #4a3fcf;
  --brand-accent: #4353ff;
  --brand-green: #1ed760;

  /* 基底 */
  --color-bg-page: #f6f9fc;
  --color-bg-card: #ffffff;
  --color-bg-surface: #f1f4f9;

  /* 文字 */
  --color-text-primary: #1a1a1a;
  --color-text-secondary: #6b7280;
  --color-text-muted: #9ca3af;

  /* 边框与分割 */
  --color-border: #e5e7eb;
  --color-divider: #e5e7eb;

  /* 阴影（轻） */
  --shadow-card: 0 2px 8px rgba(0,0,0,0.06);
  --shadow-elevated: 0 4px 20px rgba(0,0,0,0.08);
  --shadow-modal: 0 4px 24px rgba(0,0,0,0.10);

  /* 毛玻璃（亮色） */
  --glass-bg: rgba(255,255,255,0.7);
  --glass-border: rgba(255,255,255,0.3);
}

/* ---------- 暗色 ---------- */
[data-theme="dark"] {
  /* 品牌色 */
  --brand-primary: #1ed760;
  --brand-primary-hover: #1fdf64;
  --brand-accent: #4353ff;

  /* 基底 */
  --color-bg-page: #121212;
  --color-bg-card: #181818;
  --color-bg-surface: #1f1f1f;

  /* 文字 */
  --color-text-primary: #ffffff;
  --color-text-secondary: #b3b3b3;
  --color-text-muted: #6b7280;

  /* 边框与分割 */
  --color-border: #333333;
  --color-divider: #2a2a2a;

  /* 阴影（重） */
  --shadow-card: rgba(0,0,0,0.3) 0px 8px 8px;
  --shadow-elevated: rgba(0,0,0,0.5) 0px 8px 24px;
  --shadow-modal: rgba(0,0,0,0.6) 0px 8px 32px;

  /* 毛玻璃（暗色） */
  --glass-bg: rgba(255,255,255,0.05);
  --glass-border: rgba(255,255,255,0.1);
}