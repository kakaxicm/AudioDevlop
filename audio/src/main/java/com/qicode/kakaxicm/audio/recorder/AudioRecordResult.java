package com.qicode.kakaxicm.audio.recorder;

public class AudioRecordResult {
    /**
     * 保存的文件路径
     */
    public String filePath;
    /**
     * 保存的pcm文件路径，如果需要
     */
    public String pcmPath;
    /**
     * 单位ms，毫秒
     */
    public long duration;

    @Override
    public String toString() {
        return "AudioRecordResult{" +
                "filePath='" + filePath + '\'' +
                ", pcmPath='" + pcmPath + '\'' +
                ", duration=" + duration +
                '}';
    }
}
