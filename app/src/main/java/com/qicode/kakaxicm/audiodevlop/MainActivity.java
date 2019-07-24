package com.qicode.kakaxicm.audiodevlop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.qicode.kakaxicm.audio.model.PcmFile;
import com.qicode.kakaxicm.audio.player.MutilPcmPlayer;
import com.qicode.kakaxicm.audio.recorder.AbsAudioRecorder;
import com.qicode.kakaxicm.audio.recorder.AudioRecordResult;
import com.qicode.kakaxicm.audio.recorder.KAudioRecorder;
import com.qicode.kakaxicm.audio.listener.RecordAudioListener;
import com.qicode.kakaxicm.audio.listener.RecordDataCallback;
import com.qicode.kakaxicm.audio.ui.widget.AudioWaveTransView;
import com.qicode.kakaxicm.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private AbsAudioRecorder recorder;
    private AudioWaveTransView audioWaveTransView;

    private final List<PcmFile> pcmFiles = new ArrayList<>();
    private MutilPcmPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioWaveTransView = findViewById(R.id.wave_view);
        requestPermission();
        recorder = new KAudioRecorder(this, new RecordAudioListener() {
            @Override
            public void onStart() {
                Log.e("AbsAudioRecorder", "onStart");
            }

            @Override
            public void onDuration(long duration, long maxDuration) {
                Log.e("AbsAudioRecorder", "onDuration:" + duration + "," + maxDuration);
            }

            @Override
            public void onFinish(AudioRecordResult result) {
                File file = new File(result.filePath);
                PcmFile pcmFile = new PcmFile();
                pcmFile.aacPath = result.filePath;
                pcmFile.pcmPath = result.pcmPath;
                pcmFile.duration = result.duration;
                Log.e("PcmPlayer", "Record onFinish:" + pcmFile.toString());
                pcmFiles.add(pcmFile);
                audioWaveTransView.stop();

            }

            @Override
            public void onError(String msg) {
                Log.e("AbsAudioRecorder", "onError:" + msg);
            }

            @Override
            public void onCancel() {
                Log.e("AbsAudioRecorder", "onCancel:");
            }
        });
        ((KAudioRecorder) recorder).setAudioDataCallback(new RecordDataCallback() {
            @Override
            public void onData(byte[] data) {
//                Log.e("AbsAudioRecorder", "data.len:" + data.length);
                if (recorder != null && recorder.isRecording()) {
                    audioWaveTransView.insertData(data);
                }
                Log.e("audio_split", "onData");
            }
        });
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
    }

    private void initPlayer() {
        player = new MutilPcmPlayer(this, pcmFiles, new MutilPcmPlayer.PcmPlayListener() {
            @Override
            public void onStart() {
                Log.e("PcmPlayer", "onStart");
                audioWaveTransView.reset();
            }

            @Override
            public void onNext() {
                Log.e("PcmPlayer", "onNext");
                audioWaveTransView.stop();
            }

            @Override
            public void onResume() {
                Log.e("PcmPlayer", "onResume");
            }

            @Override
            public void onPause() {
                Log.e("PcmPlayer", "onPause");
            }

            @Override
            public void onFinish() {
                Log.e("PcmPlayer", "onFinish");
            }

            @Override
            public void onData(byte[] data) {
                if (player != null && player.isPlaying()) {
                    audioWaveTransView.insertData(data);
                    Log.e("PcmPlayer", "data.len:" + data.length);
                }
            }

            @Override
            public void onDuration(long duration) {
                Log.e("PcmPlayer", "onDuration:" + duration);
            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    private void playPcms() {
        if (player == null) {
            initPlayer();
            player.start();
        } else {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
            }
        }

//        final MediaPlayer player = new MediaPlayer();
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        try {
//            player.setDataSource(path);
//            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    player.start();
//                }
//            });
//            player.prepareAsync();
//        } catch (IOException e) {
//            Log.e("AbsAudioRecorder", "播放异常:" + e);
//        }
    }


    private final String[] pers = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private void requestPermission() {
        boolean b = checkPermissions();
        if (!b) {
            //权限判断，没有就去请求所需权限，传参 需要申请的权限(可以多个)， requestCode请求码用于结果回调里判断
            ActivityCompat.requestPermissions(this, pers, 1314);
        } else {
            //有权限
        }
    }

    private boolean checkPermissions() {
        boolean b = true;
        for (int i = 0; i < pers.length; i++) {
            if (ContextCompat.checkSelfPermission(this, pers[i]) != PackageManager.PERMISSION_GRANTED) {
                b = false;
                break;
            }
        }
        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (1314 == requestCode) {
            for (int x : grantResults) {
                if (x == PackageManager.PERMISSION_DENIED) {
                    //权限拒绝了
                    return;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.start) {
            ((KAudioRecorder) recorder).setPcmFile(new File(StorageUtils.getCommonCacheDir(this, "audio"), "audio_" + System.currentTimeMillis() + ".pcm"));
            recorder.start();
        } else if (id == R.id.cancel) {
            recorder.cancel();
        } else if (id == R.id.stop) {
            recorder.stop();
        } else if (id == R.id.play) {
            playPcms();
        }
    }
}
