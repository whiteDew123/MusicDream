<template>
  <div class="placeholder">
    <el-result icon="info" title="功能开发中" :sub-title="`${title} 模块即将上线，敬请期待`">
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '该')
</script>

<style scoped>
/* Webflow 亮色：白色卡片 + hairline 边框 + 轻阴影 */
.placeholder {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
