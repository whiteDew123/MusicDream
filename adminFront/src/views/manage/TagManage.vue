<template>
  <div class="tag-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">MANAGEMENT · TAGS</p>
        <h2 class="page-title">标签管理</h2>
        <p class="page-desc">维护歌曲标签字典，控制标签启用状态，并查看各标签使用频次。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 标签字典 -->
      <el-tab-pane label="标签字典" name="dict">
        <div class="panel search-panel">
          <el-input
            v-model="keyword"
            class="search-input"
            placeholder="类别码 / 标签名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" :loading="store.loading" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain @click="openAdd">新增标签</el-button>
        </div>

        <div class="panel table-panel">
          <el-table v-loading="store.loading" :data="store.tags" row-key="tagId" class="tag-table">
            <el-table-column prop="tagId" label="ID" width="70" align="center" />
            <el-table-column label="类别" width="100" align="center">
              <template #default="{ row }">{{ categoryText(row.code) }}</template>
            </el-table-column>
            <el-table-column prop="code" label="类别码" width="100" align="center" />
            <el-table-column prop="name" label="标签名" min-width="140" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status === 1"
                  :loading="store.actionId === row.tagId"
                  @change="(val) => handleStatus(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="使用次数" width="100" align="center">
              <template #default="{ row }">{{ row.usageCount ?? 0 }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" plain size="small" @click="openEdit(row)">编辑</el-button>
                <el-button
                  type="danger"
                  plain
                  size="small"
                  :loading="store.actionId === row.tagId"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="store.total"
              :page-sizes="[10, 20, 50]"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 使用统计 -->
      <el-tab-pane label="使用统计" name="stats">
        <div class="panel table-panel">
          <el-table v-loading="store.loading" :data="store.statList" row-key="tagId" class="tag-table">
            <el-table-column type="index" label="排名" width="80" align="center" />
            <el-table-column label="类别" width="120" align="center">
              <template #default="{ row }">{{ categoryText(row.code) }}</template>
            </el-table-column>
            <el-table-column prop="name" label="标签名" min-width="160" />
            <el-table-column prop="usageCount" label="使用次数" width="120" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" round>
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增标签' : '编辑标签'"
      width="420px"
      destroy-on-close
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="类别">
          <el-select v-model="form.code" placeholder="请选择类别" style="width: 100%">
            <el-option
              v-for="opt in TAG_CODE_OPTIONS"
              :key="opt.code"
              :label="opt.label"
              :value="opt.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签名">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="store.saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useTagStore } from '@/store/tag'
import { CODE_CATEGORY_MAP, TAG_CODE_OPTIONS } from '@/constants/tags'

const store = useTagStore()

const activeTab = ref('dict')
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogMode = ref('add')
const form = reactive({
  tagId: null,
  code: 'genre',
  name: ''
})

function categoryText(code) {
  return CODE_CATEGORY_MAP[code] || code
}

function loadTags() {
  store.fetchTags({
    page: pagination.current,
    pageSize: pagination.pageSize,
    keyword: keyword.value.trim()
  })
}

function handleSearch() {
  pagination.current = 1
  loadTags()
}

function handleReset() {
  keyword.value = ''
  pagination.current = 1
  loadTags()
}

function handlePageChange(page) {
  pagination.current = page
  loadTags()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadTags()
}

function handleTabChange(name) {
  if (name === 'stats') {
    store.fetchStats()
  }
}

function openAdd() {
  dialogMode.value = 'add'
  form.tagId = null
  form.code = 'genre'
  form.name = ''
  dialogVisible.value = true
}

function openEdit(row) {
  dialogMode.value = 'edit'
  form.tagId = row.tagId
  form.code = row.code
  form.name = row.name
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.code) {
    ElMessage.warning('请选择类别')
    return
  }
  if (!form.name || !form.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    if (dialogMode.value === 'add') {
      await store.createTag(form.code.trim(), form.name.trim())
      ElMessage.success('新增成功')
    } else {
      await store.editTag(form.tagId, form.code.trim(), form.name.trim())
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadTags()
  } catch (e) {
    // 失败提示由请求拦截器统一弹出
  }
}

async function handleStatus(row, val) {
  try {
    await store.setTagStatus(row.tagId, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch (e) {
    // 失败提示由请求拦截器统一弹出
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  try {
    await store.removeTag(row.tagId)
    ElMessage.success('删除成功')
    loadTags()
  } catch (e) {
    // 被引用时后端返回明确提示，由拦截器弹出
  }
}

onMounted(loadTags)
</script>

<style scoped lang="scss">
.tag-manage {
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
    margin: 0 0 4px;
  }
  .page-desc {
    font-size: 13px;
    color: var(--wf-mute);
    margin: 0;
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
    width: 300px;
    max-width: 100%;
  }
}

.table-panel {
  padding: 0;
  overflow: hidden;

  .tag-table {
    width: 100%;
  }
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