<template>
  <div class="my-songs-page">
    <el-breadcrumb separator="/" class="header">
      <el-breadcrumb-item>
        <el-icon class="breadcrumb-icon" style="margin-right: 8px"><Operation /></el-icon>
        管理中心
      </el-breadcrumb-item>
      <el-breadcrumb-item>我的歌曲</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="!hasPermission" class="no-permission">
      <el-empty description="您没有查看此页面的权限" />
    </div>

    <div v-else class="main">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索歌曲名"
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
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="playSong(row)"
            >
              试听
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { Operation } from '@element-plus/icons-vue'
import { getMySongs } from '@/api/manage'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPermission = computed(() => userStore.userInfo?.role === 1)

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterAuditStatus = ref('')

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
    const res = await getMySongs(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('加载我的歌曲失败', e)
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

function playSong(row) {
  if (row.auditStatus !== 1) {
    ElMessage.warning('歌曲尚未通过审核，无法试听')
    return
  }
  if (row.musicUrl) {
    window.open(row.musicUrl, '_blank')
  } else {
    ElMessage.warning('暂无音频链接')
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
.header {
  height: 40px;
  line-height: 40px;
  padding-left: 15px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.no-permission {
  margin: 80px;
  text-align: center;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
}

.main {
  margin: 15px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
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
