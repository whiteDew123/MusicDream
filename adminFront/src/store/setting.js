import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, updateAvatar, editPassword } from '@/api/setting'
import { setUserInfo } from '@/utils/auth'
import { useUserStore } from './user'

// 设置中心状态管理
export const useSettingStore = defineStore('setting', () => {
  const userInfo = ref(null)

  // 重新获取用户信息并同步到 userStore & localStorage
  async function gainUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      // 同步到 userStore 的 userInfo 中（合并字段）
      const userStore = useUserStore()
      const merged = {
        ...(userStore.userInfo || {}),
        ...res.data
      }
      userStore.userInfo = merged
      setUserInfo(merged)
      return merged
    } catch (e) {
      console.error('[setting/gainUserInfo]', e)
    }
  }

  // 更新用户信息（个人设置提交）
  async function setUserInfoAction(payload) {
    try {
      const res = await updateUserInfo(payload)
      if (res.code === 200) {
        ElMessage.success(res.message || '修改成功')
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (e) {
      console.error('[setting/setUserInfo]', e)
    }
  }

  // 设置头像（上传成功后写入后端）
  async function setHeadImage(imageUrl) {
    try {
      const res = await updateAvatar({ imageUrl })
      if (res.code === 200) {
        ElMessage.success('头像更新成功')
      }
    } catch (e) {
      console.error('[setting/setHeadImage]', e)
    }
  }

  // 修改密码
  async function editPwd({ oldPassword, newPassword }) {
    try {
      const res = await editPassword({ oldPassword, newPassword })
      if (res.code === 200) {
        ElMessage.success(res.message || '密码修改成功，请重新登录')
      } else if (res.code === 50) {
        ElMessage.error('原密码错误')
      } else {
        ElMessage.error(res.message || '密码修改失败')
      }
    } catch (e) {
      console.error('[setting/editPwd]', e)
    }
  }

  return {
    userInfo,
    gainUserInfo,
    setUserInfoAction,
    setHeadImage,
    editPwd
  }
})