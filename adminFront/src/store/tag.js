import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  pageTags,
  statsTags,
  addTag,
  updateTag,
  updateTagStatus,
  deleteTag
} from '@/api/tag'

/**
 * 管理中心 - 标签字典 Store
 */
export const useTagStore = defineStore('tag', () => {
  const tags = ref([])
  const statList = ref([])
  const total = ref(0)
  const current = ref(1)
  const size = ref(10)
  const pages = ref(0)
  const loading = ref(false)
  const saving = ref(false)
  const actionId = ref(null)

  /** 分页获取标签字典列表 */
  async function fetchTags({ page = 1, pageSize = 10, keyword = '' } = {}) {
    loading.value = true
    try {
      const res = await pageTags(page, pageSize, keyword)
      const data = res.data || {}
      tags.value = data.records || []
      total.value = data.total || 0
      current.value = data.current || page
      size.value = data.size || pageSize
      pages.value = data.pages || 0
    } finally {
      loading.value = false
    }
  }

  /** 获取使用频次统计 */
  async function fetchStats() {
    loading.value = true
    try {
      const res = await statsTags()
      statList.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  /** 新增标签 */
  async function createTag(code, name) {
    saving.value = true
    try {
      await addTag(code, name)
    } finally {
      saving.value = false
    }
  }

  /** 编辑标签 */
  async function editTag(tagId, code, name) {
    saving.value = true
    try {
      await updateTag(tagId, code, name)
    } finally {
      saving.value = false
    }
  }

  /** 启用/禁用标签 */
  async function setTagStatus(tagId, status) {
    actionId.value = tagId
    try {
      await updateTagStatus(tagId, status)
    } finally {
      actionId.value = null
    }
  }

  /** 删除标签（被引用时后端返回失败提示） */
  async function removeTag(tagId) {
    actionId.value = tagId
    try {
      await deleteTag(tagId)
    } finally {
      actionId.value = null
    }
  }

  return {
    tags,
    statList,
    total,
    current,
    size,
    pages,
    loading,
    saving,
    actionId,
    fetchTags,
    fetchStats,
    createTag,
    editTag,
    setTagStatus,
    removeTag
  }
})