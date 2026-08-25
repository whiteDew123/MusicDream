import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addMusic } from '@/api/manage'

export const useManageStore = defineStore('manage', () => {
  const songList = ref([])
  const loading = ref(false)

  async function handleAddMusic(payload) {
    try {
      const res = await addMusic(payload)
      if (res.code === 200) {
        ElMessage.success('歌曲发布成功')
        return res
      } else {
        ElMessage.error(res.message || '发布失败')
      }
    } catch (e) {
      console.error('[manage/addMusic]', e)
    }
  }

  return {
    songList,
    loading,
    handleAddMusic
  }
})
