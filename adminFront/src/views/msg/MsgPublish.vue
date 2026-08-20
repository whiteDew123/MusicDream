<template>
  <div class="msg-publish-container">
    <div class="page-header">
      <h1 class="page-title">发布消息</h1>
      <p class="page-desc">向用户发送系统通知或公告消息</p>
    </div>

    <el-card class="publish-card" shadow="never">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        label-position="left"
        class="publish-form"
      >
        <el-form-item label="消息标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入消息标题"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="消息内容" prop="msg">
          <el-input
            v-model="formData.msg"
            type="textarea"
            :rows="8"
            placeholder="请输入消息内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="submitting"
            @click="handleSubmit"
            class="submit-btn"
          >
            {{ submitting ? '发布中...' : '发布消息' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { publishMsg } from '@/api/msg'

const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)

const formData = reactive({
  title: '',
  msg: ''
})

const formRules = {
  title: [
    { required: true, message: '请输入消息标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  msg: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await publishMsg({
        title: formData.title,
        userId: userStore.userInfo.userId,
        msg: formData.msg
      })

      ElMessage.success('消息发布成功')
      handleReset()
    } catch (error) {
      ElMessage.error('消息发布失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

const handleReset = () => {
  formData.title = ''
  formData.msg = ''
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.msg-publish-container {
  padding: 32px;
  background-color: #f5f6f8;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #080808;
  margin: 0 0 8px 0;
  letter-spacing: -0.24px;
}

.page-desc {
  font-size: 14px;
  color: #5a5a5a;
  margin: 0;
}

.publish-card {
  background-color: #ffffff;
  border: 1px solid #d8d8d8;
  border-radius: 8px;

  :deep(.el-card__body) {
    padding: 32px;
  }
}

.publish-form {
  max-width: 600px;

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #080808;
  }

  :deep(.el-input__wrapper) {
    border-radius: 4px;
    border: 1px solid #d8d8d8;
    box-shadow: none;
    padding: 8px 12px;

    &:hover {
      border-color: #4353ff;
    }

    &.is-focus {
      border-color: #4353ff;
      box-shadow: 0 0 0 2px rgba(67, 83, 255, 0.1);
    }
  }

  :deep(.el-textarea__inner) {
    border-radius: 4px;
    border: 1px solid #d8d8d8;
    box-shadow: none;
    padding: 8px 12px;

    &:hover {
      border-color: #4353ff;
    }

    &:focus {
      border-color: #4353ff;
      box-shadow: 0 0 0 2px rgba(67, 83, 255, 0.1);
    }
  }
}

.submit-btn {
  background-color: #080808;
  border-color: #080808;
  border-radius: 4px;
  padding: 12px 24px;
  font-weight: 500;

  &:hover {
    background-color: #222222;
    border-color: #222222;
  }

  &:active {
    background-color: #363636;
    border-color: #363636;
  }
}

:deep(.el-button:not(.submit-btn)) {
  border-radius: 4px;
  padding: 12px 24px;
  font-weight: 500;
}
</style>