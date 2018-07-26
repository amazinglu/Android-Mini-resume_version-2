package com.example.amazinglu.data_storage_demo.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class ExternalStorageUtil {

    public static final int REQ_CODE_READ_EXTERNAL_STORAGE = 200;
    public static final int REQ_CODE_WRITE_EXTERNAL_STORAGE = 202;

    /**
     * If the returned state is MEDIA_MOUNTED, then you can read and write your files.
     * If it's MEDIA_MOUNTED_READ_ONLY, you can only read the files.
     * */

    public static final String LOG_TAG = "external_storage_log";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getPublicAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                albumName);
        if (!file.mkdir()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    public static File getPrivateAlbumStorageDir(Context context, String albumName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdir()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }
}
