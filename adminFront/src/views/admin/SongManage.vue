<template>
  <div class="song-manage-page">
    <div class="page-header">
      <h2>歌曲管理</h2>
      <p class="subtitle">管理所有审核通过的歌曲，支持锁定/解锁/删除操作</p>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索歌曲名或歌手"
        :prefix-icon="Search"
        style="width: 240px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filterActivation" placeholder="歌曲状态" style="width: 140px; margin-left: 12px" clearable>
        <el-option label="全部" value="" />
        <el-option label="正常" :value="0" />
        <el-option label="锁定" :value="1" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 16px"
    >
      <el-table-column prop="musicId" label="ID" width="70" align="center" />
      <el-table-column label="封面" width="80" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            :preview-src-list="[row.imageUrl]"
            style="width: 50px; height: 50px; border-radius: 4px"
            fit="cover"
          />
          <div v-else class="no-cover">无封面</div>
        </template>
      </el-table-column>
      <el-table-column prop="musicName" label="歌曲名" min-width="120" show-overflow-tooltip />
      <el-table-column prop="singerName" label="歌手" width="100" show-overflow-tooltip />
      <el-table-column label="时长" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.timelength && row.timelength > 0">{{ formatDuration(row.timelength) }}</span>
          <span v-else style="color: #e6a23c">未知</span>
        </template>
      </el-table-column>
      <el-table-column prop="listenNumb" label="播放量" width="90" align="center" />
      <el-table-column label="歌曲状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.activation === 0 ? 'success' : 'danger'">
            {{ row.activation === 0 ? '正常' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            :type="row.activation === 0 ? 'warning' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.activation === 0 ? '锁定' : '解锁' }}
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="playSong(row)"
          >
            试听
          </el-button>
          <el-button
            v-if="!row.timelength || row.timelength <= 0"
            type="success"
            size="small"
            @click="fetchAndUpdateDuration(row)"
          >
            修复时长
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMusicList, deleteMusic, updateMusicStatus, updateMusic } from '@/api/manage'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterActivation = ref('')

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      auditStatus: 1
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterActivation.value !== '') {
      params.activation = filterActivation.value
    }
    const res = await getMusicList(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('加载歌曲列表失败', e)
    ElMessage.error('加载歌曲列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  filterActivation.value = ''
  handleSearch()
}

async function toggleStatus(row) {
  const action = row.activation === 0 ? '锁定' : '解锁'
  const newStatus = row.activation === 0 ? 1 : 0
  try {
    await ElMessageBox.confirm(`确定要${action}歌曲「${row.musicName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await updateMusicStatus(row.musicId, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (e) {
    // 用户取消
  }
}

function playSong(row) {
  if (row.musicUrl) {
    window.open(row.musicUrl, '_blank')
  } else {
    ElMessage.warning('暂无音频链接')
  }
}

async function fetchAndUpdateDuration(row) {
  if (!row.musicUrl) {
    ElMessage.warning('暂无音频链接，无法获取时长')
    return
  }
  try {
    ElMessage.info('正在获取音频时长...')
    const duration = await getAudioDurationFromUrl(row.musicUrl)
    if (duration && duration > 0) {
      const res = await updateMusic(row.musicId, { timelength: duration })
      if (res.code === 200) {
        ElMessage.success(`时长已修复：${formatDuration(duration)}`)
        loadData()
      } else {
        ElMessage.error(res.message || '更新时长失败')
      }
    } else {
      ElMessage.error('无法获取音频时长')
    }
  } catch (e) {
    console.error('获取时长失败', e)
    ElMessage.error('获取音频时长失败')
  }
}

function getAudioDurationFromUrl(url) {
  return new Promise((resolve, reject) => {
    const audio = new Audio()
    audio.preload = 'metadata'
    audio.onloadedmetadata = () => {
      if (audio.duration && !isNaN(audio.duration)) {
        resolve(Math.round(audio.duration))
      } else {
        reject(new Error('无法获取时长'))
      }
    }
    audio.onerror = () => reject(new Error('音频加载失败'))
    audio.src = url
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除歌曲「${row.musicName}」吗？此操作不可恢复。`, '危险提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    const res = await deleteMusic(row.musicId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    // 用户取消
  }
}

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return '--:--'
  const s = Number(seconds)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.toString()
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.song-manage-page {
  padding: 16px;
}

.page-header {
  margin-bottom: 16px;

  h2 {
    margin: 0 0 4px 0;
    font-size: 20px;
    color: #303133;
  }

  .subtitle {
    margin: 0;
    font-size: 14px;
    color: #909399;
  }
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 12px 16px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.no-cover {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
