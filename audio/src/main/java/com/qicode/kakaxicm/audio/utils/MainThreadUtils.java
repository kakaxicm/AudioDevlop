package com.qicode.kakaxicm.audio.utils;

import android.os.Handler;
import android.os.Looper;

public class MainThreadUtils {

    private static Handler handler;

    public static Handler getHandler() {
        if (handler == null) {
            synchronized (MainThreadUtils.class) {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

    /**
     * 判断当前线程是否为主线程
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * 在主线程执行 {@link Runnable}
     */
    public static void post(Runnable run) {
        if (run != null) {
            getHandler().post(run);
        }
    }

    /**
     * 等待一会后在主线程执行 {@link Runnable}
     */
    public static void postDelayed(Runnable run, long delayMillis) {
        getHandler().postDelayed(run, delayMillis);
    }

    /**
     * 移除正在等待的 task
     */
    public static void removePendingTask(Runnable run) {
        getHandler().removeCallbacks(run);
    }

}
