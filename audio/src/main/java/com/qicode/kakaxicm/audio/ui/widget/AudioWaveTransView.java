package com.qicode.kakaxicm.audio.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.qicode.kakaxicm.audio.utils.AudioDataUtils;
import com.qicode.kakaxicm.audio.utils.UnitUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 录音和音频播放的波形view
 */
public class AudioWaveTransView extends View {

    private float MIN;//最小宽度

    /**
     * 采样率
     */
    private int sampleRate = 44100;

    /**
     * 声道数
     */
    private int channels = 2;

    /**
     * 直线数据
     */
    private float[] lines;

    private Paint paint;
    private Paint splitPaint;

    /**
     * 全部的数据
     */
    private final List<Pos> posList = new ArrayList<>();

    private int number;//一个view展示多少个柱子
    private float width;//一条直线占的宽度
    private int color = Color.BLUE;

    public AudioWaveTransView(Context context) {
        this(context, null);
    }

    public AudioWaveTransView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioWaveTransView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initOnce();
        drawAudio(canvas);
    }

    private void drawAudio(Canvas canvas) {
        if (!isReady()) {
            return;
        }

        canvas.drawLines(lines, paint);
    }

    private void initOnce() {
        MIN = UnitUtils.dp2px(getContext(), 1);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (!isReady()) {
            width = UnitUtils.dp2px(getContext(), 2);
            //画线的颜色
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(UnitUtils.dp2px(getContext(), 1));
            setColor(color);

            //画分割线的颜色
            splitPaint = new Paint();
            splitPaint.setAntiAlias(true);
            splitPaint.setColor(Color.RED);
            paint.setStrokeWidth(UnitUtils.dp2px(getContext(), 1));

            number = (int) (w / width - 1);
            lines = new float[number * 4];
        }
    }

    public boolean isReady() {
        return paint != null && splitPaint != null;
    }

    private float max;

    /**
     * data-lines
     */
    private void invalidateData() {
        if (!isReady()) {
            return;
        }

        //高度最大值
        int h = getHeight();
        if (h <= 0) {
            return;
        }

        float cy = h * 0.5f;//基准线位置

        //最大数值处理
        int size = posList.size();
        Log.e("trans_audio", "线条个数:" + size);
        float m = 0;//当前帧的最大值,
        if (size <= 0) {
            m = 400;
        } else {
            for (int i = size - 1; i > 0 && i > size - 1 - number; i--) {
                float t = posList.get(i).value;
                if (t > m) {
                    m = t;
                }
            }
        }

        float factor = 0.1f;
        max = m * factor + max * (1 - factor);
        if (max < 400) {
            max = 400;
        }

        for (int i = 0; i < number; i++) {
            int index = size - 1 - i;
            float d;
            if (index > 0) {
                final Pos pos = posList.get(index);
                d = cy * pos.value / max;
                d = Math.max(MIN, Math.min(cy, d));
            } else {
                d = MIN;
            }
            lines[4 * i] = width * (number - i);
            if (i == 0) {
                Log.e("trans_audio", "最后的线条数值x=" + lines[0] + ", y =" + d);
            }
            lines[4 * i + 1] = cy - d;
            lines[4 * i + 2] = lines[4 * i];
            lines[4 * i + 3] = cy + d;
        }

        postInvalidate();
    }

    public void insertData(byte[] data) {
        final List<Pos> list = AudioDataUtils.insert(data, sampleRate, channels);
        if (list == null) {
            return;
        }

        posList.addAll(list);
        invalidateData();
    }

    public void setColor(int color) {
        this.color = color;
        if (paint != null) {
            /*int c = color & 0x55ffffff;
            paint.setShader(
                    new LinearGradient(0, 0, getWidth(), getHeight(),
                            new int[]{c, color, c},
                            null,
                            LinearGradient.TileMode.CLAMP));*/
            paint.setColor(color);
        }
    }

    //线条
    public static class Pos {
        public float value;
        public boolean split;
    }

}
