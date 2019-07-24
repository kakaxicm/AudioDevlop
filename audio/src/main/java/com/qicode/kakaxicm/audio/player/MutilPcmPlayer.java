package com.qicode.kakaxicm.audio.player;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.SystemClock;
import android.widget.Toast;

import com.qicode.kakaxicm.audio.model.PcmFile;
import com.qicode.kakaxicm.audio.utils.MainThreadUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MutilPcmPlayer {
    private static final ExecutorService es = Executors.newSingleThreadExecutor();
    private final ExecutorService waveEs = Executors.newSingleThreadExecutor();
    /**
     * pcm文件列表
     */
    private List<PcmFile> pcmFiles;

    private PcmPlayListener listener;

    private volatile boolean isStop;
    private volatile boolean isPause;

    private long readBytes;
    private Context context;

    public MutilPcmPlayer(Context context, List<PcmFile> pcmFiles, PcmPlayListener listener) {
        this.pcmFiles = pcmFiles;
        this.listener = listener;
        isStop = true;
        isPause = false;
        this.context = context;
    }

    /**
     * 开始或者是继续播放
     */
    public void start() {
        if (!isStop) {
            if (isPause) {
                isPause = false;
                if (listener != null) {
                    listener.onResume();
                }
            }
            return;
        }
        if (listener != null) {
            listener.onStart();
        }

        if (pcmFiles == null || pcmFiles.size() == 0) {
            if (listener != null) {
                listener.onError(new Throwable("音频文件为空"));
            }
            return;
        }
        es.execute(new Runnable() {
            @Override
            public void run() {
                isStop = false;
                try {
                    play();
                } catch (Throwable t) {
                    if (!isStop) {
                        Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show();
                    }
                    MainThreadUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFinish();
                            }
                        }
                    });
                }
                isStop = true;
            }
        });
    }

    /**
     * 暂停
     */
    public void pause() {
        isPause = true;
        if (listener != null) {
            listener.onPause();
        }
    }

    public void stop() {
        isStop = true;
    }

    /**
     * 播放pcm列表
     */
    private void play() {
        //配置AudioTrack
        final int audioBuffSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        final AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, audioBuffSize, AudioTrack.MODE_STREAM);
        player.play();
        readBytes = 0;
        for (PcmFile p : pcmFiles) {
            //播放单个pcm文件
            if (isStop) {
                break;
            }

            checkPause();
            playPcm(player, p, audioBuffSize);
            //波形采集完再走next,所以放到waveEs中
            waveEs.execute(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onNext();
                    }
                }
            });
        }

        player.stop();
        player.release();

        MainThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFinish();
                }
            }
        });
    }

    /**
     * 播放单个pcm
     *
     * @param player
     * @param p
     * @param audioBuffSize 读取pcm的缓冲区大小
     */
    private void playPcm(AudioTrack player, PcmFile p, int audioBuffSize) {
        FileInputStream fi;
        try {
            int showSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            fi = new FileInputStream(p.pcmPath);
            byte[] buffer = new byte[audioBuffSize];//读取pcm缓冲
            byte[] write = new byte[showSize];//采样波形
            while (true) {
                if (isStop) {
                    break;
                }

                final int read = fi.read(buffer);
                if (read <= 0) {
                    break;
                }

                readBytes += read;
                //播放
                player.write(buffer, 0, read);
                //播放进度监听
                if (listener != null) {
                    MainThreadUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDuration(1000 * readBytes / (44100 * 2 * 2));
                        }
                    });
                }
                //采集波形
                waveEs.execute(new WaveTask(write, buffer));
                //暂停一直等待
                checkPause();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果暂停则一直等待
     */
    private void checkPause() {
        while (true) {
            if (isStop) {
                return;
            }
            if (!isPause) {
                return;
            }
            Thread.yield();//暂停则一直等待
        }
    }

    public boolean isPlaying() {
        return !isStop && !isPause;
    }

    public interface PcmPlayListener {
        void onStart();

        void onNext();

        void onResume();

        void onPause();

        void onFinish();

        void onData(byte[] data);

        void onDuration(long duration);

        void onError(Throwable t);
    }

    private class WaveTask implements Runnable {
        final byte[] tmp;
        final byte[] data;
        final boolean needToSplit;

        public WaveTask(byte[] write, byte[] buffer) {
            tmp = write;
            data = buffer;
            needToSplit = data.length > tmp.length && 1f * (data.length - tmp.length * 2) / data.length < 0.1;
        }

        @Override
        public void run() {
            if (listener == null) {
                return;
            }
            if (needToSplit) {
                System.arraycopy(data, 0, tmp, 0, tmp.length);
                listener.onData(tmp);
                SystemClock.sleep(35);
                System.arraycopy(data, data.length - tmp.length, tmp, 0, tmp.length);
                listener.onData(tmp);
            } else {
                listener.onData(data);
            }
        }
    }
}
