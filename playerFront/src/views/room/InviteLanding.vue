<template>
  <div class="invite-page">
    <!-- Stripe 式渐变网格背景 -->
    <div class="mesh-bg"></div>

    <div class="invite-card">
      <div class="brand-mark">
        <el-icon><Headset /></el-icon>
      </div>
      <p class="eyebrow">MusicDreamer · 一起听</p>

      <!-- 加载中 -->
      <div v-if="loading" class="center-hint">
        <el-icon class="spin"><Loading /></el-icon>
        <span>正在加载房间…</span>
      </div>

      <!-- 邀请失败 -->
      <div v-else-if="failed" class="center-hint fail">
        <el-icon class="err-icon"><WarningFilled /></el-icon>
        <p class="fail-title">{{ failMsg }}</p>
        <el-button round @click="goPlaza">去播放室广场看看</el-button>
      </div>

      <!-- 房间信息 -->
      <template v-else-if="room">
        <div class="room-cover" :style="{ background: coverBg }">
          <img v-if="room.cover" :src="room.cover" />
          <span v-else class="cover-symbol">♪</span>
        </div>
        <h1 class="room-name">{{ room.name }}</h1>
        <p class="room-owner">
          <el-icon><User /></el-icon>
          {{ room.ownerName || '房主' }} 创建的房间
        </p>

        <div class="room-metrics">
          <div class="metric">
            <span class="metric-value">{{ room.memberCount }}/{{ room.maxMembers }}</span>
            <span class="metric-label">房间人数</span>
          </div>
          <div class="metric">
            <span class="metric-value">{{ statusText(room.status) }}</span>
            <span class="metric-label">房间状态</span>
          </div>
          <div class="metric">
            <span class="metric-value code">{{ room.inviteCode }}</span>
            <span class="metric-label">邀请码</span>
          </div>
        </div>

        <div class="actions">
          <el-button v-if="room.isMember" type="primary" class="cta" round @click="enterRoom">
            进入房间
          </el-button>
          <el-button v-else type="primary" class="cta" round :loading="joining" @click="joinRoom">
            加入房间
          </el-button>
        </div>
        <p class="invite-tip">加入后即可一起听歌、排队点歌、边听边聊</p>
      </template>
      <el-empty v-else />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Headset, User, Loading, WarningFilled } from '@element-plus/icons-vue'
import { inviteRoomApi, joinRoomApi } from '@/api/room'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const code = ref(route.params.code)
const loading = ref(true)
const failed = ref(false)
const failMsg = ref('')
const room = ref(null)
const joining = ref(false)

onMounted(() => {
  loadInvite()
})

// 通过邀请码加载房间信息
async function loadInvite() {
  loading.value = true
  failed.value = false
  try {
    const res = await inviteRoomApi(code.value)
    room.value = res.data
  } catch (e) {
    failed.value = true
    failMsg.value = e.message || '邀请码无效或房间不存在'
  } finally {
    loading.value = false
  }
}

// 加入房间
async function joinRoom() {
  // 未登录：先跳登录，登录后回到本页
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: `/invite/${code.value}` } })
    return
  }
  joining.value = true
  try {
    const res = await joinRoomApi(room.value.id)
    ElMessage.success('加入成功')
    router.push(`/room/${room.value.id}`)
  } catch (e) {
    // 失败提示已由拦截器处理
  } finally {
    joining.value = false
  }
}

function enterRoom() {
  router.push(`/room/${room.value.id}`)
}

function goPlaza() {
  router.push('/rooms')
}

// 封面背景
const coverBg = computed(() => {
  const colors = ['#6341FF', '#7B5EFF', '#4E2AEE', '#1c1e54', '#665EFD']
  const idx = (room.value?.id ?? 0) % colors.length
  return `linear-gradient(135deg, ${colors[idx]}, ${colors[(idx + 1) % colors.length]})`
})

function statusText(status) {
  if (status === 1) return '播放中'
  if (status === 2) return '已结束'
  return '空闲'
}
</script>

<style scoped lang="scss">
.invite-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  overflow: hidden;
  background: var(--st-canvas-soft);
}

/* 顶部渐变网格（Stripe 式） */
.mesh-bg {
  position: absolute;
  inset: 0 0 auto 0;
  height: 45%;
  background:
    radial-gradient(60% 80% at 20% 20%, rgba(245, 233, 212, 0.9), transparent 60%),
    radial-gradient(50% 70% at 80% 10%, rgba(249, 107, 238, 0.35), transparent 60%),
    radial-gradient(50% 70% at 60% 30%, rgba(99, 65, 255, 0.35), transparent 60%),
    radial-gradient(55% 75% at 30% 40%, rgba(234, 34, 97, 0.2), transparent 60%);
  filter: blur(40px);
}

.invite-card {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-xl);
  box-shadow: var(--shadow-2);
  padding: 40px 32px;
  text-align: center;
}

.brand-mark {
  width: 52px;
  height: 52px;
  margin: 0 auto 12px;
  border-radius: var(--rounded-lg);
  background: linear-gradient(135deg, var(--st-primary), var(--st-primary-soft));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 8px 24px rgba(99, 65, 255, 0.3);
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.5px;
  color: var(--st-ink-mute);
  margin-bottom: 24px;
}

.room-cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: var(--rounded-lg);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  img { width: 100%; height: 100%; object-fit: cover; }
  .cover-symbol { font-size: 64px; color: rgba(255, 255, 255, 0.85); }
}

.room-name {
  font-size: 24px;
  font-weight: 300;
  letter-spacing: -0.4px;
  color: var(--st-ink);
  margin-bottom: 6px;
  word-break: break-word;
}

.room-owner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 13px;
  color: var(--st-ink-mute);
  .el-icon { font-size: 13px; }
}

.room-metrics {
  display: flex;
  justify-content: space-between;
  margin: 24px 0;
  padding: 16px 0;
  border-top: 1px solid var(--st-hairline);
  border-bottom: 1px solid var(--st-hairline);
  .metric {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    .metric-value { font-size: 16px; font-weight: 500; color: var(--st-ink); }
    .metric-value.code { font-family: ui-monospace, monospace; letter-spacing: 1px; color: var(--st-primary); }
    .metric-label { font-size: 11px; color: var(--st-ink-mute); }
  }
}

.actions {
  .cta {
    width: 100%;
    height: 44px;
  }
}

.invite-tip {
  margin-top: 14px;
  font-size: 12px;
  color: var(--st-ink-mute);
}

.center-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--st-ink-mute);
  .spin { animation: spin 1s linear infinite; font-size: 28px; color: var(--st-primary); }
  @keyframes spin { to { transform: rotate(360deg); } }
  &.fail .err-icon { font-size: 40px; color: var(--st-primary-soft); }
  .fail-title { font-size: 15px; color: var(--st-ink); }
}
</style>
