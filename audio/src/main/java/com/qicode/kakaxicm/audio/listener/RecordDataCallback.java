package com.qicode.kakaxicm.audio.listener;

/**
 * 录音采集数据监听
 */
public interface RecordDataCallback {
    void onData(byte[] data);
}
