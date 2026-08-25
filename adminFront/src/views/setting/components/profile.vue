<template>
  <div class="profile-page">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/" class="header">
      <el-breadcrumb-item>
        <el-icon class="breadcrumb-icon"><setting /></el-icon>
        设置中心
      </el-breadcrumb-item>
      <el-breadcrumb-item>个人设置</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="main">
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :action="actionImgUrl"
          :headers="uploadHeaders"
          name="file"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeImgUpload"
        >
          <img v-if="userMessage.avatar" :src="userMessage.avatar" class="avatar" />
          <el-icon v-else class="avatar-placeholder"><user-filled /></el-icon>
        </el-upload>
        <p class="upload-tip">点击头像上传（支持 JPG/PNG/GIF，≤2MB）</p>
      </div>

      <el-form
        ref="userInfoRef"
        :model="userMessage"
        :rules="rules"
        label-width="80px"
        status-icon
        class="form-card"
      >
        <el-form-item label="昵称" prop="username">
          <el-input v-model="userMessage.username" placeholder="请输入昵称" autocomplete="off" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userMessage.email" placeholder="请输入邮箱" autocomplete="off" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userMessage.phone" placeholder="请输入手机号" autocomplete="off" />
        </el-form-item>

        <el-form-item label="简介" prop="about">
          <el-input
            v-model="userMessage.about"
            type="textarea"
            :rows="3"
            :maxlength="200"
            show-word-limit
            placeholder="请输入个人简介"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" round @click="saveEdit(userInfoRef)">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, inject } from 'vue'
import { Setting, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import { get } from 'lodash-es'
import rules from '@/utils/validator'
import { useSettingStore } from '@/store/setting'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'

const settingStore = useSettingStore()
const userStore = useUserStore()

// 父组件刷新方法（用于更新头像是刷新整个页面）
const reload = inject('reload', () => {})

// 表单数据
const userMessage = reactive({
  username: '',
  email: '',
  phone: '',
  about: '',
  avatar: ''
})

// 上传图片目标地址
const actionImgUrl = '/api/upload/image'

const uploadHeaders = computed(() => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const userInfoRef = ref(null)

// localStorage 中的用户信息（computed）
const userInfo = computed(() => userStore.userInfo || {})

// 挂载：从本地 userStore 加载信息，再从后端刷新
onMounted(async () => {
  const info = userInfo.value
  userMessage.username = get(info, 'username', '')
  userMessage.email = get(info, 'email', '')
  userMessage.phone = get(info, 'phone', '')
  userMessage.about = get(info, 'about', '')
  userMessage.avatar = get(info, 'imageUrl', '')

  // 尝试从后端刷新完整数据
  try {
    const fresh = await settingStore.gainUserInfo()
    if (fresh) {
      userMessage.username = get(fresh, 'username', userMessage.username)
      userMessage.email = get(fresh, 'email', userMessage.email)
      userMessage.phone = get(fresh, 'phone', userMessage.phone)
      userMessage.about = get(fresh, 'about', userMessage.about)
      userMessage.avatar = get(fresh, 'imageUrl', userMessage.avatar)
    }
  } catch (_e) {
    // 后端未实现时仅保留本地信息
  }
})

// 上传成功回调
function handleUploadSuccess(res) {
  const url = res?.data?.fileUrl || res?.data?.url || res?.url
  if (url) {
    settingStore.setHeadImage(url)
    // 更新当前表单头像
    userMessage.avatar = url
    setTimeout(() => {
      settingStore.gainUserInfo()
    }, 300)
    setTimeout(() => {
      reload()
    }, 500)
  } else {
    ElMessage.error(res?.message || '上传失败，未获取到图片地址')
  }
}

// 上传失败
function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

// 上传前校验（图片类型 + 大小）
function beforeImgUpload(file) {
  const isJPG =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/jpg' ||
    file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElNotification.warning({
      title: '警告',
      message: '请上传格式为 image/png、image/gif、image/jpg、image/jpeg 的图片'
    })
  }
  if (!isLt2M) {
    ElNotification.warning({
      title: '警告',
      message: '图片大小必须小于 2M'
    })
  }
  return isJPG && isLt2M
}

// 保存修改
function saveEdit(ref) {
  ref.validate(async (valid) => {
    if (valid) {
      await settingStore.setUserInfoAction({
        username: userMessage.username,
        email: userMessage.email,
        phone: userMessage.phone,
        about: userMessage.about
      })
      setTimeout(() => {
        settingStore.gainUserInfo()
      }, 500)
      setTimeout(() => {
        reload()
      }, 800)
    } else {
      console.log('error save!!!')
      return false
    }
  })
}
</script>

<style lang="scss" scoped>
.header {
  height: 40px;
  line-height: 40px;
  padding-left: 15px;
  background-color: #fff;
  border-radius: 6px;
  margin-bottom: 15px;
  .breadcrumb-icon {
    margin-right: 8px;
  }
}

.main {
  padding: 30px 40px;
  background-color: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;

  .avatar-uploader :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    width: 120px;
    height: 120px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: #fafafa;
    transition: border-color 0.2s;
    &:hover {
      border-color: #409eff;
    }
  }

  .avatar {
    width: 120px;
    height: 120px;
    object-fit: cover;
    display: block;
  }

  .avatar-placeholder {
    font-size: 56px;
    color: #c0c4cc;
  }

  .upload-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #909399;
  }
}

.form-card {
  width: 100%;
  max-width: 460px;
}
</style>