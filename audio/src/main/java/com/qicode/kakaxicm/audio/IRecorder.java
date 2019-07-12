package com.qicode.kakaxicm.audio;

import java.io.IOException;

public interface IRecorder {
    boolean start() throws IOException;

    void stop();

    void cancel();

    void release();

    boolean isRecording();
}
