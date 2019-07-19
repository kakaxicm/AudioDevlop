package com.qicode.kakaxicm.audio.listener;

import com.qicode.kakaxicm.audio.AudioRecordResult;

public interface RecordAudioListener {
    void onStart();

    void onDuration(long duration, long maxDuration);

    void onFinish(AudioRecordResult result);

    void onError(String msg);

    void onCancel();
}
