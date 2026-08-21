<template>
  <div class="login-container">
    <!-- 装饰性柔光（Stripe 亮色：极浅蓝灰渐变 + 微弱光晕） -->
    <div class="bg-glow bg-glow-1"></div>
    <div class="bg-glow bg-glow-2"></div>

    <div class="login-card">
      <!-- Logo + Slogan（Design-standards.md §48：顶部居中） -->
      <div class="login-header">
        <div class="logo-wrap">
          <el-icon class="logo-icon"><Headset /></el-icon>
        </div>
        <h1>MusicDreamer</h1>
        <p class="slogan">让音乐流动 · 后台管理</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="用户名 / 邮箱 / 手机号"
            :prefix-icon="User"
            clearable
            class="st-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            clearable
            class="st-input"
          />
        </el-form-item>
        <el-form-item>
          <button
            type="button"
            class="submit-btn"
            :disabled="loading"
            @click="handleLogin"
          >
            <span v-if="!loading" class="btn-text">登 录</span>
            <span v-else class="btn-text">登录中…</span>
            <span class="ripple"></span>
          </button>
        </el-form-item>
        <div class="login-footer">
          <span class="footer-text">还没有账号？</span>
          <router-link to="/register" class="footer-link">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, Headset } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await userStore.login(loginForm)
      if (data.role !== 0 && data.role !== 1) {
        ElMessage.error('该账号无后台访问权限，请联系管理员')
        userStore.logout()
        return
      }
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/dashboard'
      router.push(redirect)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
/* ===== 登录页专属 · 极浅蓝灰渐变（Design-standards.md §44：#f0f4f8 → #e2e8f0）===== */
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(180deg, var(--st-bg-from) 0%, var(--st-bg-to) 100%);
}

/* 装饰柔光：紫色 + 浅蓝，低透明度，不抢视觉重心 */
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.35;
  pointer-events: none;
  z-index: 0;
}
.bg-glow-1 {
  width: 480px;
  height: 480px;
  background: var(--st-primary);
  top: -160px;
  left: -120px;
}
.bg-glow-2 {
  width: 560px;
  height: 560px;
  background: #8ab4f8;
  bottom: -200px;
  right: -160px;
}

/* ===== 纯白卡片 + 轻微阴影（Design-standards.md §45：0 4px 20px rgba(0,0,0,0.06)）===== */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 48px 40px 36px;
  background: var(--st-canvas);
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: var(--rounded-xl);
  box-shadow: var(--shadow-md);
  animation: cardIn 500ms ease-out both;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== Logo + 标题（顶部居中）===== */
.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-wrap {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    border-radius: var(--rounded-xl);
    /* logo 渐变：Stripe 紫 */
    background: linear-gradient(135deg, var(--st-primary) 0%, #8a85f0 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(94, 92, 230, 0.25);
  }
  .logo-icon {
    font-size: 32px;
    color: var(--st-canvas);
  }

  h1 {
    font-size: 26px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.4px;
    margin-bottom: 6px;
  }

  .slogan {
    font-size: 13px;
    color: var(--st-ink-mute);
    letter-spacing: 1px;
  }
}

/* ===== 输入框：浅灰背景 + 深色文字 + 聚焦紫色边框（Design-standards.md §46）===== */
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  :deep(.el-input__wrapper) {
    background: var(--st-input-bg);
    border: 1px solid transparent;
    border-radius: var(--rounded-md);
    padding: 4px 12px;
    box-shadow: none;
    transition: border-color 200ms ease, background 200ms ease;
  }
  :deep(.el-input__wrapper:hover) {
    background: #e9edf3;
  }
  :deep(.el-input__wrapper.is-focus) {
    border-color: var(--st-primary);
    background: var(--st-canvas);
    box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
  }
  :deep(.el-input__inner) {
    color: var(--st-ink);
    font-size: 15px;
    height: 42px;
  }
  :deep(.el-input__inner::placeholder) {
    color: var(--wf-mute);
  }
  :deep(.el-input__prefix-inner) {
    color: var(--st-ink-mute);
  }
  :deep(.el-input__clear),
  :deep(.el-input__password) {
    color: var(--st-ink-mute);
  }
}

/* ===== 紫色主按钮 + 悬停加深 + 音浪波纹（Design-standards.md §47）===== */
.submit-btn {
  position: relative;
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--rounded-md);
  background: var(--st-primary);
  color: var(--st-canvas);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  overflow: hidden;
  transition: background 250ms ease, box-shadow 250ms ease, transform 150ms ease;
}
.submit-btn:hover:not(:disabled) {
  background: var(--st-primary-hover);
  box-shadow: 0 6px 24px rgba(94, 92, 230, 0.3);
  transform: translateY(-1px);
}
.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 音浪波纹微动效（点击时触发）*/
.submit-btn:active .ripple,
.submit-btn:focus-visible .ripple {
  animation: waveRipple 600ms ease-out;
}
.ripple {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at center,
    rgba(255, 255, 255, 0.45) 0%,
    transparent 60%
  );
  opacity: 0;
  pointer-events: none;
}
@keyframes waveRipple {
  0% {
    opacity: 0.9;
    transform: scale(0.6);
  }
  100% {
    opacity: 0;
    transform: scale(1.4);
  }
}

/* ===== 底部链接 ===== */
.login-footer {
  text-align: center;
  margin-top: 4px;
  font-size: 14px;

  .footer-text {
    color: var(--st-ink-mute);
  }
  .footer-link {
    color: var(--st-primary);
    font-weight: 500;
    margin-left: 4px;
    transition: color 150ms ease;
  }
  .footer-link:hover {
    color: var(--st-primary-hover);
    text-decoration: underline;
  }
}
</style>
