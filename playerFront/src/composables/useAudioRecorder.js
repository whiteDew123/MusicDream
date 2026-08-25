import { ref } from 'vue'

/**
 * 录音 Composable
 *
 * 支持两种音频源：
 * - mic：麦克风（getUserMedia）
 * - system：系统音频 / 浏览器标签页（getDisplayMedia）
 *
 * 使用 AudioWorklet 采集 PCM，停止时编码为 WAV Blob。
 */
export function useAudioRecorder() {
  const isRecording = ref(false)
  const duration = ref(0)
  const error = ref('')
  const audioBlob = ref(null)
  const captureMode = ref('system')

  let mediaStream = null
  let audioContext = null
  let workletNode = null
  let sourceNode = null
  let chunks = []
  let timer = null
  let startTime = 0
  let sampleRate = 44100

  /**
   * 开始录音（麦克风模式）
   */
  async function start(maxDuration = 15) {
    return startCapture(maxDuration, 'mic')
  }

  /**
   * 开始捕获系统音频（浏览器标签页模式）
   */
  async function startSystem(maxDuration = 15) {
    return startCapture(maxDuration, 'system')
  }

  /**
   * 通用录音启动
   */
  async function startCapture(maxDuration, mode) {
    if (isRecording.value) return

    try {
      error.value = ''
      audioBlob.value = null
      chunks = []
      captureMode.value = mode

      if (mode === 'system') {
        if (!navigator.mediaDevices?.getDisplayMedia) {
          throw new Error('当前浏览器不支持系统音频捕获')
        }
        mediaStream = await navigator.mediaDevices.getDisplayMedia({
          video: true,
          audio: true
        })
        // 立即停止视频轨道，只保留音频
        mediaStream.getVideoTracks().forEach((t) => t.stop())
      } else {
        if (!navigator.mediaDevices?.getUserMedia) {
          throw new Error('当前浏览器不支持录音')
        }
        mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
      }

      audioContext = new AudioContext()
      sampleRate = audioContext.sampleRate || 44100

      await audioContext.audioWorklet.addModule(
        new URL('../audio/recorder-processor.js', import.meta.url)
      )

      workletNode = new AudioWorkletNode(audioContext, 'recorder-processor')
      workletNode.port.onmessage = (event) => {
        chunks.push(event.data)
      }

      sourceNode = audioContext.createMediaStreamSource(mediaStream)
      sourceNode.connect(workletNode)
      // 系统音频模式下不连接 destination，避免回音
      if (mode !== 'system') {
        workletNode.connect(audioContext.destination)
      }

      isRecording.value = true
      startTime = Date.now()
      duration.value = 0

      timer = setInterval(() => {
        duration.value = Math.min(
          Math.round((Date.now() - startTime) / 1000),
          maxDuration
        )
        if (duration.value >= maxDuration) {
          stop()
        }
      }, 1000)
    } catch (e) {
      error.value = e.message || '录音启动失败'
      cleanup()
    }
  }

  /**
   * 停止录音，返回 WAV Blob
   */
  async function stop() {
    if (!isRecording.value) return null

    clearInterval(timer)
    timer = null

    const pcm = mergeChunks(chunks, sampleRate)
    audioBlob.value = encodeWAV(pcm, sampleRate)
    isRecording.value = false

    cleanup()
    return audioBlob.value
  }

  /**
   * 重置状态
   */
  function reset() {
    cleanup()
    isRecording.value = false
    duration.value = 0
    error.value = ''
    audioBlob.value = null
    chunks = []
  }

  function cleanup() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    if (mediaStream) {
      mediaStream.getTracks().forEach((track) => track.stop())
      mediaStream = null
    }
    if (sourceNode) {
      sourceNode.disconnect()
      sourceNode = null
    }
    if (workletNode) {
      workletNode.disconnect()
      workletNode = null
    }
    if (audioContext) {
      audioContext.close().catch(() => {})
      audioContext = null
    }
  }

  /**
   * 合并 Float32Array 分片
   */
  function mergeChunks(chunks, rate) {
    let length = 0
    for (const chunk of chunks) {
      length += chunk.length
    }
    const merged = new Float32Array(length)
    let offset = 0
    for (const chunk of chunks) {
      merged.set(chunk, offset)
      offset += chunk.length
    }
    return merged
  }

  /**
   * Float32 PCM 编码为 WAV Blob
   */
  function encodeWAV(samples, rate) {
    const buffer = new ArrayBuffer(44 + samples.length * 2)
    const view = new DataView(buffer)

    writeString(view, 0, 'RIFF')
    view.setUint32(4, 36 + samples.length * 2, true)
    writeString(view, 8, 'WAVE')
    writeString(view, 12, 'fmt ')
    view.setUint32(16, 16, true)
    view.setUint16(20, 1, true)
    view.setUint16(22, 1, true)
    view.setUint32(24, rate, true)
    view.setUint32(28, rate * 2, true)
    view.setUint16(32, 2, true)
    view.setUint16(34, 16, true)
    writeString(view, 36, 'data')
    view.setUint32(40, samples.length * 2, true)

    let offset = 44
    for (let i = 0; i < samples.length; i++) {
      const s = Math.max(-1, Math.min(1, samples[i]))
      view.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7fff, true)
      offset += 2
    }

    return new Blob([buffer], { type: 'audio/wav' })
  }

  function writeString(view, offset, str) {
    for (let i = 0; i < str.length; i++) {
      view.setUint8(offset + i, str.charCodeAt(i))
    }
  }

  return {
    isRecording,
    duration,
    error,
    audioBlob,
    captureMode,
    start,
    startSystem,
    stop,
    reset
  }
}