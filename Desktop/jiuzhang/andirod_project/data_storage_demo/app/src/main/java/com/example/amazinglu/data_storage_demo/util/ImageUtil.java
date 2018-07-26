package com.example.amazinglu.data_storage_demo.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;

import com.example.amazinglu.data_storage_demo.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

public class ImageUtil {

    public static void loadImage(Context context, Uri uri, ImageView imageView) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Uri getLocalImageUri(Context context, Uri uri) {
        return downLoadImage(context, uri);
    }

    private static Uri downLoadImage(Context context, Uri uri) {
        /**
         * the way to load bitmap using uri
         * */
        InputStream inputStream = null;
        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (inputStream != null) {
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            return saveBitMapwithMediaStore(imageBitmap, context);
            return saveBitMapToFolder(imageBitmap, context);
        }
        return null;
    }

    /**
     * the bitmap will be save to a folder in external library and the folder will be show up in gallery
     * */
    private static Uri saveBitMapToFolder(Bitmap bitmap, Context context) {
        /**
         * first create the directory (the folder)
         * create a new file for each image in the directory
         *
         * the way to write bitmap to storage
         * */
        File rootDir = ExternalStorageUtil.getPublicAlbumStorageDir("MyData");
        File file = new File(rootDir, UUID.randomUUID().toString());

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * then insert the file into the MediaStore so that the gallery will show the folder
         * the file in the file of the image not the directory
         * */
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        contentValues.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        return Uri.fromFile(file);
    }

    /**
     * the bitmap will be saved to the specific folder calls "pictures" in gallery
     * */
    private static Uri saveBitMapwithMediaStore(Bitmap imageBitmap, Context context) {
        /**
         * compress the image
         * */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        /**
         * if use MediaStore.Images.Media.insertImage to insert image, the image will be save to
         * specific path, and we can not customize it
         * */
        String uriPath = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                imageBitmap, "myData", "amazing");

        return Uri.parse(uriPath);
    }
}
