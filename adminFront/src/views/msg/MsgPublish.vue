<template>
  <div class="msg-publish">
    <div class="page-header">
      <div>
        <p class="eyebrow">MESSAGE · PUBLISH</p>
        <h2 class="page-title">发布消息</h2>
        <p class="page-desc">向用户发送系统通知或公告消息。</p>
      </div>
    </div>

    <div class="panel form-panel">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
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
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ submitting ? '发布中…' : '发布消息' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
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

async function handleSubmit() {
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
    } catch {
      ElMessage.error('消息发布失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

function handleReset() {
  formData.title = ''
  formData.msg = ''
  formRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.msg-publish {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

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

.panel {
  background: var(--wf-canvas);
  border: 1px solid var(--wf-hairline);
  border-radius: var(--rounded-md);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.publish-form {
  max-width: 640px;
}
</style>