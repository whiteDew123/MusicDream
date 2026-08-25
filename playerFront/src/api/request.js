import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'
import router from '@/router'

// axios 实例
// - baseURL: '/api'，由 Vite proxy 转发到网关 9000
// - 请求拦截器：自动携带 Authorization: Bearer <token>
// - 响应拦截器：统一处理 Result 结构与 401 跳转
const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：注入 token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 Result<{code, message, data}>
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一响应：code === 200 表示成功
    if (res.code !== 200) {
      if (!response.config.silent) {
        ElMessage.error(res.message || '请求失败')
      }
      // 401: token 失效或未登录
      if (res.code === 401) {
        clearAuth()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    // HTTP 层错误（网络、超时、网关返回 401/500 等）
    const status = error.response?.status
    if (!error.config?.silent) {
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        clearAuth()
        router.push('/login')
      } else if (status !== 404) {
        // 404 静默处理（接口不存在时不弹窗）
        ElMessage.error(error.response?.data?.message || error.message || '网络异常')
      }
    }
    return Promise.reject(error)
  }
)

export default service
