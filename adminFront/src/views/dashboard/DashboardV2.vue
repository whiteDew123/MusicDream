<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <p class="eyebrow">{{ isAdmin ? 'DASHBOARD · 控制台' : 'DASHBOARD · 创作中心' }}</p>
          <h2>{{ isAdmin ? '欢迎回来，' : '你好，' }}{{ userInfo?.username || '用户' }} <span class="wave">👋</span></h2>
          <p class="sub">{{ isAdmin ? 'MusicDreamer 后台管理系统 · 数据一目了然。' : 'MusicDreamer 歌手创作中心 · 你的音乐世界。' }}</p>
        </div>
        <div class="welcome-visual">
          <div class="glow-ring"></div>
          <el-icon class="welcome-icon"><Headset /></el-icon>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="(item, idx) in stats" :key="item.title" class="stat-card">
        <div class="stat-icon-wrap" :style="{ background: item.iconBg }">
          <el-icon :style="{ color: item.iconColor }"><component :is="item.icon" /></el-icon>
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

    <!-- 管理员图表区 -->
    <div v-if="isAdmin" class="chart-row">
      <div class="panel chart-panel">
        <div class="panel-header">
          <h3 class="panel-title">近7日新增趋势</h3>
          <span class="panel-tag">用户 / 歌曲</span>
        </div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="panel chart-panel">
        <div class="panel-header">
          <h3 class="panel-title">热门歌曲 TOP5</h3>
          <span class="panel-tag">播放量</span>
        </div>
        <div ref="topMusicChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 歌手图表区 -->
    <div v-if="!isAdmin" class="chart-row">
      <div class="panel chart-panel">
        <div class="panel-header">
          <h3 class="panel-title">我的歌曲播放排行</h3>
          <span class="panel-tag">播放量 TOP5</span>
        </div>
        <div ref="myTopChartRef" class="chart-box"></div>
      </div>
      <div class="panel recent-panel">
        <div class="panel-header">
          <h3 class="panel-title">最近上传</h3>
          <span class="panel-tag">最新 5 首</span>
        </div>
        <div v-if="recentUploads.length === 0" class="empty-hint">还没有上传歌曲，快去发布吧！</div>
        <div v-else class="recent-list">
          <div v-for="(item, idx) in recentUploads" :key="item.musicId" class="recent-item">
            <span class="recent-idx">{{ idx + 1 }}</span>
            <div class="recent-info">
              <span class="recent-name">{{ item.musicName }}</span>
              <span class="recent-date">{{ item.createTime }}</span>
            </div>
            <el-tag :type="auditTagType(item.auditStatus)" size="small" effect="light" round>
              {{ auditLabel(item.auditStatus) }}
            </el-tag>
          </div>
        </div>
      </div>
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

    <!-- 系统说明 -->
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
          登录、注册、邮箱验证码由 <code>Mod_login</code> 提供服务。
        </li>
        <li>
          <span class="bullet"></span>
          所有请求经由网关 <code>music_gateway</code> 统一路由与 JWT 鉴权。
        </li>
        <li>
          <span class="bullet"></span>
          {{ isAdmin ? '数据卡片与图表由 Mod_admin 监控接口提供实时数据。' : '歌手仪表盘数据由 Mod_singer 服务提供。' }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
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
  Upload,
  Checked,
  Clock,
  DataAnalysis
} from '@element-plus/icons-vue'
import { getMonitorData, getTopMusic, getTrend } from '@/api/admin'
import { getSingerDashboard } from '@/api/singer'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin())

const monitor = ref({})
const topMusic = ref([])
const trend = ref([])
const singerDashboard = ref({})
const myTopMusic = ref([])
const recentUploads = ref([])

const trendChartRef = ref()
const topMusicChartRef = ref()
const myTopChartRef = ref()
let trendChart = null
let topMusicChart = null
let myTopChart = null

// 管理员统计卡片
const adminStats = computed(() => [
  {
    title: '用户总数',
    value: monitor.value.userTotal ?? '--',
    icon: User,
    iconBg: 'rgba(67, 83, 255, 0.10)',
    iconColor: '#4353ff',
    trendIcon: Top,
    trendText: `今日 +${monitor.value.todayNewUsers ?? 0}`,
    trendClass: 'up'
  },
  {
    title: '音乐总数',
    value: monitor.value.musicTotal ?? '--',
    icon: Headset,
    iconBg: 'rgba(0, 215, 34, 0.10)',
    iconColor: '#00d722',
    trendIcon: Top,
    trendText: `待审核 ${monitor.value.pendingMusicTotal ?? 0}`,
    trendClass: 'up'
  },
  {
    title: '歌手总数',
    value: monitor.value.singerTotal ?? '--',
    icon: Microphone,
    iconBg: 'rgba(255, 174, 19, 0.10)',
    iconColor: '#ffae13',
    trendIcon: MinusIcon,
    trendText: '实时统计',
    trendClass: 'flat'
  },
  {
    title: '歌单总数',
    value: monitor.value.songListTotal ?? '--',
    icon: Files,
    iconBg: 'rgba(238, 29, 54, 0.10)',
    iconColor: '#ee1d36',
    trendIcon: Bottom,
    trendText: '累计创建',
    trendClass: 'flat'
  }
])

// 歌手统计卡片
const singerStats = computed(() => [
  {
    title: '我的歌曲',
    value: singerDashboard.value.myMusicTotal ?? '--',
    icon: Headset,
    iconBg: 'rgba(67, 83, 255, 0.10)',
    iconColor: '#4353ff',
    trendIcon: Top,
    trendText: `已通过 ${singerDashboard.value.approvedTotal ?? 0}`,
    trendClass: 'up'
  },
  {
    title: '总播放量',
    value: formatNumber(singerDashboard.value.totalListenNumb) ?? '--',
    icon: DataAnalysis,
    iconBg: 'rgba(0, 215, 34, 0.10)',
    iconColor: '#00d722',
    trendIcon: Top,
    trendText: '累计播放',
    trendClass: 'up'
  },
  {
    title: '待审核',
    value: singerDashboard.value.pendingAuditTotal ?? '--',
    icon: Clock,
    iconBg: 'rgba(255, 174, 19, 0.10)',
    iconColor: '#ffae13',
    trendIcon: MinusIcon,
    trendText: '等待管理员审核',
    trendClass: 'flat'
  },
  {
    title: '已通过',
    value: singerDashboard.value.approvedTotal ?? '--',
    icon: Checked,
    iconBg: 'rgba(122, 61, 255, 0.10)',
    iconColor: '#7a3dff',
    trendIcon: Top,
    trendText: '已公开歌曲',
    trendClass: 'up'
  }
])

const stats = computed(() => isAdmin.value ? adminStats.value : singerStats.value)

const quickLinks = computed(() => {
  if (isAdmin.value) {
    return [
      { path: '/manage/user', label: '用户管理', icon: User, bg: 'rgba(67, 83, 255, 0.10)', color: '#4353ff' },
      { path: '/manage/music', label: '歌曲管理', icon: Headset, bg: 'rgba(0, 215, 34, 0.10)', color: '#00d722' },
      { path: '/manage/review', label: '歌曲审核', icon: Checked, bg: 'rgba(255, 174, 19, 0.10)', color: '#ffae13' },
      { path: '/msg/publish', label: '消息中心', icon: Message, bg: 'rgba(238, 29, 54, 0.10)', color: '#ee1d36' },
      { path: '/setting', label: '系统设置', icon: Setting, bg: 'rgba(122, 61, 255, 0.10)', color: '#7a3dff' }
    ]
  }
  return [
    { path: '/manage/upload', label: '发布歌曲', icon: Upload, bg: 'rgba(67, 83, 255, 0.10)', color: '#4353ff' },
    { path: '/manage/my-songs', label: '我的歌曲', icon: Headset, bg: 'rgba(0, 215, 34, 0.10)', color: '#00d722' },
    { path: '/msg/publish', label: '消息中心', icon: Message, bg: 'rgba(238, 29, 54, 0.10)', color: '#ee1d36' },
    { path: '/setting', label: '系统设置', icon: Setting, bg: 'rgba(122, 61, 255, 0.10)', color: '#7a3dff' }
  ]
})

function formatNumber(num) {
  if (num == null) return '--'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

function auditLabel(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

function auditTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

onMounted(async () => {
  await loadDashboardData()
  await nextTick()
  initCharts()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  topMusicChart?.dispose()
  myTopChart?.dispose()
})

async function loadDashboardData() {
  if (isAdmin.value) {
    try {
      const [monitorRes, topRes, trendRes] = await Promise.all([
        getMonitorData(),
        getTopMusic(5),
        getTrend(7)
      ])
      monitor.value = monitorRes.data || {}
      topMusic.value = topRes.data || []
      trend.value = trendRes.data || []
    } catch (e) {
      // 错误已由 axios 拦截器统一提示
    }
  } else {
    try {
      const singerId = userInfo.value?.userId
      const res = await getSingerDashboard(singerId)
      singerDashboard.value = res.data || {}
      myTopMusic.value = singerDashboard.value.myTopMusic || []
      recentUploads.value = singerDashboard.value.recentUploads || []
    } catch (e) {
      // 错误已由 axios 拦截器统一提示
    }
  }
}

function initCharts() {
  if (isAdmin.value) {
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['新增用户', '新增歌曲'] },
        grid: { left: 40, right: 20, top: 40, bottom: 30 },
        xAxis: { type: 'category', data: trend.value.map((i) => i.date) },
        yAxis: { type: 'value' },
        series: [
          { name: '新增用户', type: 'line', smooth: true, data: trend.value.map((i) => i.userCount), itemStyle: { color: '#4353ff' } },
          { name: '新增歌曲', type: 'line', smooth: true, data: trend.value.map((i) => i.musicCount), itemStyle: { color: '#00d722' } }
        ]
      })
    }

    if (topMusicChartRef.value) {
      topMusicChart = echarts.init(topMusicChartRef.value)
      const names = topMusic.value.map((i) => i.musicName)
      const values = topMusic.value.map((i) => i.listenNumb)
      topMusicChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 120, right: 30, top: 20, bottom: 30 },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: names.reverse() },
        series: [
          {
            name: '播放量',
            type: 'bar',
            data: values.reverse(),
            itemStyle: { color: '#4353ff', borderRadius: [0, 4, 4, 0] },
            barWidth: 16
          }
        ]
      })
    }
  } else {
    if (myTopChartRef.value && myTopMusic.value.length > 0) {
      myTopChart = echarts.init(myTopChartRef.value)
      const names = myTopMusic.value.map((i) => i.musicName)
      const values = myTopMusic.value.map((i) => i.listenNumb)
      myTopChart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 120, right: 30, top: 20, bottom: 30 },
        xAxis: { type: 'value' },
        yAxis: { type: 'category', data: names.reverse() },
        series: [
          {
            name: '播放量',
            type: 'bar',
            data: values.reverse(),
            itemStyle: { color: '#4353ff', borderRadius: [0, 4, 4, 0] },
            barWidth: 16
          }
        ]
      })
    }
  }
}

function resizeCharts() {
  trendChart?.resize()
  topMusicChart?.resize()
  myTopChart?.resize()
}
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-2xl);
}

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
  .eyebrow {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    color: var(--brand-accent);
    margin-bottom: 8px;
  }
  h2 {
    font-size: 26px;
    font-weight: 600;
    letter-spacing: -0.4px;
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
  }
}
.welcome-visual {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  .glow-ring {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(67, 83, 255, 0.3) 0%, transparent 70%);
      animation: pulseRing 2800ms ease-out infinite;
  }
  .welcome-icon {
    font-size: 56px;
    color: var(--brand-accent);
    position: relative;
    z-index: 1;
  }
}

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
  box-shadow: var(--shadow-sm);
  transition: transform 200ms ease, box-shadow 200ms ease;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;
  .el-icon { font-size: 22px; }
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--wf-ink);
  line-height: 1.1;
}
.stat-title {
  font-size: 13px;
  color: var(--wf-mute);
  margin-top: 6px;
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
  &.up { color: var(--wf-accent-green); background: rgba(0, 215, 34, 0.1); }
  &.flat { color: var(--wf-mute); background: rgba(137, 137, 137, 0.1); }
}

.recent-panel {
  .empty-hint {
    text-align: center;
    color: var(--wf-mute);
    font-size: 14px;
    padding: 40px 0;
  }
  .recent-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .recent-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: var(--rounded-md);
    background: var(--wf-canvas-soft);
    transition: background 150ms;
    &:hover { background: var(--wf-canvas-soft-hover); }
  }
  .recent-idx {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: var(--brand-accent);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  .recent-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }
  .recent-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--wf-ink);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .recent-date {
    font-size: 12px;
    color: var(--wf-mute);
  }
}

.chart-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
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
  margin-bottom: 16px;
  .panel-title { font-size: 16px; font-weight: 600; color: var(--wf-ink); }
  .panel-tag { font-size: 11px; color: var(--wf-mute); background: var(--wf-canvas-soft); padding: 2px 8px; border-radius: var(--rounded-pill); }
}
.chart-box {
  width: 100%;
  height: 320px;
}

.quick-panel {
  .quick-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    border-radius: var(--rounded-md);
    border: 1px solid transparent;
    transition: all 150ms ease;
    cursor: pointer;
    text-decoration: none;
    &:hover {
      background: var(--wf-canvas-soft);
      border-color: var(--wf-hairline);
    }
  }
  .quick-icon {
    width: 42px;
    height: 42px;
    border-radius: var(--rounded-md);
    display: flex;
    align-items: center;
    justify-content: center;
    .el-icon { font-size: 20px; }
  }
  .quick-label {
    font-size: 12.5px;
    color: var(--wf-body-mid);
    font-weight: 500;
  }
}

.intro-panel {
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

@media (max-width: 1280px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 900px) {
  .chart-row { grid-template-columns: 1fr; }
  .quick-panel .quick-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>