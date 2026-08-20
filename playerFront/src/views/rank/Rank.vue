<template>
  <div class="page-placeholder">
    <div class="placeholder-inner">
      <el-icon class="placeholder-icon"><TrophyBase /></el-icon>
      <h2>排行榜</h2>
      <p>热门歌曲榜单，敬请期待</p>
    </div>
  </div>
</template>

<script setup>
import { TrophyBase } from '@element-plus/icons-vue'
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
