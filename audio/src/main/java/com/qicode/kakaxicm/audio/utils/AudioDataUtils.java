package com.qicode.kakaxicm.audio.utils;

import com.qicode.kakaxicm.audio.ui.widget.AudioWaveTransView;

import java.util.ArrayList;
import java.util.List;

public class AudioDataUtils {
    public static List<AudioWaveTransView.Pos> insert(byte[] pcmData, int sampleRate, int channels) {
        if (pcmData == null) {
            return null;
        }
        List<AudioWaveTransView.Pos> posList = new ArrayList<>();
        //40ms有多少个数据
        int numOf40Ms = sampleRate * channels * 2 * 40 / 1000;
        //本次采集多少个点
        int N = pcmData.length / numOf40Ms;
        N = Math.max(1, N);
        for (int i = 0; i < N; i++) {
            AudioWaveTransView.Pos pos = new AudioWaveTransView.Pos();
            int count = 0;
            int energy;
            //先取40ms数据的均值
            for (int j = i * N; j < N * numOf40Ms && j < pcmData.length - 1; j += 2) {
                energy = Math.abs(mixByte((pcmData[j]), (pcmData[j + 1])));
                count++;
                pos.value += energy;
            }
            if (count <= 0) {
                count = 1;
            }
            //均值+采样点均指加权
            pos.value = pos.value / count * 0.7f
                    + 0.3f * (Math.abs(mixByte((pcmData[i * N + fitIndex(numOf40Ms / 4, channels)]),
                    (pcmData[i * N + fitIndex(numOf40Ms / 4, channels) + 1])))
                    + Math.abs(mixByte((pcmData[i * N + fitIndex(numOf40Ms / 2, channels)]), (pcmData[i * N + fitIndex
                    (numOf40Ms / 2, channels) + 1]))) + Math
                    .abs(mixByte((pcmData[i * N + fitIndex(numOf40Ms * 3 / 4, channels)]), (pcmData[i * N + fitIndex
                            (numOf40Ms * 3 / 4, channels) + 1])))) / 3;
            posList.add(pos);
            //Log.e("TAG", "energy = " + pos.value);
        }
        return posList;
    }

    /**
     * 取声道0的起始索引,下面的2是因为音频采样默认一个声道16bit
     *
     * @param index
     * @param channels
     * @return
     */
    private static int fitIndex(int index, int channels) {
        if (index % (2 * channels) == 0) {
            return index;
        } else {
            return index + 2 * channels - index % (2 * channels);
        }
    }

    public static short mixByte(byte lb, byte bb) {
        return (short) ((lb & 0xff) | (bb & 0xff) << 8);
    }
}
