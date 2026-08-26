<template>
  <div class="login-page" @click="handlePageClick">
    <!-- ===== 品牌区（全屏深色底） ===== -->
    <div class="brand-panel" :class="{ 'form-visible': showForm }" ref="brandPanelRef">
      <!-- 音柱图：底部 37% 区域 -->
      <div class="spectrum">
        <span
          v-for="i in 80"
          :key="i"
          class="bar"
          :style="barStyle(i)"
        ></span>
      </div>

      <!-- 浮动音符粒子 ×18 -->
      <div class="notes">
        <span v-for="n in 18" :key="n" class="note" :style="{ '--n': n }">♪</span>
      </div>

      <!-- 点击涟漪层 -->
      <div class="ripple-layer">
        <template v-for="r in ripples" :key="r.id">
          <span class="ripple-ring" :style="{ left: r.x + 'px', top: r.y + 'px' }"></span>
          <span class="ripple-note" :style="{ left: r.x + 'px', top: r.y + 'px', '--angle': r.angle + 'deg' }">♪</span>
          <span class="ripple-note" :style="{ left: r.x + 'px', top: r.y + 'px', '--angle': (r.angle + 60) + 'deg' }">♫</span>
          <span class="ripple-note" :style="{ left: r.x + 'px', top: r.y + 'px', '--angle': (r.angle - 45) + 'deg' }">♪</span>
        </template>
      </div>

      <!-- 品牌文案 -->
      <div class="brand-content">
        <div class="brand-glow"></div>
        <div class="logo-wrap">
          <el-icon class="logo-icon"><Headset /></el-icon>
        </div>
        <h1 class="brand-title">MusicDreamer</h1>
        <p class="brand-slogan">让音乐流动</p>
        <p v-if="!showForm" class="brand-hint">点击任意位置开始</p>
      </div>
    </div>

    <!-- ===== 右侧表单区（全列式毛玻璃，45% 宽 × 100% 高） ===== -->
    <Transition name="form-slide">
      <div v-if="showForm" class="form-panel" @click.stop>
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录你的音乐账户</p>
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
    </Transition>
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
const showForm = ref(false)
const brandPanelRef = ref(null)

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

// 音柱图：用索引生成伪随机高度，80 根柱子的基准高度
function barStyle(i) {
  const h = 12 + ((i * 7 + i * i * 3 + 17) % 72)
  const d = ((i * 0.04) % 2.4).toFixed(2)
  return {
    '--h': h + '%',
    '--d': d + 's'
  }
}

// 点击涟漪
const ripples = ref([])
let rippleId = 0

function createRipple(e) {
  const rect = brandPanelRef.value?.getBoundingClientRect()
  if (!rect) return
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const id = ++rippleId
  const angle = Math.random() * 360
  ripples.value.push({ id, x, y, angle })
  setTimeout(() => {
    ripples.value = ripples.value.filter(r => r.id !== id)
  }, 900)
}

function handlePageClick(e) {
  createRipple(e)

  if (showForm.value) {
    if (!e.target.closest('.form-panel')) {
      showForm.value = false
    }
    return
  }

  showForm.value = true
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/discover'
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
/* ================================================================
 * 播放端登录页 · 音柱图 + 全列式毛玻璃表单 + 点击涟漪
 * 遵循 frontend-design-standards：
 *   - design-stripe.md
 *   - Design-standards.md（登录页微调规则）
 * ================================================================ */

.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* ===== 品牌区：全屏深色底 ===== */
.brand-panel {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  overflow: hidden;
}

/* ===== 音柱图：底部 37% 区域 ===== */
.spectrum {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 37%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 2px;
  padding: 0 2px;
  mask-image: linear-gradient(to top, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.08) 80%, transparent 100%);
  -webkit-mask-image: linear-gradient(to top, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.08) 80%, transparent 100%);
}

.bar {
  flex: 1;
  max-width: 10px;
  min-width: 2px;
  border-radius: 3px 3px 0 0;
  background: linear-gradient(to top, var(--st-primary) 0%, rgba(94, 92, 230, 0.45) 70%, rgba(94, 92, 230, 0.1) 100%);
  height: var(--h);
  animation: barDance 1.8s ease-in-out infinite;
  animation-delay: var(--d);
  transform-origin: bottom;
}

@keyframes barDance {
  0%, 100% { transform: scaleY(0.6); }
  30%      { transform: scaleY(1.15); }
  60%      { transform: scaleY(0.75); }
  85%      { transform: scaleY(1.05); }
}

/* ===== 浮动音符 ×18 ===== */
.notes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.note {
  position: absolute;
  font-size: 22px;
  color: var(--st-primary);
  opacity: 0;
  animation: noteFloat 10s ease-in infinite;
  animation-delay: calc(var(--n) * 0.7s);

  &:nth-child(1)  { top: 8%;  left: 5%;  font-size: 30px; }
  &:nth-child(2)  { top: 50%; left: 10%; font-size: 18px; }
  &:nth-child(3)  { top: 22%; left: 20%; font-size: 24px; }
  &:nth-child(4)  { top: 65%; left: 30%; font-size: 28px; }
  &:nth-child(5)  { top: 10%; left: 40%; font-size: 20px; }
  &:nth-child(6)  { top: 55%; left: 50%; font-size: 34px; }
  &:nth-child(7)  { top: 30%; left: 60%; font-size: 22px; }
  &:nth-child(8)  { top: 70%; left: 70%; font-size: 26px; }
  &:nth-child(9)  { top: 15%; left: 80%; font-size: 32px; }
  &:nth-child(10) { top: 45%; left: 90%; font-size: 20px; }
  &:nth-child(11) { top: 35%; left: 15%; font-size: 28px; }
  &:nth-child(12) { top: 68%; left: 45%; font-size: 24px; }
  &:nth-child(13) { top: 5%;  left: 65%; font-size: 18px; }
  &:nth-child(14) { top: 60%; left: 85%; font-size: 30px; }
  &:nth-child(15) { top: 28%; left: 35%; font-size: 22px; }
  &:nth-child(16) { top: 78%; left: 20%; font-size: 26px; }
  &:nth-child(17) { top: 18%; left: 75%; font-size: 20px; }
  &:nth-child(18) { top: 40%; left: 55%; font-size: 32px; }
}

@keyframes noteFloat {
  0%   { opacity: 0; transform: translateY(60px) rotate(0deg); }
  10%  { opacity: 0.25; }
  30%  { opacity: 0.45; transform: translateY(-40px) rotate(12deg); }
  60%  { opacity: 0.15; transform: translateY(-80px) rotate(-8deg); }
  100% { opacity: 0; transform: translateY(-130px) rotate(0deg); }
}

/* ===== 点击涟漪层 ===== */
.ripple-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 3;
}

.ripple-ring {
  position: absolute;
  width: 10px;
  height: 10px;
  margin-left: -5px;
  margin-top: -5px;
  border-radius: 50%;
  border: 1.5px solid rgba(94, 92, 230, 0.6);
  animation: ringExpand 900ms ease-out forwards;
  pointer-events: none;
}

@keyframes ringExpand {
  0% {
    width: 6px;
    height: 6px;
    margin-left: -3px;
    margin-top: -3px;
    opacity: 0.9;
    border-width: 2px;
  }
  100% {
    width: 120px;
    height: 120px;
    margin-left: -60px;
    margin-top: -60px;
    opacity: 0;
    border-width: 0.5px;
  }
}

.ripple-note {
  position: absolute;
  font-size: 18px;
  color: var(--st-primary);
  pointer-events: none;
  animation: noteBurst 800ms ease-out forwards;
}

@keyframes noteBurst {
  0% {
    opacity: 0.8;
    transform: translate(0, 0) scale(0.5);
  }
  100% {
    opacity: 0;
    transform: translate(
      calc(cos(var(--angle)) * 48px),
      calc(sin(var(--angle)) * 48px - 40px)
    ) scale(1.2);
  }
}

/* ===== 品牌文案 + 呼吸光晕 ===== */
.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 48px;
  user-select: none;
  transition: transform 600ms cubic-bezier(0.16, 1, 0.3, 1);

  .brand-panel.form-visible & {
    transform: translateX(-22%);
  }
}

.brand-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 300px;
  height: 300px;
  transform: translate(-50%, -50%);
  background: radial-gradient(
    circle at center,
    rgba(94, 92, 230, 0.18) 0%,
    rgba(94, 92, 230, 0.06) 40%,
    transparent 70%
  );
  border-radius: 50%;
  pointer-events: none;
  animation: glowBreathe 3.5s ease-in-out infinite;
  transition: width 400ms ease, height 400ms ease, background 400ms ease;
}

.brand-content:hover .brand-glow {
  width: 380px;
  height: 380px;
  background: radial-gradient(
    circle at center,
    rgba(94, 92, 230, 0.32) 0%,
    rgba(94, 92, 230, 0.12) 40%,
    transparent 70%
  );
  animation: glowBreatheStrong 2s ease-in-out infinite;
}

@keyframes glowBreathe {
  0%, 100% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(0.9);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes glowBreatheStrong {
  0%, 100% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(0.85);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.logo-wrap {
  position: relative;
  z-index: 1;
  width: 72px;
  height: 72px;
  margin: 0 auto 20px;
  border-radius: var(--rounded-xl);
  background: linear-gradient(135deg, var(--st-primary) 0%, #8a85f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(94, 92, 230, 0.35);
  animation: logoPulse 3s ease-in-out infinite;
  transition: box-shadow 400ms ease, transform 300ms ease;
}

.brand-content:hover .logo-wrap {
  box-shadow: 0 12px 48px rgba(94, 92, 230, 0.55);
  transform: scale(1.05);
}

@keyframes logoPulse {
  0%, 100% { box-shadow: 0 8px 32px rgba(94, 92, 230, 0.35); }
  50%      { box-shadow: 0 8px 48px rgba(94, 92, 230, 0.55); }
}

.logo-icon {
  font-size: 36px;
  color: #fff;
}

.brand-title {
  position: relative;
  z-index: 1;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.6px;
  margin-bottom: 8px;
  transition: text-shadow 400ms ease;
}

.brand-content:hover .brand-title {
  text-shadow: 0 0 40px rgba(94, 92, 230, 0.5);
}

.brand-slogan {
  position: relative;
  z-index: 1;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.65);
  letter-spacing: 4px;
  margin-bottom: 24px;
  transition: color 400ms ease;
}

.brand-content:hover .brand-slogan {
  color: rgba(255, 255, 255, 0.85);
}

.brand-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 2px;
  animation: hintPulse 2.5s ease-in-out infinite;
}

@keyframes hintPulse {
  0%, 100% { opacity: 0.3; }
  50%      { opacity: 0.7; }
}

/* ===== 右侧表单区：全列式毛玻璃，45% 宽 × 100% 高 ===== */
.form-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 45%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(28px);
  -webkit-backdrop-filter: blur(28px);
  border-left: 1px solid rgba(255, 255, 255, 0.4);
}

.form-card {
  width: 100%;
  max-width: 420px;
  padding: 0 48px;
}

/* 表单滑入/滑出过渡 */
.form-slide-enter-active {
  transition: all 550ms cubic-bezier(0.16, 1, 0.3, 1);
}
.form-slide-leave-active {
  transition: all 400ms cubic-bezier(0.55, 0, 1, 0.45);
}
.form-slide-enter-from {
  opacity: 0;
  transform: translateX(80px);
}
.form-slide-leave-to {
  opacity: 0;
  transform: translateX(60px);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.3px;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: var(--st-ink-mute);
  }
}

/* 输入框 */
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  :deep(.el-input__wrapper) {
    background: rgba(241, 244, 249, 0.7);
    backdrop-filter: blur(4px);
    border: 1px solid rgba(226, 232, 240, 0.4);
    border-radius: var(--rounded-md);
    padding: 4px 12px;
    box-shadow: none;
    transition: border-color 200ms ease, background 200ms ease;
  }
  :deep(.el-input__wrapper:hover) {
    background: rgba(233, 237, 243, 0.85);
  }
  :deep(.el-input__wrapper.is-focus) {
    border-color: var(--st-primary);
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
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

/* 提交按钮 */
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

/* 底部链接 */
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

/* ===== 响应式：小屏 → 表单全宽覆盖 ===== */
@media (max-width: 768px) {
  .brand-content {
    .brand-panel.form-visible & {
      transform: none;
    }
  }

  .form-panel {
    width: 100%;
  }

  .form-card {
    padding: 0 32px;
  }
}
</style>