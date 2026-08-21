import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getAdminMusicPageApi,
  getSingerMusicPageApi,
  freezeMusicApi,
  unfreezeMusicApi,
  updateMusicApi,
  withdrawMusicApi,
  deleteMusicHardApi,
  adminDeleteMusicApi,
  relaunchMusicApi
} from '@/api/music'
import { useUserStore } from '@/store/user'

/**
 * 管理中心 - 歌曲管理 Store
 *
 * 管理员和歌手共享冻结/解冻/删除功能
 */
export const useAdminMusicStore = defineStore('adminMusic', () => {
  const userStore = useUserStore()

  const songs = ref([])
  const total = ref(0)
  const current = ref(1)
  const size = ref(10)
  const pages = ref(0)
  const loading = ref(false)
  const actionId = ref(null)
  const saving = ref(false)

  const isAdmin = () => userStore.isAdmin()
  const isSinger = () => userStore.isSinger()

  /**
   * 分页获取歌曲列表（根据角色自动选择接口）
   */
  async function fetchSongs({ page = 1, pageSize = 10, keyword = '' } = {}) {
    loading.value = true
    try {
      let data
      if (isAdmin()) {
        const res = await getAdminMusicPageApi(page, pageSize, keyword)
        data = res.data || {}
      } else {
        const res = await getSingerMusicPageApi(page, pageSize)
        data = res.data || {}
      }
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
   * 冻结歌曲（管理员和歌手均可）
   */
  async function freezeMusic(id) {
    actionId.value = id
    try {
      if (isAdmin()) {
        await freezeMusicApi(id)
      } else {
        await withdrawMusicApi(id)
      }
    } finally {
      actionId.value = null
    }
  }

  /**
   * 解冻歌曲（管理员和歌手均可）
   */
  async function unfreezeMusic(id) {
    actionId.value = id
    try {
      if (isAdmin()) {
        await unfreezeMusicApi(id)
      } else {
        await relaunchMusicApi(id)
      }
    } finally {
      actionId.value = null
    }
  }

  /**
   * 修改歌曲
   */
  async function updateMusic(musicId, data) {
    saving.value = true
    try {
      const res = await updateMusicApi(musicId, data)
      return res.data
    } finally {
      saving.value = false
    }
  }

  /**
   * 硬删除歌曲（物理删除）
   */
  async function deleteMusicHard(musicId) {
    actionId.value = musicId
    try {
      if (isAdmin()) {
        await adminDeleteMusicApi(musicId)
      } else {
        await deleteMusicHardApi(musicId)
      }
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
    saving,
    isAdmin,
    isSinger,
    fetchSongs,
    freezeMusic,
    unfreezeMusic,
    updateMusic,
    deleteMusicHard
  }
})