package com.parabit.beacon.dfu;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.parabit.parabeacon.app.tech.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by williamsnyder on 8/24/17.
 */

public class DfuFileHelper {

    private static final String TAG = "DfuFileHelper";

    public static final String ROOT_FOLDER = "Parabeacon";
    public static final String UPDATE_FOLDER = "Updates";

    public static void createSamples(final Context context) {

		/*
		 * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
		 */
        final File root = new File(Environment.getExternalStorageDirectory(), ROOT_FOLDER);
        if (!root.exists()) {
            root.mkdir();
        }
        final File updates = new File(root, UPDATE_FOLDER);
        if (!updates.exists()) {
            updates.mkdir();
        }

        // Remove old files. Those will be moved to a new folder structure
        new File(updates, "sample.zip").delete();

        File f = new File(updates, "sample.zip");
        if (!f.exists()) {
            copyRawResource(context, R.raw.pb_secure_dfu_package, f);
        }
    }

    public static String getSampleZipPath() {
        final File root = new File(Environment.getExternalStorageDirectory(), ROOT_FOLDER);
        final File updates = new File(root, UPDATE_FOLDER);
        return new File(updates, "sample.zip").getAbsolutePath();
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context activity context
     * @param rawResId the resource id
     * @param dest     destination file
     */
    private static void copyRawResource(final Context context, final int rawResId, final File dest) {
        try {
            final InputStream is = context.getResources().openRawResource(rawResId);
            final FileOutputStream fos = new FileOutputStream(dest);

            final byte[] buf = new byte[1024];
            int read;
            try {
                while ((read = is.read(buf)) > 0)
                    fos.write(buf, 0, read);
            } finally {
                is.close();
                fos.close();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error while copying file " + e.toString());
        }
    }

}
