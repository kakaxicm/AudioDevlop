package com.qicode.kakaxicm.audio.recorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.lurenjia.android.base.media.audio.record.Speex;
import com.qicode.kakaxicm.audio.format.FormatInfo;
import com.qicode.kakaxicm.audio.handler.RealPcmToAacHandler;
import com.qicode.kakaxicm.audio.listener.RecordAudioListener;
import com.qicode.kakaxicm.audio.listener.RecordDataCallback;
import com.qicode.kakaxicm.audio.task.EndRecordTask;
import com.qicode.kakaxicm.audio.task.ProcessRecordDataTask;
import com.qicode.kakaxicm.audio.utils.MainThreadUtils;
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

    private Speex speex; //降噪等效果

    //    private OnResampleCallback resampleCallback;

    private RecordDataCallback audioDataCallback;

    public KAudioRecorder(Context context, RecordAudioListener listener) {
        super(context, listener);
        isRecording = false;
        file = new File(StorageUtils.getCommonCacheDir(context, "audio"), "audio_" + System.currentTimeMillis() + ".aac");
    }

    public void setAudioDataCallback(RecordDataCallback audioDataCallback) {
        this.audioDataCallback = audioDataCallback;
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
                    int i = 0;
//                        error(t);
                    Log.e("AbsAudioRecorder", t.getLocalizedMessage());
                } finally {
                }
            }
        });
        return true;
//        }
//        return false;
    }

    private void doRecord() throws IOException {
        //开始录音采样
        if (record != null) {
            record.release();
            record = null;
        }

        if (listener != null) {
            MainThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStart();
                }
            });
        }
        if (speex != null) {
            speex.speexDestroy();
            speex = null;
        }

        long totalBytes = 0;
        int sampleRateInHz = 44100;
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
        record.startRecording();

        startTime = System.currentTimeMillis();

//        final RecordEffects effects = new RecordEffects(record.getAudioSessionId());
//        effects.startEffect();
        speex = new Speex();
        speex.speexInit(bufferSizeInBytes / 4, sampleRateInHz);
//        if (resampleCallback != null) {
//            speex.resampleInit(format.sampleRateInHz, resampleCallback.outRate());
//        }
        //进度校验
        //TODO
        if (listener != null) {
            MainThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    checkProgress();
                }
            });
        }

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
            if (read <= 0 || speex == null) {
                continue;
            } else {
                totalBytes += read;
                //数据编码,写pcm文件
                handleExe.execute(new ProcessRecordDataTask(fos, pcmToAacHandler, speex, mBuffer, audioDataCallback));
//                handleExe.execute(new ProcessDataTask(fos, pcmHandler, mBuffer, arrayPool, resampleCallback, speex,
//                        audioDataCallback));
            }

            long ct = System.currentTimeMillis();
            if (ct - startTime >= maxDuration + 200) {//如果已经超过最长时间了，则退出
                break;
            }
        }
        //善后
        handleExe.execute(new EndRecordTask(fos, pcmToAacHandler, speex));
        speex = null;
        if (isCancel) {
            isRecording = false;
            //取消录音的回调
            if (listener != null) {
                MainThreadUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onCancel();
                    }
                });
            }
            release();
            return;
        }

        final long duration = totalBytes * 1000 / (sampleRateInHz * 2 * 2);//单位ms
        //如果时间太短
        if (duration <= minDuration) {
            error(new RuntimeException("录音时长不能小于" + (minDuration / 1000) + "秒"));
            release();
            return;
        }

        final AudioRecordResult r = new AudioRecordResult();
        r.duration = duration;
        r.filePath = file.getAbsolutePath();
        r.pcmPath = pcmFile == null ? null : pcmFile.getAbsolutePath();
        isRecording = false;
        MainThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                //TODO 录音回调
                if (listener != null) {
                    listener.onFinish(r);
                }
            }
        });

        release();
    }

    private void checkProgress() {
        if (!isRecording || listener == null || record == null) {
            return;
        }
        long duration = System.currentTimeMillis() - startTime;
        if (duration > maxDuration + 200) {
            stop();
        } else {
            listener.onDuration(duration, maxDuration);
            MainThreadUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkProgress();
                }
            }, checkTime);
        }
    }

    private void error(final Throwable e) {
        if (!MainThreadUtils.isMainThread()) {
            MainThreadUtils.post(new Runnable() {
                @Override
                public void run() {
                    error(e);
                }
            });
            return;
        }
        if (file.exists()) {
            file.delete();
        }
        isRecording = false;
        //TODO 错误的回调
        if (listener != null) {
            listener.onError(e.getLocalizedMessage());
        }
        release();
    }


    @Override
    public void stop() {
        isRecording = false;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public void release() {
        isRecording = false;
        if (record != null) {
            try {
                record.release();
                record = null;
            } catch (Throwable t) {
                Log.e("TAG", t.getLocalizedMessage());
            }
        }
        if (speex != null) {
            speex.speexDestroy();
//            if (resampleCallback != null) {
//                speex.resampleDestroy();
//            }
            speex = null;
        }
    }

    @Override
    public boolean isRecording() {
        return false;
    }
}
