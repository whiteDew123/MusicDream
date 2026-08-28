<template>
  <div class="room-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><Headset /></el-icon>
          一起听 · 播放室
        </h2>
        <p class="page-desc">建一个房间，拉上朋友，像一群人挤在客厅听 CD 一样</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="inviteCode"
          class="invite-input"
          placeholder="输入邀请码，加入房间"
          clearable
          @keyup.enter="joinByCode"
        >
          <template #append>
            <el-button @click="joinByCode">加入</el-button>
          </template>
        </el-input>
        <el-button type="primary" class="create-btn" @click="openCreate">
          <el-icon><Plus /></el-icon>
          创建房间
        </el-button>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 8" :key="n">
        <div class="skeleton-cover"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="rooms.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Headset /></el-icon>
      <p>还没有房间，来创建一个吧</p>
      <el-button type="primary" round @click="openCreate">创建第一个房间</el-button>
    </div>

    <!-- 房间网格 -->
    <div v-else class="room-grid">
      <div
        v-for="room in rooms"
        :key="room.id"
        class="room-card"
        @click="enterRoom(room)"
      >
        <div class="room-cover" :style="{ background: coverBg(room) }">
          <img v-if="room.cover && !imgErrors[room.id]" :src="room.cover" @error="handleImgError(room.id)" />
          <span v-else class="cover-symbol">♪</span>
          <span class="status-tag" :class="'status-' + (room.status ?? 0)">
            {{ statusText(room.status) }}
          </span>
        </div>
        <div class="room-info">
          <div class="room-name" :title="room.name">{{ room.name }}</div>
          <div class="room-meta">
            <span class="owner">
              <el-icon><User /></el-icon>
              <span :title="room.ownerName">{{ room.ownerName || '未知房主' }}</span>
            </span>
            <span class="members">
              <el-icon><UserFilled /></el-icon>
              <span>{{ room.memberCount }}/{{ room.maxMembers }}</span>
            </span>
          </div>
          <div class="room-tags">
            <span v-if="room.isPublic === 1" class="pill-tag">公开</span>
            <span v-else class="pill-tag muted">私密</span>
            <span v-if="room.isMember" class="pill-tag accent">已加入</span>
            <span v-else-if="room.isOwner" class="pill-tag accent">我的房间</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建房间弹窗 -->
    <el-dialog v-model="createVisible" title="创建房间" width="460px" align-center>
      <el-form :model="form" label-position="top">
        <el-form-item label="房间名称">
          <el-input v-model="form.name" maxlength="100" placeholder="给房间起个名字" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="人数上限">
            <el-input-number v-model="form.maxMembers" :min="2" :max="5" />
          </el-form-item>
          <el-form-item label="邀请码有效期（小时）">
            <el-input-number v-model="form.inviteExpireHours" :min="1" :max="12" />
          </el-form-item>
        </div>
        <el-form-item label="公开房间">
          <el-switch v-model="form.isPublic" active-text="公开" inactive-text="私密" />
          <p class="form-tip">公开后可在播放室广场被发现，否则仅通过邀请码加入</p>
        </el-form-item>
        <el-form-item label="播放模式">
          <el-radio-group v-model="form.playMode">
            <el-radio :value="0">循环播放</el-radio>
            <el-radio :value="1">播放完毕停止</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创 建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Headset, Plus, User, UserFilled } from '@element-plus/icons-vue'
import { roomListApi, createRoomApi, joinRoomApi, inviteRoomApi } from '@/api/room'

const router = useRouter()

// 列表状态
const loading = ref(true)
const rooms = ref([])
const imgErrors = reactive({})
const inviteCode = ref('')

// 创建弹窗状态
const createVisible = ref(false)
const creating = ref(false)
const form = reactive({
  name: '',
  maxMembers: 5,
  isPublic: true,
  inviteExpireHours: 6,
  playMode: 0
})

// 加载房间列表
async function loadRooms() {
  loading.value = true
  try {
    const res = await roomListApi()
    rooms.value = res.data || []
  } catch (e) {
    console.error('加载房间列表失败:', e)
  } finally {
    loading.value = false
  }
}

// 打开创建弹窗
function openCreate() {
  // 重置默认值
  form.name = ''
  form.maxMembers = 5
  form.isPublic = true
  form.inviteExpireHours = 6
  form.playMode = 0
  createVisible.value = true
}

// 提交创建
async function handleCreate() {
  if (!form.name || !form.name.trim()) {
    ElMessage.warning('请输入房间名称')
    return
  }
  creating.value = true
  try {
    const res = await createRoomApi({
      name: form.name.trim(),
      maxMembers: form.maxMembers,
      isPublic: form.isPublic,
      inviteExpireHours: form.inviteExpireHours,
      playMode: form.playMode
    })
    createVisible.value = false
    ElMessage.success('房间创建成功')
    // 直接进入新房间
    router.push(`/room/${res.data && res.data.id}`)
  } catch (e) {
    // 提示已由拦截器处理
  } finally {
    creating.value = false
  }
}

// 进入 / 加入房间
async function enterRoom(room) {
  // 已加入：直接进入
  if (room.isMember) {
    router.push(`/room/${room.id}`)
    return
  }
  // 未加入：先加入再进入
  try {
    await joinRoomApi(room.id)
    router.push(`/room/${room.id}`)
  } catch (e) {
    // 失败（如房间已满）已由拦截器提示
  }
}

// 通过邀请码加入房间
async function joinByCode() {
  const code = (inviteCode.value || '').trim()
  if (!code) {
    ElMessage.warning('请输入邀请码')
    return
  }
  try {
    const res = await inviteRoomApi(code)
    const room = res.data
    if (!room) return
    if (room.isMember) {
      router.push(`/room/${room.id}`)
    } else {
      await joinRoomApi(room.id)
      router.push(`/room/${room.id}`)
    }
  } catch (e) {
    // 邀请码无效/过期已由拦截器提示
  }
}

// 封面加载失败：显示默认符号
function handleImgError(id) {
  imgErrors[id] = true
}

// 封面背景：无封面时的渐变
function coverBg(room) {
  const colors = ['#6341FF', '#7B5EFF', '#4E2AEE', '#1c1e54', '#665EFD']
  const idx = (room.id ?? 0) % colors.length
  return `linear-gradient(135deg, ${colors[idx]}, ${colors[(idx + 1) % colors.length]})`
}

// 状态文案
function statusText(status) {
  if (status === 1) return '播放中'
  if (status === 2) return '已结束'
  return '空闲'
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped lang="scss">
.room-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

/* === 页面头部 === */
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 24px;
    font-weight: 600;
    color: var(--st-ink);

    .el-icon {
      color: var(--st-primary);
    }
  }

  .page-desc {
    font-size: 14px;
    color: var(--st-ink-mute);
    margin-top: 4px;
  }

  .create-btn {
    border-radius: var(--rounded-pill);
    height: 40px;
    padding: 0 20px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .invite-input {
    width: 240px;

    :deep(.el-input__wrapper) {
      border-radius: var(--rounded-pill) 0 0 var(--rounded-pill);
    }
    :deep(.el-input-group__append) {
      border-radius: 0 var(--rounded-pill) var(--rounded-pill) 0;
    }
  }
}

/* === 骨架屏 === */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.skeleton-card .skeleton-cover {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--rounded-md);
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
}
.skeleton-card .skeleton-line {
  height: 14px;
  margin-top: 10px;
  border-radius: var(--rounded-sm);
  background: #e8ecf0;
  animation: skeletonPulse 1.2s infinite ease-in-out;
  &.short { width: 60%; height: 12px; }
}
@keyframes skeletonPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* === 空状态 === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: var(--st-ink-mute);
  gap: 16px;
  .empty-icon { font-size: 48px; color: var(--st-primary-subdued); }
}

/* === 房间网格 === */
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.room-card {
  cursor: pointer;
  transition: transform 200ms ease;
  &:hover { transform: translateY(-4px); }

  .room-cover {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--rounded-md);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 1px 3px rgba(0, 55, 112, 0.08);
    transition: box-shadow 250ms ease;

    img { width: 100%; height: 100%; object-fit: cover; }
    .cover-symbol { font-size: 56px; color: rgba(255, 255, 255, 0.85); }

    .status-tag {
      position: absolute;
      top: 10px;
      left: 10px;
      padding: 2px 10px;
      border-radius: var(--rounded-pill);
      font-size: 11px;
      line-height: 1.5;
      color: #fff;
      backdrop-filter: blur(4px);
      &.status-0 { background: rgba(30, 30, 30, 0.55); }
      &.status-1 { background: rgba(99, 65, 255, 0.85); }
      &.status-2 { background: rgba(120, 120, 130, 0.6); }
    }
  }

  &:hover .room-cover { box-shadow: 0 8px 24px rgba(0, 55, 112, 0.12); }

  .room-info {
    margin-top: 10px;
    .room-name {
      font-size: 15px;
      font-weight: 500;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .room-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 6px;
      font-size: 12px;
      color: var(--st-ink-mute);
      .owner, .members {
        display: flex;
        align-items: center;
        gap: 3px;
        overflow: hidden;
        .el-icon { font-size: 12px; flex-shrink: 0; }
      }
      .owner span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .members { flex-shrink: 0; }
    }
    .room-tags {
      display: flex;
      gap: 6px;
      margin-top: 8px;
      .pill-tag {
        padding: 2px 8px;
        border-radius: var(--rounded-pill);
        background: var(--st-primary-subdued);
        color: var(--st-primary-deep, var(--st-primary));
        font-size: 11px;
        &.muted { background: var(--st-canvas-hover); color: var(--st-ink-mute); }
        &.accent { background: var(--st-primary); color: #fff; }
      }
    }
  }
}

/* === 创建弹窗 === */
.form-row {
  display: flex;
  gap: 16px;
  .el-form-item { flex: 1; }
}
.form-tip {
  font-size: 12px;
  color: var(--st-ink-mute);
  margin-top: 4px;
}
</style>