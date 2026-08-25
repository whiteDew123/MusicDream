package com.itheima.recognize.algorithm;

import org.jtransforms.fft.DoubleFFT_1D;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频解码与预处理
 *
 * <p>MP3 文件使用 JLayer 直接解码，WAV/FLAC 等通过 javax.sound API。
 * 统一输出 16bit、单声道、目标采样率的 PCM。</p>
 */
public final class AudioProcessor {

    private AudioProcessor() {
    }

    /** FFmpeg 可执行文件路径，可通过环境变量 FFMPEG_PATH 指定 */
    private static final String FFMPEG_PATH = resolveFfmpegPath();

    private static String resolveFfmpegPath() {
        String env = System.getenv("FFMPEG_PATH");
        if (env != null && !env.isBlank()) {
            return env;
        }
        File[] candidates = {
                new File("C:/ffmpeg-9.0.1/bin/ffmpeg.exe"),
                new File("C:/ffmpeg-2026-08-23-git-1019f8f036-essentials_build/bin/ffmpeg.exe")
        };
        for (File file : candidates) {
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return "ffmpeg";
    }

    /**
     * 解码音频文件为归一化的单声道 PCM 数据
     */
    public static double[] decodeToPCM(File audioFile, int targetSampleRate) throws Exception {
        String format = detectFormat(audioFile);

        if ("mp3".equals(format)) {
            return decodeMp3ToPCM(audioFile, targetSampleRate);
        }

        // WAV / FLAC 直接走 JavaSound
        if ("wav".equals(format) || "flac".equals(format)) {
            return decodeStandardToPCM(audioFile, targetSampleRate);
        }

        // M4A / WebM / 其他格式：先尝试 JavaSound，失败再用 FFmpeg 兜底
        try {
            return decodeStandardToPCM(audioFile, targetSampleRate);
        } catch (Exception e) {
            return decodeWithFfmpeg(audioFile, targetSampleRate);
        }
    }

    /**
     * 通过文件头判断真实音频格式
     */
    private static String detectFormat(File file) throws IOException {
        byte[] header = new byte[12];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(header);
            if (read < 4) {
                return "unknown";
            }
        }

        // MP3：ID3 标签头，或直接以 0xFFE0 开头的 MPEG 帧
        if ((header[0] == 'I' && header[1] == 'D' && header[2] == '3')
                || ((header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0)) {
            return "mp3";
        }
        // WAV：RIFF....WAVE
        if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F') {
            return "wav";
        }
        // FLAC
        if (header[0] == 'f' && header[1] == 'L' && header[2] == 'a' && header[3] == 'C') {
            return "flac";
        }
        // MP4 / M4A：....ftyp
        if (header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
            return "mp4";
        }
        // WebM
        if ((header[0] & 0xFF) == 0x1A && (header[1] & 0xFF) == 0x45
                && (header[2] & 0xFF) == 0xDF && (header[3] & 0xFF) == 0xA3) {
            return "webm";
        }
        return "unknown";
    }

    /**
     * 使用 FFmpeg 将任意音频解码为 16bit 单声道 PCM
     */
    private static double[] decodeWithFfmpeg(File audioFile, int targetSampleRate) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_PATH, "-v", "error", "-i", audioFile.getAbsolutePath(),
                "-f", "s16le", "-ar", String.valueOf(targetSampleRate), "-ac", "1", "-"
        );
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = pb.start();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        try (java.io.InputStream is = process.getInputStream()) {
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
        int exit = process.waitFor();
        if (exit != 0) {
            throw new RuntimeException("FFmpeg 解码失败，退出码=" + exit);
        }

        byte[] pcmBytes = out.toByteArray();
        short[] shorts = new short[pcmBytes.length / 2];
        ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

        double[] mono = new double[shorts.length];
        for (int i = 0; i < shorts.length; i++) {
            mono[i] = shorts[i] / 32768.0;
        }
        normalize(mono);
        return mono;
    }

    /**
     * JLayer 解码 MP3
     */
    private static double[] decodeMp3ToPCM(File mp3File, int targetSampleRate) throws Exception {
        List<Short> allSamples = new ArrayList<>();
        int sampleRate = 44100;
        int channels = 2;

        try (FileInputStream fis = new FileInputStream(mp3File)) {
            Bitstream bitstream = new Bitstream(fis);
            Decoder decoder = new Decoder();

            Header header;
            while ((header = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                sampleRate = header.frequency();
                channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                short[] frameSamples = output.getBuffer();
                int frameLen = output.getBufferLength();
                for (int i = 0; i < frameLen; i++) {
                    allSamples.add(frameSamples[i]);
                }
                bitstream.closeFrame();
            }
        }

        int totalFrames = allSamples.size() / channels;
        double[] mono = new double[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            if (channels == 2) {
                mono[i] = (allSamples.get(i * 2) + allSamples.get(i * 2 + 1)) / 2.0;
            } else {
                mono[i] = allSamples.get(i);
            }
        }

        double[] filtered = lowPassFilter(mono, sampleRate, targetSampleRate / 2.0);
        double[] resampled = resample(filtered, sampleRate, targetSampleRate);
        normalize(resampled);
        return resampled;
    }

    /**
     * javax.sound 解码 WAV/FLAC 等
     */
    private static double[] decodeStandardToPCM(File audioFile, int targetSampleRate) throws Exception {
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
        int kernelSize = (int) (origRate / cutoffFreq / 2);
        if (kernelSize < 2) {
            return input;
        }
        double[] output = new double[input.length];
        double invKernel = 1.0 / kernelSize;
        for (int i = 0; i < input.length; i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - kernelSize / 2);
                 j < Math.min(input.length, i + kernelSize / 2);
                 j++) {
                sum += input[j];
                count++;
            }
            output[i] = sum / count;
        }
        return output;
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