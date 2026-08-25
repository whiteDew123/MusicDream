// AudioWorklet 录音处理器
// 负责把麦克风 PCM 数据转发到主线程，不做任何处理
class RecorderProcessor extends AudioWorkletProcessor {
  process(inputs) {
    const input = inputs[0]
    if (input && input.length > 0 && input[0] && input[0].length > 0) {
      // 必须拷贝，否则音频线程可能复用缓冲区导致数据被覆盖
      this.port.postMessage(new Float32Array(input[0]))
    }
    return true
  }
}

registerProcessor('recorder-processor', RecorderProcessor)
