package com.qicode.kakaxicm.audio.task;

import com.qicode.kakaxicm.audio.handler.RealPcmToAacHandler;
import com.lurenjia.android.base.media.audio.record.Speex;
import com.qicode.kakaxicm.audio.utils.CloseUtils;

import java.io.FileOutputStream;

public class EndRecordTask implements Runnable {

    private FileOutputStream fos;
    private RealPcmToAacHandler pcmToAacHandler;
    private Speex speex;

    public EndRecordTask(FileOutputStream fos, RealPcmToAacHandler pcmToAacHandler, Speex speex) {
        this.fos = fos;
        this.pcmToAacHandler = pcmToAacHandler;
        this.speex = speex;
    }

    @Override
    public void run() {
        pcmToAacHandler.end();
        if (speex != null) {
            speex.speexDestroy();
        }

        if (fos != null) {
            CloseUtils.closeOutput(fos);
        }
    }
}
