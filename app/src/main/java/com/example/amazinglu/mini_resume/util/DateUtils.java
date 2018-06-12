package com.example.amazinglu.mini_resume.util;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static DateFormat dateFormat = new SimpleDateFormat("MMM, yyyy", Locale.getDefault());

    public static Date stringToDate(@NonNull String str) {
        try {
            return dateFormat.parse(str);
        } catch (ParseException e) {
            return Calendar.getInstance().getTime();
        }
    }

    public static String dateToString(@NonNull Date date) {
        return dateFormat.format(date);
    }

}
