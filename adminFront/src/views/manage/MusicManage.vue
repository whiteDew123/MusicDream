<template>
  <div class="music-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">MANAGEMENT · MUSIC</p>
        <h2 class="page-title">歌曲管理</h2>
        <p class="page-desc">
          {{ store.isAdmin() ? '管理系统内全部歌曲，支持搜索、修改、删除、冻结与解冻。' : '管理你自己发布的歌曲，支持修改与删除。' }}
        </p>
      </div>
    </div>

    <!-- 搜索卡片（仅管理员） -->
    <div v-if="store.isAdmin()" class="panel search-panel">
      <el-input
        v-model="keyword"
        class="search-input"
        placeholder="歌曲名 / ID / 标签"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" :loading="store.loading" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 歌曲表格卡片 -->
    <div class="panel table-panel">
      <el-table
        v-loading="store.loading"
        :data="store.songs"
        row-key="musicId"
        class="music-table"
      >
        <el-table-column prop="musicId" label="ID" width="70" align="center" />
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              fit="cover"
              class="cover-img"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
            <div v-else class="cover-placeholder">
              <el-icon><Headset /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="musicName" label="歌曲名" min-width="160" show-overflow-tooltip />
        <el-table-column label="歌手" width="120">
          <template #default="{ row }">
            <span>{{ row.singerName || row.fromSinger || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="listenNumb" label="播放量" width="90" align="center" />
        <el-table-column label="时长" width="90" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.timelength) }}
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.tags || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.activation, row.auditStatus)" effect="light" round>
              {{ statusText(row.activation, row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" plain size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.activation === 2"
              type="success"
              plain
              size="small"
              :loading="store.actionId === row.musicId"
              @click="handleUnfreeze(row)"
            >
              解冻
            </el-button>
            <el-button
              v-else
              type="warning"
              plain
              size="small"
              :loading="store.actionId === row.musicId"
              @click="handleFreeze(row)"
            >
              冻结
            </el-button>
            <el-button type="danger" plain size="small" :loading="store.actionId === row.musicId" @click="handleDeleteHard(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="store.total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 编辑歌曲弹窗 -->
    <el-dialog v-model="editVisible" title="编辑歌曲" width="560px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-width="90px">
        <el-form-item label="歌曲名" prop="musicName">
          <el-input v-model="editForm.musicName" placeholder="请输入歌曲名" />
        </el-form-item>
        <el-form-item v-if="store.isAdmin()" label="歌手ID">
          <el-input-number v-model="editForm.fromSinger" :min="1" :controls="false" style="width: 100%" placeholder="请输入歌手ID" />
        </el-form-item>
        <el-form-item label="音频URL">
          <el-input v-model="editForm.musicUrl" placeholder="请输入音频文件URL" />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-upload">
            <div class="cover-preview">
              <el-image
                v-if="editForm.imageUrl"
                :src="editForm.imageUrl"
                fit="cover"
                class="preview-img"
                :preview-src-list="[editForm.imageUrl]"
                preview-teleported
              />
              <div v-else class="preview-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </div>
            <div class="cover-actions">
              <input
                ref="coverInputRef"
                type="file"
                accept="image/*"
                style="display:none"
                @change="handleCoverUpload"
              />
              <el-button
                type="primary"
                size="small"
                :loading="coverUploading"
                @click="coverInputRef?.click()"
              >
                {{ coverUploading ? '上传中...' : '本地上传' }}
              </el-button>
              <el-button
                v-if="editForm.imageUrl"
                size="small"
                @click="clearCover"
              >清除</el-button>
            </div>
            <el-input
              v-model="editForm.imageUrl"
              placeholder="或输入封面图片URL"
              size="small"
            />
          </div>
        </el-form-item>
        <el-form-item label="时长(秒)">
          <el-input-number v-model="editForm.timelength" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="歌词URL">
          <el-input v-model="editForm.lyric" placeholder="请输入歌词文件URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="store.saving" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Headset, Picture } from '@element-plus/icons-vue'
import { useAdminMusicStore } from '@/store/adminMusic'
import { uploadFileApi } from '@/api/music'

const store = useAdminMusicStore()

const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10
})

const editVisible = ref(false)
const editFormRef = ref()
const coverUploading = ref(false)
const coverInputRef = ref(null)
const editForm = reactive({
  musicId: null,
  fromSinger: null,
  musicName: '',
  musicUrl: '',
  imageUrl: '',
  timelength: 0,
  tags: '',
  lyric: ''
})

onMounted(() => {
  loadSongs()
})

async function loadSongs() {
  await store.fetchSongs({
    page: pagination.current,
    pageSize: pagination.pageSize,
    keyword: keyword.value.trim()
  })
}

function handleSearch() {
  pagination.current = 1
  loadSongs()
}

function handleReset() {
  keyword.value = ''
  pagination.current = 1
  loadSongs()
}

function handlePageChange(page) {
  pagination.current = page
  loadSongs()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadSongs()
}

function openEdit(row) {
  editForm.musicId = row.musicId
  editForm.fromSinger = row.fromSinger
  editForm.musicName = row.musicName || ''
  editForm.musicUrl = row.musicUrl || ''
  editForm.imageUrl = row.imageUrl || ''
  editForm.timelength = row.timelength || 0
  editForm.tags = row.tags || ''
  editForm.lyric = row.lyric || ''
  editVisible.value = true
}

async function handleCoverUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 5MB')
    return
  }
  coverUploading.value = true
  try {
    const res = await uploadFileApi(file, 'image')
    if (res.data?.fileUrl) {
      editForm.imageUrl = res.data.fileUrl
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('上传失败：未获取到文件路径')
    }
  } catch (err) {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
    if (coverInputRef.value) {
      coverInputRef.value.value = ''
    }
  }
}

function clearCover() {
  editForm.imageUrl = ''
}

async function handleSaveEdit() {
  const payload = {
    musicName: editForm.musicName,
    musicUrl: editForm.musicUrl,
    imageUrl: editForm.imageUrl,
    timelength: editForm.timelength,
    tags: editForm.tags,
    lyric: editForm.lyric
  }
  // 管理员可以调整歌曲所属歌手
  if (store.isAdmin()) {
    payload.fromSinger = editForm.fromSinger
  }

  await store.updateMusic(editForm.musicId, payload)
  ElMessage.success('修改成功')
  editVisible.value = false
  loadSongs()
}

async function handleFreeze(row) {
  try {
    await ElMessageBox.confirm(
      `确定要冻结歌曲「${row.musicName}」吗？冻结后应用端将不可见，管理端仍可查看。`,
      '冻结确认',
      {
        confirmButtonText: '确定冻结',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await store.freezeMusic(row.musicId)
    ElMessage.success('冻结成功')
    loadSongs()
  } catch (e) {
    // 用户取消或请求失败
  }
}

async function handleUnfreeze(row) {
  try {
    await ElMessageBox.confirm(
      `确定要解冻歌曲「${row.musicName}」吗？解冻后应用端将恢复可见。`,
      '解冻确认',
      {
        confirmButtonText: '确定解冻',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await store.unfreezeMusic(row.musicId)
    ElMessage.success('解冻成功')
    loadSongs()
  } catch (e) {
    // 用户取消或请求失败
  }
}

async function handleDeleteHard(row) {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除歌曲「${row.musicName}」吗？此操作不可恢复！`,
      '永久删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await store.deleteMusicHard(row.musicId)
    ElMessage.success('删除成功')
    loadSongs()
  } catch (e) {
    // 用户取消或请求失败
  }
}

function statusText(activation, auditStatus) {
  if (activation === 2) return '已冻结'
  if (auditStatus === 0) return '待审核'
  if (auditStatus === 2) return '已驳回'
  if (activation === 0) return '正常'
  return '未知'
}

function statusType(activation, auditStatus) {
  if (activation === 2) return 'danger'
  if (auditStatus === 0) return 'warning'
  if (auditStatus === 2) return 'danger'
  if (activation === 0) return 'success'
  return 'info'
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return '--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.music-manage {
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
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.search-panel {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  .search-input {
    width: 360px;
    max-width: 100%;
  }
}

.table-panel {
  padding: 0;
  overflow: hidden;
}

.music-table {
  width: 100%;

  :deep(.el-table__row) {
    transition: background 150ms ease;
  }
  :deep(.el-table__row:hover > td) {
    background: var(--wf-row-hover) !important;
  }
  :deep(.el-table__row:hover > td:first-child) {
    box-shadow: inset 2px 0 0 var(--brand-accent);
  }
}

.cover-img {
  width: 48px;
  height: 48px;
  border-radius: var(--rounded-sm);
  display: block;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 360px;
}

.cover-preview {
  width: 80px;
  height: 80px;
  border-radius: var(--rounded-md);
  overflow: hidden;
  border: 1px solid var(--wf-hairline);
  flex-shrink: 0;

  .preview-img {
    width: 100%;
    height: 100%;
    display: block;
  }
}

.preview-placeholder {
  width: 80px;
  height: 80px;
  border-radius: var(--rounded-md);
  background: var(--wf-canvas-soft);
  color: var(--wf-mute);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.cover-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cover-placeholder {
  width: 48px;
  height: 48px;
  border-radius: var(--rounded-sm);
  background: var(--wf-canvas-soft);
  color: var(--wf-mute);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-top: 1px solid var(--wf-hairline);
}

@media (max-width: 768px) {
  .search-panel {
    flex-direction: column;
    align-items: stretch;
    .search-input {
      width: 100%;
    }
  }
  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>