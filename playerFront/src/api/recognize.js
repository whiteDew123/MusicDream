import request from './request'

/**
 * 上传录音文件进行听歌识曲
 * POST /api/recognize
 *
 * @param {Blob} file WAV 录音文件
 */
export function recognizeApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/recognize',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 15000
  })
}