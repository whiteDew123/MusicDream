<template>
  <div class="music-upload">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">SINGER · PUBLISH</p>
        <h2 class="page-title">发布歌曲</h2>
        <p class="page-desc">上传音乐文件、封面与歌词，提交后进入自动审核流程。</p>
      </div>
    </div>

    <div class="panel form-panel">
      <el-form ref="formRef" :model="store.form" label-width="100px">
        <!-- 歌曲基本信息 -->
        <el-form-item label="歌曲名" required>
          <el-input v-model="store.form.musicName" placeholder="请输入歌曲名" maxlength="50" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="store.form.tags" placeholder="多个标签用逗号分隔，例如：流行,治愈,吉他" />
        </el-form-item>

        <el-form-item label="时长(秒)">
          <el-input-number v-model="store.form.timelength" :min="0" :max="9999" :controls="false" style="width: 100%" />
        </el-form-item>

        <!-- 文件上传 -->
        <el-form-item label="音乐文件" required>
          <el-upload
            :show-file-list="false"
            :http-request="(options) => handleUpload(options, 'music')"
            :accept="MUSIC_ACCEPT"
            :before-upload="beforeMusicUpload"
            :disabled="store.uploading"
          >
            <el-button type="primary" :loading="store.uploading && uploadType === 'music'">
              {{ store.form.musicUrl ? '重新上传音乐' : '上传音乐' }}
            </el-button>
          </el-upload>
          <div v-if="store.form.musicUrl" class="upload-tip">已上传：{{ store.form.musicUrl }}</div>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
            :show-file-list="false"
            :http-request="(options) => handleUpload(options, 'image')"
            :accept="IMAGE_ACCEPT"
            :before-upload="beforeImageUpload"
            :disabled="store.uploading"
          >
            <el-button :loading="store.uploading && uploadType === 'image'">
              {{ store.form.imageUrl ? '重新上传封面' : '上传封面' }}
            </el-button>
          </el-upload>
          <div v-if="store.form.imageUrl" class="upload-tip">已上传：{{ store.form.imageUrl }}</div>
        </el-form-item>

        <el-form-item label="歌词文件">
          <el-upload
            :show-file-list="false"
            :http-request="(options) => handleUpload(options, 'lrc')"
            :accept="LRC_ACCEPT"
            :before-upload="beforeLrcUpload"
            :disabled="store.uploading"
          >
            <el-button :loading="store.uploading && uploadType === 'lrc'">
              {{ store.form.lyric ? '重新上传歌词' : '上传歌词' }}
            </el-button>
          </el-upload>
          <div v-if="store.form.lyric" class="upload-tip">已上传：{{ store.form.lyric }}</div>
        </el-form-item>

        <!-- 提交 -->
        <el-form-item>
          <el-button type="primary" size="large" :loading="store.publishing" @click="handlePublish">
            提交发布
          </el-button>
          <el-button size="large" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useSingerUploadStore } from '@/store/singerUpload'
import {
  MUSIC_EXTENSIONS,
  MUSIC_ACCEPT,
  MUSIC_MAX_SIZE_MB,
  IMAGE_EXTENSIONS,
  IMAGE_ACCEPT,
  IMAGE_MAX_SIZE_MB,
  LRC_EXTENSIONS,
  LRC_ACCEPT,
  LRC_MAX_SIZE_MB,
  isExtensionAllowed,
  isSizeAllowed
} from '@/constants/allowedTypes'

const store = useSingerUploadStore()
const formRef = ref()
const uploadType = ref('')

// 音乐上传前：扩展名/大小校验 + 预填时长
function beforeMusicUpload(file) {
  if (!isExtensionAllowed(file.name, MUSIC_EXTENSIONS)) {
    ElMessage.warning(`音频格式不支持，支持：${MUSIC_EXTENSIONS.join('/').toUpperCase()}`)
    return false
  }
  if (!isSizeAllowed(file, MUSIC_MAX_SIZE_MB)) {
    ElMessage.warning(`音频大小必须小于 ${MUSIC_MAX_SIZE_MB}MB`)
    return false
  }
  // 预填时长
  const url = URL.createObjectURL(file)
  const audio = new Audio()
  audio.preload = 'metadata'
  audio.onloadedmetadata = () => {
    if (audio.duration && !isNaN(audio.duration)) {
      store.form.timelength = Math.round(audio.duration)
    }
    URL.revokeObjectURL(url)
  }
  audio.onerror = () => URL.revokeObjectURL(url)
  audio.src = url
  return true
}

// 封面上传前：扩展名/大小校验
function beforeImageUpload(file) {
  if (!isExtensionAllowed(file.name, IMAGE_EXTENSIONS)) {
    ElMessage.warning(`图片格式不支持，支持：${IMAGE_EXTENSIONS.join('/').toUpperCase()}`)
    return false
  }
  if (!isSizeAllowed(file, IMAGE_MAX_SIZE_MB)) {
    ElMessage.warning(`图片大小必须小于 ${IMAGE_MAX_SIZE_MB}MB`)
    return false
  }
  return true
}

// 歌词上传前：扩展名/大小校验
function beforeLrcUpload(file) {
  if (!isExtensionAllowed(file.name, LRC_EXTENSIONS)) {
    ElMessage.warning(`歌词格式不支持，支持：${LRC_EXTENSIONS.join('/').toUpperCase()}`)
    return false
  }
  if (!isSizeAllowed(file, LRC_MAX_SIZE_MB)) {
    ElMessage.warning(`歌词大小必须小于 ${LRC_MAX_SIZE_MB}MB`)
    return false
  }
  return true
}

async function handleUpload(options, type) {
  uploadType.value = type
  try {
    await store.uploadFile(options.file, type)
    ElMessage.success('上传成功')
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  } finally {
    uploadType.value = ''
  }
}

async function handlePublish() {
  if (!store.form.musicName.trim()) {
    ElMessage.warning('请输入歌曲名')
    return
  }
  if (!store.form.musicUrl) {
    ElMessage.warning('请先上传音乐文件')
    return
  }

  try {
    await store.publish()
    ElMessage.success('发布成功，等待审核')
    handleReset()
  } catch (e) {
    // 错误已由 axios 拦截器统一提示
  }
}

function handleReset() {
  store.form.musicName = ''
  store.form.musicUrl = ''
  store.form.imageUrl = ''
  store.form.timelength = 0
  store.form.tags = ''
  store.form.lyric = ''
}
</script>

<style scoped lang="scss">
.music-upload {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.page-header {
  .eyebrow {
    font-size: 12px;
    font-weight: 500;
    letter-spacing: 1.5px;
    text-transform: uppercase;
    color: var(--brand-accent);
    margin-bottom: 8px;
  }
  .page-title {
    font-size: 24px;
    font-weight: 600;
    letter-spacing: -0.4px;
    color: var(--wf-ink);
    margin-bottom: 6px;
  }
  .page-desc {
    font-size: 14px;
    color: var(--wf-body-mid);
  }
}

.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-sm);
}

.form-panel {
  max-width: 720px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--wf-mute);
  word-break: break-all;
}
</style>