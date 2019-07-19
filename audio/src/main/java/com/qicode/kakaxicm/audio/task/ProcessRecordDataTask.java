package com.qicode.kakaxicm.audio.task;

import com.qicode.kakaxicm.audio.handler.RealPcmToAacHandler;
import com.qicode.kakaxicm.audio.listener.RecordDataCallback;
import com.lurenjia.android.base.media.audio.record.Speex;

import java.io.FileOutputStream;
import java.io.IOException;

public class ProcessRecordDataTask implements Runnable {
    private FileOutputStream pcmFos;//pcm文件fos
    private RealPcmToAacHandler pcmToAacHandler;//编码aac
    private Speex speex;
    private byte[] handleData;
    private RecordDataCallback dataCallback;

    public ProcessRecordDataTask(FileOutputStream pcmFos, RealPcmToAacHandler pcmToAacHandler, Speex speex, byte[] data, RecordDataCallback dataCallback) {
        this.pcmFos = pcmFos;
        this.pcmToAacHandler = pcmToAacHandler;
        this.speex = speex;
        this.handleData = new byte[data.length];
        System.arraycopy(data, 0, handleData, 0, data.length);
        this.dataCallback = dataCallback;
    }

    @Override
    public void run() {
        //降噪音,增益等处理
        if (speex != null) {
            speex.speexProcess(handleData, 2, handleData.length / 2);
        }
        //写aac
        pcmToAacHandler.encode(handleData);
        //如果需要写pcm文件
        if (pcmFos != null) {
            try {
                pcmFos.write(handleData, 0, handleData.length);
            } catch (IOException e) {
            }
        }

        if (dataCallback != null) {
            dataCallback.onData(handleData);
        }

    }
}
