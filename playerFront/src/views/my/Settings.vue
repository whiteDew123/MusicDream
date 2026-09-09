<template>
  <div class="settings-page">
    <!-- 页面标题 -->
    <header class="page-header">
      <span class="header-icon-wrap">
        <el-icon class="header-icon"><Setting /></el-icon>
      </span>
      <h2 class="header-title">个人设置</h2>
    </header>

    <!-- 基本信息分区 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="title-accent"></span>
        <h3>基本信息</h3>
      </div>

      <div class="avatar-row">
        <div class="avatar-preview" @click="avatarLoading || triggerAvatarSelect()" title="点击更换头像">
          <img v-if="avatarPreview" :src="avatarPreview" alt="头像" />
          <el-icon v-else class="avatar-fallback"><User /></el-icon>
          <div class="avatar-mask">
            <el-icon v-if="!avatarLoading"><Camera /></el-icon>
            <el-icon v-else class="is-loading"><Loading /></el-icon>
          </div>
        </div>
        <div class="avatar-tip">
          <p class="tip-main">点击头像可重新选择</p>
          <p class="tip-sub">支持 JPG / PNG，最大 2MB</p>
        </div>
        <input
          ref="avatarInputRef"
          type="file"
          accept="image/*"
          class="avatar-input"
          @change="handleAvatarChange"
        />
      </div>

      <el-form
        ref="baseFormRef"
        :model="baseForm"
        :rules="baseRules"
        label-position="top"
        class="settings-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="baseForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="baseForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="baseForm.phone"
            placeholder="请输入手机号（选填）"
            :prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        <el-form-item label="个人简介" prop="about">
          <el-input
            v-model="baseForm.about"
            type="textarea"
            :rows="4"
            placeholder="一句话介绍自己（选填）"
            maxlength="200"
            show-word-limit
            resize="none"
          />
        </el-form-item>
        <el-form-item>
          <button
            type="button"
            class="submit-btn"
            :disabled="baseLoading"
            @click="handleSaveBase"
          >
            {{ baseLoading ? '保存中…' : '保 存' }}
          </button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 修改密码分区 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="title-accent"></span>
        <h3>修改密码</h3>
      </div>

      <el-form
        ref="pwFormRef"
        :model="pwForm"
        :rules="pwRules"
        label-position="top"
        class="settings-form"
      >
        <el-form-item label="旧密码" prop="oldPassword" :error="pwError">
          <el-input
            v-model="pwForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少 6 位）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="pwForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleChangePassword"
          />
        </el-form-item>
        <el-form-item>
          <button
            type="button"
            class="submit-btn"
            :disabled="pwLoading"
            @click="handleChangePassword"
          >
            {{ pwLoading ? '提交中…' : '修改密码' }}
          </button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting,
  User,
  Message,
  Phone,
  Lock,
  Camera,
  Loading
} from '@element-plus/icons-vue'
import { updatePasswordApi } from '@/api/setting'
import { uploadImageApi } from '@/api/auth'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 基本信息表单（从 userStore 初始化，onMounted 时从后端拉完整数据回填）
const baseFormRef = ref()
const baseLoading = ref(false)
const baseForm = reactive({
  username: userStore.userInfo?.username || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '',
  about: userStore.userInfo?.about || ''
})

const baseRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const avatarInputRef = ref(null)
const avatarLoading = ref(false)
const avatarPreview = ref(
  userStore.userInfo?.imageUrl || userStore.userInfo?.avatar || ''
)

function triggerAvatarSelect() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    e.target.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 2MB')
    e.target.value = ''
    return
  }

  const blobUrl = URL.createObjectURL(file)
  avatarLoading.value = true
  avatarPreview.value = blobUrl
  try {
    const res = await uploadImageApi(file)
    const fileUrl = (res.data?.fileUrl || res.data || '').toString().trim()
    if (!fileUrl) throw new Error('上传响应缺少 fileUrl')

    await userStore.updateProfile({ imageUrl: fileUrl })

    URL.revokeObjectURL(blobUrl)
    avatarPreview.value = fileUrl
    ElMessage.success('头像更新成功')
  } catch (err) {
    URL.revokeObjectURL(blobUrl)
    avatarPreview.value = userStore.userInfo?.imageUrl || ''
    ElMessage.error('头像上传失败，请重试')
  } finally {
    avatarLoading.value = false
    e.target.value = ''
  }
}

// 保存基本信息
async function handleSaveBase() {
  if (!baseFormRef.value) return
  await baseFormRef.value.validate(async (valid) => {
    if (!valid) return
    const userId = userStore.userInfo?.userId
    if (!userId) {
      ElMessage.warning('登录信息已失效，请重新登录')
      return
    }
    baseLoading.value = true
    try {
      await userStore.updateProfile({
        email: baseForm.email,
        phone: baseForm.phone,
        about: baseForm.about
      })
      ElMessage.success('保存成功')
    } catch (e) {
      // 错误提示已由请求拦截器统一处理
    } finally {
      baseLoading.value = false
    }
  })
}

// 修改密码表单
const pwFormRef = ref()
const pwLoading = ref(false)
const pwError = ref('') // 旧密码服务端错误内联提示
const pwForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 修改密码
async function handleChangePassword() {
  pwError.value = ''
  if (!pwFormRef.value) return
  await pwFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!userStore.isLogin()) {
      ElMessage.warning('登录信息已失效，请重新登录')
      return
    }
    pwLoading.value = true
    try {
      await updatePasswordApi({
        oldPassword: pwForm.oldPassword,
        newPassword: pwForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwFormRef.value.resetFields()
    } catch (e) {
      // 失败（如旧密码错误）：拦截器已弹出提示，这里在旧密码字段下加内联错误
      pwError.value = e?.message || '密码修改失败，请检查旧密码'
    } finally {
      pwLoading.value = false
    }
  })
}

// 组件挂载时从后端拉取完整用户资料回填表单（登录态可能只有部分字段）
onMounted(async () => {
  try {
    const full = await userStore.fetchCurrentUser()
    baseForm.username = full.username || ''
    baseForm.email = full.email || ''
    baseForm.phone = full.phone || ''
    baseForm.about = full.about || ''
    avatarPreview.value = full.imageUrl || ''
  } catch (e) {
    // 401 等会被拦截器处理
  }
})
</script>

<style scoped lang="scss">
.settings-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 8px 0 32px;
}

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  .header-icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: var(--rounded-md);
    background: rgba(94, 92, 230, 0.1);
  }

  .header-icon {
    font-size: 22px;
    color: var(--st-primary);
  }

  .header-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--st-ink);
    letter-spacing: -0.2px;
  }
}

/* ===== 分区卡片 ===== */
.settings-section {
  padding: 24px 28px 8px;
  margin-bottom: 20px;
  background: var(--st-canvas);
  border: 1px solid var(--st-hairline);
  border-radius: var(--rounded-lg);
  box-shadow: var(--shadow-sm);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;

  .title-accent {
    width: 4px;
    height: 16px;
    border-radius: var(--rounded-pill);
    background: var(--st-primary);
  }

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: var(--st-ink);
  }
}

/* ===== 头像预览行 ===== */
.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  position: relative;
}

.avatar-preview {
  position: relative;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--st-input-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid var(--st-hairline);
  transition: box-shadow 200ms ease, transform 200ms ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-fallback {
    font-size: 36px;
    color: var(--st-ink-mute);
  }

  .avatar-mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0);
    color: #fff;
    font-size: 20px;
    opacity: 0;
    transition: all 200ms ease;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);

    .avatar-mask {
      background: rgba(0, 0, 0, 0.4);
      opacity: 1;
    }
  }
}

.avatar-tip {
  .tip-main {
    font-size: 14px;
    color: var(--st-ink);
    margin-bottom: 4px;
  }
  .tip-sub {
    font-size: 12px;
    color: var(--st-ink-mute);
  }
}

.avatar-input {
  display: none;
}

/* ===== 表单通用样式 ===== */
.settings-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    color: var(--st-ink-mute);
    padding-bottom: 6px;
    line-height: 1.4;
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
    background: var(--st-input-hover);
  }
  :deep(.el-input__wrapper.is-focus) {
    border-color: var(--st-primary);
    background: var(--st-canvas);
    box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
  }
  :deep(.el-input__inner) {
    color: var(--st-ink);
    font-size: 14px;
    height: 40px;
  }
  :deep(.el-textarea__inner) {
    background: var(--st-input-bg);
    border: 1px solid transparent;
    border-radius: var(--rounded-md);
    color: var(--st-ink);
    font-size: 14px;
    line-height: 1.6;
    box-shadow: none;
    transition: border-color 200ms ease, background 200ms ease;
  }
  :deep(.el-textarea__inner:hover) {
    background: var(--st-input-hover);
  }
  :deep(.el-textarea__inner:focus) {
    border-color: var(--st-primary);
    background: var(--st-canvas);
    box-shadow: 0 0 0 3px rgba(94, 92, 230, 0.12);
  }
  :deep(.el-input__prefix-inner),
  :deep(.el-input__suffix-inner) {
    color: var(--st-ink-mute);
  }
}

/* ===== 提交按钮 ===== */
.submit-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--rounded-md);
  background: var(--st-primary);
  color: var(--st-canvas);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 250ms ease, box-shadow 250ms ease, transform 150ms ease;

  &:hover:not(:disabled) {
    background: var(--st-primary-hover);
    box-shadow: 0 6px 20px rgba(94, 92, 230, 0.28);
    transform: translateY(-1px);
  }
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}
</style>
