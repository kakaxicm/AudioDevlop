package com.qicode.kakaxicm.audio.model;

import android.util.Log;

import java.io.File;

public class PcmFile {
    /**
     * pcm文件路径
     */
    public String pcmPath;
    /**
     * aac文件路径
     */
    public String aacPath;
    /**
     * 时常，其实可以不要，可以根据pcm文件计算出来
     */
    public long duration;

    /**
     * 采样率. 固定双声道，16位深
     */
    public int sampleRate;

    public void delete() {
        try {
            File f = new File(pcmPath);
            if (f.exists()) {
                f.delete();
            }
        } catch (Throwable t) {
            Log.e("TAG", "delete pcm fail : " + t.getLocalizedMessage());
        }
        try {
            File f = new File(aacPath);
            if (f.exists()) {
                f.delete();
            }
        } catch (Throwable t) {
            Log.e("TAG", "delete aac fail : " + t.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return "PcmFile{" +
                "pcmPath='" + pcmPath + '\'' +
                ", aacPath='" + aacPath + '\'' +
                ", duration=" + duration +
                ", sampleRate=" + sampleRate +
                '}';
    }
}
