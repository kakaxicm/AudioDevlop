package com.qicode.kakaxicm.audio.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.OutputStream;

public class CloseUtils {
    private static final String TAG = "CloseUtils";

    /**
     * 关闭输出
     */
    public static void closeOutput(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
    }

    /**
     * 关闭输入
     */
    public static void close(Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
    }
}
