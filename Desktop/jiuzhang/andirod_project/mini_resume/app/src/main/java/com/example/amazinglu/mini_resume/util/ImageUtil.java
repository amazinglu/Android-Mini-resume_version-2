package com.example.amazinglu.mini_resume.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
    public static Uri loadImage(Context context, Uri uri, ImageView imageView) {
        try {
            /**
             * the way to load image from external storage
             * */

            Uri localUri = getImageUrlWithAuthority(context, uri);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), localUri);
            imageView.setImageBitmap(bitmap);
            return localUri;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadImageLocal(Context context, Uri uri, ImageView imageView) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    /**
     * 如果 uri是从google photo上面取得的话，uri的访问是暂时的，所以当我们隔一段时间后再通过这个uri加载图片的时候
     * 就找不到图片了
     *
     * 正确的方法是在第一次通过这个uri加载图片的时候直接将图片下载下来并存在本地
     * 而在存本地的过程中注意要申请 WRITE_EXTERNAL_STORAGE permission
     * 申请permission两个步骤：
     * 1. manifest
     * 2. 在程序中request
     *
     * 下面方法最后会返回一个uri指向本地的储存
     * 我们将这个uri存起来，以后打开图片的时候就用这个localUri而不是google photo那个临时的uri
     * */
    public static Uri getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        new File("/sdcard/Pictures").mkdirs();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }
}
