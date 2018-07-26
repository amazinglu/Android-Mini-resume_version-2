package com.example.amazinglu.data_storage_demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.amazinglu.data_storage_demo.util.ExternalStorageUtil;
import com.example.amazinglu.data_storage_demo.util.ImageUtil;
import com.example.amazinglu.data_storage_demo.util.InternalStorageUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_internal_storage) EditText textInput;
    @BindView(R.id.cache_internal_storage) EditText cacheInput;
    @BindView(R.id.save_text_button) Button saveText;
    @BindView(R.id.save_cache_button) Button saveCache;
    @BindView(R.id.external_storage_image) ImageView imageView;
    @BindView(R.id.save_private_external) Button savePrivate;
    @BindView(R.id.save_public_external) Button savePublic;

    public static final String TEXT_FILE_NAME = "myFile";
    public static final String TEXT_CACHE_NAME = "myCache";

    private static final int REQ_CODE_PICK_IMAGE = 202;
    private Uri localImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalStorageUtil.writeToFile(TEXT_FILE_NAME, textInput.getText().toString(),
                        MainActivity.this);

                String str = InternalStorageUtil.readFromFile(TEXT_FILE_NAME, MainActivity.this);
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });

        saveCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File cache = InternalStorageUtil.writeToCache(TEXT_CACHE_NAME, MainActivity.this,
                        cacheInput.getText().toString());

                String str = InternalStorageUtil.readFromCache(cache, MainActivity.this);
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                   openPictureChooser();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == ExternalStorageUtil.REQ_CODE_READ_EXTERNAL_STORAGE ||
                requestCode == ExternalStorageUtil.REQ_CODE_WRITE_EXTERNAL_STORAGE)
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openPictureChooser();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                loadImage(imageUri);
            }
        }
    }

    private void openPictureChooser() {
        /**
         * open the picture chooser
         * */
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        /**
         * create chooser every time
         * */
        startActivityForResult(Intent.createChooser(intent, "Select picture"),
                REQ_CODE_PICK_IMAGE);
    }

    private void loadImage(Uri uri) {
        /**
         * download the image
         * */
        localImageUri = ImageUtil.getLocalImageUri(MainActivity.this, uri);

        /**
         * load the image to imageView
         * */
        ImageUtil.loadImage(MainActivity.this, localImageUri, imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setTag(uri);
    }

    /**
     * chcek and request permission
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission() {
        boolean readable = ExternalStorageUtil.isExternalStorageReadable();
        boolean writable = ExternalStorageUtil.isExternalStorageWritable();

        if (!writable && !readable) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ExternalStorageUtil.REQ_CODE_READ_EXTERNAL_STORAGE);
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ExternalStorageUtil.REQ_CODE_WRITE_EXTERNAL_STORAGE);
            return false;
        } else if (!writable) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ExternalStorageUtil.REQ_CODE_WRITE_EXTERNAL_STORAGE);
            return false;
        } else if (!readable) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ExternalStorageUtil.REQ_CODE_READ_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }
    }
}
