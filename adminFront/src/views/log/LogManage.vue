<template>
  <div class="page-container">
    <div class="page-header">
      <h2>日志管理</h2>
    </div>
    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="logId" label="ID" width="80" />
      <el-table-column prop="userName" label="操作人" width="120" />
      <el-table-column prop="doSome" label="操作内容" min-width="150" />
      <el-table-column prop="musicName" label="音乐名称" min-width="150">
        <template #default="{ row }">
          <span v-if="row.musicName">{{ row.musicName }}</span>
          <span v-else style="color: var(--wf-mute)">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createDate" label="操作日期" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createDate) }}
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pn"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageLog } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const pn = ref(1)
const size = ref(10)
const total = ref(0)

function formatTime(time) {
  if (!time) return ''
  let d = null
  if (time instanceof Date) d = time
  else if (typeof time === 'string') d = new Date(time)
  else if (typeof time === 'number') d = new Date(time)
  if (!d || isNaN(d.getTime())) return String(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function loadData() {
  loading.value = true
  try {
    const res = await pageLog(pn.value, size.value)
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载日志列表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--wf-ink);
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>