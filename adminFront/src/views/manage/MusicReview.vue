<template>
  <div class="music-review">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">MANAGEMENT · REVIEW</p>
        <h2 class="page-title">待审核歌曲</h2>
        <p class="page-desc">自动审核未通过的歌曲会进入这里，由管理员人工兜底审核。</p>
      </div>
    </div>

    <!-- 待审核表格卡片 -->
    <div class="panel table-panel">
      <el-table
        v-loading="store.loading"
        :data="store.songs"
        row-key="musicId"
        class="review-table"
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
        <el-table-column prop="fromSinger" label="歌手ID" width="100" align="center" />
        <el-table-column prop="musicUrl" label="音频URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="imageUrl" label="封面URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="120" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              plain
              size="small"
              :loading="store.actionId === row.musicId"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              plain
              size="small"
              :loading="store.actionId === row.musicId"
              @click="handleReject(row)"
            >
              驳回
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
import { onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Headset } from '@element-plus/icons-vue'
import { useAdminReviewStore } from '@/store/adminReview'

const store = useAdminReviewStore()

const pagination = reactive({
  current: 1,
  pageSize: 10
})

onMounted(() => {
  loadSongs()
})

async function loadSongs() {
  await store.fetchPendingSongs({
    page: pagination.current,
    pageSize: pagination.pageSize
  })
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

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(
      `确定要通过歌曲「${row.musicName}」吗？通过后将对用户可见。`,
      '审核通过',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    await store.approve(row.musicId)
    ElMessage.success('审核通过，歌曲已公开')
    loadSongs()
  } catch (e) {
    // 用户取消或请求失败
  }
}

async function handleReject(row) {
  try {
    const { value: remark } = await ElMessageBox.prompt(
      `请输入驳回歌曲「${row.musicName}」的原因：`,
      '审核驳回',
      {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入驳回原因',
        inputType: 'textarea',
        type: 'warning'
      }
    )
    await store.reject(row.musicId, remark || '')
    ElMessage.success('已驳回')
    loadSongs()
  } catch (e) {
    // 用户取消或请求失败
  }
}
</script>

<style scoped lang="scss">
.music-review {
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

.table-panel {
  padding: 0;
  overflow: hidden;
}

.review-table {
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
  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>