package com.qicode.kakaxicm.audio.recorder;

public interface IRecorder {
    boolean start();

    void stop();

    void cancel();

    void release();

    boolean isRecording();
}
