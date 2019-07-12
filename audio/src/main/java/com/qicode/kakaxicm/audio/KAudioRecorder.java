package com.qicode.kakaxicm.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.qicode.kakaxicm.audio.format.FormatInfo;
import com.qicode.kakaxicm.audio.handler.RealPcmToAacHandler;
import com.qicode.kakaxicm.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KAudioRecorder extends AbsAudioRecorder {

    private AudioRecord record;

    //采样线程
    private static final ExecutorService recordExe = Executors.newSingleThreadExecutor();

    private final ExecutorService handleExe = Executors.newSingleThreadExecutor();

    private volatile boolean isRecording;
    private volatile boolean isCancel;
    private long startTime;
    private long stopTime;

    private File file;//aac
    private File pcmFile;

//    private RecordFormat format;//录音配置

//    private Speex speex; 降噪等效果
//    private OnResampleCallback resampleCallback;
//    private ZCallback<byte[]> audioDataCallback;

    public KAudioRecorder(Context context) {
        super(context);
        isRecording = false;
        file = new File(StorageUtils.getCommonCacheDir(context, "audio"), "audio_" + System.currentTimeMillis() + ".aac");
    }

    /**
     * 必须在start之前设置
     */
    public void setPcmFile(File pcmFile) {
        this.pcmFile = pcmFile;
    }


    @Override
    public boolean start() {
//        if (isPermissionEnabled) {
            if (isRecording) {
                return true;
            }
            isRecording = true;
            isCancel = false;
            recordExe.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        doRecord();
                    } catch (Throwable t) {
//                        error(t);
                    } finally {
                    }
                }
            });
            return true;
//        }
//        return false;
    }

    private boolean doRecord() throws IOException {
        //TODO 开始录音采样
        if (record != null) {
            record.release();
        }
        //TODO

//        if (speex != null) {
//            speex.speexDestroy();
//            speex = null;
//        }

        long totalBytes = 0;

        int bufferSizeInBytes = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        record.startRecording();

        startTime = System.currentTimeMillis();

//        final RecordEffects effects = new RecordEffects(record.getAudioSessionId());
//        effects.startEffect();
//        speex = new Speex();
//        speex.speexInit(format.bufferSizeInBytes / 4, format.sampleRateInHz);
//        if (resampleCallback != null) {
//            speex.resampleInit(format.sampleRateInHz, resampleCallback.outRate());
//        }
        //进度校验
//        MainThreadUtils.post(new Runnable() {
//            @Override
//            public void run() {
//                if (listener != null) {
//                    listener.onStart();
//                }
//                checkProgress();
//            }
//        });

        final byte mBuffer[] = new byte[bufferSizeInBytes];

        final FormatInfo fi = new FormatInfo();
        fi.channelCount = 2;
        fi.sampleRate = 44100;
        file.createNewFile();

        FileOutputStream fos = null;//pcm输出
        if (pcmFile != null) {
            fos = new FileOutputStream(pcmFile);
        }
        final RealPcmToAacHandler pcmToAacHandler = new RealPcmToAacHandler(file, fi);
        handleExe.execute(new Runnable() {
            @Override
            public void run() {
                pcmToAacHandler.start();//开启编码器
            }
        });
        //循环读
        while (isRecording && !isCancel && record != null) {
            final int read = record.read(mBuffer, 0, mBuffer.length);
            if (read <= 0) {//todo || speex == null
                continue;
            } else {
                totalBytes += read;
                //TODO 数据编码
//                handleExe.execute(new ProcessDataTask(fos, pcmHandler, mBuffer, arrayPool, resampleCallback, speex,
//                        audioDataCallback));
            }

            long ct = System.currentTimeMillis();
            if (ct - startTime >= maxDuration + 200) {//如果已经超过最长时间了，则退出
                break;
            }

        }
        //善后
//        handleExe.execute(new EndTask(fos, pcmHandler, speex, arrayPool, resampleCallback != null));
        final long duration = totalBytes * 1000 / (44100 * 2 * 2);//单位s
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void release() {

    }

    @Override
    public boolean isRecording() {
        return false;
    }
}
