<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 0 ? 'danger' : row.role === 1 ? 'warning' : 'info'">
            {{ row.role === 0 ? '管理员' : row.role === 1 ? '歌手' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activation" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.activation === 0 ? 'success' : 'danger'">
            {{ row.activation === 0 ? '正常' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.activation === 0" size="small" type="danger" @click="freeze(row)">冻结</el-button>
          <el-button v-else size="small" type="success" @click="unfreeze(row)">解冻</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUser, freezeUser, unfreezeUser } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const pn = ref(1)
const size = ref(10)
const total = ref(0)

async function loadData() {
  loading.value = true
  try {
    const res = await pageUser(pn.value, size.value)
    if (res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载用户列表失败:', e)
  } finally {
    loading.value = false
  }
}

async function freeze(row) {
  try {
    await ElMessageBox.confirm(`确定冻结用户 "${row.username}" 吗？`, '确认冻结', {
      type: 'warning'
    })
    const res = await freezeUser(row.id)
    if (res.code === 200) {
      ElMessage.success('冻结成功')
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function unfreeze(row) {
  try {
    await ElMessageBox.confirm(`确定解冻用户 "${row.username}" 吗？`, '确认解冻', {
      type: 'warning'
    })
    const res = await unfreezeUser(row.id)
    if (res.code === 200) {
      ElMessage.success('解冻成功')
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
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