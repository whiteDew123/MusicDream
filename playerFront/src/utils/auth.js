// Token 本地存储工具
// 使用 localStorage 持久化 JWT，刷新页面后仍保持登录态
const TOKEN_KEY = 'md_player_token'
const USER_KEY = 'md_player_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setUserInfo(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUserInfo() {
  localStorage.removeItem(USER_KEY)
}

// 清空登录态
export function clearAuth() {
  removeToken()
  removeUserInfo()
}
