<template>
  <div class="create-box">
    <div class="create-header">
      <h2 class="create-title">🎁 创建音乐盲盒</h2>
      <p class="create-desc">选择3-5首歌曲，放入你的音乐盲盒</p>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" class="create-form">
      <!-- 盲盒标题 -->
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="给你的盲盒起个名字" maxlength="50" show-word-limit />
      </el-form-item>

      <!-- 心情标签 -->
      <el-form-item label="心情标签" prop="moodTag">
        <el-select v-model="form.moodTag" placeholder="选择心情标签" style="width: 100%">
          <el-option label="🌙 深夜" value="深夜" />
          <el-option label="☕ 治愈" value="治愈" />
          <el-option label="🎸 摇滚" value="摇滚" />
          <el-option label="💔 失恋" value="失恋" />
          <el-option label="🌈 开心" value="开心" />
          <el-option label="🌧️ 雨天" value="雨天" />
          <el-option label="📚 学习" value="学习" />
          <el-option label="🏃 运动" value="运动" />
        </el-select>
      </el-form-item>

      <!-- 留言 -->
      <el-form-item label="留言" prop="message">
        <el-input
          v-model="form.message"
          type="textarea"
          :rows="3"
          placeholder="写一段话，开启盲盒的人才能看到（可选）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 选择歌曲 -->
      <el-form-item label="选择歌曲" prop="songIds">
        <div class="song-selector">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索歌曲"
            :prefix-icon="Search"
            clearable
            @keyup.enter="searchSongs"
          />
          <div v-loading="searchLoading" class="song-list">
            <div
              v-for="song in searchResults"
              :key="song.musicId"
              class="song-item"
              :class="{ selected: form.songIds.includes(song.musicId) }"
              @click="toggleSong(song.musicId)"
            >
              <el-checkbox :model-value="form.songIds.includes(song.musicId)" />
              <img v-if="song.imageUrl" class="song-cover" :src="song.imageUrl" alt="cover" />
              <div v-else class="song-cover-placeholder">
                <el-icon><Headset /></el-icon>
              </div>
              <div class="song-info">
                <span class="song-name">{{ song.musicName }}</span>
                <span class="song-singer">{{ song.singerName || '未知歌手' }}</span>
              </div>
            </div>
            <el-empty v-if="!searchLoading && searchResults.length === 0 && searchKeyword" description="未找到相关歌曲" />
          </div>
          <div class="selected-songs">
            <span class="selected-label">已选 {{ form.songIds.length }}/5 首</span>
            <div class="selected-list">
              <el-tag
                v-for="song in selectedSongsList"
                :key="song.musicId"
                closable
                @close="toggleSong(song.musicId)"
              >
                {{ song.musicName }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          创建盲盒
        </el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Headset } from '@element-plus/icons-vue'
import { createMusicBoxApi } from '@/api/musicbox'
import request from '@/api/request'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const searchKeyword = ref('')
const searchLoading = ref(false)
const searchResults = ref([])
const selectedSongs = ref([])

const form = ref({
  title: '',
  moodTag: '',
  message: '',
  songIds: []
})

const rules = {
  title: [
    { required: true, message: '请输入盲盒标题', trigger: 'blur' }
  ],
  moodTag: [
    { required: true, message: '请选择心情标签', trigger: 'change' }
  ],
  songIds: [
    {
      validator: (rule, value, callback) => {
        if (value.length < 3) {
          callback(new Error('至少选择3首歌曲'))
        } else if (value.length > 5) {
          callback(new Error('最多选择5首歌曲'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 已选歌曲列表
const selectedSongsList = computed(() => {
  return selectedSongs.value
})

// 搜索歌曲
async function searchSongs() {
  if (!searchKeyword.value.trim()) return
  searchLoading.value = true
  try {
    const res = await request.get('/music/search', {
      params: { keyword: searchKeyword.value.trim(), page: 1, size: 20 }
    })
    searchResults.value = res.data?.records || res.data || []
  } catch (error) {
    ElMessage.error(error.message || '搜索失败')
  } finally {
    searchLoading.value = false
  }
}

// 切换歌曲选择
function toggleSong(musicId) {
  const index = form.value.songIds.indexOf(musicId)
  if (index > -1) {
    form.value.songIds.splice(index, 1)
    const songIndex = selectedSongs.value.findIndex(s => s.musicId === musicId)
    if (songIndex > -1) {
      selectedSongs.value.splice(songIndex, 1)
    }
  } else {
    if (form.value.songIds.length >= 5) {
      ElMessage.warning('最多只能选择5首歌曲')
      return
    }
    form.value.songIds.push(musicId)
    const song = searchResults.value.find(s => s.musicId === musicId)
    if (song) {
      selectedSongs.value.push(song)
    }
  }
}

// 提交创建
async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await createMusicBoxApi({
      title: form.value.title,
      moodTag: form.value.moodTag,
      message: form.value.message,
      songIds: form.value.songIds
    })
    ElMessage.success('盲盒创建成功')
    router.push('/Musicbox/plaza')
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

// 页面加载时搜索热门歌曲
onMounted(() => {
  searchKeyword.value = '热门'
  searchSongs()
})
</script>

<style scoped lang="scss">
.create-box {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px;
}

.create-header {
  text-align: center;
  margin-bottom: 32px;

  .create-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--st-ink);
    margin: 0 0 8px;
  }

  .create-desc {
    font-size: 14px;
    color: var(--st-ink-secondary);
    margin: 0;
  }
}

.create-form {
  background: #ffffff;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.song-selector {
  width: 100%;

  .song-list {
    max-height: 300px;
    overflow-y: auto;
    margin-top: 12px;
    border: 1px solid var(--st-hairline);
    border-radius: 8px;
  }

  .song-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    cursor: pointer;
    transition: background 150ms ease;
    border-bottom: 1px solid var(--st-hairline);

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: var(--st-primary-subdued);
    }

    &.selected {
      background: rgba(94, 92, 230, 0.08);
    }

    .song-cover {
      width: 40px;
      height: 40px;
      border-radius: 6px;
      object-fit: cover;
    }

    .song-cover-placeholder {
      width: 40px;
      height: 40px;
      border-radius: 6px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;

      .el-icon {
        font-size: 18px;
        color: rgba(255, 255, 255, 0.6);
      }
    }

    .song-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;

      .song-name {
        font-size: 14px;
        color: var(--st-ink);
        font-weight: 500;
      }

      .song-singer {
        font-size: 12px;
        color: var(--st-ink-secondary);
      }
    }
  }

  .selected-songs {
    margin-top: 16px;

    .selected-label {
      font-size: 14px;
      color: var(--st-ink-secondary);
      margin-bottom: 8px;
      display: block;
    }

    .selected-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
  }
}
</style>