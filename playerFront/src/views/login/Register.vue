<template>
  <div class="register-container">
    <!-- ===== Stripe 渐变 mesh 背景层（与登录页同款，保持两端门面统一）===== -->
    <div class="mesh-bg" aria-hidden="true"></div>

    <div class="register-card">
      <!-- Logo + Slogan（Design-standards.md §48：顶部居中） -->
      <div class="register-header">
        <div class="logo-wrap">
          <el-icon class="logo-icon"><Headset /></el-icon>
        </div>
        <h1>注册账号</h1>
        <p class="slogan">开启你的音乐之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="rules"
        size="large"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名（2-20 个字符）"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码（至少 6 位）"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="邮箱"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div class="code-row">
            <el-input
              v-model="registerForm.emailCode"
              placeholder="邮箱验证码"
              :prefix-icon="Key"
              clearable
            />
            <button
              type="button"
              class="code-btn"
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </div>
        </el-form-item>
        <el-form-item>
          <button
            type="button"
            class="submit-btn"
            :disabled="loading"
            @click="handleRegister"
          >
            <span v-if="!loading" class="btn-text">注 册</span>
            <span v-else class="btn-text">
              <span class="spinner"></span>注册中…
            </span>
            <span class="ripple"></span>
          </button>
        </el-form-item>
        <div class="register-footer">
          <span class="footer-text">已有账号？</span>
          <router-link to="/login" class="footer-link">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Key, Headset } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { registerApi, sendEmailCodeApi } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref()
const loading = ref(false)
const countdown = ref(0)
let timer = null

const registerForm = reactive({
  username: '',
  password: '',
  email: '',
  emailCode: ''
})

const emailValidator = (rule, value, callback) => {
  const reg = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  if (!reg.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: emailValidator, trigger: 'blur' }
  ],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }]
}

async function handleSendCode() {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    await registerFormRef.value.validateField('email')
    await sendEmailCodeApi(registerForm.email)
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown()
  } catch (e) {
    console.error(e)
  }
}

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function handleRegister() {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await registerApi(registerForm)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
/* ================================================================
 * 注册页 V2 · 与登录页共享门面语言（mesh + 白卡表单）
 * 依据：Design-standards.md §44-47 登录页微调规则
 * ================================================================ */

.register-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* ===== Stripe mesh 背景（与登录页.mesh-bg 同款，保证门面统一）===== */
.mesh-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(720px 420px at 12% 6%, rgba(245, 233, 212, 0.95), transparent 62%),
    radial-gradient(640px 400px at 90% 10%, rgba(185, 185, 249, 0.72), transparent 64%),
    radial-gradient(600px 380px at 44% 16%, rgba(99, 65, 255, 0.10), transparent 62%),
    radial-gradient(560px 360px at 74% 84%, rgba(234, 34, 97, 0.16), transparent 60%),
    radial-gradient(680px 460px at 6% 88%, rgba(249, 107, 238, 0.14), transparent 62%),
    linear-gradient(180deg, #f6f9fc 0%, #eef2f8 100%);
}

/* ===== 纯白卡片 + 轻微阴影（Design-standards.md §45）===== */
.register-card {
  position: relative;
  z-index: 1;
  width: 440px;
  padding: 40px 40px 32px;
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
.register-header {
  text-align: center;
  margin-bottom: 24px;

  .logo-wrap {
    width: 56px;
    height: 56px;
    margin: 0 auto 12px;
    border-radius: var(--rounded-xl);
    background: linear-gradient(135deg, var(--st-primary) 0%, #8a85f0 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(99, 65, 255, 0.28);
  }
  .logo-icon {
    font-size: 28px;
    color: var(--st-canvas);
  }

  h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.2px;
    margin-bottom: 6px;
  }

  .slogan {
    font-size: 13px;
    color: var(--st-ink-mute);
    letter-spacing: 1px;
  }
}

/* ===== 输入框：浅灰背景 + 深色文字 + 聚焦紫色边框（Design-standards.md §46）===== */
.register-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
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
    box-shadow: 0 0 0 3px rgba(99, 65, 255, 0.14);
  }
  :deep(.el-input__inner) {
    color: var(--st-ink);
    font-size: 15px;
    height: 42px;
  }
  :deep(.el-input__inner::placeholder) {
    color: var(--st-ink-mute);
  }
  :deep(.el-input__prefix-inner) {
    color: var(--st-ink-mute);
  }
  :deep(.el-input__clear),
  :deep(.el-input__password) {
    color: var(--st-ink-mute);
  }
}

/* ===== 验证码按钮行 ===== */
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;

  :deep(.el-form-item) {
    margin: 0;
    flex: 1;
  }
}

.code-btn {
  flex-shrink: 0;
  width: 120px;
  height: 42px;
  border-radius: var(--rounded-pill);
  border: 1px solid rgba(99, 65, 255, 0.4);
  background: rgba(99, 65, 255, 0.08);
  color: var(--st-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 200ms ease;
}
.code-btn:hover:not(:disabled) {
  background: rgba(99, 65, 255, 0.16);
  border-color: var(--st-primary);
}
.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== 紫色主提交按钮（pill）+ 音浪波纹（Design-standards.md §47）===== */
.submit-btn {
  position: relative;
  width: 100%;
  height: 48px;
  border: none;
  border-radius: var(--rounded-pill);
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
  box-shadow: 0 6px 24px rgba(99, 65, 255, 0.32);
  transform: translateY(-1px);
}
.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}
.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

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

/* 加载圆环（animation.md §76） */
.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  margin-right: 8px;
  vertical-align: -3px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 底部链接 ===== */
.register-footer {
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
