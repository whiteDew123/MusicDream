<template>
  <div class="page-placeholder">
    <div class="placeholder-inner">
      <el-icon class="placeholder-icon"><Bell /></el-icon>
      <h2>消息通知</h2>
      <p>你的消息通知将在这里展示</p>
    </div>
  </div>
</template>

<script setup>
import { Bell } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.page-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;

  .placeholder-inner {
    text-align: center;

    .placeholder-icon {
      font-size: 64px;
      color: var(--st-primary-subdued);
      margin-bottom: 16px;
    }

    h2 {
      font-size: 24px;
      font-weight: 300;
      color: var(--st-ink);
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: var(--st-ink-mute);
    }
  }
}
</style>
