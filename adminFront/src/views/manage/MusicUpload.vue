<template>
  <div class="music-upload">
    <div class="page-header">
      <div>
        <p class="eyebrow">SINGER · PUBLISH</p>
        <h2 class="page-title">发布歌曲</h2>
        <p class="page-desc">上传音乐文件、封面与歌词，选择风格标签后提交发布。</p>
      </div>
    </div>

    <div class="panel form-panel">
      <el-form ref="formRef" :model="store.form" label-width="100px">
        <el-form-item label="歌曲名" required>
          <el-input v-model="store.form.musicName" placeholder="请输入歌曲名" maxlength="50" />
        </el-form-item>

        <el-form-item label="风格标签">
          <div class="tag-selector">
            <div class="tag-grid">
              <button
                v-for="tag in PRESET_TAGS"
                :key="tag"
                type="button"
                class="tag-chip"
                :class="{ selected: selectedTags.has(tag) }"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </button>
            </div>
            <div class="tag-footer">
              <span class="tag-count">已选 {{ selectedTags.size }} 个标签</span>
              <button
                v-if="selectedTags.size > 0"
                type="button"
                class="tag-clear"
                @click="clearTags"
              >
                清空全部
              </button>
            </div>
            <div v-if="selectedTags.size > 0" class="tag-selected-list">
              <span
                v-for="tag in selectedTags"
                :key="tag"
                class="tag-selected-chip"
              >
                {{ tag }}
                <span class="tag-remove" @click="toggleTag(tag)">×</span>
              </span>
            </div>
            <div v-else class="tag-empty">尚未选择标签，请从上方点击选择</div>
            <div class="tag-hint">选择最能代表歌曲风格的标签，可多选</div>
          </div>
        </el-form-item>

        <el-form-item label="时长(秒)">
          <el-input-number v-model="store.form.timelength" :min="0" :max="9999" :controls="false" style="width: 100%" />
        </el-form-item>

        <el-form-item label="音乐文件" required>
          <el-upload
            :show-file-list="false"
            :http-request="(options) => handleUpload(options, 'music')"
            accept="audio/*"
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
            accept="image/*"
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
            accept=".lrc,.txt"
            :disabled="store.uploading"
          >
            <el-button :loading="store.uploading && uploadType === 'lrc'">
              {{ store.form.lyric ? '重新上传歌词' : '上传歌词' }}
            </el-button>
          </el-upload>
          <div v-if="store.form.lyric" class="upload-tip">已上传：{{ store.form.lyric }}</div>
        </el-form-item>

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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useSingerUploadStore } from '@/store/singerUpload'

const PRESET_TAGS = [
  '流行', '摇滚', '电子', '民谣', 'R&B', '嘻哈',
  '爵士', '古典', '轻音乐', '古风', '国风', '说唱',
  '雷鬼', '灵魂乐', '另类/独立'
]

const store = useSingerUploadStore()
const formRef = ref()
const uploadType = ref('')
const selectedTags = reactive(new Set())

function toggleTag(tag) {
  if (selectedTags.has(tag)) {
    selectedTags.delete(tag)
  } else {
    selectedTags.add(tag)
  }
  store.form.tags = Array.from(selectedTags).join(',')
}

function clearTags() {
  selectedTags.clear()
  store.form.tags = ''
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
  clearTags()
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

// ===== 标签选择器 =====
.tag-selector {
  width: 100%;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  letter-spacing: -0.1px;
  border-radius: var(--rounded-sm);
  border: 1px solid var(--wf-hairline);
  background: var(--wf-canvas);
  color: var(--wf-body-mid);
  cursor: pointer;
  user-select: none;
  transition: all 0.15s ease;
  white-space: nowrap;
  outline: none;

  &:hover {
    border-color: var(--brand-accent);
    color: var(--brand-accent);
    background: rgba(67, 83, 255, 0.04);
  }

  &:active {
    transform: scale(0.96);
  }

  &.selected {
    background: var(--brand-accent);
    border-color: var(--brand-accent);
    color: #ffffff;

    &::after {
      content: "✕";
      font-size: 10px;
      margin-left: 4px;
      opacity: 0.7;
      font-weight: 600;
    }

    &:hover {
      background: var(--wf-accent-blue-info);
      border-color: var(--wf-accent-blue-info);
      color: #ffffff;

      &::after {
        opacity: 1;
      }
    }
  }
}

.tag-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 4px;
  margin-top: 12px;
  border-top: 1px solid var(--wf-hairline);
}

.tag-count {
  font-size: 12px;
  font-weight: 500;
  color: var(--wf-mute);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tag-clear {
  font-size: 12px;
  font-weight: 500;
  color: var(--wf-mute);
  cursor: pointer;
  background: none;
  border: none;
  font-family: inherit;
  padding: 2px 6px;
  border-radius: var(--rounded-sm);
  transition: all 0.15s;

  &:hover {
    color: var(--wf-accent-red);
    background: rgba(238, 29, 54, 0.06);
  }
}

.tag-selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.tag-selected-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 500;
  font-family: inherit;
  border-radius: var(--rounded-sm);
  background: rgba(67, 83, 255, 0.06);
  border: 1px solid rgba(67, 83, 255, 0.18);
  color: var(--brand-accent);
  transition: all 0.15s;
  animation: tagIn 150ms ease;

  .tag-remove {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 14px;
    height: 14px;
    border-radius: 2px;
    font-size: 10px;
    font-weight: 600;
    cursor: pointer;
    opacity: 0.5;
    transition: all 0.15s;

    &:hover {
      opacity: 1;
      background: rgba(67, 83, 255, 0.15);
    }
  }
}

.tag-empty {
  font-size: 13px;
  font-weight: 400;
  color: var(--wf-mute-soft);
  margin-top: 8px;
}

.tag-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--wf-mute);
  margin-top: 4px;
}

@keyframes tagIn {
  from { opacity: 0; transform: scale(0.9); }
  to   { opacity: 1; transform: scale(1); }
}
</style>