<template>
  <div class="dashboard">
    <!-- 欢迎横幅（Webflow：白色 canvas + hairline 边框 + eyebrow 大写标题） -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <p class="eyebrow">DASHBOARD · 控制台</p>
          <h2>欢迎回来，{{ userInfo?.username || '管理员' }} <span class="wave">👋</span></h2>
          <p class="sub">MusicDreamer 后台管理系统 · 让每一次管理都高效优雅。</p>
        </div>
        <div class="welcome-visual">
          <div class="glow-ring"></div>
          <el-icon class="welcome-icon"><Headset /></el-icon>
        </div>
      </div>
    </div>

    <!-- 统计卡片（Webflow card-feature：canvas + hairline + 悬浮轻阴影） -->
    <div class="stat-grid">
      <div
        v-for="(item, idx) in stats"
        :key="item.title"
        class="stat-card"
        :style="{ animationDelay: `${idx * 60}ms` }"
      >
        <div class="stat-icon-wrap" :style="{ background: item.iconBg }">
          <el-icon :style="{ color: item.iconColor }">
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-title">{{ item.title }}</div>
        </div>
        <div class="stat-trend" :class="item.trendClass">
          <el-icon><component :is="item.trendIcon" /></el-icon>
          <span>{{ item.trendText }}</span>
        </div>
      </div>
    </div>

    <!-- 下方内容区：系统说明 + 快速入口 -->
    <div class="content-row">
      <!-- 系统说明（feature-card） -->
      <div class="panel intro-panel">
        <div class="panel-header">
          <h3 class="panel-title">系统说明</h3>
          <span class="panel-tag">v1.0.0</span>
        </div>
        <ul class="intro-list">
          <li>
            <span class="bullet"></span>
            本系统基于 <code>Spring Cloud</code> 微服务架构，前端采用 <code>Vue 3 + Element Plus</code>。
          </li>
          <li>
            <span class="bullet"></span>
            登录、注册、邮箱验证码由 <code>Mod_login</code>（端口 8001）提供服务。
          </li>
          <li>
            <span class="bullet"></span>
            所有请求经由网关 <code>music_gateway</code>（端口 9000）统一路由与 JWT 鉴权。
          </li>
          <li>
            <span class="bullet"></span>
            左侧菜单的业务模块需后续开发对应微服务接口（占位页已就绪）。
          </li>
        </ul>
      </div>

      <!-- 快速入口 -->
      <div class="panel quick-panel">
        <div class="panel-header">
          <h3 class="panel-title">快速入口</h3>
          <span class="panel-tag">常用功能</span>
        </div>
        <div class="quick-grid">
          <router-link
            v-for="q in quickLinks"
            :key="q.path"
            :to="q.path"
            class="quick-item"
          >
            <div class="quick-icon" :style="{ background: q.bg }">
              <el-icon :style="{ color: q.color }"><component :is="q.icon" /></el-icon>
            </div>
            <div class="quick-label">{{ q.label }}</div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import {
  Headset,
  User,
  Microphone,
  Files,
  Top,
  Bottom,
  Minus as MinusIcon,
  Setting,
  Message,
  FolderOpened,
  Upload
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 统计卡片：占位数据，后续对接业务接口
// icon 配色使用 Webflow 五色 chromatic accent（design-webflow.md §329）
const stats = [
  {
    title: '用户总数',
    value: '--',
    icon: User,
    iconBg: 'rgba(67, 83, 255, 0.10)',
    iconColor: '#4353ff',
    trendIcon: Top,
    trendText: '+12%',
    trendClass: 'up'
  },
  {
    title: '音乐总数',
    value: '--',
    icon: Headset,
    iconBg: 'rgba(0, 215, 34, 0.10)',
    iconColor: '#00d722',
    trendIcon: Top,
    trendText: '+8%',
    trendClass: 'up'
  },
  {
    title: '歌手总数',
    value: '--',
    icon: Microphone,
    iconBg: 'rgba(255, 174, 19, 0.10)',
    iconColor: '#ffae13',
    trendIcon: MinusIcon,
    trendText: '持平',
    trendClass: 'flat'
  },
  {
    title: '歌单总数',
    value: '--',
    icon: Files,
    iconBg: 'rgba(238, 29, 54, 0.10)',
    iconColor: '#ee1d36',
    trendIcon: Bottom,
    trendText: '-2%',
    trendClass: 'down'
  }
]

// 快速入口（icon 配色同上，使用 Webflow chromatic accent）
const quickLinks = [
  {
    path: '/placeholder',
    label: '音乐管理',
    icon: Headset,
    bg: 'rgba(67, 83, 255, 0.10)',
    color: '#4353ff'
  },
  {
    path: '/placeholder',
    label: '歌单管理',
    icon: FolderOpened,
    bg: 'rgba(0, 215, 34, 0.10)',
    color: '#00d722'
  },
  {
    path: '/placeholder',
    label: '消息中心',
    icon: Message,
    bg: 'rgba(255, 174, 19, 0.10)',
    color: '#ffae13'
  },
  {
    path: '/placeholder',
    label: '上传中心',
    icon: Upload,
    bg: 'rgba(238, 29, 54, 0.10)',
    color: '#ee1d36'
  },
  {
    path: '/placeholder',
    label: '用户管理',
    icon: User,
    bg: 'rgba(20, 110, 245, 0.10)',
    color: '#146ef5'
  },
  {
    path: '/placeholder',
    label: '系统设置',
    icon: Setting,
    bg: 'rgba(122, 61, 255, 0.10)',
    color: '#7a3dff'
  }
]
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2xl);
}

/* ===== 欢迎横幅：Webflow 亮色面板（design-webflow.md card-feature）===== */
.welcome-banner {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-lg);
  padding: 28px 32px;
  overflow: hidden;
  position: relative;
}
.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  position: relative;
  z-index: 1;
}
.welcome-text {
  flex: 1;
  /* eyebrow 大写小标题（design-webflow.md §382：15px/500/1.5px 字距）*/
  .eyebrow {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    color: var(--brand-accent);
    margin-bottom: 10px;
  }
  h2 {
    font-size: 26px;
    font-weight: 600;
    letter-spacing: -0.4px;
    line-height: 1.25;
    color: var(--wf-ink);
    margin-bottom: 8px;

    .wave {
      display: inline-block;
      transform-origin: 70% 70%;
      animation: waveHand 2400ms ease-in-out infinite;
    }
  }
  .sub {
    font-size: 14px;
    color: var(--wf-body-mid);
    line-height: 1.6;
  }
}

/* 右侧视觉元素 */
.welcome-visual {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .glow-ring {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: radial-gradient(
      circle,
      rgba(67, 83, 255, 0.30) 0%,
      transparent 70%
    );
    animation: pulseRing 2800ms ease-out infinite;
  }
  .welcome-icon {
    font-size: 56px;
    color: var(--brand-accent);
    position: relative;
    z-index: 1;
  }
}

@keyframes waveHand {
  0%, 60%, 100% { transform: rotate(0deg); }
  10% { transform: rotate(14deg); }
  20% { transform: rotate(-8deg); }
  30% { transform: rotate(14deg); }
  40% { transform: rotate(-4deg); }
  50% { transform: rotate(10deg); }
}
@keyframes pulseRing {
  0% { transform: scale(0.85); opacity: 0.9; }
  100% { transform: scale(1.35); opacity: 0; }
}

/* ===== 统计卡片网格 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.stat-card {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: relative;
  /* 性能规范 §3：亮色卡片默认轻阴影，hover 加深 */
  box-shadow: var(--shadow-sm);
  transition: transform 200ms ease, box-shadow 200ms ease,
    border-color 200ms ease;
  animation: cardFadeUp 500ms ease-out both;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: #c5c5c5;
}

@keyframes cardFadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 22px;
  }
}

.stat-info {
  .stat-value {
    font-size: 28px;
    font-weight: 600;
    letter-spacing: -0.4px;
    color: var(--wf-ink);
    line-height: 1.1;
  }
  .stat-title {
    font-size: 13px;
    color: var(--wf-mute);
    margin-top: 6px;
  }
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  align-self: flex-start;
  padding: 2px 8px;
  border-radius: var(--rounded-pill);

  .el-icon {
    font-size: 12px;
  }
  &.up {
    color: var(--wf-accent-green);
    background: rgba(0, 215, 34, 0.10);
  }
  &.down {
    color: var(--wf-accent-red);
    background: rgba(238, 29, 54, 0.10);
  }
  &.flat {
    color: var(--wf-mute);
    background: rgba(137, 137, 137, 0.10);
  }
}

/* ===== 下方两列布局 ===== */
.content-row {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: var(--spacing-lg);
}

.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-2xl);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--wf-ink);
    letter-spacing: -0.2px;
  }
  .panel-tag {
    font-size: 11px;
    color: var(--wf-mute);
    background: var(--wf-canvas-soft);
    padding: 2px 8px;
    border-radius: var(--rounded-pill);
    letter-spacing: 0.5px;
  }
}

/* 系统说明列表 */
.intro-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    font-size: 14px;
    color: var(--wf-body);
    line-height: 1.65;
  }
  .bullet {
    flex-shrink: 0;
    width: 6px;
    height: 6px;
    margin-top: 9px;
    border-radius: 50%;
    background: var(--brand-accent);
  }
  code {
    background: var(--wf-canvas-soft);
    color: var(--brand-accent);
    padding: 2px 6px;
    border-radius: var(--rounded-xs);
    font-size: 12.5px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  }
}

/* 快速入口网格 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: var(--rounded-md);
  background: transparent;
  border: 1px solid transparent;
  transition: all 150ms ease;
  cursor: pointer;
  text-decoration: none;
}
.quick-item:hover {
  background: var(--wf-canvas-soft);
  border-color: var(--wf-hairline);
}
.quick-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 20px;
  }
}
.quick-label {
  font-size: 12.5px;
  color: var(--wf-body-mid);
  font-weight: 500;
}

/* ===== 响应式：大屏 → 小屏 ===== */
@media (max-width: 1280px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 900px) {
  .content-row {
    grid-template-columns: 1fr;
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .welcome-content {
    flex-direction: column;
    text-align: left;
  }
}
</style>
