<template>
  <div class="setting-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <p class="eyebrow">ACCOUNT · SETTING</p>
        <h2 class="page-title">个人设置</h2>
        <p class="page-desc">管理个人信息、修改密码与更换头像。</p>
      </div>
    </div>

    <div class="setting-grid">
      <!-- 左侧：个人信息 -->
      <div class="panel info-panel">
        <h3 class="panel-title">个人信息</h3>
        <el-form
          ref="infoFormRef"
          :model="infoForm"
          :rules="infoRules"
          label-width="80px"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="infoForm.username" placeholder="请输入用户名" maxlength="30" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="infoForm.phone" placeholder="请输入手机号" maxlength="20" />
          </el-form-item>
          <el-form-item label="个人简介" prop="about">
            <el-input
              v-model="infoForm.about"
              type="textarea"
              :rows="3"
              placeholder="介绍一下自己..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="infoLoading" @click="handleSaveInfo">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：头像 + 密码 -->
      <div class="right-col">
        <!-- 头像 -->
        <div class="panel avatar-panel">
          <h3 class="panel-title">头像</h3>
          <div class="avatar-section">
            <el-avatar
              :size="80"
              :src="avatarUrl || userStore.userInfo?.imageUrl"
              class="avatar-img"
            >
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <div class="avatar-actions">
              <el-upload
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
                :disabled="avatarLoading"
              >
                <el-button :loading="avatarLoading" size="small">
                  {{ avatarUrl ? '更换头像' : '上传头像' }}
                </el-button>
              </el-upload>
              <p class="upload-hint">支持 JPG / PNG，大小不超过 10MB</p>
            </div>
          </div>
        </div>

        <!-- 密码 -->
        <div class="panel password-panel">
          <h3 class="panel-title">修改密码</h3>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="80px"
            label-position="top"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleSavePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { updateUserInfo, updatePassword, updateAvatar } from '@/api/setting'
import { uploadFileApi } from '@/api/music'

const userStore = useUserStore()

const infoFormRef = ref(null)
const passwordFormRef = ref(null)
const infoLoading = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)
const avatarUrl = ref('')

const infoForm = reactive({
  username: '',
  email: '',
  phone: '',
  about: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 30, message: '用户名长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  loadUserInfo()
})

function loadUserInfo() {
  const info = userStore.userInfo
  if (info) {
    infoForm.username = info.username || ''
    infoForm.email = ''
    infoForm.phone = ''
    infoForm.about = ''
  }
}

async function handleSaveInfo() {
  try {
    await infoFormRef.value.validate()
  } catch {
    return
  }
  infoLoading.value = true
  try {
    const userId = userStore.userInfo?.userId
    await updateUserInfo(userId, {
      username: infoForm.username || undefined,
      email: infoForm.email || undefined,
      phone: infoForm.phone || undefined,
      about: infoForm.about || undefined
    })
    ElMessage.success('个人信息修改成功')
  } catch (e) {
    // 失败由拦截器处理
  } finally {
    infoLoading.value = false
  }
}

async function handleSavePassword() {
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }
  passwordLoading.value = true
  try {
    const userId = userStore.userInfo?.userId
    await updatePassword(userId, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()
  } catch (e) {
    // 失败由拦截器处理
  } finally {
    passwordLoading.value = false
  }
}

async function handleAvatarUpload(options) {
  avatarLoading.value = true
  try {
    const res = await uploadFileApi(options.file, 'image')
    const imageUrl = res.data?.fileUrl
    if (imageUrl) {
      const userId = userStore.userInfo?.userId
      await updateAvatar(userId, { imageUrl })
      avatarUrl.value = imageUrl
      ElMessage.success('头像修改成功')
    }
  } catch (e) {
    // 失败由拦截器处理
  } finally {
    avatarLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.setting-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* ===== 页面标题（Webflow eyebrow 风格）===== */
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
    color: var(--wf-ink);
    margin-bottom: 6px;
  }
  .page-desc {
    font-size: 14px;
    color: var(--wf-body-mid);
  }
}

/* ===== 两栏布局 ===== */
.setting-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  align-items: start;
}

/* ===== 通用面板 ===== */
.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--wf-ink);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--wf-hairline);
}

/* ===== 右侧列 ===== */
.right-col {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* ===== 头像区 ===== */
.avatar-panel {
  .avatar-section {
    display: flex;
    align-items: center;
    gap: var(--spacing-xl);
  }

  .avatar-img {
    flex-shrink: 0;
    border: 2px solid var(--wf-hairline);
    border-radius: var(--rounded-md);
  }

  .avatar-actions {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .upload-hint {
    font-size: 12px;
    color: var(--wf-mute);
  }
}

/* ===== 表单内边距 ===== */
:deep(.el-form-item) {
  margin-bottom: var(--spacing-lg);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .setting-grid {
    grid-template-columns: 1fr;
  }
  .avatar-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>