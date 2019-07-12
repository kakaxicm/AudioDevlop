package com.qicode.kakaxicm.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class StorageUtils {
    /**
     * 获取通用缓存目录，优先外部缓存，其次内部缓存
     *
     * @return 返回已经创建好的文件目录
     */
    public static String getCommonCacheDir(Context context, String dir) {
        String path = getExternalAppPath(context);
        if (TextUtils.isEmpty(path)) {
            path = getInternalCachePath(context);
        }
        File file = new File(path, dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 得到系统标准的外村路径
     * 例如：/storage/emulated/0/Android/data/cn.zmqd.android.core.app
     */
    public static String getExternalAppPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + context.getPackageName());
            return file.getPath();
        }
        return null;
    }

    /**
     * 返回系统标准内存的 cache 路径
     * 例如： /data/data/cn.zmqd.android.core.app/cache
     */
    public static String getInternalCachePath(Context context) {
        return context.getCacheDir().getPath();
    }
}
