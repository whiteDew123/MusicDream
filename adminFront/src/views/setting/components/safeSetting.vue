<template>
  <div class="safe-page">
    <!-- 面包屑 -->
    <div>
      <el-breadcrumb separator="/" class="header">
        <el-breadcrumb-item>
          <el-icon class="breadcrumb-icon"><warning-filled /></el-icon>
          设置中心
        </el-breadcrumb-item>
        <el-breadcrumb-item>安全设置</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="main">
      <el-form
        ref="loginFormRef"
        status-icon
        :model="password"
        label-width="100px"
        :rules="rules"
        class="password-form"
      >
        <el-form-item label="原密码：" prop="oldPassword">
          <el-input
            type="password"
            v-model="password.oldPassword"
            autocomplete="off"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码：" prop="newPassword" :rules="rules.passwordRules">
          <el-input
            type="password"
            v-model="password.newPassword"
            autocomplete="off"
            placeholder="请设置新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round @click="editPassword">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import allRules from '@/utils/validator'
import { useSettingStore } from '@/store/setting'

const settingStore = useSettingStore()

const loginFormRef = ref(null)

const password = reactive({
  oldPassword: '',
  newPassword: ''
})

const rules = {
  oldPassword: allRules.oldPasswordRules,
  newPassword: allRules.passwordRules,
  passwordRules: allRules.passwordRules
}

function editPassword() {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      await settingStore.editPwd({
        oldPassword: password.oldPassword,
        newPassword: password.newPassword
      })
      // 成功后清空表单
      password.oldPassword = ''
      password.newPassword = ''
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
  margin: 0 0;
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
}

.password-form {
  width: 100%;
  max-width: 460px;
}
</style>