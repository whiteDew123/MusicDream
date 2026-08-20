<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-decor"></div>
      <div class="welcome-content">
        <div class="welcome-left">
          <div class="avatar-wrap">
            <el-avatar
              :size="56"
              :src="userInfo?.imageUrl ? '/api' + userInfo.imageUrl : ''"
              class="welcome-avatar"
            >
              <el-icon :size="28"><UserFilled /></el-icon>
            </el-avatar>
            <span class="online-dot" />
          </div>
          <div class="welcome-text">
            <p class="greeting">{{ timeGreeting }}，{{ userInfo?.username || '管理员' }}</p>
            <p class="welcome-sub">
              欢迎回到 MusicDreamer 后台管理系统 · {{ currentDate }}
            </p>
          </div>
        </div>
        <div class="welcome-right">
          <div class="mini-stat">
            <span class="mini-label">系统状态</span>
            <span class="mini-value online">
              <span class="status-dot" /> 运行中
            </span>
          </div>
          <div class="mini-stat">
            <span class="mini-label">服务节点</span>
            <span class="mini-value">{{ serverCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div
        v-for="(item, idx) in stats"
        :key="item.title"
        class="stat-card"
        :style="{ '--delay': `${idx * 60}ms` }"
        v-loading="monitorLoading"
      >
        <div class="stat-top">
          <div class="stat-icon" :style="{ background: item.iconBg }">
            <el-icon :style="{ color: item.iconColor }">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-trend" :class="item.trendClass">
            <el-icon><component :is="item.trendIcon" /></el-icon>
            <span>{{ item.trendText }}</span>
          </div>
        </div>
        <div class="stat-value-row">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-unit">{{ item.unit }}</span>
        </div>
        <div class="stat-title">{{ item.title }}</div>
        <div class="stat-footer">
          <span class="stat-desc">{{ item.desc }}</span>
        </div>
      </div>
    </div>

    <!-- 下方内容区：日志 + 排行榜 -->
    <div class="content-row">
      <!-- 最近日志 -->
      <div class="panel log-panel">
        <div class="panel-header">
          <div class="panel-title-wrap">
            <h3 class="panel-title">最近操作日志</h3>
            <span class="panel-sub">实时追踪管理员操作记录</span>
          </div>
          <div class="panel-actions">
            <el-button
              type="primary"
              plain
              size="small"
              @click="$router.push('/log')"
            >
              查看全部
            </el-button>
          </div>
        </div>

        <div class="log-table-wrap">
          <el-table
            :data="logList"
            stripe
            style="width: 100%"
            v-loading="logLoading"
            :header-cell-style="{ background: 'transparent', color: 'var(--wf-mute)', fontWeight: 500, fontSize: '12px' }"
            empty-text="暂无日志记录"
          >
            <el-table-column prop="userName" label="操作人" width="100">
              <template #default="{ row }">
                <div class="user-cell">
                  <el-avatar :size="28" class="log-avatar">
                    {{ row.userName?.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <span class="user-name">{{ row.userName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="doSome" label="操作内容" min-width="180">
              <template #default="{ row }">
                <span class="log-action">{{ row.doSome }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="musicName" label="音乐" min-width="140">
              <template #default="{ row }">
                <span v-if="row.musicName" class="music-name">{{ row.musicName }}</span>
                <span v-else class="text-muted">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="createDate" label="时间" width="160">
              <template #default="{ row }">
                <span class="log-time">{{ formatTime(row.createDate) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 热门音乐排行榜 -->
      <div class="panel chart-panel">
        <div class="panel-header">
          <div class="panel-title-wrap">
            <h3 class="panel-title">热门音乐 TOP 5</h3>
            <span class="panel-sub">按播放量排行</span>
          </div>
          <div class="rank-badge">
            <el-icon><TrendCharts /></el-icon>
            <span>TOP 5</span>
          </div>
        </div>

        <div class="chart-list" v-if="rankList.length && !rankLoading">
          <div
            v-for="(item, idx) in rankList"
            :key="item.musicId"
            class="chart-row"
          >
            <div class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</div>
            <div class="chart-info">
              <div class="chart-label">
                <span class="chart-title">{{ item.musicName }}</span>
                <span class="chart-play">{{ formatNumber(item.listenNumb) }} 次</span>
              </div>
              <div class="chart-bar">
                <div
                  class="chart-bar-fill"
                  :style="{ width: getBarWidth(item.listenNumb) + '%' }"
                />
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="rankLoading" class="chart-empty">
          <el-icon :size="32" class="is-loading"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        <div v-else class="chart-empty">
          <el-icon :size="48"><Headset /></el-icon>
          <p>暂无排行榜数据</p>
        </div>
      </div>
    </div>

    <!-- 底部快捷入口 -->
    <div class="quick-bar">
      <div
        v-for="q in quickLinks"
        :key="q.label"
        class="quick-chip"
        @click="goTo(q.path)"
      >
        <div class="chip-icon" :style="{ background: q.bg, color: q.color }">
          <el-icon><component :is="q.icon" /></el-icon>
        </div>
        <span class="chip-label">{{ q.label }}</span>
        <el-icon class="chip-arrow"><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  UserFilled,
  User,
  Headset,
  Microphone,
  Document,
  DataAnalysis,
  Top,
  Bottom,
  Minus,
  TrendCharts,
  ArrowRight,
  Setting,
  ChatDotRound,
  Loading
} from '@element-plus/icons-vue'
import { getMonitorData, getTopMusic, pageLog } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// ========== 时间问候语 ==========
const currentDate = ref('')
const timeGreeting = ref('你好')

function updateTime() {
  const now = new Date()
  const h = now.getHours()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][now.getDay()]
  currentDate.value = `${y}-${m}-${d} ${week}`

  if (h < 6) timeGreeting.value = '凌晨好'
  else if (h < 12) timeGreeting.value = '早上好'
  else if (h < 14) timeGreeting.value = '中午好'
  else if (h < 18) timeGreeting.value = '下午好'
  else timeGreeting.value = '晚上好'
}
updateTime()
setInterval(updateTime, 60000)

// ========== 统计卡片 ==========
const monitorLoading = ref(false)
const stats = ref([
  { title: '用户总数', value: 0, unit: '人', icon: User,
    iconBg: 'rgba(67, 83, 255, 0.10)', iconColor: '#4353ff',
    trendIcon: Top, trendText: '实时', trendClass: 'up', desc: '不含管理员' },
  { title: '音乐总数', value: 0, unit: '首', icon: Headset,
    iconBg: 'rgba(0, 215, 34, 0.10)', iconColor: '#00d722',
    trendIcon: Top, trendText: '实时', trendClass: 'up', desc: '正常状态' },
  { title: '歌手总数', value: 0, unit: '人', icon: Microphone,
    iconBg: 'rgba(255, 174, 19, 0.10)', iconColor: '#ffae13',
    trendIcon: Minus, trendText: '实时', trendClass: 'flat', desc: 'role=1' },
  { title: '今日日志', value: 0, unit: '条', icon: Document,
    iconBg: 'rgba(238, 29, 54, 0.10)', iconColor: '#ee1d36',
    trendIcon: Bottom, trendText: '今日', trendClass: 'down', desc: '当天操作' }
])

// ========== 日志列表 ==========
const logList = ref([])
const logLoading = ref(false)

// ========== 排行榜 ==========
const rankList = ref([])
const rankLoading = ref(false)
const maxPlayCount = computed(() => {
  if (!rankList.value.length) return 1
  return Math.max(...rankList.value.map((i) => i.listenNumb || 0))
})

function getBarWidth(value) {
  if (!maxPlayCount.value) return 0
  return Math.max(8, (value / maxPlayCount.value) * 100)
}

function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

function formatTime(time) {
  if (!time) return ''
  let d = null
  if (time instanceof Date) {
    d = time
  } else if (typeof time === 'string') {
    d = new Date(time)
  } else if (typeof time === 'number') {
    d = new Date(time)
  }
  if (!d || isNaN(d.getTime())) return String(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ========== 快捷入口 ==========
const quickLinks = [
  { path: '/user', label: '用户管理', icon: User, bg: 'rgba(67, 83, 255, 0.10)', color: '#4353ff' },
  { path: '/music', label: '歌曲管理', icon: Headset, bg: 'rgba(0, 215, 34, 0.10)', color: '#00d722' },
  { path: '/log', label: '日志管理', icon: Document, bg: 'rgba(255, 174, 19, 0.10)', color: '#ffae13' },
  { path: '/data', label: '数据统计', icon: DataAnalysis, bg: 'rgba(122, 61, 255, 0.10)', color: '#7a3dff' },
  { path: '/setting', label: '系统设置', icon: Setting, bg: 'rgba(20, 110, 245, 0.10)', color: '#146ef5' },
  { path: '/msg/publish', label: '消息发布', icon: ChatDotRound, bg: 'rgba(238, 29, 54, 0.10)', color: '#ee1d36' }
]

function goTo(path) {
  router.push(path)
}

const serverCount = ref(3)

// ========== 加载仪表盘真实数据 ==========
async function loadDashboardData() {
  monitorLoading.value = true
  logLoading.value = true
  rankLoading.value = true

  // 并行加载：统计数据 + 日志 + 排行榜
  const results = await Promise.allSettled([
    getMonitorData(),
    pageLog(1, 5),
    getTopMusic(5)
  ])

  // 1. 统计数据
  if (results[0].status === 'fulfilled' && results[0].value?.data) {
    const d = results[0].value.data
    stats.value[0].value = d.userCount || 0
    stats.value[1].value = d.musicCount || 0
    stats.value[2].value = d.singerCount || 0
    stats.value[3].value = d.todayLogCount || 0
  }
  monitorLoading.value = false

  // 2. 日志列表
  if (results[1].status === 'fulfilled' && results[1].value?.data) {
    const pageData = results[1].value.data
    logList.value = pageData.records || pageData.list || []
  }
  logLoading.value = false

  // 3. 排行榜
  if (results[2].status === 'fulfilled' && results[2].value?.data) {
    rankList.value = results[2].value.data || []
  }
  rankLoading.value = false
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
  padding: 4px 2px;
}

/* ========== 欢迎横幅 ========== */
.welcome-banner {
  position: relative;
  background: linear-gradient(135deg, #f8f9ff 0%, #eef2ff 50%, #f0f9ff 100%);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-lg);
  padding: 28px 32px;
  overflow: hidden;
}
.welcome-decor {
  position: absolute;
  right: -40px;
  top: -40px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(67, 83, 255, 0.12) 0%, transparent 70%);
}
.welcome-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}
.welcome-left {
  display: flex;
  align-items: center;
  gap: 18px;
}
.avatar-wrap {
  position: relative;
}
.welcome-avatar {
  background: linear-gradient(135deg, var(--brand-accent) 0%, var(--wf-accent-blue) 100%);
  color: #fff;
  border: 3px solid #fff;
  box-shadow: 0 4px 16px rgba(67, 83, 255, 0.25);
}
.online-dot {
  position: absolute;
  right: 0;
  bottom: 2px;
  width: 14px;
  height: 14px;
  background: #00d722;
  border: 3px solid #fff;
  border-radius: 50%;
}
.welcome-text {
  .greeting {
    font-size: 22px;
    font-weight: 600;
    color: var(--wf-ink);
    margin-bottom: 4px;
  }
  .welcome-sub {
    font-size: 13px;
    color: var(--wf-body-mid);
  }
}
.welcome-right {
  display: flex;
  gap: 32px;
  padding-left: 32px;
  border-left: 1px solid rgba(216, 216, 216, 0.6);
}
.mini-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.mini-label {
  font-size: 12px;
  color: var(--wf-mute);
}
.mini-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--wf-ink);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.mini-value.online {
  color: var(--wf-accent-green);
}
.status-dot {
  width: 8px;
  height: 8px;
  background: var(--wf-accent-green);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(0, 215, 34, 0.2);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ========== 统计卡片 ========== */
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
  position: relative;
  box-shadow: var(--shadow-sm);
  transition: transform 200ms ease, box-shadow 200ms ease, border-color 200ms ease;
  animation: cardEnter 500ms ease-out both;
  animation-delay: var(--delay, 0ms);
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
  border-color: #c5c5c5;
}
@keyframes cardEnter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
.stat-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;
  .el-icon { font-size: 22px; }
}
.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: var(--rounded-pill);
  .el-icon { font-size: 11px; }
  &.up { color: var(--wf-accent-green); background: rgba(0, 215, 34, 0.10); }
  &.down { color: var(--wf-accent-red); background: rgba(238, 29, 54, 0.10); }
  &.flat { color: var(--wf-mute); background: rgba(137, 137, 137, 0.10); }
}
.stat-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--wf-ink);
  line-height: 1.1;
}
.stat-unit {
  font-size: 14px;
  color: var(--wf-mute);
}
.stat-title {
  font-size: 14px;
  color: var(--wf-body);
  font-weight: 500;
  margin-bottom: 4px;
}
.stat-footer {
  .stat-desc {
    font-size: 12px;
    color: var(--wf-mute);
  }
}

/* ========== 下方两列布局 ========== */
.content-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: var(--spacing-lg);
}
.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}
.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 18px;
}
.panel-title-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--wf-ink);
  letter-spacing: -0.2px;
}
.panel-sub {
  font-size: 12px;
  color: var(--wf-mute);
}
.panel-actions {
  flex-shrink: 0;
}

/* 日志表格 */
.log-table-wrap {
  :deep(.el-table) {
    --el-table-border-color: var(--wf-hairline);
    --el-table-header-bg-color: transparent;
    --el-table-row-hover-bg-color: var(--wf-row-hover);
    font-size: 13px;
  }
  :deep(.el-table th.el-table__cell) {
    background: transparent !important;
  }
  :deep(.el-table td.el-table__cell) {
    padding: 10px 0;
  }
  :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
    background: #fafbfc;
  }
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.log-avatar {
  background: linear-gradient(135deg, var(--brand-accent), var(--wf-accent-blue));
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.user-name {
  font-weight: 500;
  color: var(--wf-ink);
}
.log-action {
  color: var(--wf-body);
}
.music-name {
  color: var(--wf-ink);
  font-weight: 500;
}
.text-muted {
  color: var(--wf-mute-soft);
}
.log-time {
  font-size: 12px;
  color: var(--wf-mute);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

/* 排行榜 */
.rank-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 11px;
  color: var(--brand-accent);
  background: rgba(67, 83, 255, 0.08);
  border-radius: var(--rounded-pill);
  font-weight: 500;
  .el-icon { font-size: 12px; }
}
.chart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 8px;
}
.chart-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.rank-num {
  width: 26px;
  height: 26px;
  flex-shrink: 0;
  border-radius: var(--rounded-sm);
  background: var(--wf-canvas-soft);
  color: var(--wf-mute);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  &.rank-1 {
    background: linear-gradient(135deg, #ffd700, #ffb300);
    color: #fff;
  }
  &.rank-2 {
    background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
    color: #fff;
  }
  &.rank-3 {
    background: linear-gradient(135deg, #cd7f32, #b87333);
    color: #fff;
  }
}
.chart-info {
  flex: 1;
  min-width: 0;
}
.chart-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.chart-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--wf-ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}
.chart-play {
  font-size: 12px;
  color: var(--wf-mute);
  font-weight: 500;
}
.chart-bar {
  height: 8px;
  background: var(--wf-canvas-soft);
  border-radius: var(--rounded-pill);
  overflow: hidden;
}
.chart-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--brand-accent) 0%, var(--wf-accent-blue) 100%);
  border-radius: var(--rounded-pill);
  transition: width 800ms cubic-bezier(0.22, 1, 0.36, 1);
  animation: barGrow 1s ease-out both;
}
@keyframes barGrow {
  from { width: 0 !important; }
}
.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 0;
  color: var(--wf-mute);
  p { font-size: 13px; }
}

/* ========== 底部快捷入口 ========== */
.quick-bar {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--spacing-md);
}
.quick-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  cursor: pointer;
  transition: all 180ms ease;
  box-shadow: var(--shadow-sm);
}
.quick-chip:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: #c5c5c5;
  .chip-arrow { opacity: 1; transform: translateX(0); }
}
.chip-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: var(--rounded-md);
  display: flex;
  align-items: center;
  justify-content: center;
  .el-icon { font-size: 18px; }
}
.chip-label {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: var(--wf-ink);
}
.chip-arrow {
  opacity: 0;
  color: var(--wf-mute);
  font-size: 14px;
  transition: all 180ms ease;
}

/* ========== 响应式 ========== */
@media (max-width: 1280px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-bar { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 900px) {
  .content-row { grid-template-columns: 1fr; }
  .welcome-content { flex-direction: column; align-items: flex-start; gap: 20px; }
  .welcome-right { padding-left: 0; border-left: none; width: 100%; justify-content: space-around; }
  .quick-bar { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .stat-grid { grid-template-columns: 1fr; }
  .quick-bar { grid-template-columns: 1fr; }
  .welcome-banner { padding: 20px; }
}
</style>