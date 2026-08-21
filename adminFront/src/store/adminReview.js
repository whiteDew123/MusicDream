import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getPendingMusicPageApi,
  approveMusicApi,
  rejectMusicApi
} from '@/api/music'

/**
 * 管理中心 - 待审核歌曲 Store
 *
 * 管理员人工兜底审核：查看自动审核未通过的歌曲，可选择通过或驳回。
 */
export const useAdminReviewStore = defineStore('adminReview', () => {
  const songs = ref([])
  const total = ref(0)
  const current = ref(1)
  const size = ref(10)
  const pages = ref(0)
  const loading = ref(false)
  const actionId = ref(null)

  /**
   * 分页获取待审核歌曲
   */
  async function fetchPendingSongs({ page = 1, pageSize = 10 } = {}) {
    loading.value = true
    try {
      const res = await getPendingMusicPageApi(page, pageSize)
      const data = res.data || {}
      songs.value = data.records || []
      total.value = data.total || 0
      current.value = data.current || page
      size.value = data.size || pageSize
      pages.value = data.pages || 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 审核通过
   */
  async function approve(id) {
    actionId.value = id
    try {
      await approveMusicApi(id)
    } finally {
      actionId.value = null
    }
  }

  /**
   * 审核驳回
   */
  async function reject(id, remark) {
    actionId.value = id
    try {
      await rejectMusicApi(id, remark)
    } finally {
      actionId.value = null
    }
  }

  return {
    songs,
    total,
    current,
    size,
    pages,
    loading,
    actionId,
    fetchPendingSongs,
    approve,
    reject
  }
})