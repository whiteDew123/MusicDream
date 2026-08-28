import request from './request'

// 对接 music_gateway 路由：
// /api/login/**    → Mod_login LoginController
// /api/register/** → Mod_login RegisterController
// /api/email/**    → Mod_login EmailController

// 用户登录
// POST /api/login
// body: { account: '用户名/邮箱/手机号', password: '明文密码' }
export function loginApi(data) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

// 用户注册
// POST /api/register
// body: { username, password, email, emailCode }
export function registerApi(data) {
  return request({
    url: '/register',
    method: 'post',
    data
  })
}

// 发送邮箱验证码
// GET /api/email/code?email=xxx
export function sendEmailCodeApi(email) {
  return request({
    url: '/email/code',
    method: 'get',
    params: { email }
  })
}

// 获取当前登录用户完整资料
// GET /api/login/current
// 从 X-User-Id 头解析身份
export function getCurrentUserApi() {
  return request({
    url: '/login/current',
    method: 'get'
  })
}

// 更新当前用户资料（email / phone / about / imageUrl）
// PUT /api/login/update
export function updateUserApi(data) {
  return request({
    url: '/login/update',
    method: 'put',
    data
  })
}

// 上传用户头像
// POST /api/upload/image
// body: FormData { file }
export function uploadImageApi(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request({
    url: '/upload/image',
    method: 'post',
    data: fd,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
