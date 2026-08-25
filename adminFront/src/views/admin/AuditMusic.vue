<template>
  <div class="audit-page">
    <div class="page-header">
      <h2>歌曲审核</h2>
      <p class="subtitle">管理员审核歌手发布的歌曲，审核通过后方可在歌曲列表中展示</p>
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
      <el-select v-model="filterAuditStatus" placeholder="审核状态" style="width: 140px; margin-left: 12px" clearable>
        <el-option label="全部" value="" />
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
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
      <el-table-column label="时长" width="90" align="center">
        <template #default="{ row }">
          {{ formatDuration(row.timelength) }}
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="auditStatusTag(row.auditStatus)">
            {{ auditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="歌曲状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.activation === 0 ? 'success' : 'danger'">
            {{ row.activation === 0 ? '正常' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="驳回原因" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.auditStatus === 2 && row.auditRemark" class="reject-reason">
            {{ row.auditRemark }}
          </span>
          <span v-else style="color: #c0c4cc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.auditStatus === 0"
            type="success"
            size="small"
            @click="handleApprove(row)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.auditStatus === 0"
            type="danger"
            size="small"
            @click="handleReject(row)"
          >
            驳回
          </el-button>
          <el-button
            v-if="row.auditStatus === 1"
            :type="row.activation === 0 ? 'warning' : 'success'"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.activation === 0 ? '锁定' : '解锁' }}
          </el-button>
          <el-button
            v-if="row.auditStatus === 2"
            type="info"
            size="small"
            @click="handleApprove(row)"
          >
            重新通过
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

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回歌曲" width="480px">
      <el-form label-width="80px">
        <el-form-item label="歌曲名">
          <span>{{ currentRow?.musicName }}</span>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因，歌手将看到此信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMusicList, deleteMusic, updateMusicStatus, auditMusic } from '@/api/manage'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterAuditStatus = ref('')
const filterActivation = ref('')

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRow = ref(null)

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (filterAuditStatus.value !== '') {
      params.auditStatus = filterAuditStatus.value
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
  filterAuditStatus.value = ''
  filterActivation.value = ''
  handleSearch()
}

function auditStatusTag(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

function auditStatusText(status) {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定要通过歌曲「${row.musicName}」的审核吗？`, '审核通过', {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res = await auditMusic(row.musicId, 1, null)
    if (res.code === 200) {
      ElMessage.success('审核通过成功')
      loadData()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (e) {
    // 用户取消
  }
}

function handleReject(row) {
  currentRow.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    const res = await auditMusic(currentRow.value.musicId, 2, rejectReason.value.trim())
    if (res.code === 200) {
      ElMessage.success('已驳回')
      rejectDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '驳回失败')
    }
  } catch (e) {
    console.error(e)
  }
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
  if (!seconds) return '00:00'
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
.audit-page {
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

.reject-reason {
  color: #f56c6c;
  font-size: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
