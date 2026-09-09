<template>
  <div class="achievement-page">
    <!-- 页头 -->
    <header class="page-header">
      <span class="header-icon-wrap">
        <el-icon class="header-icon"><Trophy /></el-icon>
      </span>
      <h2 class="header-title">听歌成就</h2>
    </header>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-wrap">
      <div class="skeleton-card"></div>
      <div class="skeleton-grid">
        <div class="skeleton-badge" v-for="n in 9" :key="n"></div>
      </div>
    </div>

    <!-- 内容区 -->
    <template v-else>
      <!-- 顶部：等级卡片 -->
      <section class="level-card">
        <div class="level-main">
          <div class="level-badge">
            <span class="level-num">Lv.{{ levelInfo.level || 1 }}</span>
          </div>
          <div class="level-meta">
            <div class="level-points">
              <span class="points-num">{{ levelInfo.points || 0 }}</span>
              <span class="points-label">积分</span>
            </div>
            <div class="level-progress">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: progressPercent + '%' }"
                ></div>
              </div>
              <div class="progress-text">
                <template v-if="levelInfo.pointsToNext > 0">
                  距 Lv.{{ (levelInfo.level || 1) + 1 }} 还差 {{ levelInfo.pointsToNext }} 积分
                </template>
                <template v-else>
                  已达满级，继续解锁成就获取积分
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 打卡区 -->
        <div class="checkin-block">
          <div class="checkin-info">
            <el-icon class="checkin-icon"><Calendar /></el-icon>
            <div>
              <div class="checkin-streak">
                连续打卡 <b>{{ levelInfo.checkinStreak || 0 }}</b> 天
              </div>
              <div class="checkin-tip">
                {{ levelInfo.checkedInToday ? '今日已打卡，明日再来' : '今日还未打卡，打卡 +5 积分' }}
              </div>
            </div>
          </div>
          <button
            class="checkin-btn"
            :disabled="levelInfo.checkedInToday || checkinLoading"
            @click="handleCheckin"
          >
            {{ checkinLoading ? '打卡中…' : (levelInfo.checkedInToday ? '已打卡' : '立即打卡') }}
          </button>
        </div>
      </section>

      <!-- 成就墙：按档位分组 -->
      <section
        v-for="tier in tierList"
        :key="tier.key"
        class="tier-section"
      >
        <div class="section-title">
          <span class="title-accent" :class="'tier-' + tier.key"></span>
          <h3>{{ tier.label }}</h3>
          <span class="tier-count">{{ tier.unlocked }}/{{ tier.badges.length }}</span>
        </div>

        <div class="badge-grid">
          <div
            v-for="badge in tier.badges"
            :key="badge.ruleId"
            class="badge-item"
            :class="{ unlocked: badge.unlocked, locked: !badge.unlocked }"
          >
            <div class="badge-icon-wrap">
              <el-icon class="badge-icon"><component :is="badgeIcon(badge)" /></el-icon>
              <el-icon v-if="!badge.unlocked" class="badge-lock"><Lock /></el-icon>
            </div>
            <div class="badge-name">{{ badge.name }}</div>
            <div class="badge-desc">{{ badge.description }}</div>
            <div v-if="!badge.unlocked" class="badge-progress">
              <div class="mini-progress-bar">
                <div
                  class="mini-progress-fill"
                  :style="{ width: miniPercent(badge) + '%' }"
                ></div>
              </div>
              <span class="mini-progress-text">{{ badge.progress || 0 }}/{{ badge.target }}</span>
            </div>
            <div v-else class="badge-unlock-time">
              {{ formatDate(badge.unlockTime) }} 解锁
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Trophy, Calendar, Lock, Headset, Star, Medal } from '@element-plus/icons-vue'
import {
  achievementWallApi,
  levelInfoApi,
  checkinApi
} from '@/api/achievement'

const loading = ref(true)
const checkinLoading = ref(false)
const badges = ref([])
const levelInfo = ref({})

// 档位分组
const tierList = computed(() => {
  const groups = [
    { key: 'bronze', label: '铜级成就', unlocked: 0, badges: [] },
    { key: 'silver', label: '银级成就', unlocked: 0, badges: [] },
    { key: 'gold', label: '金级成就', unlocked: 0, badges: [] }
  ]
  const tierIndex = { 1: 0, 2: 1, 3: 2 }
  for (const b of badges.value) {
    const idx = tierIndex[b.tier] ?? 0
    groups[idx].badges.push(b)
    if (b.unlocked) groups[idx].unlocked++
  }
  return groups.filter((g) => g.badges.length > 0)
})

// 等级进度条百分比
const progressPercent = computed(() => {
  if (!levelInfo.value || levelInfo.value.pointsToNext == null) return 0
  if (levelInfo.value.pointsToNext === 0) return 100
  const min = levelInfo.value.currentLevelMin || 0
  const max = levelInfo.value.nextLevelMin || 100
  const cur = levelInfo.value.points || 0
  if (max <= min) return 100
  return Math.min(100, Math.max(0, ((cur - min) / (max - min)) * 100))
})

// 单个徽章进度百分比
function miniPercent(badge) {
  if (!badge.target) return 0
  return Math.min(100, Math.max(0, ((badge.progress || 0) / badge.target) * 100))
}

// 徽章图标（按类型）
function badgeIcon(badge) {
  if (badge.type === 'LISTEN_TOTAL') return Headset
  if (badge.type === 'CHECKIN') return Calendar
  if (badge.type === 'LIKE_TOTAL') return Star
  return Medal
}

// 格式化日期
function formatDate(dt) {
  if (!dt) return ''
  return String(dt).substring(0, 10)
}

// 加载成就墙与等级
async function loadData() {
  loading.value = true
  try {
    const [wallRes, levelRes] = await Promise.all([
      achievementWallApi(),
      levelInfoApi()
    ])
    badges.value = wallRes.data || []
    levelInfo.value = levelRes.data || {}
  } catch (e) {
    // 拦截器已处理提示
  } finally {
    loading.value = false
  }
}

// 打卡
async function handleCheckin() {
  checkinLoading.value = true
  try {
    const res = await checkinApi()
    const data = res.data || {}
    if (data.success) {
      ElMessage.success(data.message || '打卡成功')
      // 若新解锁成就，额外提示
      if (data.unlockedAchievements && data.unlockedAchievements.length > 0) {
        ElMessage({
          message: '解锁新成就：' + data.unlockedAchievements.join('、'),
          type: 'success',
          duration: 3500
        })
      }
    } else {
      ElMessage.info(data.message || '今日已打卡')
    }
    // 刷新数据
    await loadData()
  } catch (e) {
    // 拦截器已处理
  } finally {
    checkinLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.achievement-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 8px 0 40px;
}

/* ===== 页头 ===== */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  .header-icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: var(--rounded-md);
    background: rgba(94, 92, 230, 0.1);
  }

  .header-icon {
    font-size: 22px;
    color: var(--st-primary);
  }

  .header-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.2px;
  }
}

/* ===== 骨架屏 ===== */
.skeleton-wrap {
  .skeleton-card {
    height: 160px;
    border-radius: var(--rounded-lg);
    background: var(--st-canvas);
    border: 1px solid var(--st-hairline);
    margin-bottom: 24px;
    background: linear-gradient(90deg, var(--st-canvas) 25%, var(--st-input-bg) 50%, var(--st-canvas) 75%);
    background-size: 200% 100%;
    animation: shimmer 1.4s infinite;
  }
  .skeleton-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  .skeleton-badge {
    height: 180px;
    border-radius: var(--rounded-lg);
    background: var(--st-canvas);
    background: linear-gradient(90deg, var(--st-canvas) 25%, var(--st-input-bg) 50%, var(--st-canvas) 75%);
    background-size: 200% 100%;
    animation: shimmer 1.4s infinite;
  }
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ===== 等级卡片 ===== */
.level-card {
  padding: 24px 28px;
  margin-bottom: 24px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-sm);
}

.level-main {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--st-hairline);
  margin-bottom: 20px;
}

.level-badge {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #5e5ce6, #7c7af0);
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(94, 92, 230, 0.32);

  .level-num {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: -0.5px;
  }
}

.level-meta {
  flex: 1;
  min-width: 0;
}

.level-points {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;

  .points-num {
    font-size: 28px;
    font-weight: 700;
    color: var(--st-ink);
    letter-spacing: -0.5px;
  }
  .points-label {
    font-size: 13px;
    color: var(--st-ink-mute);
  }
}

.level-progress {
  .progress-bar {
    height: 8px;
    border-radius: var(--rounded-pill);
    background: var(--st-input-bg);
    overflow: hidden;
  }
  .progress-fill {
    height: 100%;
    border-radius: var(--rounded-pill);
    background: linear-gradient(90deg, #5e5ce6, #7c7af0);
    transition: width 400ms ease;
  }
  .progress-text {
    margin-top: 8px;
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}

.checkin-block {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.checkin-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .checkin-icon {
    font-size: 22px;
    color: var(--st-primary);
    flex-shrink: 0;
  }
  .checkin-streak {
    font-size: 14px;
    color: var(--st-ink);
    b {
      color: var(--st-primary);
      font-size: 16px;
      margin: 0 2px;
    }
  }
  .checkin-tip {
    font-size: 12px;
    color: var(--st-ink-mute);
    margin-top: 2px;
  }
}

.checkin-btn {
  padding: 8px 20px;
  border: none;
  border-radius: var(--rounded-md);
  background: var(--st-primary);
  color: var(--st-canvas);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 200ms ease, transform 150ms ease, opacity 200ms ease;
  white-space: nowrap;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover);
    transform: translateY(-1px);
  }
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  &:disabled {
    background: var(--st-input-bg);
    color: var(--st-ink-mute);
    cursor: not-allowed;
  }
}

/* ===== 档位分区 ===== */
.tier-section {
  margin-bottom: 28px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;

  .title-accent {
    width: 4px;
    height: 16px;
    border-radius: var(--rounded-pill);
    &.tier-bronze { background: #cd7f32; }
    &.tier-silver { background: #b0b0b0; }
    &.tier-gold   { background: #d4af37; }
  }

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--st-ink);
  }

  .tier-count {
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}

/* ===== 徽章网格 ===== */
.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.badge-item {
  padding: 20px 18px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: transform 200ms ease, box-shadow 200ms ease, opacity 200ms ease;

  &.unlocked {
    border-color: var(--st-primary);
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.12);
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(94, 92, 230, 0.2);
    }
  }

  &.locked {
    opacity: 0.65;
    .badge-icon {
      color: var(--st-ink-mute);
      filter: grayscale(1);
    }
  }
}

.badge-icon-wrap {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(94, 92, 230, 0.08);
  margin-bottom: 12px;

  .badge-icon {
    font-size: 28px;
    color: var(--st-primary);
  }

  .badge-lock {
    position: absolute;
    right: -2px;
    bottom: -2px;
    font-size: 14px;
    color: var(--st-ink-mute);
    background: var(--st-canvas);
    border-radius: 50%;
    padding: 2px;
  }
}

.badge-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--st-ink);
  margin-bottom: 4px;
}

.badge-desc {
  font-size: 12px;
  color: var(--st-ink-mute);
  line-height: 1.4;
  margin-bottom: 12px;
  min-height: 16px;
}

.badge-progress {
  width: 100%;

  .mini-progress-bar {
    height: 4px;
    border-radius: var(--rounded-pill);
    background: var(--st-input-bg);
    overflow: hidden;
    margin-bottom: 6px;
  }
  .mini-progress-fill {
    height: 100%;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
    transition: width 300ms ease;
  }
  .mini-progress-text {
    font-size: 11px;
    color: var(--st-ink-mute);
  }
}

.badge-unlock-time {
  font-size: 11px;
  color: var(--st-primary);
}
</style>
