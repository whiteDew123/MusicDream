<template>
  <div class="not-found">
    <div class="nf-inner">
      <div class="nf-code">404</div>
      <p class="nf-text">页面走丢了</p>
      <router-link to="/discover" class="nf-link">
        <el-icon><Back /></el-icon>
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { Back } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;

  .nf-inner {
    text-align: center;

    .nf-code {
      font-size: 80px;
      font-weight: 300;
      color: var(--st-primary);
      letter-spacing: -2px;
      line-height: 1;
    }

    .nf-text {
      font-size: 16px;
      color: var(--st-ink-mute);
      margin: 12px 0 24px;
    }

    .nf-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 8px 20px;
      border-radius: var(--rounded-pill);
      background: var(--st-primary);
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      transition: all 200ms ease;

      &:hover {
        background: var(--st-primary-hover);
        transform: translateY(-1px);
      }
    }
  }
}
</style>
