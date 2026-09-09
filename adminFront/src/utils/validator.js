/**
 * 通用表单校验规则
 * 用于 Element Plus 的 el-form 中 :rules="rules"
 */

// 校验：用户名（3-20 位，字母/数字/中文/下划线）
const usernameRules = [
  { required: true, message: '请输入用户名', trigger: 'blur' },
  {
    validator: (_rule, value, callback) => {
      if (!value) return callback()
      if (value.length < 3 || value.length > 20) {
        callback(new Error('用户名长度需在 3-20 之间'))
      } else if (!/^[\w\u4e00-\u9fa5]+$/.test(value)) {
        callback(new Error('用户名仅支持字母、数字、下划线和中文'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

// 校验：邮箱格式
const emailRules = [
  { required: true, message: '请输入邮箱', trigger: 'blur' },
  { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
]

// 校验：手机号（1 开头 11 位）
const phoneRules = [
  {
    validator: (_rule, value, callback) => {
      if (!value) return callback()
      if (!/^1\d{10}$/.test(value)) {
        callback(new Error('请输入正确的 11 位手机号'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

// 校验：简介长度（最多 200 字符）
const aboutRules = [
  {
    validator: (_rule, value, callback) => {
      if (value && value.length > 200) {
        callback(new Error('简介长度不能超过 200 字'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

// 校验：原密码（必填）
const oldPasswordRules = [
  { required: true, message: '请输入原密码', trigger: 'blur' }
]

// 校验：新密码（6-32 位）
const passwordRules = [
  { required: true, message: '请设置新密码', trigger: 'blur' },
  {
    validator: (_rule, value, callback) => {
      if (!value) return callback()
      if (value.length < 6 || value.length > 32) {
        callback(new Error('密码长度需在 6-32 之间'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

export default {
  usernameRules,
  emailRules,
  phoneRules,
  aboutRules,
  oldPasswordRules,
  passwordRules
}