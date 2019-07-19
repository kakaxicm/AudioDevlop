package com.qicode.kakaxicm.audio;

import android.content.Context;

import com.qicode.kakaxicm.audio.listener.RecordAudioListener;

public abstract class AbsAudioRecorder implements IRecorder {

    protected RecordAudioListener listener;
    //TODO 录音过程和结果监听
    protected int checkTime;//没隔多久监听一次
    protected static final int AUDIO_MIN_DURATION = 5 * 1000;
    //默认最长时间5分钟
    public static final int AUDIO_MAX_DURATION = 5 * 60 * 1000;

    protected long minDuration = AUDIO_MIN_DURATION;
    protected long maxDuration = AUDIO_MAX_DURATION;

    protected Context context;

    protected AbsAudioRecorder(Context context, RecordAudioListener listener) {
        this.context = context;
        checkTime = 500;
        this.listener = listener;
    }

    /**
     * 最小录制时长，毫秒
     */
    public void setMinDuration(long minDuration) {
        this.minDuration = minDuration;
    }

    /**
     * 设置最长录制时间，毫秒
     */
    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public void setCheckTime(int checkTime) {
        this.checkTime = Math.max(16, checkTime);
    }
}
