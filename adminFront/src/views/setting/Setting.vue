<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>
    <el-card class="setting-card">
      <template #header>基础配置</template>
      <el-form label-width="120px">
        <el-form-item label="系统名称">
          <el-input v-model="form.systemName" />
        </el-form-item>
        <el-form-item label="系统Logo">
          <el-input v-model="form.logo" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  systemName: 'MusicDreamer 后台管理',
  logo: ''
})

function save() {
  ElMessage.success('设置已保存')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--wf-ink);
}
.setting-card {
  max-width: 600px;
}
</style>