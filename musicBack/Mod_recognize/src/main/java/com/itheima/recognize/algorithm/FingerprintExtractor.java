package com.itheima.recognize.algorithm;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 频谱图生成与峰值提取
 */
public class FingerprintExtractor {

    public static final int FFT_SIZE = 1024;
    public static final int HOP_SIZE = 512;
    private static final float ENERGY_THRESHOLD = 1.0f;
    private static final int TOP_PEAKS_PER_FRAME = 5;

    /**
     * 计算声谱图
     */
    public float[][] computeSpectrogram(double[] pcmData) {
        int numFrames = (pcmData.length - FFT_SIZE) / HOP_SIZE + 1;
        if (numFrames < 1) {
            return new float[0][0];
        }
        float[][] spectrogram = new float[numFrames][FFT_SIZE / 2 + 1];

        DoubleFFT_1D fft = new DoubleFFT_1D(FFT_SIZE);
        double[] window = createHanningWindow(FFT_SIZE);
        double[] frame = new double[FFT_SIZE];

        for (int t = 0; t < numFrames; t++) {
            int start = t * HOP_SIZE;
            System.arraycopy(pcmData, start, frame, 0, FFT_SIZE);
            for (int i = 0; i < FFT_SIZE; i++) {
                frame[i] *= window[i];
            }

            fft.realForward(frame);

            // JTransforms realForward 布局：
            // [0]=Re(0), [1]=Re(N/2), 之后 [2]=Re(1), [3]=Im(1) ...
            for (int f = 0; f <= FFT_SIZE / 2; f++) {
                double re;
                double im = 0;
                if (f == 0) {
                    re = frame[0];
                } else if (f == FFT_SIZE / 2) {
                    re = frame[1];
                } else {
                    re = frame[2 * f];
                    im = frame[2 * f + 1];
                }
                spectrogram[t][f] = (float) Math.sqrt(re * re + im * im);
            }
        }
        return spectrogram;
    }

    private double[] createHanningWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
        }
        return window;
    }

    /**
     * 二维局部极大值检测，每帧保留 Top-K 峰值
     */
    public List<Peak> findPeaks(float[][] spectrogram) {
        List<Peak> peaks = new ArrayList<>();
        if (spectrogram.length < 3 || spectrogram[0].length < 3) {
            return peaks;
        }

        for (int t = 1; t < spectrogram.length - 1; t++) {
            for (int f = 1; f < spectrogram[t].length - 1; f++) {
                float current = spectrogram[t][f];
                boolean isFreqPeak = current > spectrogram[t][f - 1] && current > spectrogram[t][f + 1];
                boolean isTimePeak = current > spectrogram[t - 1][f] && current > spectrogram[t + 1][f];
                boolean isLoudEnough = current > ENERGY_THRESHOLD;
                if (isFreqPeak && isTimePeak && isLoudEnough) {
                    peaks.add(new Peak(t, f, current));
                }
            }
        }
        return keepTopKPerFrame(peaks, TOP_PEAKS_PER_FRAME);
    }

    private List<Peak> keepTopKPerFrame(List<Peak> peaks, int k) {
        Map<Integer, List<Peak>> frameMap = new HashMap<>();
        for (Peak p : peaks) {
            frameMap.computeIfAbsent(p.frame, key -> new ArrayList<>()).add(p);
        }
        List<Peak> result = new ArrayList<>();
        for (List<Peak> framePeaks : frameMap.values()) {
            framePeaks.sort((a, b) -> Float.compare(b.amplitude, a.amplitude));
            result.addAll(framePeaks.subList(0, Math.min(k, framePeaks.size())));
        }
        return result;
    }

    public static class Peak {
        public final int frame;
        public final int freq;
        public final float amplitude;

        public Peak(int frame, int freq, float amplitude) {
            this.frame = frame;
            this.freq = freq;
            this.amplitude = amplitude;
        }
    }
}