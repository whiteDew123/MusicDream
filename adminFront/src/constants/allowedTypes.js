/**
 * 文件上传白名单常量
 * 与后端 Mod_upload.application.yml 保持同步
 */

// 音乐文件：扩展名白名单
export const MUSIC_EXTENSIONS = ['mp3', 'wav', 'flac', 'm4a', 'aac', 'ogg', 'wma', 'mp4']

// 音乐文件：el-upload accept 属性（扩展名 + audio/* 兜底）
export const MUSIC_ACCEPT =
  MUSIC_EXTENSIONS.map((ext) => `.${ext}`).join(',') + ',audio/*'

// 音乐文件：大小限制 50MB
export const MUSIC_MAX_SIZE_MB = 50

// 封面图片：扩展名白名单
export const IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'webp']

// 封面图片：el-upload accept 属性
export const IMAGE_ACCEPT =
  IMAGE_EXTENSIONS.map((ext) => `.${ext}`).join(',') + ',image/*'

// 封面图片：大小限制 5MB
export const IMAGE_MAX_SIZE_MB = 5

// 歌词文件：扩展名白名单
export const LRC_EXTENSIONS = ['lrc', 'txt']

// 歌词文件：el-upload accept 属性
export const LRC_ACCEPT = LRC_EXTENSIONS.map((ext) => `.${ext}`).join(',')

// 歌词文件：大小限制 1MB
export const LRC_MAX_SIZE_MB = 1

/**
 * 通用工具：判断扩展名是否在白名单中
 */
export function isExtensionAllowed(filename, allowedExtensions) {
  if (!filename) return false
  const ext = filename.split('.').pop().toLowerCase()
  return allowedExtensions.includes(ext)
}

/**
 * 通用工具：判断文件大小是否超限（MB）
 */
export function isSizeAllowed(file, maxSizeMb) {
  return file.size / 1024 / 1024 < maxSizeMb
}
