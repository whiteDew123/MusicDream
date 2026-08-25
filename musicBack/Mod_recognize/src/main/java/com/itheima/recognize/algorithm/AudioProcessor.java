package com.itheima.recognize.algorithm;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 音频解码与预处理
 *
 * <p>将 MP3/WAV/FLAC 等格式统一解码为 16bit、单声道、目标采样率的 PCM。
 * 降采样前先做低通滤波，避免混叠干扰。</p>
 */
public final class AudioProcessor {

    private AudioProcessor() {
    }

    /**
     * 解码音频文件为归一化的单声道 PCM 数据
     */
    public static double[] decodeToPCM(File audioFile, int targetSampleRate) throws Exception {
        AudioInputStream rawStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat rawFormat = rawStream.getFormat();

        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                rawFormat.getSampleRate(),
                16,
                rawFormat.getChannels(),
                rawFormat.getChannels() * 2,
                rawFormat.getSampleRate(),
                false
        );
        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, rawStream);

        byte[] pcmBytes = readAllBytes(pcmStream);
        int channels = pcmFormat.getChannels();
        int sampleRate = (int) pcmFormat.getSampleRate();
        short[] shorts = bytesToShorts(pcmBytes, channels);
        double[] mono = toMono(shorts, channels);

        // 先低通滤波再降采样，防止混叠
        double[] filtered = lowPassFilter(mono, sampleRate, targetSampleRate / 2.0);
        double[] resampled = resample(filtered, sampleRate, targetSampleRate);
        normalize(resampled);
        return resampled;
    }

    private static double[] lowPassFilter(double[] input, int origRate, double cutoffFreq) {
        DoubleFFT_1D fft = new DoubleFFT_1D(input.length);
        double[] data = new double[input.length * 2];
        System.arraycopy(input, 0, data, 0, input.length);

        fft.realForwardFull(data);

        int cutoffBin = (int) (cutoffFreq * input.length / origRate);
        for (int i = cutoffBin * 2; i < data.length; i++) {
            data[i] = 0.0;
        }

        fft.complexInverse(data, true);

        double[] result = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = data[i * 2];
        }
        return result;
    }

    private static byte[] readAllBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = stream.read(buf)) != -1) {
            buffer.write(buf, 0, n);
        }
        return buffer.toByteArray();
    }

    private static short[] bytesToShorts(byte[] bytes, int channels) {
        int totalFrames = bytes.length / (channels * 2);
        short[] shorts = new short[totalFrames * channels];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    private static double[] toMono(short[] shorts, int channels) {
        int totalFrames = shorts.length / channels;
        double[] mono = new double[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            if (channels == 2) {
                mono[i] = (shorts[i * 2] + shorts[i * 2 + 1]) / 2.0;
            } else {
                mono[i] = shorts[i];
            }
        }
        return mono;
    }

    private static double[] resample(double[] input, int origRate, int targetRate) {
        if (origRate == targetRate) {
            return input;
        }
        double ratio = (double) targetRate / origRate;
        int newLen = (int) (input.length * ratio);
        double[] output = new double[newLen];
        for (int i = 0; i < newLen; i++) {
            double srcPos = i / ratio;
            int idx = (int) srcPos;
            double frac = srcPos - idx;
            if (idx + 1 < input.length) {
                output[i] = input[idx] * (1 - frac) + input[idx + 1] * frac;
            } else {
                output[i] = input[idx];
            }
        }
        return output;
    }

    private static void normalize(double[] data) {
        double max = 0.0;
        for (double v : data) {
            max = Math.max(max, Math.abs(v));
        }
        if (max > 0) {
            for (int i = 0; i < data.length; i++) {
                data[i] /= max;
            }
        }
    }
}
