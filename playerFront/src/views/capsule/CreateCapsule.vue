<template>
  <div class="create-capsule-page">
    <!-- 页头 -->
    <header class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><MagicStick /></el-icon>
        <h2>创建时空胶囊</h2>
      </div>
      <p class="header-sub">把一首歌和一句话封存起来，留给未来的自己</p>
    </header>

    <!-- 步骤指示器 -->
    <div class="steps-indicator">
      <div
        v-for="(step, idx) in steps"
        :key="idx"
        class="step-item"
        :class="{ active: currentStep === idx, done: currentStep > idx }"
      >
        <div class="step-circle">
          <el-icon v-if="currentStep > idx"><Check /></el-icon>
          <span v-else>{{ idx + 1 }}</span>
        </div>
        <span class="step-label">{{ step.label }}</span>
        <div v-if="idx < steps.length - 1" class="step-line"></div>
      </div>
    </div>

    <!-- 步骤内容卡片 -->
    <div class="step-card">
      <!-- 步骤 1：选歌 -->
      <div v-show="currentStep === 0" class="step-content">
        <h3 class="step-title">选一首歌</h3>
        <p class="step-desc">从音乐库中挑选一首作为胶囊里的旋律</p>
        <div class="search-row">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索歌曲或歌手"
            :prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 搜索中 -->
        <div v-if="searching" class="search-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>搜索中…</span>
        </div>

        <!-- 搜索结果 -->
        <div v-else-if="searchResults.length" class="song-list">
          <div
            v-for="(song, idx) in searchResults"
            :key="song.musicId"
            class="song-item"
            :class="{ selected: selectedSong && selectedSong.musicId === song.musicId }"
            @click="selectSong(song)"
          >
            <span class="song-num">{{ idx + 1 }}</span>
            <div class="song-cover">
              <img v-if="song.imageUrl" :src="song.imageUrl" :alt="song.musicName" loading="lazy" />
              <el-icon v-else><Headset /></el-icon>
            </div>
            <div class="song-info">
              <span class="song-name">{{ song.musicName }}</span>
              <span class="song-singer">{{ song.singerName }}</span>
            </div>
            <span class="song-time">{{ formatTime(song.timelength) }}</span>
            <el-icon v-if="selectedSong && selectedSong.musicId === song.musicId" class="song-check">
              <Select />
            </el-icon>
          </div>
        </div>

        <div v-else-if="searched" class="search-empty">没有找到相关歌曲</div>
        <div v-else class="search-tip">输入关键词开始搜索</div>
      </div>

      <!-- 步骤 2：写话 -->
      <div v-show="currentStep === 1" class="step-content">
        <h3 class="step-title">写一段话</h3>
        <p class="step-desc">写给未来的自己，封印前可见，解锁后重见</p>
        <el-input
          v-model="message"
          type="textarea"
          :rows="8"
          placeholder="此刻的心情、想记住的人、对未来的期待……"
          maxlength="500"
          show-word-limit
          resize="none"
          class="message-input"
        />
        <!-- 选中歌曲预览 -->
        <div v-if="selectedSong" class="selected-preview">
          <div class="preview-cover">
            <img v-if="selectedSong.imageUrl" :src="selectedSong.imageUrl" :alt="selectedSong.musicName" loading="lazy" />
            <el-icon v-else><Headset /></el-icon>
          </div>
          <div class="preview-info">
            <span class="preview-name">{{ selectedSong.musicName }}</span>
            <span class="preview-singer">{{ selectedSong.singerName }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤 3：选解锁时间 -->
      <div v-show="currentStep === 2" class="step-content">
        <h3 class="step-title">选择解锁时间</h3>
        <p class="step-desc">时间到达后，胶囊将自动解锁</p>
        <div class="time-presets">
          <button
            v-for="preset in timePresets"
            :key="preset.key"
            class="time-pill"
            :class="{ active: activePreset === preset.key }"
            @click="selectPreset(preset)"
          >
            <span class="pill-label">{{ preset.label }}</span>
            <span class="pill-desc">{{ preset.desc }}</span>
          </button>
        </div>
        <div class="custom-time">
          <span class="custom-label">自定义时间</span>
          <el-date-picker
            v-model="customTime"
            type="datetime"
            placeholder="选择未来的某个时刻"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
            class="custom-picker"
            @change="onCustomTimeChange"
          />
        </div>
        <div v-if="unlockTime" class="unlock-preview">
          <el-icon><Clock /></el-icon>
          <span>将于 {{ formatUnlockTime(unlockTime) }} 解锁</span>
        </div>
      </div>

      <!-- 步骤 4：确认 -->
      <div v-show="currentStep === 3" class="step-content">
        <h3 class="step-title">确认封印</h3>
        <p class="step-desc">检查胶囊内容，确认后即可封存</p>
        <div class="confirm-card">
          <div class="confirm-cover">
            <img
              v-if="selectedSong && selectedSong.imageUrl"
              :src="selectedSong.imageUrl"
              alt=""
              loading="lazy"
            />
            <el-icon v-else><Headset /></el-icon>
          </div>
          <div class="confirm-info">
            <div class="confirm-row">
              <span class="confirm-label">歌曲</span>
              <span class="confirm-value">
                {{ selectedSong?.musicName }} - {{ selectedSong?.singerName }}
              </span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">留言</span>
              <span class="confirm-value message-text">{{ message || '（未填写留言）' }}</span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">解锁</span>
              <span class="confirm-value">{{ formatUnlockTime(unlockTime) }}</span>
            </div>
            <div class="confirm-row">
              <span class="confirm-label">公开</span>
              <el-switch v-model="isPublic" />
              <span class="confirm-hint">{{ isPublic ? '解锁后同步到时空广场' : '仅自己可见' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="step-actions">
        <button v-if="currentStep > 0" class="btn btn-ghost" @click="prevStep">上一步</button>
        <button
          v-if="currentStep < steps.length - 1"
          class="btn btn-primary"
          :disabled="!canNext"
          @click="nextStep"
        >
          下一步
        </button>
        <button
          v-else
          class="btn btn-primary"
          :disabled="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '封印中…' : '封印胶囊' }}
        </button>
      </div>
    </div>

    <!-- 封印动画遮罩 -->
    <transition name="seal-fade">
      <div v-if="sealing" class="seal-overlay">
        <div class="capsule-anim">
          <div class="capsule-half capsule-top"></div>
          <div class="capsule-half capsule-bottom"></div>
          <div class="capsule-core"></div>
        </div>
        <p class="seal-text">封印中…</p>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  MagicStick,
  Check,
  Search,
  Loading,
  Headset,
  Select,
  Clock
} from '@element-plus/icons-vue'
import { createCapsuleApi, searchMusicApi } from '@/api/capsule'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const steps = [
  { label: '选歌' },
  { label: '写话' },
  { label: '解锁时间' },
  { label: '确认' }
]
const currentStep = ref(0)

// === 选歌 ===
const searchKeyword = ref('')
const searching = ref(false)
const searched = ref(false)
const searchResults = ref([])
const selectedSong = ref(null)

// === 写话 ===
const message = ref('')

// === 解锁时间 ===
const activePreset = ref('')
const customTime = ref('')
const unlockTime = ref('')
const timePresets = [
  { key: '7d', label: '7 天后', desc: '一周之约', days: 7 },
  { key: '30d', label: '30 天后', desc: '一月之约', days: 30 },
  { key: '1y', label: '1 年后', desc: '一年之约', days: 365 }
]
const isPublic = ref(false)

// === 提交 ===
const submitting = ref(false)
const sealing = ref(false)

const canNext = computed(() => {
  if (currentStep.value === 0) return !!selectedSong.value
  if (currentStep.value === 2) return !!unlockTime.value
  return true
})

// 搜索歌曲
async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) {
    searchResults.value = []
    searched.value = false
    return
  }
  searching.value = true
  searched.value = false
  try {
    const res = await searchMusicApi(kw)
    searchResults.value = res.data || []
    searched.value = true
  } catch (e) {
    searchResults.value = []
    searched.value = true
  } finally {
    searching.value = false
  }
}

function selectSong(song) {
  selectedSong.value = song
}

// 选择预设时间
function selectPreset(preset) {
  activePreset.value = preset.key
  customTime.value = ''
  const d = new Date(Date.now() + preset.days * 24 * 3600 * 1000)
  unlockTime.value = formatLocal(d)
}

function onCustomTimeChange(val) {
  if (val) {
    activePreset.value = ''
    unlockTime.value = val
  } else {
    unlockTime.value = ''
  }
}

function disabledDate(date) {
  // 不允许选择今天之前
  return date.getTime() < Date.now() - 86400000
}

function formatLocal(d) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
    d.getHours()
  )}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatUnlockTime(t) {
  if (!t) return '未选择'
  return t.replace('T', ' ')
}

function formatTime(seconds) {
  if (!seconds) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function nextStep() {
  if (!canNext.value) return
  if (currentStep.value < steps.length - 1) currentStep.value++
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

// 提交封印
async function handleSubmit() {
  const userId = userStore.userInfo?.userId
  if (!userId) {
    ElMessage.warning('登录信息已失效，请重新登录')
    return
  }
  if (!selectedSong.value || !unlockTime.value) {
    ElMessage.warning('请完整填写胶囊内容')
    return
  }
  submitting.value = true
  sealing.value = true
  try {
    await createCapsuleApi({
      receiverId: userId,
      musicId: selectedSong.value.musicId,
      message: message.value,
      unlockTime: unlockTime.value,
      isPublic: isPublic.value
    })
    // 封印动画展示一会儿
    await new Promise((resolve) => setTimeout(resolve, 1600))
    ElMessage.success('胶囊已封印，等待时间解锁')
    router.push('/my/capsules')
  } catch (e) {
    sealing.value = false
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.create-capsule-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 8px 0 48px;
  animation: pageFadeIn 250ms ease-out;
}

@keyframes pageFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* === 页头 === */
.page-header {
  margin-bottom: 24px;

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;

    .title-icon {
      font-size: 26px;
      color: var(--st-primary);
    }

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: var(--st-ink);
      letter-spacing: -0.2px;
    }
  }

  .header-sub {
    margin-top: 6px;
    margin-left: 36px;
    font-size: 13px;
    color: var(--st-ink-mute);
  }
}

/* === 步骤指示器 === */
.steps-indicator {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.step-item {
  display: flex;
  align-items: center;
  flex: 1;

  &:last-child {
    flex: 0;
  }

  .step-circle {
    width: 32px;
    height: 32px;
    border-radius: var(--rounded-pill);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    background: var(--st-input-bg);
    color: var(--st-ink-mute);
    border: 1px solid var(--st-hairline);
    transition: all 250ms ease;
    flex-shrink: 0;
  }

  .step-label {
    margin-left: 8px;
    font-size: 13px;
    color: var(--st-ink-mute);
    transition: color 250ms ease;
  }

  .step-line {
    flex: 1;
    height: 2px;
    margin: 0 12px;
    background: var(--st-hairline);
    border-radius: var(--rounded-pill);
    transition: background 250ms ease;
  }

  &.active {
    .step-circle {
      background: var(--st-primary);
      color: #fff;
      border-color: var(--st-primary);
      box-shadow: 0 0 0 4px rgba(94, 92, 230, 0.12);
    }
    .step-label {
      color: var(--st-ink);
      font-weight: 600;
    }
  }

  &.done {
    .step-circle {
      background: var(--st-primary);
      color: #fff;
      border-color: var(--st-primary);
    }
    .step-label {
      color: var(--st-ink-secondary);
    }
    .step-line {
      background: var(--st-primary);
    }
  }
}

/* === 步骤卡片 === */
.step-card {
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-sm);
  padding: 28px 32px 24px;
}

.step-content {
  animation: stepFadeIn 250ms ease-out;
}

@keyframes stepFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--st-ink);
  margin-bottom: 4px;
}

.step-desc {
  font-size: 13px;
  color: var(--st-ink-mute);
  margin-bottom: 20px;
}

/* === 搜索行 === */
.search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .search-input {
    flex: 1;
  }

  .search-btn {
    padding: 0 22px;
    border: none;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    transition: background 200ms ease, transform 150ms ease;

    &:hover {
      background: var(--st-primary-hover);
      transform: translateY(-1px);
    }
    &:active {
      transform: translateY(0);
    }
  }
}

:deep(.search-input .el-input__wrapper) {
  background: var(--st-input-bg);
  border: 1px solid transparent;
  border-radius: var(--rounded-md);
  box-shadow: none;
  height: 42px;
}
:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--st-primary);
  background: var(--st-canvas);
  box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
}

.search-loading,
.search-empty,
.search-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--st-ink-mute);
  font-size: 14px;

  .is-loading {
    animation: spin 1s linear infinite;
    color: var(--st-primary);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* === 歌曲列表 === */
.song-list {
  max-height: 360px;
  overflow-y: auto;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-md);
}

.song-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--st-hairline);
  transition: background 150ms ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--st-canvas-hover);
  }

  &.selected {
    background: rgba(94, 92, 230, 0.06);
  }

  .song-num {
    width: 24px;
    text-align: center;
    font-size: 13px;
    color: var(--st-ink-mute);
    font-feature-settings: 'tnum';
    flex-shrink: 0;
  }

  .song-cover {
    width: 40px;
    height: 40px;
    border-radius: var(--rounded-sm);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 18px;
      color: var(--st-ink-mute);
    }
  }

  .song-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .song-name {
      font-size: 14px;
      color: var(--st-ink);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .song-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .song-time {
    font-size: 13px;
    color: var(--st-ink-mute);
    font-feature-settings: 'tnum';
    flex-shrink: 0;
  }

  .song-check {
    color: var(--st-primary);
    font-size: 18px;
    flex-shrink: 0;
  }
}

/* === 写话 === */
:deep(.message-input .el-textarea__inner) {
  background: var(--st-input-bg);
  border: 1px solid transparent;
  border-radius: var(--rounded-md);
  color: var(--st-ink);
  font-size: 14px;
  line-height: 1.7;
  box-shadow: none;
  font-family: inherit;
}
:deep(.message-input .el-textarea__inner:focus) {
  border-color: var(--st-primary);
  background: var(--st-canvas);
  box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
}

.selected-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 12px 16px;
  background: var(--st-canvas-soft);
  border-radius: var(--rounded-md);

  .preview-cover {
    width: 44px;
    height: 44px;
    border-radius: var(--rounded-sm);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 20px;
      color: var(--st-ink-mute);
    }
  }

  .preview-info {
    display: flex;
    flex-direction: column;

    .preview-name {
      font-size: 14px;
      color: var(--st-ink);
    }

    .preview-singer {
      font-size: 12px;
      color: var(--st-ink-mute);
    }
  }
}

/* === 时间预设 === */
.time-presets {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.time-pill {
  flex: 1;
  min-width: 130px;
  padding: 14px 18px;
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-pill);
  background: var(--st-canvas);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  transition: all 200ms ease;

  .pill-label {
    font-size: 15px;
    font-weight: 600;
    color: var(--st-ink);
  }

  .pill-desc {
    font-size: 12px;
    color: var(--st-ink-mute);
  }

  &:hover {
    border-color: var(--st-primary-subdued);
    transform: translateY(-2px);
  }

  &.active {
    border-color: var(--st-primary);
    background: rgba(94, 92, 230, 0.06);

    .pill-label {
      color: var(--st-primary);
    }
  }
}

.custom-time {
  display: flex;
  align-items: center;
  gap: 14px;

  .custom-label {
    font-size: 14px;
    color: var(--st-ink-secondary);
    flex-shrink: 0;
  }

  .custom-picker {
    flex: 1;
  }
}

:deep(.custom-picker .el-input__wrapper) {
  background: var(--st-input-bg);
  border: 1px solid transparent;
  border-radius: var(--rounded-md);
  box-shadow: none;
  height: 42px;
}
:deep(.custom-picker .el-input__wrapper.is-focus) {
  border-color: var(--st-primary);
  background: var(--st-canvas);
}

.unlock-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding: 12px 16px;
  background: rgba(94, 92, 230, 0.06);
  border-radius: var(--rounded-md);
  font-size: 13px;
  color: var(--st-primary);
  font-weight: 500;
}

/* === 确认卡片 === */
.confirm-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: var(--st-canvas-soft);
  border-radius: var(--rounded-md);

  .confirm-cover {
    width: 100px;
    height: 100px;
    border-radius: var(--rounded-md);
    overflow: hidden;
    background: var(--st-input-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .el-icon {
      font-size: 36px;
      color: var(--st-ink-mute);
    }
  }

  .confirm-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .confirm-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
  }

  .confirm-label {
    width: 48px;
    flex-shrink: 0;
    color: var(--st-ink-mute);
    font-size: 13px;
  }

  .confirm-value {
    color: var(--st-ink);
    word-break: break-word;
  }

  .message-text {
    color: var(--st-ink-secondary);
    line-height: 1.6;
  }

  .confirm-hint {
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}

/* === 操作按钮 === */
.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--st-hairline);
}

.btn {
  padding: 10px 28px;
  border: none;
  border-radius: var(--rounded-pill);
  font-size: 14px;
  cursor: pointer;
  transition: all 200ms ease;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.btn-ghost {
  background: var(--st-canvas);
  color: var(--st-ink-secondary);
  border: 1px solid var(--st-hairline);

  &:hover:not(:disabled) {
    background: var(--st-canvas-hover);
  }
}

.btn-primary {
  background: var(--st-primary);
  color: #fff;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover);
    transform: translateY(-1px);
    box-shadow: 0 6px 18px rgba(94, 92, 230, 0.28);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }
}

/* === 封印动画遮罩 === */
.seal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  z-index: 2000;
}

.seal-fade-enter-active,
.seal-fade-leave-active {
  transition: opacity 250ms ease-out;
}
.seal-fade-enter-from,
.seal-fade-leave-to {
  opacity: 0;
}

.capsule-anim {
  position: relative;
  width: 120px;
  height: 160px;
}

.capsule-half {
  position: absolute;
  left: 0;
  width: 120px;
  height: 80px;
  background: linear-gradient(135deg, var(--st-primary), var(--st-primary-soft));
  box-shadow: 0 4px 20px rgba(94, 92, 230, 0.4);
}

.capsule-top {
  top: 0;
  border-radius: 60px 60px 8px 8px;
  animation: capsuleCloseTop 1.2s ease-in-out infinite;
}

.capsule-bottom {
  bottom: 0;
  border-radius: 8px 8px 60px 60px;
  animation: capsuleCloseBottom 1.2s ease-in-out infinite;
}

@keyframes capsuleCloseTop {
  0% {
    transform: translateY(-40px) rotate(-3deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(0) rotate(0);
    opacity: 1;
  }
  100% {
    transform: translateY(-40px) rotate(-3deg);
    opacity: 0.6;
  }
}

@keyframes capsuleCloseBottom {
  0% {
    transform: translateY(40px) rotate(3deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(0) rotate(0);
    opacity: 1;
  }
  100% {
    transform: translateY(40px) rotate(3deg);
    opacity: 0.6;
  }
}

.capsule-core {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 14px;
  height: 14px;
  border-radius: var(--rounded-pill);
  background: #fff;
  transform: translate(-50%, -50%) scale(0);
  animation: corePulse 1.2s ease-in-out infinite;
}

@keyframes corePulse {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.4);
    opacity: 1;
  }
}

.seal-text {
  font-size: 15px;
  color: var(--st-ink-secondary);
  letter-spacing: 2px;
  animation: textPulse 1.2s ease-in-out infinite;
}

@keyframes textPulse {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}
</style>
