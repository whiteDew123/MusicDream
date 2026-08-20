import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/auth'
import { getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '@/utils/auth'

// 用户状态管理
// - 登录后存储 token 与用户信息（token/userId/username/role/imageUrl）
// - role: 0-管理员, 1-歌手, 2-普通用户；应用端允许所有角色进入
export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfo())

  // 登录：调用后端接口，成功后写入本地存储
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const data = res.data
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      role: data.role,
      imageUrl: data.imageUrl
    }
    setToken(data.token)
    setUserInfo(userInfo.value)
    return data
  }

  // 退出：清空本地登录态
  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }

  // 是否已登录
  function isLogin() {
    return !!token.value
  }

  return {
    token,
    userInfo,
    login,
    logout,
    isLogin
  }
})
