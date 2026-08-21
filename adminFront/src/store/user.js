import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/auth'
import { getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '@/utils/auth'

// 用户状态管理
// - role: 0-管理员, 1-歌手, 2-普通用户
// - 后台管理端允许 role=0 和 role=1 进入
export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfo())

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

  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }

  function isLogin() {
    return !!token.value
  }

  function isAdmin() {
    return userInfo.value?.role === 0
  }

  function isSinger() {
    return userInfo.value?.role === 1
  }

  function hasRole(...roles) {
    return roles.includes(userInfo.value?.role)
  }

  return {
    token,
    userInfo,
    login,
    logout,
    isLogin,
    isAdmin,
    isSinger,
    hasRole
  }
})