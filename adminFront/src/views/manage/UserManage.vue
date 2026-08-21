<template>
  <div class="user-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">MANAGEMENT · USER</p>
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">管理系统内所有歌手与普通用户，支持账号冻结与解冻。</p>
      </div>
    </div>

    <!-- 搜索卡片 -->
    <div class="panel search-panel">
      <el-input
        v-model="keyword"
        class="search-input"
        placeholder="用户名 / 邮箱 / 手机号 / ID"
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

    <!-- 用户表格卡片 -->
    <div class="panel table-panel">
      <el-table
        v-loading="loading"
        :data="store.users"
        row-key="id"
        class="user-table"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.phone || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'warning' : 'info'" effect="light" round>
              {{ row.role === 1 ? '歌手' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.activation === 0 ? 'success' : 'danger'" effect="light" round>
              {{ row.activation === 0 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="120" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.activation === 0"
              type="danger"
              plain
              size="small"
              :loading="store.actionId === row.id"
              @click="handleFreeze(row)"
            >
              冻结
            </el-button>
            <el-button
              v-else
              type="success"
              plain
              size="small"
              :loading="store.actionId === row.id"
              @click="handleUnfreeze(row)"
            >
              解冻
            </el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useAdminUserStore } from '@/store/adminUser'

const store = useAdminUserStore()

const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    await store.fetchUsers({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value.trim()
    })
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadUsers()
}

function handleReset() {
  keyword.value = ''
  pagination.current = 1
  loadUsers()
}

function handlePageChange(page) {
  pagination.current = page
  loadUsers()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadUsers()
}

async function handleFreeze(row) {
  try {
    await ElMessageBox.confirm(
      `确定要冻结用户「${row.username}」吗？冻结后其歌曲也会同步冻结。`,
      '冻结确认',
      {
        confirmButtonText: '确定冻结',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await store.freezeUser(row.id)
    ElMessage.success('冻结成功')
    loadUsers()
  } catch (e) {
    // 用户取消或请求失败，失败提示已由 axios 拦截器统一处理
  }
}

async function handleUnfreeze(row) {
  try {
    await ElMessageBox.confirm(
      `确定要解冻用户「${row.username}」吗？解冻后其歌曲会同步恢复。`,
      '解冻确认',
      {
        confirmButtonText: '确定解冻',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await store.unfreezeUser(row.id)
    ElMessage.success('解冻成功')
    loadUsers()
  } catch (e) {
    // 用户取消或请求失败
  }
}
</script>

<style scoped lang="scss">
.user-manage {
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

.user-table {
  width: 100%;

  /* 管理端表格行悬停：背景变暗 + 左侧蓝色竖条（animation.md 第四章） */
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