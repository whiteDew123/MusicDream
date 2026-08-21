<template>
  <div class="log-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">MANAGEMENT · LOG</p>
        <h2 class="page-title">操作日志</h2>
        <p class="page-desc">记录管理员的所有操作行为，支持按操作人、操作内容、歌曲名搜索，仅支持查看不可修改。</p>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="panel search-panel">
      <el-input
        v-model="keyword"
        class="search-input"
        placeholder="操作人 / 操作内容 / 歌曲名"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 日志表格 -->
    <div class="panel table-panel">
      <el-table
        v-loading="loading"
        :data="logs"
        row-key="logId"
        class="log-table"
        empty-text="暂无操作记录"
      >
        <el-table-column prop="logId" label="ID" width="70" align="center" />
        <el-table-column prop="userName" label="操作人" width="130" show-overflow-tooltip />
        <el-table-column prop="doSome" label="操作内容" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              :type="getActionTag(row.doSome)"
              effect="light"
              round
              size="small"
            >
              {{ row.doSome }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="musicName" label="关联歌曲" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.musicName || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createDate" label="操作时间" width="130" align="center" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { pageLog } from '@/api/admin'

const loading = ref(false)
const keyword = ref('')
const logs = ref([])
const total = ref(0)
const pagination = reactive({
  current: 1,
  pageSize: 10
})

onMounted(() => {
  loadLogs()
})

async function loadLogs() {
  loading.value = true
  try {
    const res = await pageLog(pagination.current, pagination.pageSize, keyword.value.trim())
    const data = res.data
    logs.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadLogs()
}

function handleReset() {
  keyword.value = ''
  pagination.current = 1
  loadLogs()
}

function handlePageChange(page) {
  pagination.current = page
  loadLogs()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadLogs()
}

function getActionTag(doSome) {
  if (!doSome) return 'info'
  if (doSome.includes('冻结') || doSome.includes('驳回') || doSome.includes('删除')) return 'danger'
  if (doSome.includes('解冻') || doSome.includes('通过')) return 'success'
  return 'info'
}
</script>

<style scoped lang="scss">
.log-manage {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* ===== 页面标题（Webflow eyebrow 风格）===== */
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

/* ===== 通用面板 ===== */
.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

/* ===== 搜索区 ===== */
.search-panel {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  .search-input {
    width: 360px;
    max-width: 100%;
  }
}

/* ===== 表格区 ===== */
.table-panel {
  padding: 0;
  overflow: hidden;
}

.log-table {
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-top: 1px solid var(--wf-hairline);
}

/* ===== 响应式 ===== */
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