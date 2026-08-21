import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserPageApi, freezeUserApi, unfreezeUserApi } from '@/api/admin'

/**
 * 管理中心 - 用户管理 Store
 *
 * 采用 Pinia 管理用户列表、分页信息和加载状态。
 * 说明：当前 adminFront 使用 Pinia（Vue 3 官方推荐状态库），
 * 它在职责上等同于 Vuex，但 API 更简洁。
 */
export const useAdminUserStore = defineStore('adminUser', () => {
  // ===== State =====
  const users = ref([])
  const total = ref(0)
  const current = ref(1)
  const size = ref(10)
  const pages = ref(0)
  const loading = ref(false)
  const actionId = ref(null)

  // ===== Actions =====
  /**
   * 分页获取用户列表
   */
  async function fetchUsers({ page = 1, pageSize = 10, keyword = '' } = {}) {
    loading.value = true
    try {
      const res = await getUserPageApi(page, pageSize, keyword)
      const data = res.data || {}
      users.value = data.records || []
      total.value = data.total || 0
      current.value = data.current || page
      size.value = data.size || pageSize
      pages.value = data.pages || 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 冻结用户
   */
  async function freezeUser(id) {
    actionId.value = id
    try {
      await freezeUserApi(id)
    } finally {
      actionId.value = null
    }
  }

  /**
   * 解冻用户
   */
  async function unfreezeUser(id) {
    actionId.value = id
    try {
      await unfreezeUserApi(id)
    } finally {
      actionId.value = null
    }
  }

  return {
    users,
    total,
    current,
    size,
    pages,
    loading,
    actionId,
    fetchUsers,
    freezeUser,
    unfreezeUser
  }
})
