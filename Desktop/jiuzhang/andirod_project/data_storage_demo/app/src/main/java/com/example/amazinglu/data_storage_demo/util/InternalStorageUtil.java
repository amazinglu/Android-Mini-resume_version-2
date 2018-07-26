package com.example.amazinglu.data_storage_demo.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class InternalStorageUtil {

    /**
     * use outputStream and inputStream
     * the method look for the file base on file in the root directory
     * the system will create the file in a specific place
     * we cannot define the path of the file
     *
     * use fileWrite adn fileReader
     * the method look for file base on File object
     * we can write contents to a specific file base on the path in the file Object
     * I prefer this way !!
     *
     * for normal file we write to file directory
     * for cache we write to cache directory
     * */
    public static void writeToFile(String fileName, String content, Context context) {
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            byte[] test = content.getBytes();

            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String fileName, Context context) {
        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (inputStream != null) {
            int len = 0;
            try {
                len = (int) inputStream.getChannel().size();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] bytes = new byte[len];
            try {
                inputStream.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new String(bytes);
        }
        return null;
    }

    public static File writeToCache(String fileName, Context context, String content) {
        File outputCache = getTempFile(context, fileName);
        myFileWriter(content, context, outputCache);
        return outputCache;
    }

    public static String readFromCache(File inputCache, Context context) {
        return myFileReader(context, inputCache);
    }

    private static File getTempFile(Context context, String fileName) {
        File file = null;
        try {
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void myFileWriter(String content, Context context, File outputCache) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(outputCache);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String myFileReader(Context context, File inputCache) {
        FileReader reader = null;
        int len = (int) inputCache.length();
        char[] buffer = new char[len];
        try {
            reader = new FileReader(inputCache);
            reader.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer);
    }

}
