import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import { uploadFileApi, publishMusicApi } from '@/api/music'
import { useUserStore } from '@/store/user'

/**
 * 歌手 - 发布歌曲 Store
 *
 * 管理上传状态、发布表单与提交状态。
 */
export const useSingerUploadStore = defineStore('singerUpload', () => {
  const userStore = useUserStore()

  const uploading = ref(false)
  const publishing = ref(false)

  const form = reactive({
    fromSinger: null,
    musicName: '',
    musicUrl: '',
    imageUrl: '',
    timelength: 0,
    tags: '',
    lyric: ''
  })

  /**
   * 上传文件并回填对应 URL 字段，音频类型自动回填时长
   */
  async function uploadFile(file, type) {
    uploading.value = true
    try {
      const res = await uploadFileApi(file, type)
      const result = res.data
      if (type === 'music') {
        form.musicUrl = result.fileUrl
        if (result.duration != null) {
          form.timelength = result.duration
        }
      }
      if (type === 'image') form.imageUrl = result.fileUrl
      if (type === 'lrc') form.lyric = result.fileUrl
      return result.fileUrl
    } finally {
      uploading.value = false
    }
  }

  /**
   * 提交发布歌曲
   */
  async function publish() {
    publishing.value = true
    try {
      // 歌手发布时强制使用当前登录用户作为歌手ID
      form.fromSinger = userStore.userInfo?.userId
      const res = await publishMusicApi({
        fromSinger: form.fromSinger,
        musicName: form.musicName,
        musicUrl: form.musicUrl,
        imageUrl: form.imageUrl,
        timelength: form.timelength,
        tags: form.tags,
        lyric: form.lyric
      })
      return res.data
    } finally {
      publishing.value = false
    }
  }

  return {
    form,
    uploading,
    publishing,
    uploadFile,
    publish
  }
})