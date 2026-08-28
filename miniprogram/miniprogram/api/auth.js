/**
 * 账号相关 API（对齐 playerFront/src/api/auth.js）
 * 网关路由：/api/login|register|email → Mod_login（StripPrefix=1 去掉 /api，baseURL 已含 /api）
 */
const request = require('../utils/request')

// 用户登录（邮箱/用户名/手机号）
// POST /login  body: { account, password }
function loginApi(data) {
  return request({ url: '/login', method: 'POST', data })
}

// 微信一键登录（后端待新增）
// POST /login/wx  body: { code }
function wxLoginApi(data) {
  return request({ url: '/login/wx', method: 'POST', data })
}

// 用户注册
// POST /register  body: { username, password, email, emailCode }
function registerApi(data) {
  return request({ url: '/register', method: 'POST', data })
}

// 发送邮箱验证码
// GET /email/code?email=xxx
function sendEmailCodeApi(email) {
  return request({ url: '/email/code', method: 'GET', data: { email } })
}

module.exports = {
  loginApi,
  wxLoginApi,
  registerApi,
  sendEmailCodeApi
}
