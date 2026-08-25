<template>
  <div class="publish-page">
    <el-breadcrumb separator="/" class="header">
      <el-breadcrumb-item>
        <el-icon class="breadcrumb-icon" style="margin-right: 8px"><Operation /></el-icon>
        管理中心
      </el-breadcrumb-item>
      <el-breadcrumb-item>发布歌曲</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="!hasPermission" class="no-permission">
      <el-empty description="您没有发布歌曲的权限，请使用歌手账号登录" />
    </div>

    <div v-else class="main">
      <el-form
        ref="formDataRef"
        :model="formData"
        :rules="rules"
        label-width="110px"
      >
        <el-form-item label="歌曲名：" prop="musicName">
          <el-input
            v-model="formData.musicName"
            placeholder="请输入歌曲名"
          />
        </el-form-item>

        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="提示"
          description="发布的歌曲将提交给管理员审核，审核通过后才能在歌曲列表中显示"
          style="margin-bottom: 18px"
        />

        <el-form-item label="封面：">
          <el-upload
            class="upload-img"
            :action="actionImgUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImgUploadSuccess"
            :on-error="handleImgUploadError"
            :before-upload="beforeImgUpload"
            accept="image/jpeg,image/png,image/gif"
          >
            <img v-if="formData.imgUrl" :src="formData.imgUrl" class="cover-img" />
            <el-icon v-else class="uploader-icon"><plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="音频文件：">
          <el-upload
            :action="actionMusicUrl"
            :headers="uploadHeaders"
            v-model:file-list="musicFileList"
            :show-file-list="true"
            :on-success="handleMusicFileSuccess"
            :on-error="handleMusicFileError"
            :before-upload="beforeMusicUpload"
            :on-change="changeMusicUpload"
          >
            <el-button type="primary" :icon="upload">上传音频</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="歌词文件：">
          <el-upload
            :action="actionLrcUrl"
            :headers="uploadHeaders"
            v-model:file-list="lyricFileList"
            :show-file-list="true"
            :on-success="handleLrcUploadSuccess"
            :on-error="handleLrcUploadError"
            :before-upload="beforeLrcUpload"
            :on-change="changeLrcUpload"
          >
            <el-button type="primary" :icon="upload">上传歌词</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="标签：">
          <div class="tags-wrap">
            <el-tag
              v-for="(tag, tagIndex) in dynamicTags"
              :key="tagIndex"
              :closable="true"
              :disable-transitions="false"
              @close="handleTagClose(tag)"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="inputVisible"
              class="input-tag"
              v-model="inputValue"
              size="small"
              style="width: 120px"
              ref="saveTagInputRef"
              @keyup.enter="handleInputConfirm"
              @blur="handleInputConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showInput">
              + New Tag
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="onSubmit">提交</el-button>
          <el-button @click="resetForm">重置表单</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Plus, Operation } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'
import { useManageStore } from '@/store/manage'

const userStore = useUserStore()
const manageStore = useManageStore()

// 权限检查：仅歌手角色可发布歌曲
const hasPermission = computed(() => userStore.userInfo?.role === 1)

const formDataRef = ref(null)

// 表单数据
const formData = reactive({
  musicName: '',
  imgUrl: '',
  musicFile: '',
  lyricFile: '',
  timelength: null,
  dynamicTags: []
})

// 标签相关
const dynamicTags = ref([])
const inputVisible = ref(false)
const inputValue = ref('')
const saveTagInputRef = ref(null)

// 文件列表（单文件上传）
const musicFileList = ref([])
const lyricFileList = ref([])

// 上传地址
const actionImgUrl = '/api/upload/image'
const actionMusicUrl = '/api/upload/music'
const actionLrcUrl = '/api/upload/lrc'

const uploadHeaders = computed(() => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 表单校验规则
const rules = {
  musicName: [
    { required: true, message: '歌曲名不能为空', trigger: 'blur' },
    { min: 1, max: 15, message: '歌曲名长度在 1 到 15 个字符', trigger: 'blur' }
  ],
  musicFile: [{ required: true, message: '请上传音频文件', trigger: 'blur' }],
  lyricFile: [{ required: true, message: '请上传歌词文件', trigger: 'blur' }]
}

// 封面上传成功
function handleImgUploadSuccess(res) {
  formData.imgUrl = res?.data?.fileUrl || res?.data?.url || res?.url || ''
}

// 封面上传失败
function handleImgUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

// 封面上传前校验
function beforeImgUpload(file) {
  const isJPG = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) {
    ElMessage.warning('图片格式必须为 JPG/PNG/GIF')
    return false
  }
  if (!isLt2M) {
    ElMessage.warning('图片大小必须小于 2MB')
    return false
  }
  return isJPG && isLt2M
}

// 音频上传成功
function handleMusicFileSuccess(res) {
  formData.musicFile = res?.data?.fileUrl || res?.data?.url || res?.url || ''
  // 如果后端返回了时长，优先使用
  if (res?.data?.timelength) {
    formData.timelength = res.data.timelength
  }
}

// 音频上传失败
function handleMusicFileError() {
  ElMessage.error('音频上传失败，请重试')
}

// 音频上传前校验 + 自动获取时长
function beforeMusicUpload(file) {
  const allowed = ['audio/mpeg', 'audio/wav', 'audio/flac', 'audio/mp4', 'audio/x-m4a']
  const isAudio = allowed.includes(file.type)
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isAudio) {
    ElMessage.warning('音频格式不支持')
    return false
  }
  if (!isLt50M) {
    ElMessage.warning('音频大小必须小于 50MB')
    return false
  }
  // 使用 HTML5 Audio API 获取时长
  getAudioDuration(file)
  return isAudio && isLt50M
}

// 获取音频时长（秒）
function getAudioDuration(file) {
  const url = URL.createObjectURL(file)
  const audio = new Audio()
  audio.preload = 'metadata'
  audio.onloadedmetadata = () => {
    if (audio.duration && !isNaN(audio.duration)) {
      formData.timelength = Math.round(audio.duration)
    }
    URL.revokeObjectURL(url)
  }
  audio.onerror = () => {
    URL.revokeObjectURL(url)
  }
  audio.src = url
}

// 音频上传改变（单文件模式）
function changeMusicUpload(file, fileList) {
  if (fileList.length > 1) {
    fileList.splice(0, 1)
  }
}

// 歌词上传成功
function handleLrcUploadSuccess(res) {
  formData.lyricFile = res?.data?.fileUrl || res?.data?.url || res?.url || ''
}

// 歌词上传失败
function handleLrcUploadError() {
  ElMessage.error('歌词上传失败，请重试')
}

// 歌词上传前校验
function beforeLrcUpload(file) {
  const isLrc = file.name.endsWith('.lrc') || file.type === 'text/plain'
  const isLt1M = file.size / 1024 / 1024 < 1
  if (!isLrc) {
    ElMessage.warning('歌词文件必须为 .lrc 格式或纯文本')
    return false
  }
  if (!isLt1M) {
    ElMessage.warning('歌词文件大小必须小于 1MB')
    return false
  }
  return isLrc && isLt1M
}

// 歌词上传改变
function changeLrcUpload(file, fileList) {
  if (fileList.length > 1) {
    fileList.splice(0, 1)
  }
}

// 标签：关闭
function handleTagClose(tag) {
  const idx = dynamicTags.value.indexOf(tag)
  if (idx > -1) {
    dynamicTags.value.splice(idx, 1)
  }
}

// 标签：显示输入框
function showInput() {
  inputVisible.value = true
  nextTick(() => {
    saveTagInputRef.value?.focus()
  })
}

// 标签：输入确认
function handleInputConfirm() {
  if (inputValue.value.trim()) {
    dynamicTags.value.push(inputValue.value.trim())
  }
  inputVisible.value = false
  inputValue.value = ''
}

// 提交
async function onSubmit() {
  if (!formDataRef.value) return
  if (!hasPermission.value) {
    ElMessage.error('您没有发布歌曲的权限')
    return
  }
  formDataRef.value.validate(async (valid) => {
    if (valid) {
      const res = await manageStore.handleAddMusic({
        fromSinger: userStore.userInfo?.userId,
        musicName: formData.musicName,
        musicUrl: formData.musicFile,
        imageUrl: formData.imgUrl,
        lyricUrl: formData.lyricFile,
        timelength: formData.timelength,
        tagList: dynamicTags.value
      })
      if (res && res.code === 200) {
        ElMessage.success('歌曲已提交，等待管理员审核')
        resetForm()
      }
    } else {
      ElMessage.error('请完善表单信息')
      return false
    }
  })
}

// 重置
function resetForm() {
  formDataRef.value?.resetFields()
  formData.musicName = ''
  formData.imgUrl = ''
  formData.musicFile = ''
  formData.lyricFile = ''
  formData.timelength = null
  dynamicTags.value = []
  musicFileList.value = []
  lyricFileList.value = []
}

// 挂载时检查权限
onMounted(() => {
  if (userStore.userInfo && userStore.userInfo.role !== 1) {
    ElMessage.warning('您没有发布歌曲的权限，请使用歌手账号登录')
  }
})
</script>

<style lang="scss" scoped>
.header {
  height: 40px;
  line-height: 40px;
  padding-left: 15px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.no-permission {
  margin: 80px;
  text-align: center;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
}

.main {
  margin: 15px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
}

.upload-img {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.cover-img {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.uploader-icon {
  font-size: 32px;
  color: #8c939d;
}

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.input-tag {
  width: 120px !important;
}

.button-new-tag {
  margin-left: 0;
}
</style>
