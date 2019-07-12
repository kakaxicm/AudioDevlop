package com.qicode.kakaxicm.audio.format;

import android.media.MediaFormat;

/**
 * aac格式信息,这些信息都会由源文件解析出来，然后在将pcm文件压缩为aac文件的时候使用,假如源文件没有，则会有默认值
 */
public class FormatInfo {
    /**
     * 默认比特率，越大越清晰，但是文件也越大
     */
    private static final int DEFAULT_BIT_RATE = 96000;

    /**
     * 暂时只支持aac，其他以后再说
     */
    public final String mime = "audio/mp4a-latm";

    /**
     * 比特率,越高越清晰，但文件也越大
     */
    public int bitRate;

    /**
     * 频道数量,这个没用上，最后统一用2
     */
    public int channelCount;

    /**
     * 采样率,这个没用上，最后统一用44100
     */
    public int sampleRate;

    /**
     * 最大输入尺寸
     */
    public int maxInputSize;

    /**
     * 每一帧处理的长度，这个可以自己定，比如4k，或者8k
     */
    public int frameSize;

    /**
     * 采样频率索引
     */
    private int freqIndex;

    private final int samplingFreq[] = {
            96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000
    };

    public FormatInfo() {
        frameSize = 1024 * 4;
        bitRate = DEFAULT_BIT_RATE;
        sampleRate = 22050;
        channelCount = 2;
        maxInputSize = 1024 * 1024;
        freqIndex = -1;
    }

    public static FormatInfo from(MediaFormat format) {
        FormatInfo formatInfo = new FormatInfo();
        formatInfo.bitRate = get(format, MediaFormat.KEY_BIT_RATE, DEFAULT_BIT_RATE);
        formatInfo.sampleRate = get(format, MediaFormat.KEY_SAMPLE_RATE, 22050);
        formatInfo.channelCount = get(format, MediaFormat.KEY_CHANNEL_COUNT, 2);
        formatInfo.maxInputSize = get(format, MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024);

        //比特率一定要设置
        if (formatInfo.bitRate <= DEFAULT_BIT_RATE) {
            formatInfo.bitRate = DEFAULT_BIT_RATE;
        }
        /**
         * 为啥会这样，谁能告诉我吗，为啥明明解析出来的是1和22050，可是合成会慢
         *
         */
        // TODO: 2018/4/25 这里有疑问，有时间慢慢来研究,这里用来应对网易云的编码。自从了自己的编码录音器后，这个问题不再有了
        if (formatInfo.channelCount == 1 && formatInfo.sampleRate == 22050) {
            formatInfo.channelCount = 2;
            formatInfo.sampleRate = 44100;
        }

        return formatInfo;
    }

    private static int get(MediaFormat m, String key, int v) {
        try {
            return m.getInteger(key);
        } catch (Throwable t) {
            return v;
        }
    }

    /**
     * 获取sample rate索引
     */
    public int freqIndex() {
        if (freqIndex == -1) {
            for (int i = 0; i < samplingFreq.length; i++) {
                if (samplingFreq[i] == sampleRate) {
                    freqIndex = i;
                    break;
                }
            }
        }
        if (freqIndex == -1) {
            return 11;
        }
        return freqIndex;
    }
}
